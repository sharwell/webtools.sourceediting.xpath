/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * <p>
 * Conversion to upper-case function.
 * </p>
 * 
 * <p>
 * Usage: fn:upper-case($arg as xs:string?) as xs:string
 * </p>
 * 
 * <p>
 * This class returns the value of $arg after translating every character to its
 * upper-case correspondent. Every character that does not have an upper-case
 * correspondent is included in the returned value in its original form.
 * </p>
 * 
 * <p>
 * If the value of $arg is the empty sequence, the zero-length string is
 * returned.
 * </p>
 */
public class FnUpperCase extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnUpperCase.
	 */
	public FnUpperCase() {
		super(new QName("upper-case"), 1);
	}

	/**
	 * Evaluate the arguments.
	 * 
	 * @param args
	 *            are evaluated.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The evaluation of the arguments being converted to upper case.
	 */
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		return upper_case(args);
	}

	/**
	 * Convert arguments to upper case.
	 * 
	 * @param args
	 *            are converted to upper case.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return The result of converting the arguments to upper case.
	 */
	public static ResultSequence upper_case(Collection<ResultSequence> args)
			throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultSequence arg1 = cargs.iterator().next();

		if (arg1.empty()) {
			return new XSString("");
		}

		String str = ((XSString) arg1.first()).value();

		return new XSString(str.toUpperCase());
	}

	/**
	 * Calculate the expected arguments.
	 * 
	 * @return The expected arguments.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
