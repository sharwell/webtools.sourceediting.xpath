/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 282223 - Implemented XSDuration type for castable checking.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the xs:duration data type. Other duration implementations
 * should inherit from this implementation.
 * 
 * @since 1.1 This used to be an abstract class but was incorrectly implemented
 *        as such.
 */
public class XSDuration extends CtrType implements CmpEq, CmpLt, CmpGt, Cloneable {

	private static final String XS_DURATION = "xs:duration";

	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;

	private static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	private static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

	private static final int MONTHS_PER_YEAR = 12;

	private static Pattern DURATION_PATTERN = Pattern.compile("-?P(?:([0-9]+)Y)?(?:([0-9]+)M)?(?:([0-9]+)D)?(?:T(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+(?:\\.[0-9]+)?)S)?)?");

	private final int _year;
	private final int _month;
	private final int _days;
	private final int _hours;
	private final int _minutes;
	private final BigDecimal _seconds;
	private final boolean _negative;

	/**
	 * Initializes to the supplied parameters. If more than 24 hours is
	 * supplied, the number of days is adjusted accordingly. The same occurs for
	 * minutes and seconds
	 * 
	 * @param years
	 *            Number of years in this duration of time.
	 * @param months
	 *            Number of months in this duration of time.
	 * @param days
	 *            Number of days in this duration of time
	 * @param hours
	 *            Number of hours in this duration of time
	 * @param minutes
	 *            Number of minutes in this duration of time
	 * @param seconds
	 *            Number of seconds in this duration of time
	 * @param negative
	 *            True if this duration of time represents a backwards passage
	 *            through time. False otherwise
	 */
	public XSDuration(int years, int months, int days, int hours, int minutes,
			BigDecimal seconds, boolean negative) {
		assert years >= 0;
		assert months >= 0;
		assert days >= 0;
		assert hours >= 0;
		assert minutes >= 0;
		assert seconds != null && seconds.compareTo(BigDecimal.ZERO) >= 0;

		int adjustedYear = years;
		int adjustedMonth = months;
		int adjustedDays = days;
		int adjustedHours = hours;
		int adjustedMinutes = minutes;
		BigDecimal adjustedSeconds = seconds;
		boolean adjustedNegative = negative;

		if (adjustedMonth >= MONTHS_PER_YEAR) {
			adjustedYear += adjustedMonth / MONTHS_PER_YEAR;
			adjustedMonth = adjustedMonth % MONTHS_PER_YEAR;
		}

		if (adjustedSeconds.compareTo(BigDecimal.valueOf(SECONDS_PER_MINUTE)) >= 0) {
			int isec = adjustedSeconds.intValue();
			BigDecimal rem = adjustedSeconds.subtract(BigDecimal.valueOf(isec));

			adjustedMinutes += isec / SECONDS_PER_MINUTE;
			adjustedSeconds = BigDecimal.valueOf(isec % SECONDS_PER_MINUTE);
			adjustedSeconds = adjustedSeconds.add(rem);
		}
		if (adjustedMinutes >= MINUTES_PER_HOUR) {
			adjustedHours += adjustedMinutes / MINUTES_PER_HOUR;
			adjustedMinutes = adjustedMinutes % MINUTES_PER_HOUR;
		}
		if (adjustedHours >= HOURS_PER_DAY) {
			adjustedDays += adjustedHours / HOURS_PER_DAY;
			adjustedHours = adjustedHours % HOURS_PER_DAY;
		}

		_year = adjustedYear;
		_month = adjustedMonth;
		_days = adjustedDays;
		_hours = adjustedHours;
		_minutes = adjustedMinutes;
		_seconds = adjustedSeconds;
		_negative = adjustedNegative;
	}

	/**
	 * Initialises to the given number of seconds
	 * 
	 * @param secs
	 *            Number of seconds in the duration of time
	 */
	public XSDuration(BigDecimal secs) {
		this(0, 0, 0, 0, 0, secs.abs(), secs.compareTo(BigDecimal.ZERO) < 0);
	}

	/**
	 * Initialises to a duration of no time (0days, 0hours, 0minutes, 0seconds)
	 */
	public XSDuration() {
		this(0, 0, 0, 0, 0, BigDecimal.ZERO, false);
	}

	@Override
	public String type_name() {
		return "duration";
	}

	@Override
	public String string_type() {
		return XS_DURATION;
	}

