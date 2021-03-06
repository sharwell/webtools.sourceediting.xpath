/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver - bug 282096 - improvements for surrogate handling  
 *     Jesper Steen Moeller - bug 282096 - clean up string storage
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.CollationProvider;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Returns the substring of the value of $arg1 that precedes in the value of
 * $arg1 the first occurrence of a sequence of collation units that provides a
 * minimal match to the collation units of $arg2 according to the collation that
 * is used.
 */
public class FnSubstringBefore extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnSubstringBefore.
	 */
	public FnSubstringBefore() {
		super(new QName("substring-before"), 2, 3);
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
		return substring_before(args, ec);
	}

	/**
	 * Substring-Before operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:substring-before operation.
	 */
	public static ResultSequence substring_before(Collection<ResultSequence> args, EvaluationContext evaluationContext)
			throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		String str1 = "";
		String str2 = "";
		if (!arg1.empty()) {
			str1 = ((XSString) arg1.first()).value();
		}

		ResultSequence arg2 = argiter.next();
		if (!arg2.empty()) {
			str2 = ((XSString) arg2.first()).value();
		}

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

		int str2len = str2.length();

		if (str2len == 0) {
			return new XSString("");
		}

		int index = str1.indexOf(str2);
		if (index == -1) {
			return new XSString("");
		}

		
		return new XSString(str1.substring(0, index));
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
