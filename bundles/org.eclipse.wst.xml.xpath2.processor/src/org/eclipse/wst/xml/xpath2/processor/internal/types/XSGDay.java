/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 262765 - Correct parsing of Date to get day correctly.
 *     David Carver (STAR) - bug 282223 - fixed issue with casting.
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
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the Day datatype
 */
public class XSGDay extends CalendarType implements CmpEq {

	private static final String XS_G_DAY = "xs:gDay";
	private final int _day;
	private final TimeZone _timeZone;

	/**
	 * Initializes a representation of the supplied day
	 * 
	 * @param cal
	 *            Calendar representation of the day to be stored
	 * @param tz
	 *            Timezone associated with this day
	 */
//	@Deprecated
//	public XSGDay(Calendar cal, XSDuration tz) {
//		_calendar = cal;
//		if (tz != null) {
//			_timezoned = true;
//			_tz = tz;
//		}
//	}

	public XSGDay(int day, TimeZone timeZone) {
		assert day >= 1 && day <= 31;
		_day = day;
		_timeZone = timeZone;
	}

	/**
	 * Initializes a representation of the current day
	 */
	public XSGDay() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")).get(Calendar.DAY_OF_MONTH), TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "gDay" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "gDay";
	}

	/**
	 * Parses a String representation of a day and constructs a new XSGDay
	 * representation of it.
	 * 
	 * @param str
	 *            The String representation of the day (and optional timezone)
	 * @return The XSGDay representation of the supplied date
	 */
	public static XSGDay parse_gDay(String str) {
		/* The lexical representation for gDay is the left truncated lexical
		 * representation for date: ---DD . An optional following time zone
		 * qualifier is allowed as for date. No preceding sign is allowed. No
		 * other formats are allowed. See also ISO 8601 Date and Time Formats.
		 */
		str = str.trim();
		if (str.length() < 5 || !str.startsWith("---")) {
			return null;
		}

		String day = str.substring(3, 5);
		String timezone = str.substring(5);
		String startdate = "1972-12-";
		String starttime = "T00:00:00";

		String datetime = startdate + day + starttime + timezone;

		XSDateTime dt = XSDateTime.parseDateTime(datetime);
		if (dt == null)
			return null;

		return new XSGDay(dt.calendar().get(Calendar.DAY_OF_MONTH), dt.timezoned() ? dt.calendar().getTimeZone() : null);
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable gDay in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the gDay is to be extracted
	 * @return New ResultSequence consisting of the supplied day
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
			|| aat instanceof XSDate
			|| aat instanceof XSGDay))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}
		
		XSGDay val = castGDay(aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		return val;
	}
	
	private boolean isCastable(AnyAtomicType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic) {
			return true;
		}
		
		if (aat instanceof XSTime) {
			return false;
		}
		
		if (aat instanceof XSDate || aat instanceof XSDateTime ||
			aat instanceof XSGDay) {
			return true;
		}
		
		return false;
	}
	
	protected boolean isGDataType(AnyAtomicType aat) {
		String type = aat.string_type();
		if (type.equals("xs:gMonthDay") ||
			type.equals("xs:gMonth") ||
			type.equals("xs:gYear") ||
			type.equals("xs:gYearMonth")) {
			return true;
		}
		return false;
	}
	
	
	private XSGDay castGDay(AnyAtomicType aat) {
		if (aat instanceof XSGDay) {
			XSGDay gday = (XSGDay) aat;
			return new XSGDay(gday._day, gday._timeZone);
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGDay(date.calendar().get(Calendar.DAY_OF_MONTH), date.timezoned() ? date.calendar().getTimeZone() : null);
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGDay(dateTime.calendar().get(Calendar.DAY_OF_MONTH), dateTime.timezoned() ? dateTime.calendar().getTimeZone() : null);
		}
		return parse_gDay(aat.getStringValue()); 
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
	 * Retrieves a String representation of the stored day
	 * 
	 * @return String representation of the stored day
	 */
	@Override
	public String getStringValue() {
		String ret = "---";

		ret += XSDateTime.pad_int(day(), 2);
		
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
	 * @return "xs:gDay" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_G_DAY;
	}

	/**
	 * Retrieves the Calendar representation of the day stored
	 * 
	 * @return Calendar representation of the day stored
	 */
	@Override
	public Calendar calendar() {
		GregorianCalendar calendar = new GregorianCalendar(_timeZone != null ? _timeZone : TimeZone.getTimeZone("GMT"));
		calendar.clear();
		calendar.setGregorianChange(new Date(Long.MIN_VALUE));
		calendar.set(Calendar.DAY_OF_MONTH, _day);
		return calendar;
	}

	/**
	 * Equality comparison between this and the supplied representation. This
	 * representation must be of type XSGDay
	 * 
	 * @param arg
	 *            The XSGDay to compare with
	 * @return True if the two representations are of the same day. False
	 *         otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSGDay val = NumericType.get_single_type(arg, XSGDay.class);
		Duration implicitTimezoneOffset = evaluationContext.getDynamicContext().getTimezoneOffset();
		Calendar thiscal = getTimezonedCalendar(implicitTimezoneOffset);
		Calendar thatcal = val.getTimezonedCalendar(implicitTimezoneOffset);

		return thiscal.compareTo(thatcal) == 0;
	}
	
	/**
	 * Retrieves the timezone associated with the date stored
	 * 
	 * @return the timezone associated with the date stored
	 * @since 1.1
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
		return BuiltinTypeLibrary.XS_GDAY;
	}

}
