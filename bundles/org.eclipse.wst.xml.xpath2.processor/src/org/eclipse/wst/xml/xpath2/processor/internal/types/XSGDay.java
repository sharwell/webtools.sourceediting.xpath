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
 * A representation of the Day datatype
 */
public class XSGDay extends CalendarType implements CmpEq {

	private static final String XS_G_DAY = "xs:gDay";
	private Calendar _calendar;
	private boolean _timezoned;
	private XSDuration _tz;

	/**
	 * Initializes a representation of the supplied day
	 * 
	 * @param cal
	 *            Calendar representation of the day to be stored
	 * @param tz
	 *            Timezone associated with this day
	 */
	public XSGDay(Calendar cal, XSDuration tz) {
		_calendar = cal;
		if (tz != null) {
			_timezoned = true;
			_tz = tz;
		}
	}

	/**
	 * Initialises a representation of the current day
	 */
	public XSGDay() {
		this(new GregorianCalendar(TimeZone.getTimeZone("GMT")), null);
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
		
		String startdate = "1972-12-";
		String starttime = "T00:00:00";

		int index = str.lastIndexOf('+', str.length());
		
		if (index == -1)
			index = str.lastIndexOf('-');
		if (index == -1)
			index = str.lastIndexOf('Z', str.length());
		if (index != -1) {
			int zIndex = str.lastIndexOf('Z', str.length());
			if (zIndex == -1) {
				if (index > 4)
					zIndex = index;
			}
			if (zIndex == -1) {
				zIndex = str.lastIndexOf('+');
			}
			
			String[] split = str.split("-");
			startdate += split[3].replace("Z", "");
			
			if (str.indexOf('T') != -1) {
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
			}
			startdate = startdate.trim();
			startdate += starttime;

			if (zIndex != -1) {
				
				startdate += str.substring(zIndex);
			}
		} else {
			startdate += str + starttime;
		}

		XSDateTime dt = XSDateTime.parseDateTime(startdate);
		if (dt == null)
			return null;

		return new XSGDay(dt.calendar(), dt.tz());
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

		AnyType aat = FnData.atomize(arg.first());
		if (!isCastable(aat)) {
			throw DynamicError.invalidType();
		}
		
		XSGDay val = castGDay(aat);

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
				|| aat instanceof XSGDay;
	}

	private XSGDay castGDay(AnyType aat) {
		if (aat instanceof XSGDay) {
			XSGDay gday = (XSGDay) aat;
			return new XSGDay(gday.calendar(), gday.tz());
		}
		
		if (aat instanceof XSDate) {
			XSDate date = (XSDate) aat;
			return new XSGDay(date.calendar(), date.tz());
		}
		
		if (aat instanceof XSDateTime) {
			XSDateTime dateTime = (XSDateTime) aat;
			return new XSGDay(dateTime.calendar(), dateTime.tz());
		}
		return parse_gDay(aat.getStringValue().trim());
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
	 * Retrieves a String representation of the stored day
	 * 
	 * @return String representation of the stored day
	 */
	@Override
	public String getStringValue() {
		String ret = "---";

		Calendar adjustFortimezone = calendar();
		
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
		return _calendar;
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
		Calendar thiscal = normalizeCalendar(calendar(), tz());
		Calendar thatcal = normalizeCalendar(val.calendar(), val.tz());
		
		return thiscal.equals(thatcal);
	}
	
	/**
	 * Retrieves the timezone associated with the date stored
	 * 
	 * @return the timezone associated with the date stored
	 * @since 1.1
	 */
	public XSDuration tz() {
		return _tz;
	}	

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_GDAY;
	}

}
