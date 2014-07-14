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

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSTime;

/**
 * Returns the timezone component of $arg if any. If $arg has a timezone
 * component, then the result is an xdt:dayTimeDuration that indicates deviation
 * from GMT; its value may range from +14:00 to -14:00 hours, both inclusive.
 * Otherwise, the result is the empty sequence. If $arg is the empty sequence,
 * returns the empty sequence.
 */
public class FnTimezoneFromTime extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnTimezoneFromTime.
	 */
	public FnTimezoneFromTime() {
		super(new QName("timezone-from-time"), 1);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		return timezone_from_time(args);
	}

	/**
	 * Timezone-from-Time operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:timezone-from-time operation.
	 */
	public static ResultSequence timezone_from_time(Collection<ResultSequence> args)
			throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultSequence arg1 = cargs.iterator().next();

		if (arg1.empty()) {
			return ResultBuffer.EMPTY;
		}

		XSTime dt = (XSTime) arg1.first();

		if (dt.timezoned()) {
			return dt.tz();
		}
		return ResultBuffer.EMPTY;
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(new XSTime(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
