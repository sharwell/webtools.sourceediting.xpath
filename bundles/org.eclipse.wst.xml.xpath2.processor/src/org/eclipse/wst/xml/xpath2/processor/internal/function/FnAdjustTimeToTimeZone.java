/*******************************************************************************
 * Copyright (c) 2009, 2010 Standards for Technology in Automotive Retail, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Carver - bug 280547 - initial API and implementation. 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSTime;

/**
 * Adjusts an xs:dateTime value to a specific timezone, or to no timezone at
 * all. If <code>$timezone</code> is the empty sequence, returns an
 * <code>xs:dateTime</code> without timezone. Otherwise, returns an
 * <code>xs:dateTime</code> with a timezone.
 */
public class FnAdjustTimeToTimeZone extends Function {

	private static Collection<SeqType> _expected_args = null;
	private static final XSDayTimeDuration minDuration = new XSDayTimeDuration(
			0, 14, 0, BigDecimal.ZERO, true);
	private static final XSDayTimeDuration maxDuration = new XSDayTimeDuration(
			0, 14, 0, BigDecimal.ZERO, false);

	/**
	 * Constructor for FnDateTime.
	 */
	public FnAdjustTimeToTimeZone() {
		super(new QName("adjust-time-to-timezone"), 1, 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) {
		return adjustTime(args, ec);
	}

	/**
	 * Evaluate the function using the arguments passed.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param evaluationContext
	 *            Current evaluation context.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the fn:dateTime operation.
	 */
	public static ResultSequence adjustTime(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expectedArgs());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		if (arg1.empty()) {
			return ResultBuffer.EMPTY;
		}
		ResultSequence arg2 = ResultBuffer.EMPTY;
		if (argiter.hasNext()) {
			arg2 = argiter.next();
		}
		XSTime time = (XSTime) arg1.first();
		XSDayTimeDuration timezone = null;
		
		if (arg2.empty()) {
			if (time.timezoned()) {
				XSTime localized = new XSTime(time.calendar(), null);
				return localized;
			} else {
				return arg1;
			}
		}


		XMLGregorianCalendar xmlCalendar = null;
		
		if (time.tz() != null) {
			xmlCalendar = _datatypeFactory.newXMLGregorianCalendar((GregorianCalendar)time.normalizeCalendar(time.calendar(), time.tz()));
		} else {
			xmlCalendar = _datatypeFactory.newXMLGregorianCalendarTime(time.hour(), time.minute(), time.second().intValue(), 0);
		}

		timezone = (XSDayTimeDuration) arg2.first();
		if (timezone.lt(minDuration, evaluationContext) || timezone.gt(maxDuration, evaluationContext)) {
			throw DynamicError.invalidTimezone();
		}
		
		if (time.tz() == null) {
			return new XSTime(time.calendar(), timezone);
		}
		
		Duration duration = _datatypeFactory.newDuration(timezone.getStringValue());
		xmlCalendar.add(duration);

		return new XSTime(xmlCalendar.toGregorianCalendar(), timezone);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expectedArgs() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args
					.add(new SeqType(new XSTime(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(new XSDayTimeDuration(),
					SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
