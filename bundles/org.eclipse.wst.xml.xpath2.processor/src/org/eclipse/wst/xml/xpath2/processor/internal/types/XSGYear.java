/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 282223 - fix casting issues. 
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
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the gMonth datatype
 */
public class XSGYear extends CalendarType implements CmpEq {

	private static final String XS_G_YEAR = "xs:gYear";
	private final int _year;
	private final TimeZone _timeZone;

	/**
	 * Initialises a representation of the supplied month
	 * 
	 * @param cal
	 *            Calendar representation of the month to be stored
	 * @param tz
	 *            Timezone associated with this month
	 */
//	@Deprecated
//	public XSGYear(Calendar cal, XSDuration tz) {
//		_calendar = cal;
//		if (tz != null) {
//			_timezoned = true;
//			_tz = tz;
//		}
//		
//	}

	public XSGYear(int year, TimeZone timeZone) {
		_year = year;
		_timeZone = timeZone;
	}

	/**
	 * Initialises a representation of the current year
	 */
	public XSGYear() {
		this(getYear(new GregorianCalendar(TimeZone.getTimeZone("GMT"))), TimeZone.getTimeZone("GMT"));
	}

	static int getYear(Calendar calendar) {
		// make sure we are working with Gregorian rules
		GregorianCalendar gregorianCalendar;
		if (calendar instanceof GregorianCalendar) {
			gregorianCalendar = (GregorianCalendar)calendar;
			if (gregorianCalendar.getTime().before(gregorianCalendar.getGregorianChange())) {
				gregorianCalendar = (GregorianCalendar)gregorianCalendar.clone();
				gregorianCalendar.setGregorianChange(new Date(Long.MIN_VALUE));
			}
		} else {
			gregorianCalendar = new GregorianCalendar(calendar.getTimeZone());
			gregorianCalendar.setGregorianChange(new Date(Long.MIN_VALUE));
			gregorianCalendar.setTimeInMillis(calendar.getTimeInMillis());
		}

		if (gregorianCalendar.get(Calendar.ERA) == GregorianCalendar.AD) {
			return gregorianCalendar.get(Calendar.YEAR);
		} else {
			// 1 BC ==> year 0
			// 2 BC ==> year -1
			return 1 - gregorianCalendar.get(Calendar.YEAR);
		}
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "gYear" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "gYear";
	}

	/**
	 * Parses a String representation of a year and constructs a new XSGYear
	 * representation of it.
	 * 
	 * @param str
	 *            The String representation of the year (and optional timezone)
	 * @return The XSGYear representation of the supplied date
	 */
	public static XSGYear parse_gYear(String str) {
		/* The lexical representation for gYear is the reduced (right truncated)
		 * lexical representation for dateTime: CCYY. No left truncation is
		 * allowed. An optional following time zone qualifier is allowed as for
		 * dateTime. To accommodate year values outside the range from 0001 to
		 * 9999, additional digits can be added to the left of this
		 * representation and a preceding "-" sign is allowed.
		 */
		str = str.trim();

		Pattern pattern = Pattern.compile("^-?[0-9]{4,}");
		Matcher matcher = pattern.matcher(str);
		if (!matcher.find()) {
			return null;
		}

		String year = matcher.group();
		String timezone = str.substring(year.length());

		String monthDaytime = "-01-01T00:00:00.0";
		String datetime = year + monthDaytime + timezone;

		XSDateTime dt = XSDateTime.parseDateTime(datetime);
		if (dt == null)
			return null;

		return new XSGYear(getYear(dt.calendar()), dt.timezoned() ? dt.calendar().getTimeZone() : null);
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable gYear in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the gYear is to be extracted
	 * @return New ResultSequence consisting of the supplied year
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
			|| aat instanceof XSGYear))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}
		
		XSGYear val = castGYear(aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		return val;
	}
	
	protected boolean isGDataType(AnyAtomicType aat) {
		String type = aat.string_type();
		if (type.equals("xs:gMonthDay") ||
			type.equals("xs:gDay") ||
			type.equals("xs:gMonth") ||
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
			aat instanceof XSGYear) {
			return true;
		}
		
		return false;
	}
	
	private XSGYear castGYear(AnyAtomicType aat) {
		if (aat instanceof XSGYear) {
			XSGYear gy = (XSGYear) aat;
			return new XSGYear(gy._year, gy._timeZone);
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGYear(getYear(date.calendar()), date.timezoned() ? date.calendar().getTimeZone() : null);
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGYear(getYear(dateTime.calendar()), dateTime.timezoned() ? dateTime.calendar().getTimeZone() : null);
		}
		
		return parse_gYear(aat.getStringValue());
	}

	/**
	 * Retrieves the actual year as an integer
	 * 
	 * @return The actual year as an integer
	 */
	public int year() {
		return _year;
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
	 * Retrieves a String representation of the stored year
	 * 
	 * @return String representation of the stored year
	 */
	@Override
	public String getStringValue() {
		String ret = "";

		ret += XSDateTime.pad_int(year(), 4);

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
	 * @return "xs:gYear" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_G_YEAR;
	}

	/**
	 * Retrieves the Calendar representation of the year stored
	 * 
	 * @return Calendar representation of the year stored
	 */
	@Override
	public Calendar calendar() {
		GregorianCalendar calendar = new GregorianCalendar(_timeZone != null ? _timeZone : TimeZone.getTimeZone("GMT"));
		calendar.clear();
		calendar.setGregorianChange(new Date(Long.MIN_VALUE));

		if (_year <= 0) {
			calendar.set(Calendar.ERA, GregorianCalendar.BC);
			calendar.set(Calendar.YEAR, 1 - _year);
		} else {
			calendar.set(Calendar.YEAR, _year);
		}

		return calendar;
	}

	/**
	 * Equality comparison between this and the supplied representation. This
	 * representation must be of type XSGYear
	 * 
	 * @param arg
	 *            The XSGYear to compare with
	 * @return True if the two representations are of the same year. False
	 *         otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSGYear val = NumericType.get_single_type(arg, XSGYear.class);
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
		return BuiltinTypeLibrary.XS_GYEAR;
	}
}
