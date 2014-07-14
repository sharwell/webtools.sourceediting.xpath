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
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDateTime;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSTime;

/**
 * Adjusts an xs:dateTime value to a specific timezone, or to no timezone at
 * all. If <code>$timezone</code> is the empty sequence, returns an
 * <code>xs:dateTime</code> without timezone. Otherwise, returns an
 * <code>xs:dateTime</code> with a timezone.
 *
 * @see <a href="http://www.w3.org/TR/xquery-operators/#func-adjust-time-to-timezone">fn:adjust-time-to-timezone</a>
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
	 * Adjusts an {@code xs:time} value to a specific timezone, or to no
	 * timezone at all. If {@code $timezone} is the empty sequence, returns an
	 * {@code xs:time} without a timezone. Otherwise, returns an {@code xs:time}
	 * with a timezone.
	 *
	 * <pre>
	 * fn:adjust-time-to-timezone($arg as xs:time?) as xs:time?
	 *
	 * fn:adjust-time-to-timezone($arg as xs:time?, $timezone as xs:dayTimeDuration?) as xs:time?
	 * </pre>
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
	 * If {@code $arg} does not have a timezone component and {@code $timezone}
	 * is the empty sequence, then the result is {@code $arg}.</p>
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
	 * empty sequence, then:</p>
	 *
	 * <ul>
	 * <li>Let {@code $srcdt} be an {@code xs:dateTime} value, with an arbitrary
	 * date for the date component and time and timezone components that are the
	 * same as the time and timezone components of {@code $arg}.</li>
	 * <li>Let {@code $r} be the result of evaluating <a
	 * href="http://www.w3.org/TR/xquery-operators/#func-adjust-dateTime-to-timezone">fn:adjust-dateTime-to-timezone($srcdt,
	 * $timezone)</a></li>
	 * <li>The result of this function will be a time value that has time and
	 * timezone components that are the same as the time and timezone components
	 * of {@code $r}.</li>
	 * </ul>
	 *
	 * @param args Result from the expressions evaluation.
	 * @param sc Result of static context operation.
	 * @throws DynamicError Dynamic error.
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

		ResultSequence arg2 = null;
		if (argiter.hasNext()) {
			arg2 = argiter.next();
		}

		XSTime time = (XSTime) arg1.first();

		if (arg2 != null && arg2.empty()) {
			// this is the only case where a value without a timezone is returned
			if (!time.timezoned()) {
				return time;
			}

			Calendar adjusted = time.getNonTimezonedCalendar(evaluationContext.getDynamicContext().getTimezoneOffset());
			return new XSTime(XSTime.getTime(adjusted), false);
		}

		XSDayTimeDuration timezone;
		if (arg2 == null) {
			timezone = new XSDayTimeDuration(evaluationContext.getDynamicContext().getTimezoneOffset());
		} else {
			timezone = (XSDayTimeDuration) arg2.first();
		}

		if (timezone.hours() < 0 || timezone.hours() > 14)
			throw DynamicError.invalidTimezone();
		if (timezone.hours() == 14 && timezone.minutes() != 0)
			throw DynamicError.invalidTimezone();
		if (timezone.seconds().compareTo(BigDecimal.ZERO) != 0)
			throw DynamicError.invalidTimezone();

		Calendar calendar = time.getTimezonedCalendar(_datatypeFactory.newDuration(!timezone.negative(), 0, 0, 0, timezone.hours(), timezone.minutes(), 0));
		Calendar adjustedCalendar = (Calendar)calendar.clone();
		adjustedCalendar.setTimeZone(XSDateTime.getTimeZone(timezone.hours(), timezone.minutes(), timezone.negative()));
		adjustedCalendar.setTimeInMillis(calendar.getTimeInMillis());
		return new XSTime(XSTime.getTime(adjustedCalendar), true);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expectedArgs() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(new XSTime(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(new XSDayTimeDuration(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
