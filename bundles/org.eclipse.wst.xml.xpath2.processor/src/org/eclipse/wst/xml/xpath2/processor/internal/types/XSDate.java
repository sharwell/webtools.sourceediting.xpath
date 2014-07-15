/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 273760 - wrong namespace for functions and data types
 *     Mukul Gandhi - bug 274792 - improvements to xs:date constructor function.
 *     David Carver - bug 282223 - implementation of xs:duration.
 *                                 fixed casting issue. 
 *     David Carver - bug 280547 - fix dates for comparison 
 *     Jesper Steen Moller  - bug 262765 - fix type tests
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.Duration;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathMinus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathPlus;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * Representation of a date of the form year-month-day and optional timezone
 */
public class XSDate extends CalendarType implements CmpEq, CmpLt, CmpGt,

MathMinus, MathPlus,

Cloneable {
	private static final String XS_DATE = "xs:date";
	private final Calendar _calendar;
	private final boolean _timezoned;

	/**
	 * Initializes a new representation of a supplied date
	 * 
	 * @param cal
	 *            The Calendar representation of the date to be stored
	 * @param tz
	 *            The time zone of the date to be stored.
	 */
//	@Deprecated
//	public XSDate(Calendar cal, XSDuration tz) {
//		assert TimeZone.getTimeZone("GMT").equals(cal.getTimeZone());
//
//		_calendar = cal;
//		_tz = tz;
//		_timezoned = _tz != null;
//	}

	public XSDate(Calendar cal, boolean hasTimeZone) {
		assert cal.get(Calendar.HOUR_OF_DAY) == 0;
		assert cal.get(Calendar.MINUTE) == 0;
		assert cal.get(Calendar.SECOND) == 0;
		assert cal.get(Calendar.MILLISECOND) == 0;

		_calendar = cal;
		_timezoned = hasTimeZone;
	}

	/**
	 * Initializes a new representation of the current date
	 */
	public XSDate() {
		this(getDate(new GregorianCalendar(TimeZone.getTimeZone("GMT"))), true);
	}

	public static Calendar getDate(Calendar calendar) {
		GregorianCalendar time = new GregorianCalendar(calendar.getTimeZone());
		time.setGregorianChange(new Date(Long.MIN_VALUE));
		time.setTimeInMillis(calendar.getTimeInMillis());
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		return time;
	}

	/**
	 * Retrieves the datatype name
	 * 
	 * @return "date" which is the dataype name
	 */
	@Override
	public String type_name() {
		return "date";
	}

	/**
	 * Creates a copy of this date representation
	 * 
	 * @return A copy of this date representation
	 */
	@Override
	public XSDate clone() {
		Calendar c = (Calendar) calendar().clone();
		return new XSDate(c, timezoned());
	}

	/**
	 * Parses a String representation of a date (of the form year-month-day or
	 * year-month-day+timezone) and constructs a new XSDate representation of
	 * it.
	 * 
	 * @param str
	 *            The String representation of the date (and optional timezone)
	 * @return The XSDate representation of the supplied date
	 */
	public static XSDate parse_date(String str) {
		str = str.trim();

		String date = "";
		String time = "T00:00:00.0";

		int index = str.indexOf('+', 1);
		if (index == -1) {
			index = str.indexOf('-', 1);
			if (index == -1)
				return null;
			index = str.indexOf('-', index + 1);
			if (index == -1)
				return null;
			index = str.indexOf('-', index + 1);
		}
		if (index == -1)
			index = str.indexOf('Z', 1);
		if (index != -1) {
			date = str.substring(0, index);
			// here we go
			date += time;
			date += str.substring(index, str.length());
		} else {
			date = str + time;
		}

		// sorry again =D
		XSDateTime dt = XSDateTime.parseDateTime(date);
		if (dt == null)
			return null;

		return new XSDate(dt.calendar(), dt.timezoned());
	}

	/**
	 * Creates a new result sequence consisting of the retrievable date value in
	 * the supplied result sequence
	 * 
	 * @param arg
	 *            The result sequence from which to extract the date value.
	 * @throws DynamicError
	 * @return A new result sequence consisting of the date value supplied.
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		Item aat = arg.first();
		if (!(aat instanceof XSString
			|| aat instanceof XSUntypedAtomic
			|| aat instanceof XSDateTime
			|| aat instanceof XSDate))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.invalidType();
		}

		XSDate dt = castDate(aat);

		if (dt == null)
			throw DynamicError.cant_cast(null);

		return dt;
	}

	private boolean isCastable(Item aat) {

		// We might be able to cast these.
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic
				|| aat instanceof NodeType) {
			return true;
		}

		if (aat instanceof XSTime) {
			return false;
		}

		if (aat instanceof XSDateTime) {
			return true;

		}

		if (aat instanceof XSDate) {
			return true;
		}

		return false;
	}

	private XSDate castDate(Item aat) {
		if (aat instanceof XSDate) {
			return ((XSDate)aat).clone();
		}

		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSDate(getDate(dateTime.calendar()), dateTime.timezoned());
		}

		return parse_date(aat.getStringValue());
	}

	/**
	 * Retrieve the year from the date stored
	 * 
	 * @return the year value of the date stored
	 */
	public int year() {
		int y = _calendar.get(Calendar.YEAR);
		if (_calendar.get(Calendar.ERA) == GregorianCalendar.BC)
			y = 1 - y;

		return y;
	}

	/**
	 * Retrieve the month from the date stored
	 * 
	 * @return the month value of the date stored
	 */
	public int month() {
		return _calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * Retrieve the day from the date stored
	 * 
	 * @return the day value of the date stored
	 */
	public int day() {
		return _calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Retrieves whether this date has an optional timezone associated with it
	 * 
	 * @return True if there is a timezone associated with this date. False
	 *         otherwise.
	 */
	@Override
	public boolean timezoned() {
		return _timezoned;
	}

	/**
	 * Retrieves a String representation of the date stored
	 * 
	 * @return String representation of the date stored
	 */
	@Override
	public String getStringValue() {
		String ret = "";

		Calendar adjustFortimezone = calendar();

		int year = adjustFortimezone.get(Calendar.YEAR);
		if (adjustFortimezone.get(Calendar.ERA) == GregorianCalendar.BC) {
			year--;
			if (year > 0) {
				ret += "-";
			}
		}

		ret += XSDateTime.pad_int(year, 4);

		ret += "-";
		ret += XSDateTime.pad_int(month(), 2);

		ret += "-";
		ret += XSDateTime.pad_int(adjustFortimezone.get(Calendar.DAY_OF_MONTH),
				2);

		if (timezoned()) {
			XSDuration tz = tz();
			int hrs = tz.hours();
			int min = tz.minutes();
			BigDecimal secs = tz.seconds();
			if (hrs == 0 && min == 0 && secs.compareTo(BigDecimal.ZERO) == 0) {
				ret += "Z";
			} else {
				String tZoneStr = "";
				if (tz.negative()) {
					tZoneStr += "-";
				} else {
					tZoneStr += "+";
				}
				tZoneStr += XSDateTime.pad_int(hrs, 2);
				tZoneStr += ":";
				tZoneStr += XSDateTime.pad_int(min, 2);

				ret += tZoneStr;
			}
		}

		return ret;
	}

	/**
	 * Retrive the datatype full pathname
	 * 
	 * @return "xs:date" which is the datatype full pathname
	 */
	@Override
	public String string_type() {
		return XS_DATE;
	}

	/**
	 * Retrieves the Calendar representation of the date stored
	 * 
	 * @return Calendar representation of the date stored
	 */
	@Override
	public Calendar calendar() {
		return _calendar;
	}

	/**
	 * Retrieves the timezone associated with the date stored
	 * 
	 * @return the timezone associated with the date stored
	 */
	public XSDayTimeDuration tz() {
		if (!timezoned()) {
			return null;
		}

		TimeZone timeZone = calendar().getTimeZone();
		BigDecimal rawOffset = new BigDecimal(timeZone.getRawOffset()).divide(new BigDecimal(1000));
		return new XSDayTimeDuration(rawOffset);
	}

	// comparisons
	/**
	 * Equality comparison on this and the supplied dates (taking timezones into
	 * account)
	 * 
	 * @param arg
	 *            XSDate representation of the date to compare to
	 * @throws DynamicError
	 * @return True if the two dates are represent the same exact point in time.
	 *         False otherwise.
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDate val = NumericType.get_single_type(arg, XSDate.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.compareTo(thatcal) == 0;
	}

	/**
	 * Comparison on this and the supplied dates (taking timezones into account)
	 * 
	 * @param arg
	 *            XSDate representation of the date to compare to
	 * @throws DynamicError
	 * @return True if in time, this date lies before the date supplied. False
	 *         otherwise.
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDate val = NumericType.get_single_type(arg, XSDate.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.before(thatcal);
	}

	/**
	 * Comparison on this and the supplied dates (taking timezones into account)
	 * 
	 * @param arg
	 *            XSDate representation of the date to compare to
	 * @throws DynamicError
	 * @return True if in time, this date lies after the date supplied. False
	 *         otherwise.
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDate val = NumericType.get_single_type(arg, XSDate.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.after(thatcal);
	}

	// XXX this is incorrect [epoch]
	/**
	 * Currently unsupported method. Retrieves the date in milliseconds since
	 * the begining of epoch
	 * 
	 * @return Number of milliseconds since the begining of the epoch
	 */
	public double value() {
		return calendar().getTimeInMillis() / 1000.0;
	}

	// math
	/**
	 * Mathematical minus operator between this XSDate and a supplied result
	 * sequence (XSDate, XSYearMonthDuration and XSDayTimeDuration are only
	 * valid ones).
	 * 
	 * @param arg
	 *            The supplied ResultSequence that is on the right of the minus
	 *            operator. If this is an XSDate, the result will be a
	 *            XSDayTimeDuration of the duration of time between these two
	 *            dates. If arg is an XSYearMonthDuration or an
	 *            XSDayTimeDuration the result will be a XSDate of the result of
	 *            the current date minus the duration of time supplied.
	 * @return New ResultSequence consisting of the result of the mathematical
	 *         minus operation.
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();

		if (!(at instanceof XSDate) && !(at instanceof XSYearMonthDuration)
				&& !(at instanceof XSDayTimeDuration)) {
			throw DynamicError.throw_type_error();
		}

		if (at instanceof XSDate) {
			return minusXSDate(arg, evaluationContext.getDynamicContext().getTimezoneOffset());
		}
		
		if (at instanceof XSYearMonthDuration) {
			return minusXSYearMonthDuration((XSYearMonthDuration)at);
		}
		
		if (at instanceof XSDayTimeDuration) {
			return minusXSDayTimeDuration((XSDayTimeDuration)at);
		}

		return null;
	}

	private ResultSequence minusXSDayTimeDuration(XSDayTimeDuration val) {
		Calendar resultCalendar = (Calendar)calendar().clone();
		// multiplier is negated due to this being a 'minus' operation
		int multiplier = val.negative() ? 1 : -1;
		resultCalendar.add(Calendar.DAY_OF_YEAR, multiplier * val.days());
		resultCalendar.add(Calendar.MILLISECOND, multiplier * val.time_value().multiply(new BigDecimal(1000)).intValue());
		return new XSDate(getDate(resultCalendar), timezoned());
	}

	private ResultSequence minusXSYearMonthDuration(XSYearMonthDuration val) {
		Calendar resultCalendar = (Calendar)calendar().clone();
		resultCalendar.add(Calendar.MONTH, val.monthValue() * -1);
		return new XSDate(getDate(resultCalendar), timezoned());
	}

	private ResultSequence minusXSDate(ResultSequence arg, Duration implicitTimezoneOffset) throws DynamicError {
		XSDate val = NumericType.get_single_type(arg, XSDate.class);

		Calendar thisCal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatCal = val.getTimezonedCalendar(implicitTimezoneOffset);
		long duration = thisCal.getTimeInMillis() - thatCal.getTimeInMillis();

		return new XSDayTimeDuration(_datatypeFactory.newDuration(duration));
	}

	/**
	 * Mathematical addition operator between this XSDate and a supplied result
	 * sequence (XDTYearMonthDuration and XDTDayTimeDuration are only valid
	 * ones).
	 * 
	 * @param arg
	 *            The supplied ResultSequence that is on the right of the minus
	 *            operator. If arg is an XDTYearMonthDuration or an
	 *            XDTDayTimeDuration the result will be a XSDate of the result
	 *            of the current date minus the duration of time supplied.
	 * @return New ResultSequence consisting of the result of the mathematical
	 *         minus operation.
	 */
	@Override
	public ResultSequence plus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();

		if (at instanceof XSYearMonthDuration) {
			XSYearMonthDuration val = (XSYearMonthDuration) at;
			
			Calendar adjusted = (Calendar)calendar().clone();
			adjusted.add(Calendar.MONTH, val.monthValue());
			return new XSDate(adjusted, timezoned());
		} else if (at instanceof XSDayTimeDuration) {
			XSDayTimeDuration val = (XSDayTimeDuration) at;
			
			// We only need to add the Number of days dropping the rest.
			int days = val.days();
			if (val.negative()) {
				days *= -1;
			}

			Calendar adjusted = (Calendar)calendar().clone();
			adjusted.add(Calendar.DAY_OF_MONTH, days);
			adjusted.add(Calendar.MILLISECOND, val.time_value().multiply(new BigDecimal(1000)).intValue());
			return new XSDate(getDate(adjusted), timezoned());
		} else {
			throw DynamicError.throw_type_error();
		}
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_DATE;
	}

}
