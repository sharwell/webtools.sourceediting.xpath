/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 262765 - Fix parsing of gMonthDay to valid date
 *     David Carver (STAR) - bug 282223 - fix timezone adjustment creation.
 *                                        fixed casting issue.
 *     David Carver - bug 280547 - fix dates for comparison 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.Duration;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the MonthDay datatype
 */
public class XSGMonthDay extends CalendarType implements CmpEq {

	private static final String XS_G_MONTH_DAY = "xs:gMonthDay";
	private final int _month;
	private final int _day;
	private final TimeZone _timeZone;

	/**
	 * Initialises a representation of the supplied month and day
	 * 
	 * @param cal
	 *            Calendar representation of the month and day to be stored
	 * @param tz
	 *            Timezone associated with this month and day
	 */
//	@Deprecated
//	public XSGMonthDay(Calendar cal, XSDuration tz) {
//		_calendar = cal;
//		if (tz != null) {
//			_timezoned = true;
//			_tz = tz;
//		}
//	}

	public XSGMonthDay(int month, int day, TimeZone timeZone) {
		assert month >= 1 && month <= 12;
		assert day >= 1 && day <= 31;
		_month = month;
		_day = day;
		_timeZone = timeZone;
	}

	/**
	 * Initializes a representation of the current month and day
	 */
	public XSGMonthDay() {
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		_month = calendar.get(Calendar.MONTH) - 1;
		_day = calendar.get(Calendar.DAY_OF_MONTH);
		_timeZone = calendar.getTimeZone();
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "gMonthDay" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "gMonthDay";
	}

	/**
	 * Parses a String representation of a month and day and constructs a new
	 * XSGMonthDay representation of it.
	 * 
	 * @param str
	 *            The String representation of the month and day (and optional
	 *            timezone)
	 * @return The XSGMonthDay representation of the supplied date
	 */
	public static XSGMonthDay parse_gMonthDay(String str) {
		/* The lexical representation for gMonthDay is the left truncated
		 * lexical representation for date: --MM-DD. An optional following time
		 * zone qualifier is allowed as for date. No preceding sign is allowed.
		 * No other formats are allowed.
		 */

		str = str.trim();

		Pattern pattern = Pattern.compile("^--[0-9]{2}-[0-9]{2}");
		Matcher matcher = pattern.matcher(str);
		if (!matcher.find()) {
			return null;
		}

		String monthday = matcher.group();
		String timezone = str.substring(monthday.length());

		String year = "1972";
		String time = "T00:00:00.0";
		String datetime =  year + monthday.substring(1) + time + timezone;

		XSDateTime dt = XSDateTime.parseDateTime(datetime);
		if (dt == null)
			return null;

		return new XSGMonthDay(dt.calendar().get(Calendar.MONTH) + 1, dt.calendar().get(Calendar.DAY_OF_MONTH), dt.timezoned() ? dt.calendar().getTimeZone() : null);
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable gMonthDay in
	 * the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the gMonthDay is to be extracted
	 * @return New ResultSequence consisting of the supplied month and day
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyType aat = FnData.atomize(arg.first());
		if (!(aat instanceof XSString
			|| aat instanceof XSUntypedAtomic
			|| aat instanceof XSDateTime
			|| aat instanceof XSDate
			|| aat instanceof XSGMonthDay))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable((AnyAtomicType) aat)) {
			throw DynamicError.cant_cast(null);
		}
		
		XSGMonthDay val = castGMonthDay((AnyAtomicType) aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		return val;
	}

	protected boolean isGDataType(AnyAtomicType aat) {
		String type = aat.string_type();
		if (type.equals("xs:gDay") ||
			type.equals("xs:gMonth") ||
			type.equals("xs:gYear") ||
			type.equals("xs:gYearMonth")) {
			return true;
		}
		return false;
	}
	
	private boolean isCastable(AnyAtomicType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic) {
			return true;
		}
		
		if (aat instanceof XSTime) {
			return false;
		}
		
		if (aat instanceof XSDate || aat instanceof XSDateTime || 
			aat instanceof XSGMonthDay) {
			return true;
		}
		
		return false;
	}
	
	private XSGMonthDay castGMonthDay(AnyAtomicType aat) {
		if (aat instanceof XSGMonthDay) {
			XSGMonthDay gmd = (XSGMonthDay) aat;
			return new XSGMonthDay(gmd._month, gmd._day, gmd._timeZone);
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGMonthDay(date.calendar().get(Calendar.MONTH) + 1, date.calendar().get(Calendar.DAY_OF_MONTH), date.timezoned() ? date.calendar().getTimeZone() : null);
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGMonthDay(dateTime.calendar().get(Calendar.MONTH) + 1, dateTime.calendar().get(Calendar.DAY_OF_MONTH), dateTime.timezoned() ? dateTime.calendar().getTimeZone() : null);
		}
		
		return parse_gMonthDay(aat.getStringValue());
	}
	
	/**
	 * Retrieves the actual month as an integer
	 * 
	 * @return The actual month as an integer
	 */
	public int month() {
		return _month;
	}

	/**
	 * Retrieves the actual day as an integer
	 * 
	 * @return The actual day as an integer
	 */
	public int day() {
		return _day;
	}

	/**
	 * Check for whether a timezone was specified at creation
	 * 
	 * @return True if a timezone was specified. False otherwise
	 */
	@Override
	public boolean timezoned() {
		return _timeZone != null;
	}

	/**
	 * Retrieves a String representation of the stored month and day
	 * 
	 * @return String representation of the stored month and day
	 */
	@Override
	public String getStringValue() {
		String ret = "--";

		Calendar adjustFortimezone = calendar();
		
		ret += XSDateTime.pad_int(month(), 2);

		ret += "-";
		ret += XSDateTime.pad_int(adjustFortimezone.get(Calendar.DAY_OF_MONTH), 2);

		if (timezoned()) {
			
			int hrs = tz().hours();
			int min = tz().minutes();
			BigDecimal secs = tz().seconds();
			if (hrs == 0 && min == 0 && secs.compareTo(BigDecimal.ZERO) == 0) {
			  ret += "Z";
			}
			else {
			  String tZoneStr = "";
			  if (tz().negative()) {
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
	 * @return "xs:gMonthDay" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_G_MONTH_DAY;
	}

	/**
	 * Retrieves the Calendar representation of the month and day stored
	 * 
	 * @return Calendar representation of the month and day stored
	 */
	@Override
	public Calendar calendar() {
		GregorianCalendar calendar = new GregorianCalendar(_timeZone != null ? _timeZone : TimeZone.getTimeZone("GMT"));
		calendar.clear();
		calendar.setGregorianChange(new Date(Long.MIN_VALUE));
		calendar.set(Calendar.MONTH, _month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, _day);
		return calendar;
	}

	/**
	 * Equality comparison between this and the supplied representation. This
	 * representation must be of type XSGMonthDay
	 * 
	 * @param arg
	 *            The XSGMonthDay to compare with
	 * @return True if the two representations are of the same month and day.
	 *         False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSGMonthDay val = NumericType.get_single_type(arg, XSGMonthDay.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.compareTo(thatcal) == 0;
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

		BigDecimal rawOffset = new BigDecimal(_timeZone.getRawOffset()).divide(new BigDecimal(1000));
		return new XSDayTimeDuration(rawOffset);
	}	

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_GMONTHDAY;
	}
}
