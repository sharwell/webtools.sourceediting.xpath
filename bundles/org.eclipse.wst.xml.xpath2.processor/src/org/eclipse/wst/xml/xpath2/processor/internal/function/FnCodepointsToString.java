/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - improvements to the function implementation
 *     David Carver - bug 282096 - improvements for surrogate handling 
 *     Jesper Steen Moeller - bug 282096 - clean up string storage
 *     Jesper Steen Moeller - bug 280553 - further checks of legal Unicode codepoints.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.icu.text.UTF16;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Creates an xs:string from a sequence of code points. Returns the zero-length
 * string if $arg is the empty sequence. If any of the code points in $arg is
 * not a legal XML character, an error is raised [err:FOCH0001].
 */
public class FnCodepointsToString extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnCodepointsToString.
	 */
	public FnCodepointsToString() {
		super(new QName("codepoints-to-string"), 1);
	}

	/**
	 * Evaluate arguments.
	 * 
	 * @param args
	 *            argument expressions.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of evaluation.
	 */
	@Override
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		return codepoints_to_string(args);
	}

	/**
	 * Codepoints to string operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:codepoints-to-string operation.
	 */
	public static ResultSequence codepoints_to_string(Collection<ResultSequence> args)
			throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultSequence arg1 = cargs.iterator().next();
		if (arg1.empty()) {
			return new XSString("");
		}

		int[] codePointArray = new int[arg1.size()];
		int codePointIndex = 0;
		for (Iterator<Item> i = arg1.iterator(); i.hasNext();) {
			XSInteger code = (XSInteger) i.next();
			
			int codepoint = code.int_value().intValue();
			boolean valid = true;
			if (!Character.isValidCodePoint(codepoint)) {
				valid = false;
			} else if (!isValidXmlCharacter(codepoint)) {
				valid = false;
			}

			if (!valid) {
				throw DynamicError.unsupported_codepoint("U+" + Integer.toString(codepoint, 16).toUpperCase(), null);
			}

			codePointArray[codePointIndex] = codepoint;			
			codePointIndex++;
		}

		try {
			String str = UTF16.newString(codePointArray, 0, codePointArray.length);
			return new XSString(str);
		} catch (IllegalArgumentException iae) {
			// This should be duoble checked above, but rather safe than sorry
			throw DynamicError.unsupported_codepoint(iae.getMessage(), iae);
		}
	}

	private static boolean isValidXmlCharacter(int codepoint) {
		return (codepoint >= 0x0020 && codepoint <= 0xD7FF)
			|| (codepoint >= 0xE000 && codepoint <= 0xFFFD)
			|| (codepoint >= 0x10000 && codepoint <= 0x10FFFF)
			|| codepoint == 0x0009
			|| codepoint == 0x000A
			|| codepoint == 0x000D;
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(new XSInteger(), SeqType.OCC_STAR));
		}

		return _expected_args;
	}
}
