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
 *     David Carver - bug 282223 - implementation of xs:duration data type.
 *                                 correction of casting to time. 
 *     David Carver - bug 280547 - fix dates for comparison 
 *     Jesper Steen Moller - bug 262765 - fix type tests
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
 * A representation of the Time datatype
 */
public class XSTime extends CalendarType implements CmpEq, CmpLt, CmpGt,

MathMinus, MathPlus,

Cloneable {

	private static final String XS_TIME = "xs:time";
	private Calendar _calendar;
	private boolean _timezoned;

	/**
	 * Initialises to the supplied time and timezone
	 * 
	 * @param cal
	 *            Calendar representation of the time to be stored
	 * @param tz
	 *            The timezone (possibly null) associated with this time
	 */
//	@Deprecated
//	public XSTime(Calendar cal, XSDuration tz) {
//		_calendar = cal;
//
//		_tz = tz;
//		if (tz == null)
//			_timezoned = false;
//		else
//			_timezoned = true;
//	}

	public XSTime(Calendar calendar, boolean hasTimeZone) {
		assert calendar.get(Calendar.ERA) == GregorianCalendar.AD;
		assert calendar.get(Calendar.YEAR) == 1970;
		assert calendar.get(Calendar.DAY_OF_YEAR) == 1;

		_calendar = calendar;
		_timezoned = hasTimeZone;
	}

	/**
	 * Initializes to the current time
	 */
	public XSTime() {
		this(getTime(new GregorianCalendar(TimeZone.getTimeZone("GMT"))), true);
	}

	public static Calendar getTime(Calendar calendar) {
		GregorianCalendar time = new GregorianCalendar(calendar.getTimeZone());
		time.setGregorianChange(new Date(Long.MIN_VALUE));
		time.setTimeInMillis(calendar.getTimeInMillis());
		time.set(Calendar.ERA, GregorianCalendar.AD);
		time.set(Calendar.YEAR, 1970);
		time.set(Calendar.DAY_OF_YEAR, 1);
		return time;
	}

	/**
	 * Creates a new copy of the time (and timezone) stored
	 * 
	 * @return New XSTime representing the copy of the time and timezone
	 * @throws CloneNotSupportedException
	 */
	@Override
	public XSTime clone() {
		Calendar c = (Calendar) calendar().clone();
		return new XSTime(c, timezoned());
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "time" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "time";
	}

	/**
	 * Creates a new XSTime representing the String represented supplied time
	 * 
	 * @param str
	 *            String represented time and timezone to be stored
	 * @return New XSTime representing the supplied time
	 */
	public static CalendarType parse_time(String str) {
		str = str.trim();

		String startdate = "1983-11-29T";
		
		XSDateTime dt = XSDateTime.parseDateTime(startdate + str);
		if (dt == null)
			return null;

		return new XSTime(getTime(dt.calendar()), dt.timezoned());
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable time from the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which to extract the time
	 * @return New ResultSequence consisting of the supplied time
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyAtomicType aat = (AnyAtomicType) arg.first();
		if (!(aat instanceof XSString
			|| aat instanceof XSUntypedAtomic
			|| aat instanceof XSDateTime
			|| aat instanceof XSTime))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.invalidType();
		}
		
		CalendarType t = castTime(aat);

		if (t == null)
			throw DynamicError.cant_cast(null);

		return t;
	} 

	private boolean isCastable(AnyAtomicType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic) {
			return true;
		}
		
		if (aat instanceof XSDateTime) {
			return true;
		}
		
		if (aat instanceof XSTime) {
			return true;
		}
		return false;
	}
	
	private CalendarType castTime(AnyAtomicType aat) {
		if (aat instanceof XSTime) {
			XSTime time = (XSTime) aat;
			return new XSTime(time.calendar(), time.timezoned());
		}
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSTime(getTime(dateTime.calendar()), dateTime.timezoned());
		}
		
		return parse_time(aat.getStringValue());
	}
	/**
	 * Retrieves the hour stored as an integer
	 * 
	 * @return The hour stored
	 */
	public int hour() {
		return _calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * Retrieves the minute stored as an integer
	 * 
	 * @return The minute stored
	 */
	public int minute() {
		return _calendar.get(Calendar.MINUTE);
	}

	/**
	 * Retrieves the seconds stored as an integer
	 * 
	 * @return The second stored
	 */
	public double second() {
		double s = _calendar.get(Calendar.SECOND);

		double ms = _calendar.get(Calendar.MILLISECOND);

		ms /= 1000;

		s += ms;
		return s;
	}

	/**
	 * Check for whether the time stored has a timezone associated with it
	 * 
	 * @return True if the time has a timezone associated. False otherwise
	 */
	@Override
	public boolean timezoned() {
		return _timezoned;
	}

	/**
	 * Retrieves a String representation of the time stored
	 * 
	 * @return String representation of the time stored
	 */
	@Override
	public String getStringValue() {
		String ret = "";
		
		Calendar adjustFortimezone = calendar();
		ret += XSDateTime.pad_int(adjustFortimezone.get(Calendar.HOUR_OF_DAY), 2);
		
		ret += ":";
		ret += XSDateTime.pad_int(adjustFortimezone.get(Calendar.MINUTE), 2);
		

		ret += ":";
		int isecond = (int) second();
		double sec = second();

		if ((sec - (isecond)) == 0.0)
			ret += XSDateTime.pad_int(isecond, 2);
		else {
			if (sec < 10.0)
				ret += "0" + sec;
			else
				ret += sec;
		}

		if (timezoned()) {
			XSDuration tz = tz();
			int hrs = tz.hours();
			int min = tz.minutes();
			BigDecimal secs = tz.seconds();
			if (hrs == 0 && min == 0 && secs.compareTo(BigDecimal.ZERO) == 0) {
			  ret += "Z";
			}
			else {
			  String tZoneStr = "";
			  if (tz.negative()) {
				tZoneStr += "-";  
			  }
			  else {
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
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:time" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_TIME;
	}

	/**
	 * Retrieves a Calendar representation of time stored
	 * 
	 * @return Calendar representation of the time stored
	 */
	@Override
	public Calendar calendar() {
		return _calendar;
	}

	/**
	 * Retrieves the timezone associated with the time stored as a duration of
	 * time
	 * 
	 * @return The duration of time between the time stored and the actual time
	 *         after the timezone is taken into account
	 */
	public XSDayTimeDuration tz() {
		if (!timezoned()) {
			return null;
		}

		TimeZone timeZone = calendar().getTimeZone();
		BigDecimal rawOffset = new BigDecimal(timeZone.getRawOffset()).divide(new BigDecimal(1000));
		return new XSDayTimeDuration(rawOffset);
	}

	/**
	 * Retrieves the time in milliseconds since the epoch
	 * 
	 * @return time stored in milliseconds since the epoch
	 */
	public double value() {
		return calendar().getTimeInMillis() / 1000.0;
	}

	/**
	 * Equality comparison between this and the supplied XSTime representation
	 * 
	 * @param arg
	 *            The XSTime to compare with
	 * @return True if both XSTime's represent the same time. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSTime val = NumericType.get_single_type(arg, XSTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.compareTo(thatcal) == 0;
	}

	/**
	 * Comparison between this and the supplied XSTime representation
	 * 
	 * @param arg
	 *            The XSTime to compare with
	 * @return True if the supplied time represnts a point in time after that
	 *         represented by the time stored. False otherwise
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSTime val = NumericType.get_single_type(arg, XSTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.before(thatcal);
	}
	
	/**
	 * Comparison between this and the supplied XSTime representation
	 * 
	 * @param arg
	 *            The XSTime to compare with
	 * @return True if the supplied time represnts a point in time before that
	 *         represented by the time stored. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSTime val = NumericType.get_single_type(arg, XSTime.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.after(thatcal);
	}

	/**
	 * Mathematical subtraction between this time stored and the supplied
	 * representation. This supplied representation must be of either type
	 * XSTime (in which case the result is the duration of time between these
	 * two times) or a XSDayTimeDuration (in which case the result is the time
	 * when this duration is subtracted from the time stored).
	 * 
	 * @param arg
	 *            The representation to subtract (either XSTim or
	 *            XDTDayTimeDuration)
	 * @return A ResultSequence representing the result of the subtraction
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();
		
		if (!(at instanceof XSTime) && !(at instanceof XSDayTimeDuration)) {
			throw DynamicError.throw_type_error();
		}

		if (at instanceof XSTime) {
			return minusXSTimeDuration((XSTime) at, evaluationContext);
		}
		
		if (at instanceof XSDayTimeDuration) {
			return minusXSDayTimeDuration((XSDayTimeDuration) at);
		}
		return null; // unreach
	}

	private ResultSequence minusXSDayTimeDuration(XSDayTimeDuration val) {
		Calendar resultCalendar = (Calendar)calendar().clone();
		// multiplier is negated due to this being a 'minus' operation
		int multiplier = val.negative() ? 1 : -1;
		resultCalendar.add(Calendar.DAY_OF_YEAR, multiplier * val.days());
		resultCalendar.add(Calendar.HOUR_OF_DAY, multiplier * val.hours());
		resultCalendar.add(Calendar.MINUTE, multiplier * val.minutes());
		resultCalendar.add(Calendar.MILLISECOND, multiplier * val.seconds().multiply(new BigDecimal(1000)).intValue());
		return new XSTime(getTime(resultCalendar), timezoned());
	}

	private ResultSequence minusXSTimeDuration(XSTime val, EvaluationContext evaluationContext) {
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		long duration = thiscal.getTimeInMillis() - thatcal.getTimeInMillis();
		return new XSDayTimeDuration(_datatypeFactory.newDuration(duration));
	}

	/**
	 * Mathematical addition between this time stored and the supplied time
	 * duration.
	 * 
	 * @param arg
	 *            A XDTDayTimeDuration representation of the duration of time to
	 *            add
	 * @return A XSTime representing the result of this addition.
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence plus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDuration val = NumericType.get_single_type(arg, XSDayTimeDuration.class);

		int ms = val.time_value().multiply(new BigDecimal(1000)).intValue();
		Calendar adjusted = (Calendar)calendar().clone();
		adjusted.add(Calendar.MILLISECOND, ms);
		return new XSTime(getTime(adjusted), timezoned());
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_TIME;
	}

	@Override
	public Object getNativeValue() {
		return _calendar.clone();
	}	
}