	/**
	 * Retrieves a String representation of the duration stored
	 * 
	 * @return String representation of the duration stored
	 */
	@Override
	public String getStringValue() {
		String ret = "";
		boolean did_something = false;
		String tret = "";

		if (negative() && !(days() == 0 && hours() == 0 && seconds().compareTo(BigDecimal.ZERO) == 0))
			ret += "-";

		ret += "P";

		int years = year();
		if (years != 0)
			ret += years + "Y";

		int months = month();
		if (months != 0) {
			ret += months + "M";
		}

		if (days() != 0) {
			ret += days() + "D";
			did_something = true;
		}

		// do the "time" bit
		int hours = hours();
		int minutes = minutes();
		BigDecimal seconds = seconds();
		
		if (hours != 0) {
			tret += hours + "H";
			did_something = true;
		}
		if (minutes != 0) {
			tret += minutes + "M";
			did_something = true;
		}
		if (seconds.compareTo(BigDecimal.ZERO) != 0) {
			boolean isInteger = seconds.scale() <= 0
					|| seconds.stripTrailingZeros().scale() <= 0;
			if (isInteger) {
				seconds = seconds.setScale(0, RoundingMode.UNNECESSARY);
			}

			tret += seconds + "S";
			did_something = true;
		} else if (!did_something) {
				tret += "0S";
		}
		
		if ((year() == 0 && month() == 0) || (hours > 0 || minutes > 0 || seconds.compareTo(BigDecimal.ZERO) > 0)) {
			if (tret.length() > 0) {
				ret += "T" + tret;
			}
		}

		return ret;
	}

	/**
	 * Retrieves the number of days within the duration of time stored
	 * 
	 * @return Number of days within the duration of time stored
	 */
	public int days() {
		return _days;
	}

	/**
	 * Retrieves the number of minutes (max 60) within the duration of time
	 * stored
	 * 
	 * @return Number of minutes within the duration of time stored
	 */
	public int minutes() {
		return _minutes;
	}

	/**
	 * Retrieves the number of hours (max 24) within the duration of time stored
	 * 
	 * @return Number of hours within the duration of time stored
	 */
	public int hours() {
		return _hours;
	}

	/**
	 * Retrieves the number of seconds (max 60) within the duration of time
	 * stored
	 * 
	 * @return Number of seconds within the duration of time stored
	 */
	public BigDecimal seconds() {
		return _seconds;
	}

	/**
	 * Equality comparison between this and the supplied duration of time.
	 * 
	 * @param arg
	 *            The duration of time to compare with
	 * @return True if they both represent the duration of time. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDuration val = NumericType.get_single_type(arg, XSDuration.class);

		return value().compareTo(val.value()) == 0
				&& monthValue() == val.monthValue();
	}

	/**
	 * Comparison between this and the supplied duration of time.
	 * 
	 * @param arg
	 *            The duration of time to compare with
	 * @return True if the supplied time represents a larger duration than that
	 *         stored. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDuration val = NumericType.get_single_type(arg, XSDayTimeDuration.class);

		return value().compareTo(val.value()) < 0;
	}

	/**
	 * Comparison between this and the supplied duration of time.
	 * 
	 * @param arg
	 *            The duration of time to compare with
	 * @return True if the supplied time represents a smaller duration than that
	 *         stored. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDuration val = NumericType.get_single_type(arg, XSDayTimeDuration.class);

		return value().compareTo(val.value()) > 0;
	}

	/**
	 * Retrieves whether this duration represents a backward passage through
	 * time
	 * 
	 * @return True if this duration represents a backward passage through time.
	 *         False otherwise
	 */
	public boolean negative() {
		return _negative;
	}

	/**
	 * Retrieves the duration of time stored as the number of months within it.
	 * This method only counts the {@link #month()} and {@link #year()}
	 * components of the duration.
	 *
	 * @return Number of months making up this duration of time
	 */
	public final int monthValue() {
		int value = year() * MONTHS_PER_YEAR + month();
		if (negative()) {
			value = -value;
		}

		return value;
	}

	/**
	 * Retrieves the duration of time stored as the number of seconds within it.
	 * This method does not count the {@link #month()} or {@link #year()}
	 * components of the duration.
	 *
	 * @return Number of seconds making up this duration of time
	 */
	public BigDecimal value() {
		BigDecimal ret = BigDecimal.valueOf(days() * SECONDS_PER_DAY);

		ret = ret.add(BigDecimal.valueOf(hours() * SECONDS_PER_HOUR));
		ret = ret.add(BigDecimal.valueOf(minutes() * SECONDS_PER_MINUTE));
		ret = ret.add(seconds());

		if (negative())
			ret = ret.negate();
		
		

		return ret;
	}
	
