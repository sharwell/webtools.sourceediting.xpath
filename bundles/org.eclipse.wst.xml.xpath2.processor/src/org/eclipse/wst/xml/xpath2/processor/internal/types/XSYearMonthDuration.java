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
 *     Mukul Gandhi - bug 279373 - improvements to multiply operation on xs:yearMonthDuration
 *                                 data type.
 *     David Carver - bug 282223 - implementation of xs:duration data type.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathDiv;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathMinus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathPlus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathTimes;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the YearMonthDuration datatype
 */
public class XSYearMonthDuration extends XSDuration implements CmpEq, CmpLt,
		CmpGt, MathPlus, MathMinus, MathTimes, MathDiv {


	private static final String XS_YEAR_MONTH_DURATION = "xs:yearMonthDuration";

	/**
	 * Initialises using the supplied parameters. If the number of months
	 * supplied is more than 12, the number of years is adjusted accordingly.
	 * 
	 * @param year
	 *            Number of years in this duration of time
	 * @param month
	 *            Number of months in this duration of time
	 * @param negative
	 *            True if this duration of time represents a backwards passage
	 *            through time. False otherwise
	 */
	public XSYearMonthDuration(int year, int month, boolean negative) {
		super(year, month, 0, 0, 0, BigDecimal.ZERO, negative);
	}

	/**
	 * Initialises to the given number of months
	 * 
	 * @param months
	 *            Number of months in the duration of time
	 */
	public XSYearMonthDuration(int months) {
		this(0, Math.abs(months), months < 0);
	}

	/**
	 * Initialises to a duration of no time (0years and 0months)
	 */
	public XSYearMonthDuration() {
		this(0, 0, false);
	}

	/**
	 * Creates a new XSYearMonthDuration by parsing the supplied String
	 * represented duration of time
	 * 
	 * @param str
	 *            String represented duration of time
	 * @return New XSYearMonthDuration representing the duration of time
	 *         supplied
	 */
	public static XSYearMonthDuration parseYMDuration(String str) {
		if (str.indexOf('T') >= 0) {
			// xs:yearMonthDuration cannot have a time
			return null;
		}

		if (str.indexOf('D') >= 0) {
			// xs:yearMonthDuration cannot have a number of days
			return null;
		}

		XSDuration duration = parseDuration(str);
		if (duration == null) {
			return null;
		}

		return new XSYearMonthDuration(duration.year(), duration.month(), duration.negative());
	}

	/**
	 * Retrives the datatype's name
	 * 
	 * @return "yearMonthDuration" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "yearMonthDuration";
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


		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		XSDuration ymd = castYearMonthDuration(aat);

		if (ymd == null)
			throw DynamicError.cant_cast(null);

		return ymd;
	}
	
	private XSYearMonthDuration castYearMonthDuration(AnyType aat) {
		if (aat instanceof XSDuration) {
			XSDuration duration = (XSDuration) aat;
			return new XSYearMonthDuration(duration.year(), duration.month(), duration.negative());
		}
		
		return parseYMDuration(aat.getStringValue());
	}

	/**
	 * Retrieves a String representation of the duration of time stored
	 * 
	 * @return String representation of the duration of time stored
	 */
	@Override
	public String getStringValue() {
		String strval = "";

		if (negative())
			strval += "-";

		strval += "P";

		int years = year();
		if (years != 0)
			strval += years + "Y";

		int months = month();
		if (months == 0) {
			if (years == 0)
				strval += months + "M";
		} else
			strval += months + "M";

		return strval;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:yearMonthDuration" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_YEAR_MONTH_DURATION;
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
		XSYearMonthDuration val = NumericType.get_single_type(arg, XSYearMonthDuration.class);

		return monthValue() < val.monthValue();
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
		XSYearMonthDuration val = NumericType.get_single_type(arg, XSYearMonthDuration.class);

		return monthValue() > val.monthValue();
	}

	/**
	 * Mathematical addition between this duration stored and the supplied
	 * duration of time (of type XSYearMonthDuration)
	 * 
	 * @param arg
	 *            The duration of time to add
	 * @return New XSYearMonthDuration representing the resulting duration
	 *         after the addition
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence plus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() == 1) {
			Item first = arg.first();
			if (first instanceof XSDate || first instanceof XSDateTime) {
				// This is a special case for xs:date and xs:dateTime
				// https://www.w3.org/TR/xpath20/#mapping
				return ((MathPlus)first).plus(this, evaluationContext);
			}
		}

		XSYearMonthDuration val = NumericType.get_single_type(arg, XSYearMonthDuration.class);

		int res = monthValue() + val.monthValue();

		return new XSYearMonthDuration(res);
	}

	/**
	 * Mathematical subtraction between this duration stored and the supplied
	 * duration of time (of type XSYearMonthDuration)
	 * 
	 * @param arg
	 *            The duration of time to subtract
	 * @return New XSYearMonthDuration representing the resulting duration
	 *         after the subtraction
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		XSYearMonthDuration val = NumericType.get_single_type(arg, XSYearMonthDuration.class);

		int res = monthValue() - val.monthValue();

		return new XSYearMonthDuration(res);
	}

	/**
	 * Mathematical multiplication between this duration stored and the supplied
	 * duration of time (of type XSYearMonthDuration)
	 * 
	 * @param arg
	 *            The duration of time to multiply by
	 * @return New XSYearMonthDuration representing the resulting duration
	 *         after the multiplication
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence times(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence convertedRS = arg;		
		if (arg.size() == 1) {
			Item argValue = arg.first();
            if (argValue instanceof XSDecimal) {
            	convertedRS = new XSDouble(argValue.getStringValue());
            }
		}
		
		XSDouble val = NumericType.get_single_type(convertedRS, XSDouble.class);

		if (val.nan()) {
			throw DynamicError.nan();
		} else if (val.infinite()) {
			throw DynamicError.overflowUnderflow();
		}

		int res = (int) Math.round(monthValue() * val.double_value());

		return new XSYearMonthDuration(res);
	}

	/**
	 * Mathematical division between this duration stored and the supplied
	 * duration of time (of type XSYearMonthDuration)
	 * 
	 * @param arg
	 *            The duration of time to divide by
	 * @return New XSYearMonthDuration representing the resulting duration
	 *         after the division
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence div(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();

		if (at instanceof XSDouble) {
			XSDouble dt = (XSDouble) at;
			if (dt.zero() || dt.negativeZero()) {
				throw DynamicError.overflowUnderflow();
			} else if (dt.nan()) {
				throw DynamicError.nan();
			}

			int ret = (int) Math.round(monthValue() / dt.double_value());
			return new XSYearMonthDuration(ret);
		} else if (at instanceof XSDecimal) {
			XSDecimal dt = (XSDecimal) at;
			if (dt.zero()) {
				throw DynamicError.overflowUnderflow();
			}

			int ret = (int) Math.round(monthValue() / dt.getValue().doubleValue());
			return new XSYearMonthDuration(ret);
		} else if (at instanceof XSYearMonthDuration) {
			XSYearMonthDuration md = (XSYearMonthDuration) at;
			if (md.monthValue() == 0) {
				throw DynamicError.overflowUnderflow();
			}

			try {
				BigDecimal res = BigDecimal.valueOf(monthValue()).divide(BigDecimal.valueOf(md.monthValue()));
				return new XSDecimal(res);
			} catch (ArithmeticException ex) {
				double res = (double)monthValue() / md.monthValue();
				return new XSDecimal(new BigDecimal(res));
			}
		} else {
			throw DynamicError.throw_type_error();
		}
	}	

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_YEARMONTHDURATION;
	}

}
