/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 262765 - Fixed parsing of gMonth values
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
 * A representation of the gMonth datatype
 */
public class XSGMonth extends CalendarType implements CmpEq {

	private static final String XS_G_MONTH = "xs:gMonth";
	private final int _month;
	private final TimeZone _timeZone;

	/**
	 * Initializes a representation of the supplied month
	 * 
	 * @param cal
	 *            Calendar representation of the month to be stored
	 * @param tz
	 *            Time zone associated with this month
	 */
//	@Deprecated
//	public XSGMonth(Calendar cal, XSDuration tz) {
//		_calendar = cal;
//		if (tz != null) {
//			_timezoned = true;
//			_tz = tz;
//		}
//	}

	public XSGMonth(int month, TimeZone timeZone) {
		assert month >= 1 && month <= 12;
		_month = month;
		_timeZone = timeZone;
	}

	/**
	 * Initializes a representation of the current month
	 */
	public XSGMonth() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")).get(Calendar.MONTH) + 1, TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "gMonth" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "gMonth";
	}

	/**
	 * Parses a String representation of a month and constructs a new XSGMonth
	 * representation of it.
	 * 
	 * @param str
	 *            The String representation of the month (and optional timezone)
	 * @return The XSGMonth representation of the supplied date
	 */
	public static XSGMonth parse_gMonth(String str) {
		/* The lexical representation for gMonth is the left and right truncated
		 * lexical representation for date: --MM. An optional following time
		 * zone qualifier is allowed as for date. No preceding sign is allowed.
		 * No other formats are allowed. See also ISO 8601 Date and Time
		 * Formats.
		 */

		str = str.trim();
		if (str.length() < 4 || !str.startsWith("--")) {
			return null;
		}

		String month = str.substring(2, 4);
		String timezone = str.substring(4);
		String startdate = "1972-";
		String starttime = "T00:00:00";

		String datetime = startdate + month + "-01" + starttime + timezone;

		XSDateTime dt = XSDateTime.parseDateTime(datetime);
		if (dt == null)
			return null;

		return new XSGMonth(dt.calendar().get(Calendar.MONTH) + 1, dt.timezoned() ? dt.calendar().getTimeZone() : null);
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable gMonth in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the gMonth is to be extracted
	 * @return New ResultSequence consisting of the supplied month
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
			|| aat instanceof XSGMonth))
		{
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		XSGMonth val = castGMonth(aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		return val;
	}

	protected boolean isGDataType(AnyAtomicType aat) {
		String type = aat.string_type();
		if (type.equals("xs:gMonthDay") ||
			type.equals("xs:gDay") ||
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
			aat instanceof XSGMonth) {
			return true;
		}
		
		return false;
	}
	
	private XSGMonth castGMonth(AnyAtomicType aat) {
		if (aat instanceof XSGMonth) {
			XSGMonth gm = (XSGMonth) aat;
			return new XSGMonth(gm._month, gm._timeZone);
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGMonth(date.calendar().get(Calendar.MONTH) + 1, date.timezoned() ? date.calendar().getTimeZone() : null);
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGMonth(dateTime.calendar().get(Calendar.MONTH) + 1, dateTime.timezoned() ? dateTime.calendar().getTimeZone() : null);
		}
		
		return parse_gMonth(aat.getStringValue());
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
	 * Check for whether a timezone was specified at creation
	 * 
	 * @return True if a timezone was specified. False otherwise
	 */
	@Override
	public boolean timezoned() {
		return _timeZone != null;
	}

	/**
	 * Retrieves a String representation of the stored month
	 * 
	 * @return String representation of the stored month
	 */
	@Override
	public String getStringValue() {
		String ret = "--";

		ret += XSDateTime.pad_int(month(), 2);

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
	 * @return "xs:gMonth" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_G_MONTH;
	}

	/**
	 * Retrieves the Calendar representation of the month stored
	 * 
	 * @return Calendar representation of the month stored
	 */
	@Override
	public Calendar calendar() {
		GregorianCalendar calendar = new GregorianCalendar(_timeZone != null ? _timeZone : TimeZone.getTimeZone("GMT"));
		calendar.clear();
		calendar.setGregorianChange(new Date(Long.MIN_VALUE));
		calendar.set(Calendar.MONTH, _month - 1);
		return calendar;
	}

	/**
	 * Equality comparison between this and the supplied representation. This
	 * representation must be of type XSGMonth
	 * 
	 * @param arg
	 *            The XSGMonth to compare with
	 * @return True if the two representations are of the same month. False
	 *         otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		XSGMonth val = NumericType.get_single_type(arg, XSGMonth.class);
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
		return BuiltinTypeLibrary.XS_GMONTH;
	}
}
