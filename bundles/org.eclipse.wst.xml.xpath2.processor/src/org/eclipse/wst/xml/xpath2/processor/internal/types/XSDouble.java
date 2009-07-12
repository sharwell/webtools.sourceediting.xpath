/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 274805 - improvements to xs:integer data type
 *     David Carver - bug 277770 - format of XSDouble for zero values incorrect.
 *     Mukul Gandhi - bug 279406 - improvements to negative zero values for xs:double
 *     David Carver (STAR) - bug 262765 - various numeric formatting fixes and calculations
 *     David Carver (STAR) - bug 262765 - fixed rounding errors.      
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;

/**
 * A representation of the Double datatype
 */
public class XSDouble extends NumericType {

	private Double _value;
	private XPathDecimalFormat format = new XPathDecimalFormat(
			"0.################E0");

	/**
	 * Initialises a representation of the supplied number
	 * 
	 * @param x
	 *            Number to be stored
	 */
	public XSDouble(double x) {
		_value = new Double(x);
	}

	/**
	 * Initializes a representation of 0
	 */
	public XSDouble() {
		this(0);
	}

	/**
	 * Initialises using a String represented number
	 * 
	 * @param init
	 *            String representation of the number to be stored
	 */
	public XSDouble(String init) throws DynamicError {
		try {
			if (init.equals("-INF")) {
				_value = Double.NEGATIVE_INFINITY;
			} else if (init.equals("INF")) {
				_value = Double.POSITIVE_INFINITY;
			} else {
				_value = new Double(init);
			}
		} catch (NumberFormatException e) {
			throw DynamicError.cant_cast(null);
		}
	}

	/**
	 * Creates a new representation of the String represented number
	 * 
	 * @param i
	 *            String representation of the number to be stored
	 * @return New XSDouble representing the number supplied
	 */
	public static XSDouble parse_double(String i) {
		try {
			Double d = null;
			if (i.equals("INF")) {
				d = Double.POSITIVE_INFINITY;
			} else if (i.equals("-INF")) {
				d = Double.NEGATIVE_INFINITY;
			} else {
				d = new Double(i);
			}
			return new XSDouble(d.doubleValue());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Creates a new result sequence consisting of the retrievable double number
	 * in the supplied result sequence
	 * 
	 * @param arg
	 *            The result sequence from which to extract the double number.
	 * @throws DynamicError
	 * @return A new result sequence consisting of the double number supplied.
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		ResultSequence rs = ResultSequenceFactory.create_new();

		if (arg.empty())
			return rs;

		AnyType aat = arg.first();

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		XSDouble d = castDouble(aat);
		
		if (d == null)
			throw DynamicError.cant_cast(null);

		rs.add(d);
		return rs;
	}
	
	private boolean isCastable(AnyType aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic ||
			aat instanceof NodeType) {
			return true;
		}
		if (aat instanceof XSBoolean || aat instanceof NumericType) {
			return true;
		}
		return false;
	}
	
	private XSDouble castDouble(AnyType aat) {
		if (aat instanceof XSBoolean) {
			if (aat.string_value().equals("true")) {
				return new XSDouble(new Double("1.0E0"));
			} else {
				return new XSDouble(new Double("0.0E0"));
			}
		}
		return parse_double(aat.string_value());
		
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:double" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "xs:double";
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "double" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "double";
	}

	/**
	 * Retrieves a String representation of the Decimal value stored
	 * 
	 * @return String representation of the Decimal value stored
	 */
	@Override
	public String string_value() {
		if (zero()) {
			return "0";
		}

		if (negativeZero()) {
			return "-0";
		}

		if (nan()) {
			return "NaN";
		}

		return format.xpathFormat(_value);
	}

	/**
	 * Check for whether this XSDouble represents NaN
	 * 
	 * @return True if this XSDouble represents NaN. False otherwise.
	 */
	public boolean nan() {
		return Double.isNaN(_value);
	}

	/**
	 * Check for whether this XSDouble represents 0
	 * 
	 * @return True if this XSDouble represents 0. False otherwise.
	 */
	@Override
	public boolean zero() {
		return (Double.compare(_value, 0.0E0) == 0);
	}

	/*
	 * Check for whether this XSDouble represents -0
	 * 
	 * @return True if this XSDouble represents -0. False otherwise.
	 * 
	 * @since 1.1
	 */
	public boolean negativeZero() {
		return (Double.compare(_value, -0.0E0) == 0);
	}

	/**
	 * Retrieves the actual value of the number stored
	 * 
	 * @return The actual value of the number stored
	 */
	public double double_value() {
		return _value.doubleValue();
	}

	/**
	 * Equality comparison between this number and the supplied representation.
	 * 
	 * @param aa
	 *            Representation to be compared with (must currently be of type
	 *            XSDouble)
	 * @return True if the 2 representations represent the same number. False
	 *         otherwise
	 * @since 1.1
	 */
	public boolean eq(AnyType aa) throws DynamicError {
		ResultSequence rs = ResultSequenceFactory.create_new(aa);
		ResultSequence crs = constructor(rs);
		
		if (crs.empty()) {
			throw DynamicError.throw_type_error();
		}
		AnyType cat = crs.first();

		XSDouble d = (XSDouble) cat;

		return double_value() == d.double_value();
	}

	/**
	 * Comparison between this number and the supplied representation. 
	 * 
	 * @param arg
	 *            Representation to be compared with (must currently be of type
	 *            XSDouble)
	 * @return True if the supplied type represents a number smaller than this
	 *         one stored. False otherwise
	 */
	public boolean gt(AnyType arg) throws DynamicError {
		AnyType carg = convertArg(arg);
		
		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return double_value() > val.double_value();
	}

