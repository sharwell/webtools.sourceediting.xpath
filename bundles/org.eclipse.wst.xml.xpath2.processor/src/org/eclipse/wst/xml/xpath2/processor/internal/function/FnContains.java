/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver - STAR - bug 262765 - check for empty string in second arg before first. 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.CollationProvider;
import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Returns an xs:boolean indicating whether or not the value of $arg1 contains
 * (at the beginning, at the end, or anywhere within) at least one sequence of
 * collation units that provides a minimal match to the collation units in the
 * value of $arg2, according to the collation that is used.
 */
public class FnContains extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnContains.
	 */
	public FnContains() {
		super(new QName("contains"), 2, 3);
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
		return contains(args, ec);
	}

	/**
	 * Contains operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:contains operation.
	 */
	public static ResultSequence contains(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		String str1 = "";
		String str2 = "";
		if (!arg1.empty())
			str1 = ((XSString) arg1.first()).value();

		ResultSequence arg2 = argiter.next();
		if (!arg2.empty())
			str2 = ((XSString) arg2.first()).value();

		ResultSequence arg3 = null;
		if (argiter.hasNext()) {
			arg3 = argiter.next();
		}

		CollationProvider collationProvider = evaluationContext.getDynamicContext().getCollationProvider();
		String collationName;
		if (arg3 != null) {
			if (arg3.empty() || !(arg3.first() instanceof XSString)) {
				throw DynamicError.argument_type_error(null);
			}

			collationName = arg3.first().getStringValue();
		} else {
			collationName = collationProvider.getDefaultCollation();
		}

		Comparator<String> collation = collationProvider.getCollation(collationName);
		if (collation == null) {
			throw DynamicError.unsupported_collation(collationName);
		}

		int str1len = str1.length();
		int str2len = str2.length();

		if (str2len == 0) {
			return XSBoolean.TRUE;
		}
		
		if (str1len == 0) {
			return XSBoolean.FALSE;
		}

		return XSBoolean.valueOf(str1.indexOf(str2) != -1);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(new XSString(), SeqType.OCC_QMARK);
			_expected_args.add(arg);
			_expected_args.add(arg);
			_expected_args.add(arg);
		}

		return _expected_args;
	}
}
