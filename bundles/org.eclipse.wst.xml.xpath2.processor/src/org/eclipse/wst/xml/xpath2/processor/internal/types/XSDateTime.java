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
 *     Mukul Gandhi - improved string_value() implementation (motivated by bug, 281822)
 *     David Carver - bug 282223 - implementation of xs:duration.
 *                  - bug 262765 - additional tweak to convert 24:00:00 to 00:00:00
 *     David Carver - bug 280547 - fix dates for comparison 
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
 * A representation of a date and time (and optional timezone)
 */
public class XSDateTime extends CalendarType implements CmpEq, CmpLt, CmpGt,

MathMinus, MathPlus,

Cloneable {
	private static final String XS_DATE_TIME = "xs:dateTime";
	private final Calendar _calendar;
	private final boolean _timezoned;

	/**
	 * Initiates a new representation of a supplied date and time
	 * 
	 * @param cal
	 *            The Calendar representation of the date and time to be stored
	 * @param tz
	 *            The timezone of the date to be stored.
	 */
//	@Deprecated
//	public XSDateTime(Calendar cal, XSDuration tz) {
//		assert TimeZone.getTimeZone("GMT").equals(cal.getTimeZone());
//
//		_calendar = cal;
//		_tz = tz;
//		_timezoned = _tz != null;
//	}

	public XSDateTime(Calendar cal, boolean hasTimeZone) {
		_calendar = cal;
		_timezoned = hasTimeZone;
	}

	/**
	 * Creates a copy of this date and time representation
	 * 
	 * @return A copy of this date and time representation
	 */
	@Override
	public XSDateTime clone() {
		Calendar c = (Calendar) calendar().clone();
		return new XSDateTime(c, timezoned());
	}

	/**
	 * Inititates a new representation of the current date and time
	 */
	public XSDateTime() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")), true);
	}

	/**
	 * Retrieves the datatype name
	 * 
	 * @return "dateTime" which is the dataype name
	 */
	@Override
	public String type_name() {
		return "dateTime";
	}

	/**
	 * Check to see if a character is numeric
	 * 
	 * @param x
	 *            Character to be tested
	 * @return True if the character is numeric. False otherwise.
	 */
	public static boolean is_digit(char x) {
		if ('0' <= x && x <= '9')
			return true;
		return false;
	}

	/**
	 * Parses a String representation of a date and time and retrieves the year,
	 * month and day from it
	 * 
	 * @param str
	 *            The String representation of the date (and optional timezone)
	 * @return Integer array of size 3. Element 1 is the year, element 2 is the
	 *         month and element 3 is the day
	 */
	public static int[] parse_date(String str) {
		int state = 0; // 0 expect year or minus
		// 1 getting year
		// 2 getting month
		// 3 getting day

		int[] ret = new int[3];

		for (int i = 0; i < ret.length; i++)
			ret[i] = 0;

		String token = "";
		for (int i = 0; i < str.length(); i++) {
			char x = str.charAt(i);

			switch (state) {
			case 0:
				if (is_digit(x)) {
					token += x;
				} else if (x == '-') {
					token += x;
				} else
					return null;
				state = 1;
				break;

			case 1:
				// we got the year in theory...
				if (x == '-') {

					// check out the unsigned year
					String uy = token;
					if (uy.startsWith("-"))
						uy = uy.substring(1, uy.length());
					int uyl = uy.length();

					if (uyl < 4)
						return null;

					if (uyl == 4) {
						if (uy.compareTo("0000") == 0)
							return null;
					} else if (uy.charAt(0) == '0')
						return null;

					// semms good to me
					ret[0] = Integer.parseInt(token);
					token = "";
					state = 2;
				} else if (is_digit(x))
					token += x;
				else
					return null;
				break;

			case 2:
				// we got the month
				if (x == '-') {
					if (token.length() != 2)
						return null;

					ret[1] = Integer.parseInt(token);
					token = "";
					state = 3;
				} else if (is_digit(x))
					token += x;
				else
					return null;
				break;

			case 3:
				// getting day
				if (is_digit(x))
					token += x;
				else
					return null;
				break;

			default:
				assert false;
				return ret;
			}
		}
		if (state != 3)
			return null;

		// got the day
		if (token.length() != 2)
			return null;

		ret[2] = Integer.parseInt(token);

		return ret;
	}

	// return
	// hour
	// minute
	// seconds
	/**
	 * Parses a String representation of a date and time and retrieves the hour,
	 * minute and seconds from it
	 * 
	 * @param str
	 *            The String representation of the date (and optional timezone)
	 * @return Integer array of size 3. Element 1 is the hour, element 2 is the
	 *         minute and element 3 is the seconds
	 */
	public static BigDecimal[] parse_time(String str) {
		int state = 0; // 0 getting minute
		// 1 getting hour
		// 2 getting seconds [the whole part]
		// 3 getting fraction of seconds

		BigDecimal[] ret = new BigDecimal[3];

		String token = "";

		for (int i = 0; i < str.length(); i++) {
			char x = str.charAt(i);

			switch (state) {
			case 0:
			case 1:
				// got minute / hour
				if (x == ':') {
					if (token.length() != 2)
						return null;
					ret[state] = new BigDecimal(Integer.parseInt(token));
					state++;
					token = "";
				} else if (is_digit(x))
					token += x;
				else
					return null;
				break;

			case 2:
				if (is_digit(x)) {
					token += x;
					if (token.length() > 2)
						return null;
				} else if (x == '.') {
					token += x;
					state = 3;
				} else
					return null;
				break;

			case 3:
				if (is_digit(x))
					token += x;
				else
					return null;
				break;

			default:
				assert false;
				return null;
			}
		}
		if (!(state == 3 || state == 2))
			return null;

		// get seconds
		// this is whole + dot + nothing else
		if (token.length() == 3)
			return null;

		ret[2] = new BigDecimal(token);

		// XXX sanity check args...
		return ret;
	}

	// returns
	// positive/negative
	// hour
	// minute
	/**
	 * Parses a String representation of a date and time and retrieves the
	 * timezone from it
	 * 
	 * @param str
	 *            The String representation of the date (and optional timezone)
	 * @return Integer array of size 3. Element 1 represents whether the
	 *         timezone is ahead or behind GMT, element 2 is the hour
	 *         displacement and element 3 is the minute displacement.
	 */
	public static int[] parse_timezone(String str) {
		int[] ret = new int[3];

		for (int i = 0; i < ret.length; i++)
			ret[i] = 0;
		ret[0] = 1;

		if (str.equals("Z")) {
			return ret;
		}

		// get initial plus/minus
		if (str.startsWith("+"))
			ret[0] = 1;
		else if (str.startsWith("-"))
			ret[0] = -1;
		else
			return null;

		str = str.substring(1, str.length());

		if (str.length() != (2 + 1 + 2))
			return null;

		try {
			ret[1] = Integer.parseInt(str.substring(0, 2));
			ret[2] = Integer.parseInt(str.substring(3, 5));

			// According to schema spec, timezone is limited to
			// this... [well.. almost...]
			if (ret[1] > 14)
				return null;
			if (ret[2] > 59)
				return null;

			return ret;
		} catch (NumberFormatException err) {
			return null;
		}
	}

	/**
	 * Attempts to set a particular field in the Calendar
	 * 
	 * @param cal
	 *            The Calendar object to set the field in
	 * @param item
	 *            The field to set
	 * @param val
	 *            The value to set the field to
	 * @return True if successfully set. False otherwise (due to out of bounds
	 *         for that field)
	 */
	private static boolean set_item(Calendar cal, int item, int val) {
		int min = cal.getActualMinimum(item);

		if (val < min)
			return false;

		int max = cal.getActualMaximum(item);

		if (val > max)
			return false;

		cal.set(item, val);
		return true;
	}

	/**
	 * Parses a String representation of a date and time and constructs a new
	 * XSDateTime object using that information
	 * 
	 * @param str
	 *            The String representation of the date (and optional timezone)
	 * @return The XSDateTime representation of the date and time (and optional
	 *         timezone)
	 */
	public static XSDateTime parseDateTime(String str) {
		// oh no... not again

		// ok its three things:
		// date T time timezone
		str = str.trim();

		int index = str.indexOf('T');
		if (index == -1)
			return null;

		// split date and rest...
		String date = str.substring(0, index);
		String time = str.substring(index + 1, str.length());
		String timezone = null;

		// check for timezone
		index = time.indexOf('+');
		if (index == -1)
			index = time.indexOf('-');
		if (index == -1)
			index = time.indexOf('Z');
		if (index != -1) {
			timezone = time.substring(index, time.length());
			time = time.substring(0, index);
		}

		// get date
		int d[] = parse_date(date);
		if (d == null)
			return null;

		// get timezone
		TimeZone calendarTimeZone;
		if (timezone != null) {
			int[] tz = parse_timezone(timezone);
			if (tz == null)
				return null;

			XSDuration tzd = new XSDayTimeDuration(0, tz[1], tz[2], BigDecimal.ZERO, tz[0] < 0);
			calendarTimeZone = getTimeZone(tz[1], tz[2], tz[0] < 0);
		} else {
			calendarTimeZone = TimeZone.getTimeZone("GMT");
		}

		GregorianCalendar cal = new GregorianCalendar(calendarTimeZone);
		cal.setGregorianChange(new Date(Long.MIN_VALUE));

		// year
		int year = d[0];
		if (year <= 0) {
			year = 1 - year;
			cal.set(Calendar.ERA, GregorianCalendar.BC);
		} else {
			cal.set(Calendar.ERA, GregorianCalendar.AD);
		}

		// this is a nice bug....
		// if say the current day is 29...
		// then if we set the month to feb for example, and 29 doesn't
		// exist in that year, then the date screws up.
		cal.set(Calendar.DAY_OF_MONTH, 2);
		cal.set(Calendar.MONTH, 2);

		if (!set_item(cal, Calendar.YEAR, year))
			return null;

		if (!set_item(cal, Calendar.MONTH, d[1] - 1))
			return null;

		if (!set_item(cal, Calendar.DAY_OF_MONTH, d[2]))
			return null;

		// get time
		BigDecimal[] t = parse_time(time);
		if (t == null)
			return null;

		if (t[0].intValueExact() == 24) {
			// '24' is permitted if the minutes and seconds represented are zero
			if (t[1].compareTo(BigDecimal.ZERO) != 0 || t[2].compareTo(BigDecimal.ZERO) != 0) {
				return null;
			}

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.add(Calendar.HOUR_OF_DAY, 24);
		} else if (!set_item(cal, Calendar.HOUR_OF_DAY, t[0].intValueExact())) {
			return null;
		}

		if (!set_item(cal, Calendar.MINUTE, t[1].intValueExact()))
			return null;

		int totalMilliseconds = t[2].multiply(new BigDecimal(1000)).intValue();
		if (!set_item(cal, Calendar.SECOND, totalMilliseconds / 1000))
			return null;

		if (!set_item(cal, Calendar.MILLISECOND, totalMilliseconds % 1000))
			return null;

		return new XSDateTime(cal, timezone != null);
	}

	public static TimeZone getTimeZone(int hours, int minutes, boolean negative) {
		return TimeZone.getTimeZone(String.format("GMT%s%s%s", negative ? "-" : "+", pad_int(hours, 2), pad_int(minutes, 2)));
	}

	/**
	 * Creates a new result sequence consisting of the retrievable date and time
	 * value in the supplied result sequence
	 * 
	 * @param arg
	 *            The result sequence from which to extract the date and time
	 *            value.
	 * @throws DynamicError
	 * @return A new result sequence consisting of the date and time value
	 *         supplied.
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyAtomicType aat = (AnyAtomicType) arg.first();
		if (!(aat instanceof XSString
			|| aat instanceof XSUntypedAtomic
			|| aat instanceof XSDateTime
			|| aat instanceof XSDate))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		CalendarType dt = castDateTime(aat);

		if (dt == null)
			throw DynamicError.cant_cast(null);

		return dt;
	}

	private boolean isCastable(AnyAtomicType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic) {
			return true;
		}

		if (aat instanceof XSTime) {
			return false;
		}

		if (aat instanceof XSDate || aat instanceof XSDateTime) {
			return true;
		}

		return false;
	}

	private CalendarType castDateTime(AnyAtomicType aat) {
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSDateTime(date.calendar(), date.timezoned());
		}

		if (aat instanceof XSDateTime) {
			return (XSDateTime)aat;
		}

		return parseDateTime(aat.getStringValue());
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
	 * Retrieve the hour from the date stored
	 * 
	 * @return the hour value of the date stored
	 */
	public int hour() {
		return _calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * Retrieve the minute from the date stored
	 * 
	 * @return the minute value of the date stored
	 */
	public int minute() {
		return _calendar.get(Calendar.MINUTE);
	}

	/**
	 * Retrieve the seconds from the date stored
	 * 
	 * @return the seconds value of the date stored
	 */
	public BigDecimal second() {
		BigDecimal s = new BigDecimal(_calendar.get(Calendar.SECOND));

		int ms = _calendar.get(Calendar.MILLISECOND);
		if (ms != 0) {
			s = s.add(new BigDecimal(ms).divide(new BigDecimal(1000)));
		}

		return s;
	}

	@Override
	public boolean timezoned() {
		return _timezoned;
	}

	/**
	 * Pads the supplied number to the supplied number of digits by adding 0's
	 * in front of it
	 * 
	 * @param num
	 *            Number that si to be padded (if neccessay)
	 * @param len
	 *            Desired length after padding
	 * @return String representation of the padded integer
	 */
	public static String pad_int(int num, int len) {
		String ret = "";
		String snum = "" + num;

		int pad = len - snum.length();

		// sort out the negative
		if (num < 0) {
			ret += "-";
			snum = snum.substring(1, snum.length());
			pad++;
		}

		StringBuilder buf = new StringBuilder(ret);
		for (int i = 0; i < pad; i++) {
			buf.append("0");
		}
		buf.append(snum);
		ret = buf.toString();
		return ret;
	}

	/**
	 * Retrieves a String representation of the date and time stored
	 * 
	 * @return String representation of the date and time stored
	 */
	@Override
	public String getStringValue() {
		String ret = "";

		Calendar adjustFortimezone = calendar();

		int year = adjustFortimezone.get(Calendar.YEAR);
		if (adjustFortimezone.get(Calendar.ERA) == GregorianCalendar.BC) {
			year--;
			ret += "-";
		}

		ret += pad_int(year, 4);

		ret += "-";
		ret += pad_int(month(), 2);

		ret += "-";
		ret += pad_int(adjustFortimezone.get(Calendar.DAY_OF_MONTH), 2);

		// time
		ret += "T";

		ret += pad_int(adjustFortimezone.get(Calendar.HOUR_OF_DAY), 2);

		ret += ":";
		ret += pad_int(adjustFortimezone.get(Calendar.MINUTE), 2);

		ret += ":";
		BigDecimal sec = second();
		int isecond = sec.intValue();

		if (sec.compareTo(new BigDecimal(isecond)) == 0) {
			ret += pad_int(isecond, 2);
		} else {
			if (sec.compareTo(new BigDecimal(10)) < 0) {
				ret += 0;
			}

			ret += sec;
		}

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
				tZoneStr += pad_int(hrs, 2);
				tZoneStr += ":";
				tZoneStr += pad_int(min, 2);

				ret += tZoneStr;
			}
		}

		return ret;
	}

	/**
	 * Retrive the datatype full pathname
	 * 
	 * @return "xs:dateTime" which is the datatype full pathname
	 */
	@Override
	public String string_type() {
		return XS_DATE_TIME;
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

	// comparisons
	/**
	 * Equality comparison on this and the supplied dates and times (taking
	 * timezones into account)
	 * 
	 * @param arg
	 *            XSDateTime representation of the date to compare to
	 * @throws DynamicError
	 * @return True if the two dates and times are represent the same exact
	 *         point in time. False otherwise.
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDateTime val = NumericType.get_single_type(arg, XSDateTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.compareTo(thatcal) == 0;
	}

	/**
	 * Comparison on this and the supplied dates and times (taking timezones
	 * into account)
	 * 
	 * @param arg
	 *            XSDateTime representation of the date to compare to
	 * @throws DynamicError
	 * @return True if in time, this date and time lies before the date and time
	 *         supplied. False otherwise.
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDateTime val = NumericType.get_single_type(arg, XSDateTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.before(thatcal);
	}

	/**
	 * Comparison on this and the supplied dates and times (taking timezones
	 * into account)
	 * 
	 * @param arg
	 *            XSDateTime representation of the date to compare to
	 * @throws DynamicError
	 * @return True if in time, this date and time lies after the date and time
	 *         supplied. False otherwise.
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDateTime val = NumericType.get_single_type(arg, XSDateTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.after(thatcal);
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
	 * Mathematical minus operator between this XSDateTime and a supplied result
	 * sequence (XSDateTime, XDTYearMonthDuration and XDTDayTimeDuration are
	 * only valid ones).
	 * 
	 * @param arg
	 *            The supplied ResultSequence that is on the right of the minus
	 *            operator. If this is an XSDateTime, the result will be a
	 *            XDTDayTimeDuration of the duration of time between these two
	 *            dates. If arg is an XDTYearMonthDuration or an
	 *            XDTDayTimeDuration the result will be a XSDateTime of the
	 *            result of the current date minus the duration of time
	 *            supplied.
	 * @return New ResultSequence consisting of the result of the mathematical
	 *         minus operation.
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();

		if (!(at instanceof XSDateTime) && !(at instanceof XSYearMonthDuration)
				&& !(at instanceof XSDayTimeDuration)) {
			throw DynamicError.throw_type_error();
		}

		if (at instanceof XSDateTime) {
			return minusXSDateTime(arg, evaluationContext);
		}

		if (at instanceof XSYearMonthDuration) {
			return minusXSYearMonthDuration((XSYearMonthDuration) at);
		}

		if (at instanceof XSDayTimeDuration) {
			return minusXSDayTimeDuration((XSDayTimeDuration) at);
		}
		return null; // unreach

	}

	private ResultSequence minusXSDateTime(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDateTime val = NumericType.get_single_type(arg, XSDateTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		long duration = thiscal.getTimeInMillis() - thatcal.getTimeInMillis();
		return new XSDayTimeDuration(_datatypeFactory.newDuration(duration));
	}

	private ResultSequence minusXSDayTimeDuration(XSDayTimeDuration val) {
		Calendar resultCalendar = (Calendar)calendar().clone();
		// multiplier is negated due to this being a 'minus' operation
		int multiplier = val.negative() ? 1 : -1;
		resultCalendar.add(Calendar.DAY_OF_YEAR, multiplier * val.days());
		resultCalendar.add(Calendar.HOUR_OF_DAY, multiplier * val.hours());
		resultCalendar.add(Calendar.MINUTE, multiplier * val.minutes());
		resultCalendar.add(Calendar.MILLISECOND, multiplier * val.seconds().multiply(new BigDecimal(1000)).intValue());
		return new XSDateTime(resultCalendar, timezoned());
	}

	private ResultSequence minusXSYearMonthDuration(XSYearMonthDuration val) {
		Calendar resultCalendar = (Calendar)calendar().clone();
		resultCalendar.add(Calendar.MONTH, val.monthValue() * -1);
		return new XSDateTime(resultCalendar, timezoned());
	}

	/**
	 * Mathematical addition operator between this XSDateTime and a supplied
	 * result sequence (XDTYearMonthDuration and XDTDayTimeDuration are only
	 * valid ones).
	 * 
	 * @param arg
	 *            The supplied ResultSequence that is on the right of the minus
	 *            operator. If arg is an XDTYearMonthDuration or an
	 *            XDTDayTimeDuration the result will be a XSDateTime of the
	 *            result of the current date minus the duration of time
	 *            supplied.
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
			return new XSDateTime(adjusted, timezoned());
		} else if (at instanceof XSDayTimeDuration) {
			XSDayTimeDuration duration = (XSDayTimeDuration)at;
			XSDayTimeDuration negatedDuration = new XSDayTimeDuration(duration.days(), duration.hours(), duration.minutes(), duration.seconds(), !duration.negative());
			return minusXSDayTimeDuration(negatedDuration);
		} else {
			throw DynamicError.throw_type_error();
		}

	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_DATETIME;
	}

}
