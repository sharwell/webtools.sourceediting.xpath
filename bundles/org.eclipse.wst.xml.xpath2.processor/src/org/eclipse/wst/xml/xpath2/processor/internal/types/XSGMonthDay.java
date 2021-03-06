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
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
	private Calendar _calendar;
	private boolean _timezoned;
	private XSDuration _tz;
	

	/**
	 * Initialises a representation of the supplied month and day
	 * 
	 * @param cal
	 *            Calendar representation of the month and day to be stored
	 * @param tz
	 *            Timezone associated with this month and day
	 */
	public XSGMonthDay(Calendar cal, XSDuration tz) {
		_calendar = cal;
		if (tz != null) {
			_timezoned = true;
			_tz = tz;
		}
	}

	/**
	 * Initialises a representation of the current month and day
	 */
	public XSGMonthDay() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")), null);
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

		String startdate = "1972-";
		String starttime = "T00:00:00";

		int index = str.lastIndexOf('+', str.length());
				
		if (index == -1)
			index = str.lastIndexOf('-');
		if (index == -1)
			index = str.lastIndexOf('Z', str.length());
		if (index != -1) {
			int zIndex = str.lastIndexOf('Z', str.length());
			if (zIndex == -1) {
				if (index > 5)
					zIndex = index;
			}
			if (zIndex == -1) {
				zIndex = str.lastIndexOf('+');
			}

			
			String[] split = str.split("-");
			startdate += split[2].replace("Z", "") + "-" + split[3].replace("Z", "").substring(0, 2);
			
			if (split.length > 4) {
				String[] timesplit = split[4].split(":");
				if (timesplit.length < 3) {
					starttime = "T";
					StringBuilder buf = new StringBuilder(starttime);
					for (String timesplit1 : timesplit) {
						buf.append(timesplit1).append(":");
					}
					buf.append("00");
					starttime = buf.toString();
				} else {
					starttime += timesplit[0] + ":" + timesplit[1] + ":" + timesplit[2];
				}
			}

			startdate = startdate.trim();
			startdate += starttime;

			
			if (zIndex != -1) {
				startdate += str.substring(zIndex);
			}
		} else {
			startdate += starttime;
		}

		XSDateTime dt = XSDateTime.parseDateTime(startdate);
		if (dt == null)
			return null;

		return new XSGMonthDay(dt.calendar(), dt.tz());
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
		if (!isCastable(aat)) {
			throw DynamicError.invalidType();
		}

		XSGMonthDay val = castGMonthDay(aat);

		if (val == null)
			throw DynamicError.cant_cast(null);

		return val;
	}

	private boolean isCastable(AnyType aat) {
		// From 17.1.5 (Casting to date and time types)
		return aat instanceof XSString
				|| aat instanceof XSUntypedAtomic
				|| aat instanceof XSDateTime
				|| aat instanceof XSDate
				|| aat instanceof XSGMonthDay;
	}

	private XSGMonthDay castGMonthDay(AnyType aat) {
		if (aat instanceof XSGMonthDay) {
			XSGMonthDay gmd = (XSGMonthDay) aat;
			return new XSGMonthDay(gmd.calendar(), gmd.tz());
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGMonthDay(date.calendar(), date.tz());
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGMonthDay(dateTime.calendar(), dateTime.tz());
		}
		
		return parse_gMonthDay(aat.getStringValue().trim());
	}
	
	/**
	 * Retrieves the actual month as an integer
	 * 
	 * @return The actual month as an integer
	 */
	public int month() {
		return _calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * Retrieves the actual day as an integer
	 * 
	 * @return The actual day as an integer
	 */
	public int day() {
		return _calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Check for whether a timezone was specified at creation
	 * 
	 * @return True if a timezone was specified. False otherwise
	 */
	public boolean timezoned() {
		return _timezoned;
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
		return _calendar;
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

		return calendar().equals(val.calendar());
	}
	
	/**
	 * Retrieves the timezone associated with the date stored
	 * 
	 * @return the timezone associated with the date stored
	 */
	public XSDuration tz() {
		return _tz;
	}	

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_GMONTHDAY;
	}
}
