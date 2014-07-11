/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver - bug 280547 - fix dates for comparison 
 *     Jesper Steen Moller  - bug 262765 - fix type tests
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// common base for anything that uses a calendar... basically stuff doing with
// time... hopefully in the future this may be factored out here
/**
 * Common base for all Calendar based classes
 */
public abstract class CalendarType extends CtrType {

	/**
	 * Gets the {@link Calendar} representing a date or time.
	 */
	public abstract Calendar calendar();

	/**
	 * Determines if the time zone included with the {@link Calendar} should be
	 * treated as unspecified.
	 *
	 * @return {@code true} if the time zone included in the {@link Calendar}
	 * should be used as the time zone for this object; otherwise, {@code false}
	 * if the time zone should be treated as unspecified.
	 */
	public abstract boolean timezoned();

	/**
	 * Gets a {@link Calendar} representing the {@link CalendarType} in the UTC
	 * time zone.
	 */
	public static Calendar normalizeCalendar(Calendar cal) {
		Calendar normalized = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		normalized.setTimeInMillis(cal.getTimeInMillis());
		return normalized;
	}

	protected boolean isGDataType(AnyType aat) {
		if (! (aat instanceof AnyAtomicType)) return false;
	
		String type = aat.string_type();
		if (type.equals("xs:gMonthDay") ||
			type.equals("xs:gDay") ||
			type.equals("xs:gMonth") ||
			type.equals("xs:gYear") ||
			type.equals("xs:gYearMonth")) {
			return true;
		}
		return false;
	}
	
	@Override
	public Object getNativeValue() {
		return _datatypeFactory.newXMLGregorianCalendar((GregorianCalendar)calendar());
	}
}