	protected AnyType convertArg(AnyType arg) throws DynamicError {
		ResultSequence rs = ResultSequenceFactory.create_new(arg);
		rs = constructor(rs);
		AnyType carg = rs.first();
		return carg;
	}

	/**
	 * Comparison between this number and the supplied representation. Currently
	 * no numeric type promotion exists so the supplied representation must be
	 * of type XSDouble.
	 * 
	 * @param arg
	 *            Representation to be compared with (must currently be of type
	 *            XSDouble)
	 * @return True if the supplied type represents a number greater than this
	 *         one stored. False otherwise
	 */
	public boolean lt(AnyType arg) throws DynamicError {
		AnyType carg = convertArg(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return double_value() < val.double_value();
	}

	// math
	/**
	 * Mathematical addition operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an addition with
	 * @return A XSDouble consisting of the result of the mathematical addition.
	 */
	public ResultSequence plus(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		AnyType at = get_single_arg(carg);
		
		if (!(at instanceof XSDouble))
			DynamicError.throw_type_error();
		XSDouble val = (XSDouble) at;

		return ResultSequenceFactory.create_new(new XSDouble(double_value()
				+ val.double_value()));
	}

	private ResultSequence convertResultSequence(ResultSequence arg)
			throws DynamicError {
		ResultSequence carg = arg;
		Iterator it = carg.iterator();
		while (it.hasNext()) {
			AnyType type = (AnyType) it.next();
			if (type.string_type().equals("xs:untypedAtomic") ||
				type.string_type().equals("xs:string")) {
				throw DynamicError.throw_type_error();
			}
		}

		carg = constructor(carg);
		return carg;
	}

	/**
	 * Mathematical subtraction operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an subtraction with
	 * @return A XSDouble consisting of the result of the mathematical
	 *         subtraction.
	 */
	public ResultSequence minus(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		
		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);

		return ResultSequenceFactory.create_new(new XSDouble(double_value()
				- val.double_value()));
	}

	/**
	 * Mathematical multiplication operator between this XSDouble and the
	 * supplied ResultSequence. Due to no numeric type promotion or conversion,
	 * the ResultSequence must be of type XSDouble.
	 * 
	 * @param arg
	 *            The ResultSequence to perform an multiplication with
	 * @return A XSDouble consisting of the result of the mathematical
	 *         multiplication.
	 */
	public ResultSequence times(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return ResultSequenceFactory.create_new(new XSDouble(double_value()
				* val.double_value()));
	}

	/**
	 * Mathematical division operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an division with
	 * @return A XSDouble consisting of the result of the mathematical division.
	 */
	public ResultSequence div(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return ResultSequenceFactory.create_new(new XSDouble(double_value()
				/ val.double_value()));
	}

	/**
	 * Mathematical integer division operator between this XSDouble and the
	 * supplied ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an integer division with
	 * @return A XSInteger consisting of the result of the mathematical integer
	 *         division.
	 */
	public ResultSequence idiv(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);

		if (val.zero())
			throw DynamicError.div_zero(null);

		return ResultSequenceFactory.create_new(new XSInteger(BigInteger
				.valueOf((int) (double_value() / val.double_value()))));
	}

	/**
	 * Mathematical modulus operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform a modulus with
	 * @return A XSDouble consisting of the result of the mathematical modulus.
	 */
	public ResultSequence mod(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return ResultSequenceFactory.create_new(new XSDouble(double_value()
				% val.double_value()));
	}

	/**
	 * Negation of the number stored
	 * 
	 * @return A XSDouble representing the negation of this XSDecimal
	 */
	@Override
	public ResultSequence unary_minus() {
		return ResultSequenceFactory.create_new(new XSDouble(-1
				* double_value()));
	}

	// functions
	/**
	 * Absolutes the number stored
	 * 
	 * @return A XSDouble representing the absolute value of the number stored
	 */
	@Override
	public NumericType abs() {
		return new XSDouble(Math.abs(double_value()));
	}

	/**
	 * Returns the smallest integer greater than the number stored
	 * 
	 * @return A XSDouble representing the smallest integer greater than the
	 *         number stored
	 */
	@Override
	public NumericType ceiling() {
		return new XSDouble(Math.ceil(double_value()));
	}

	/**
	 * Returns the largest integer smaller than the number stored
	 * 
	 * @return A XSDouble representing the largest integer smaller than the
	 *         number stored
	 */
	@Override
	public NumericType floor() {
		return new XSDouble(Math.floor(double_value()));
	}

	/**
	 * Returns the closest integer of the number stored.
	 * 
	 * @return A XSDouble representing the closest long of the number stored.
	 */
	@Override
	public NumericType round() {
		BigDecimal value = new BigDecimal(_value);
		BigDecimal round = value.setScale(0, RoundingMode.HALF_UP);
		return new XSDouble(round.doubleValue());
	}

	/**
	 * Returns the closest integer of the number stored.
	 * 
	 * @return A XSDouble representing the closest long of the number stored.
	 */
	@Override
	public NumericType round_half_to_even() {

		return round_half_to_even(0);
	}

	/**
	 * Returns the closest integer of the number stored with the specified
	 * precision.
	 * 
	 * @param precision
	 *            An integer precision
	 * @return A XSDouble representing the closest long of the number stored.
	 */
	@Override
	public NumericType round_half_to_even(int precision) {
		BigDecimal value = new BigDecimal(_value);
		BigDecimal round = value.setScale(precision, RoundingMode.HALF_EVEN);
		return new XSDouble(round.doubleValue());
	}
}
