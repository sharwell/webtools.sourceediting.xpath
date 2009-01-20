/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.function;

import org.eclipse.wst.xml.xpath2.processor.*;
import org.eclipse.wst.xml.xpath2.processor.types.*;

import java.util.*;
/**
 * Returns an xs:integer between 1 and 31, both inclusive, representing the
 * day component in the localized value of $arg.
 * If $arg is the empty sequence, returns the empty sequence.
 */
public class FnDayFromDateTime extends Function {
	private static Collection _expected_args = null;
	/**
	 * Constructor for FnDayFromDateTime.
	 */
	public FnDayFromDateTime() {
		super(new QName("day-from-dateTime"), 1);
	}
	/**
         * Evaluate arguments.
         * @param args argument expressions.
         * @throws DynamicError Dynamic error.
         * @return Result of evaluation.
         */
	public ResultSequence evaluate(Collection args) throws DynamicError {
		return day_from_date_time(args);
	}
	/**
         * Day-From-DateTime operation.
         * @param args Result from the expressions evaluation.
         * @throws DynamicError Dynamic error.
         * @return Result of fn:day-from-datetime operation.
         */
	public static ResultSequence day_from_date_time(Collection args) throws DynamicError {
		Collection cargs = Function.convert_arguments(args,
                                                              expected_args());

		ResultSequence arg1 = (ResultSequence) cargs.iterator().next();

		ResultSequence rs = ResultSequenceFactory.create_new();

		if(arg1.empty()) {
			return rs;
		}

		XSDateTime dt = (XSDateTime) arg1.first();

		int res = dt.day();

		rs.add(new XSInteger(res));	

		return rs;
	}
	/**
         * Obtain a list of expected arguments.
         * @return Result of operation.
         */
	public static Collection expected_args() {
		if(_expected_args == null) {
			_expected_args = new ArrayList();
			_expected_args.add(new SeqType(new XSDateTime(),
						       SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
