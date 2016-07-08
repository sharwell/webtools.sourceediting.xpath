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
 *     Mukul Gandhi - bug 279377 - improvements to multiplication and division operations
 *                                 on xs:dayTimeDuration.
 *     David Carver - bug 282223 - implementation of xs:duration
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathDiv;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathMinus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathPlus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathTimes;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the DayTimeDuration datatype
 */
public class XSDayTimeDuration extends XSDuration implements CmpEq, CmpLt,
		CmpGt,

		MathPlus, MathMinus, MathTimes, MathDiv,

		Cloneable {


	private static final String XS_DAY_TIME_DURATION = "xs:dayTimeDuration";

	/**
	 * Initialises to the supplied parameters. If more than 24 hours is
	 * supplied, the number of days is adjusted acordingly. The same occurs for
	 * minutes and seconds
	 * 
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
	public XSDayTimeDuration(int days, int hours, int minutes, BigDecimal seconds,
			boolean negative) {
		super(0, 0, days, hours, minutes, seconds, negative);
	}

	/**
	 * Initialises to the given number of seconds
	 * 
	 * @param secs
	 *            Number of seconds in the duration of time
	 */
	public XSDayTimeDuration(BigDecimal secs) {
		super(0, 0, 0, 0, 0, secs.abs(), secs.compareTo(BigDecimal.ZERO) < 0);
	}

	/**
	 * Initialises to a duration of no time (0days, 0hours, 0minutes, 0seconds)
	 */
	public XSDayTimeDuration() {
		super(0, 0, 0, 0, 0, BigDecimal.ZERO, false);
	}

	public XSDayTimeDuration(Duration d) {
		this(d.getDays(), d.getHours(), d.getMinutes(), BigDecimal.ZERO, d.getSign() == -1);
	}

	/**
	 * Creates a copy of this representation of a time duration
	 * 
	 * @return New XSDayTimeDuration representing the duration of time stored
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new XSDayTimeDuration(days(), hours(), minutes(), seconds(),
				negative());
	}

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
	
		XSDuration dtd = castDayTimeDuration(aat);
	
		if (dtd == null)
			throw DynamicError.cant_cast(null);
	
		return dtd;
	}
	
	private XSDuration castDayTimeDuration(AnyType aat) {
		if (aat instanceof XSDuration) {
			XSDuration duration = (XSDuration) aat;
			return new XSDayTimeDuration(duration.days(), duration.hours(), duration.minutes(), duration.seconds(), duration.negative());
		}
		
		return parseDTDuration(aat.getStringValue());
	}

	/**
	 * Creates a new XSDayTimeDuration by parsing the supplied String
	 * represented duration of time
	 * 
	 * @param str
	 *            String represented duration of time
	 * @return New XSDayTimeDuration representing the duration of time supplied
	 */
	public static XSDayTimeDuration parseDTDuration(String str) {
		// xs:dayTimeDuration is not allowed to have a 'Y' or 'M' in front of the 'T'
		if (str.indexOf('Y') >= 0) {
			// xs:dayTimeDuration cannot have a year
			return null;
		}

		int firstM = str.indexOf('M');
		if (firstM >= 0) {
			int timeIndex = str.indexOf('T');
			if (timeIndex < 0 || firstM < timeIndex) {
				// xs:dayTimeDuration cannot have a month
				return null;
			}
		}

		XSDuration duration = XSDuration.parseDuration(str);
		if (duration == null) {
			return null;
		}

		return new XSDayTimeDuration(duration.days(), duration.hours(), duration.minutes(), duration.seconds(), duration.negative());
	}

	/**
	 * Retrives the datatype's name
	 * 
	 * @return "dayTimeDuration" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "dayTimeDuration";
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:dayTimeDuration" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_DAY_TIME_DURATION;
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
	 * Mathematical addition between this duration stored and the supplied
	 * duration of time (of type XSDayTimeDuration)
	 * 
	 * @param arg
	 *            The duration of time to add
	 * @return New XSDayTimeDuration representing the resulting duration after
	 *         the addition
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence plus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() == 1) {
			Item first = arg.first();
			if (first instanceof XSDate
					|| first instanceof XSTime
					|| first instanceof XSDateTime) {
				// This is a special case for xs:date, xs:time, and xs:dateTime
				// https://www.w3.org/TR/xpath20/#mapping
				return ((MathPlus)first).plus(this, evaluationContext);
			}
		}

		XSDuration val = NumericType.get_single_type(arg, XSDayTimeDuration.class);
		
		BigDecimal res = value().add(val.value());

		return new XSDayTimeDuration(res);
	}

	/**
	 * Mathematical subtraction between this duration stored and the supplied
	 * duration of time (of type XSDayTimeDuration)
	 * 
	 * @param arg
	 *            The duration of time to subtract
	 * @return New XSDayTimeDuration representing the resulting duration after
	 *         the subtraction
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		XSDuration val = NumericType.get_single_type(arg, XSDayTimeDuration.class);

		BigDecimal res = value().subtract(val.value());

		return new XSDayTimeDuration(res);
	}

	/**
	 * Mathematical multiplication between this duration stored and the supplied
	 * duration of time (of type XSDayTimeDuration)
	 * 
	 * @param arg
	 *            The duration of time to multiply by
	 * @return New XSDayTimeDuration representing the resulting duration after
	 *         the multiplication
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

		BigDecimal res = value().multiply(BigDecimal.valueOf(val.double_value()));
		res = res.setScale(3, RoundingMode.DOWN);

		return new XSDayTimeDuration(res);
	}

	/**
	 * Mathematical division between this duration stored and the supplied
	 * duration of time (of type XSDayTimeDuration)
	 * 
	 * @param arg
	 *            The duration of time to divide by
	 * @return New XSDayTimeDuration representing the resulting duration after
	 *         the division
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence div(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();

		Item at = arg.first();

		if (at instanceof XSDouble) {
			XSDouble dt = (XSDouble) at;
			BigDecimal retval;
			
			if (dt.nan()) {
				throw DynamicError.nan();
			} else if (dt.infinite()) {
				retval = BigDecimal.ZERO;
			} else if (!dt.zero() && !dt.negativeZero()) {
				retval = value().divide(new BigDecimal(dt.double_value()), 18, BigDecimal.ROUND_HALF_EVEN);
			} else {
				throw DynamicError.overflowUnderflow();
			}

			return new XSDayTimeDuration(retval);
		} else if (at instanceof XSDecimal) {
			XSDecimal dt = (XSDecimal) at;
			
			BigDecimal ret;
							
			if (!dt.zero()) {
				ret = value().divide(dt.getValue(), 18, BigDecimal.ROUND_HALF_EVEN);
			} else {
				throw DynamicError.overflowUnderflow();
			}
			
			return new XSDayTimeDuration(ret);
		} else if (at instanceof XSDayTimeDuration) {
			XSDuration md = (XSDuration) at;

			BigDecimal res = value();
			BigDecimal l = md.value();
			res = res.divide(l, 18, BigDecimal.ROUND_HALF_EVEN);

			return new XSDecimal(res);
		} else {
			throw DynamicError.throw_type_error();
		}
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_DAYTIMEDURATION;
	}

}
