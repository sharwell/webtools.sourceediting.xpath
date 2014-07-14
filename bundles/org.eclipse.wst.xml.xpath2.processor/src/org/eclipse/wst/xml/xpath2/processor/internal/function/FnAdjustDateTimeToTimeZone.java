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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.CalendarType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDateTime;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;

/**
 * Adjusts an xs:dateTime value to a specific timezone, or to no timezone at
 * all. If <code>$timezone</code> is the empty sequence, returns an
 * <code>xs:dateTime</code> without timezone. Otherwise, returns an
 * <code>xs:dateTime</code> with a timezone.
 */
public class FnAdjustDateTimeToTimeZone extends Function {
	private static Collection<SeqType> _expected_args = null;
	private static final XSDayTimeDuration minDuration = new XSDayTimeDuration(
			0, 14, 0, 0, true);
	private static final XSDayTimeDuration maxDuration = new XSDayTimeDuration(
			0, 14, 0, 0, false);

	/**
	 * Constructor for FnDateTime.
	 */
	public FnAdjustDateTimeToTimeZone() {
		super(new QName("adjust-dateTime-to-timezone"), 1, 2);
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
		return adjustdateTime(args, ec);
	}

	/**
	 * Adjusts an {@code xs:dateTime} value to a specific timezone, or to no
	 * timezone at all. If {@code $timezone} is the empty sequence, returns an
	 * {@code xs:dateTime} without a timezone. Otherwise, returns an
	 * {@code xs:dateTime} with a timezone.
	 *
	 * <p>
	 * If {@code $timezone} is not specified, then {@code $timezone} is the
	 * value of the implicit timezone in the dynamic context.</p>
	 *
	 * <p>
	 * If {@code $arg} is the empty sequence, then the result is the empty
	 * sequence.</p>
	 *
	 * <p>
	 * A dynamic error is raised [<a
	 * href="http://www.w3.org/TR/xquery-operators/#ERRFODT0003">err:FODT0003</a>]
	 * if {@code $timezone} is less than {@code -PT14H} or greater than
	 * {@code PT14H} or if does not contain an integral number of minutes.</p>
	 *
	 * <p>
	 * If {@code $arg} does not have a timezone component and $timezone} is the
	 * empty sequence, then the result is {@code $arg}.</p>
	 *
	 * <p>
	 * If {@code $arg} does not have a timezone component and {@code $timezone}
	 * is not the empty sequence, then the result is {@code $arg} with
	 * {@code $timezone} as the timezone component.</p>
	 *
	 * <p>
	 * If {@code $arg} has a timezone component and {@code $timezone} is the
	 * empty sequence, then the result is the localized value of {@code $arg}
	 * without its timezone component.</p>
	 *
	 * <p>
	 * If {@code $arg} has a timezone component and {@code $timezone} is not the
	 * empty sequence, then the result is an {@code xs:dateTime} value with a
	 * timezone component of {@code $timezone} that is equal to
	 * {@code $arg}.</p>
	 *
	 * @param args Result from the expressions evaluation.
	 * @param sc Result of static context operation.
	 * @throws DynamicError Dynamic error.
	 * @return Result of the fn:dateTime operation.
	 */
	public static ResultSequence adjustdateTime(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expectedArgs());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();
		if (arg1.empty()) {
			return ResultBuffer.EMPTY;
		}

		ResultSequence arg2 = null;
		if (argiter.hasNext()) {
			arg2 = argiter.next();
		}

		XSDateTime dateTime = (XSDateTime) arg1.item(0);

		if (arg2 != null && arg2.empty()) {
			// this is the only case where a value without a timezone is returned
			if (!dateTime.timezoned()) {
				return dateTime;
			}

			Calendar adjusted = dateTime.getNonTimezonedCalendar(evaluationContext.getDynamicContext().getTimezoneOffset());
			return new XSDateTime(adjusted, false);
		}

		XSDayTimeDuration timezone;
		if (arg2 == null) {
			timezone = new XSDayTimeDuration(evaluationContext.getDynamicContext().getTimezoneOffset());
		} else {
			timezone = (XSDayTimeDuration) arg2.item(0);
		}

		if (timezone.hours() < 0 || timezone.hours() > 14)
			throw DynamicError.invalidTimezone();
		if (timezone.hours() == 14 && timezone.minutes() != 0)
			throw DynamicError.invalidTimezone();
		if (timezone.seconds() != 0)
			throw DynamicError.invalidTimezone();

		Calendar calendar = dateTime.getTimezonedCalendar(_datatypeFactory.newDuration(!timezone.negative(), 0, 0, 0, timezone.hours(), timezone.minutes(), 0));
		Calendar adjustedCalendar = (Calendar)calendar.clone();
		adjustedCalendar.setTimeZone(XSDateTime.getTimeZone(timezone.hours(), timezone.minutes(), timezone.negative()));
		adjustedCalendar.setTimeInMillis(calendar.getTimeInMillis());
		return new XSDateTime(adjustedCalendar, true);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expectedArgs() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(new XSDateTime(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(new XSDayTimeDuration(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
