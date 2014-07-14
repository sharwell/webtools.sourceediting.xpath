/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper S Moller - bug 286452 - always return the stable date/time from dynamic context
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.math.BigDecimal;
import java.util.Collection;

import javax.xml.datatype.Duration;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSTime;

/**
 * Returns xs:time(fn:current-dateTime()). This is a xs:time (with timezone) that
 * is current at some time during the evaluation of a query or transformation in
 * which fn:current-time() is executed. This function is stable. The precise
 * instant during the query or transformation represented by the value of
 * fn:current-time() is implementation dependent.
 */
public class FnCurrentTime extends Function {
	/**
	 * Constructor for FnCurrentTime.
	 */
	public FnCurrentTime() {
		super(new QName("current-time"), 0);
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
		return current_time(args, ec.getDynamicContext());
	}

	/**
	 * Current-Time operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param dc
	 *            Result of dynamic context operation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:current-time operation.
	 */
	public static ResultSequence current_time(Collection<ResultSequence> args, DynamicContext dc)
			throws DynamicError {
		assert args.size() == 0;

		Duration d = dc.getTimezoneOffset();
		XSDayTimeDuration tz = new XSDayTimeDuration(0, d.getHours(), d.getMinutes(), BigDecimal.ZERO, d.getSign() == -1);

		AnyType res = new XSTime(XSTime.getTime(dc.getCurrentDateTime()), true);

		return res;
	}
}
