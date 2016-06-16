/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
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
 *     Jesper Steen Moller - Bug 286062 - Fix idiv error cases and increase precision  
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the Double datatype
 */
public class XSDouble extends NumericType {

	private static final String XS_DOUBLE = "xs:double";
	private final Double _value;
	private final XPathDecimalFormat format = new XPathDecimalFormat(
			"0.################E0");

	/**
	 * Initialises a representation of the supplied number
	 * 
	 * @param x
	 *            Number to be stored
	 */
	public XSDouble(double x) {
		_value = x;
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
			throw DynamicError.cant_cast(null, e);
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
			return new XSDouble(d);
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

		if (arg.empty())
			return ResultBuffer.EMPTY;

		Item aat = arg.first();

		if (aat instanceof XSDuration || aat instanceof CalendarType ||
			aat instanceof XSBase64Binary || aat instanceof XSHexBinary ||
			aat instanceof XSAnyURI) {
			throw DynamicError.invalidType();
		}
		
		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		XSDouble d = castDouble(aat);
		
		if (d == null)
			throw DynamicError.cant_cast(null);

		return d;
	}
	
	private boolean isCastable(Item aat) {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic ||
			aat instanceof NodeType) {
			return true;
		}
		if (aat instanceof XSBoolean || aat instanceof NumericType) {
			return true;
		}
		return false;
	}
	
	private XSDouble castDouble(Item aat) {
		if (aat instanceof XSBoolean) {
			if (aat.getStringValue().equals("true")) {
				return new XSDouble(1.0E0);
			} else {
				return new XSDouble(0.0E0);
			}
		}
		return parse_double(aat.getStringValue());
		
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:double" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_DOUBLE;
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
	public String getStringValue() {
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
	 * Check for whether this XSDouble represents an infinite number (negative or positive)
	 * 
	 * @return True if this XSDouble represents infinity. False otherwise.
	 */
	public boolean infinite() {
		return Double.isInfinite(_value);
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
		return _value;
	}

	/**
	 * Equality comparison between this number and the supplied representation.
	 * @param aa
	 *            Representation to be compared with (must currently be of type
	 *            XSDouble)
	 * 
	 * @return True if the 2 representations represent the same number. False
	 *         otherwise
	 * @since 1.1
	 */
	@Override
	public boolean eq(AnyType aa, DynamicContext dynamicContext) throws DynamicError {
		ResultSequence crs = constructor(aa);
		
		if (crs.empty()) {
			throw DynamicError.throw_type_error();
		}
		Item cat = crs.first();

		XSDouble d = (XSDouble) cat;
		/* [XQuery 1.0 and XPath 2.0 Functions and Operators (Second Edition)]
		 * op:numeric-equal($arg1 as numeric, $arg2 as numeric) as xs:boolean
		 *
		 *   Summary: Returns true if and only if the value of $arg1 is equal to
		 *   the value of $arg2. For xs:float and xs:double values, positive
		 *   zero and negative zero compare equal. INF equals INF and -INF
		 *   equals -INF. NaN does not equal itself.
		 */

		// Operator == for double values in Java performs as described above.
		// Note that this is NOT equivalent to the Double.equals method.
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
	@Override
	public boolean gt(AnyType arg, DynamicContext context) throws DynamicError {
		Item carg = convertArg(arg);
		
		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return double_value() > val.double_value();
	}

	protected Item convertArg(AnyType arg) throws DynamicError {
		ResultSequence rs = constructor(arg);
		Item carg = rs.first();
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
	@Override
	public boolean lt(AnyType arg, DynamicContext context) throws DynamicError {
		Item carg = convertArg(arg);

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
	@Override
	public ResultSequence plus(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		Item at = get_single_arg(carg);
		
		if (!(at instanceof XSDouble))
			throw DynamicError.throw_type_error();
		XSDouble val = (XSDouble) at;

		return new XSDouble(double_value() + val.double_value());
	}

	private ResultSequence convertResultSequence(ResultSequence arg)
			throws DynamicError {
		ResultSequence carg = arg;
		Iterator<Item> it = carg.iterator();
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
	@Override
	public ResultSequence minus(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		
		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);

		return new XSDouble(double_value() - val.double_value());
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
	@Override
	public ResultSequence times(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return new XSDouble(double_value() * val.double_value());
	}

	/**
	 * Mathematical division operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an division with
	 * @return A XSDouble consisting of the result of the mathematical division.
	 */
	@Override
	public ResultSequence div(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return new XSDouble(double_value() / val.double_value());
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
	@Override
	public ResultSequence idiv(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);

		if (this.nan() || val.nan())
			throw DynamicError.numeric_overflow("Dividend or divisor is NaN");

		if (this.infinite())
			throw DynamicError.numeric_overflow("Dividend is infinite");

		if (val.zero())
			throw DynamicError.div_zero(null);

		BigDecimal result = new BigDecimal((double_value() / val.double_value()));
		return new XSInteger(result.toBigInteger());
	}

	/**
	 * Mathematical modulus operator between this XSDouble and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform a modulus with
	 * @return A XSDouble consisting of the result of the mathematical modulus.
	 */
	@Override
	public ResultSequence mod(ResultSequence arg) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSDouble val = (XSDouble) get_single_type(carg, XSDouble.class);
		return new XSDouble(double_value() % val.double_value());
	}

	/**
	 * Negation of the number stored
	 * 
	 * @return A XSDouble representing the negation of this XSDecimal
	 */
	@Override
	public ResultSequence unary_minus() {
		return new XSDouble(-1 * double_value());
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
		if (nan() || infinite() || zero() || negativeZero()) {
			return this;
		}

		BigDecimal value = new BigDecimal(_value);
		BigDecimal round = value.setScale(0, BigDecimal.ROUND_HALF_UP);
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
		if (nan() || infinite() || zero() || negativeZero()) {
			return this;
		}

		BigDecimal value = new BigDecimal(_value);
		BigDecimal round = value.setScale(precision, BigDecimal.ROUND_HALF_EVEN);
		return new XSDouble(round.doubleValue());
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_DOUBLE;
	}

	@Override
    public Object getNativeValue() {
    	return double_value();
    }
}