	public BigDecimal time_value() {
		BigDecimal ret = BigDecimal.ZERO;
		ret = ret.add(BigDecimal.valueOf(hours() * SECONDS_PER_HOUR));
		ret = ret.add(BigDecimal.valueOf(minutes() * SECONDS_PER_MINUTE));
		ret = ret.add(seconds());

		if (negative())
			ret = ret.negate();
		return ret;
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable time duration
	 * from the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which to extract
	 * @return New ResultSequence consisting of the time duration extracted
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyType aat = FnData.atomize(arg.first());
		// From 17.1.4 (Casting to duration types)
		if (!(aat instanceof XSString
				|| aat instanceof XSUntypedAtomic
				|| aat instanceof XSDuration)) {
			throw DynamicError.invalidType();
		}

		if (!(isCastable(aat))) {
			throw DynamicError.cant_cast(null);
		}
		
		XSDuration duration = castDuration(aat);

		if (duration == null)
			throw DynamicError.cant_cast(null);

		return duration;
	}

	private XSDuration castDuration(AnyType aat) {
		if (aat instanceof XSDuration) {
			XSDuration duration = (XSDuration) aat;
			return new XSDuration(duration.year(), duration.month(), duration.days(), duration.hours(), duration.minutes(), duration.seconds(), duration.negative());
		}
		
		return parseDuration(aat.getStringValue());
	}

	/**
	 * Creates a new XSDuration by parsing the supplied String
	 * represented duration of time
	 * 
	 * @param str
	 *            String represented duration of time
	 * @return New XSDayTimeDuration representing the duration of time supplied
	 */
	public static XSDuration parseDuration(String str) {
		str = str.trim();

		Matcher matcher = DURATION_PATTERN.matcher(str);
		if (!matcher.matches()) {
			return null;
		}

		boolean negative = str.startsWith("-");
		String yearStr = matcher.group(1);
		String monthStr = matcher.group(2);
		String dayStr = matcher.group(3);
		String hourStr = matcher.group(4);
		String minuteStr = matcher.group(5);
		String secondStr = matcher.group(6);

		int years = 0;
		int months = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		BigDecimal seconds = BigDecimal.ZERO;

		boolean hasElement = false;
		boolean hasTimeElement = false;

		if (yearStr != null) {
			hasElement = true;
			years = Integer.parseInt(yearStr);
		}

		if (monthStr != null) {
			hasElement = true;
			months = Integer.parseInt(monthStr);
		}

		if (dayStr != null) {
			hasElement = true;
			days = Integer.parseInt(dayStr);
		}

		if (hourStr != null) {
			hasElement = true;
			hasTimeElement = true;
			hours = Integer.parseInt(hourStr);
		}

		if (minuteStr != null) {
			hasElement = true;
			hasTimeElement = true;
			minutes = Integer.parseInt(minuteStr);
		}

		if (secondStr != null) {
			hasElement = true;
			hasTimeElement = true;
			seconds = new BigDecimal(secondStr);
		}

		if (!hasElement) {
			// At least one number and its designator ·must· be present.
			return null;
		}

		if (!hasTimeElement) {
			if (str.contains("T")) {
				// The designator 'T' must be absent if and only if all of the time items are absent.
				return null;
			}
		}

		return new XSDuration(years, months, days, hours, minutes, seconds,
				negative);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new XSDuration(year(), month(), days(), hours(), minutes(),
				seconds(), negative());
	}

	/**
	 * Retrieves the number of years within the duration of time stored
	 * 
	 * @return Number of years within the duration of time stored
	 */
	public int year() {
		return _year;
	}

	/**
	 * Retrieves the number of months within the duration of time stored
	 * 
	 * @return Number of months within the duration of time stored
	 */
	public int month() {
		return _month;
	}

	protected boolean isCastable(AnyType aat) {
		String type = aat.string_type();
		if (type.equals("xs:string") || type.equals("xs:untypedAtomic")) {
			if (isDurationValue(aat.getStringValue().trim())) {
				return true;  // We might be able to cast this.
			}
		}
		
		// We can cast from ourself or derivations of ourselves.
		if (aat instanceof XSDuration) {
			return true;
		}
				
		return false;
	}

	private boolean isDurationValue(String value) {
		return value.startsWith("P") || value.startsWith("-P");
	}
	

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_DURATION;
	}

	@Override
	public Object getNativeValue() {
		return _datatypeFactory.newDuration(! negative(), year(), month(), days(), hours(), minutes(), seconds().intValue());
	}
}
