/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 274805 - improvements to xs:integer data type. Using Java
 *                      BigInteger class to enhance numerical range to -INF -> +INF.
 *     David Carver (STAR) - bug 262765 - fix comparision to zero.
 *     David Carver (STAR) - bug 282223 - fix casting issues.
 *     Jesper Steen Moller - bug 262765 - fix type tests
 *     Jesper Steen Moller - bug 281028 - Added constructor from string
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the Integer datatype
 */
public class XSInteger extends XSDecimal {

	private static final String XS_INTEGER = "xs:integer";
	private BigInteger _value;

	/**
	 * Initializes a representation of 0
	 */
	public XSInteger() {
		this(BigInteger.valueOf(0));
	}

	/**
	 * Initializes a representation of the supplied integer
	 * 
	 * @param x
	 *            Integer to be stored
	 */
	public XSInteger(BigInteger x) {
		super(new BigDecimal(x));
		_value = x;
	}

	/**
	 * Initializes a representation of the supplied integer
	 * 
	 * @param x
	 *            Integer to be stored
	 */
	public XSInteger(String x) {
		super(new BigDecimal(x));
		_value = new BigInteger(x);
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:integer" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_INTEGER;
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "integer" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "integer";
	}

	/**
	 * Retrieves a String representation of the integer stored
	 * 
	 * @return String representation of the integer stored
	 */
	@Override
	public String getStringValue() {
		return _value.toString();
	}

	@Override
	public Number getNativeValue() {
		return _value;
	}
	
	/**
	 * Check whether the integer represented is 0
	 * 
	 * @return True is the integer represented is 0. False otherwise
	 */
	@Override
	public boolean zero() {
		return (_value.compareTo(BigInteger.ZERO) == 0);
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable integer in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the integer is to be extracted
	 * @return New ResultSequence consisting of the integer supplied
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		// the function conversion rules apply here too. Get the argument
		// and convert it's string value to an integer.
		AnyType aat = FnData.atomize(arg.first());
		// From 17.1.3.4 (Casting to xs:integer)
		if (!(aat instanceof XSString
				|| aat instanceof XSUntypedAtomic
				|| aat instanceof XSFloat
				|| aat instanceof XSDouble
				|| aat instanceof XSDecimal
				|| aat instanceof XSBoolean)) {
			throw DynamicError.invalidType();
		}

		if (!isCastable(aat)) {
			throw DynamicError.cant_cast(null);
		}

		try {
			BigInteger bigInt = castInteger(aat);
			if (getMinValue() != null && bigInt.compareTo(getMinValue()) < 0) {
				throw DynamicError.cant_cast(null);
			}

			if (getMaxValue() != null && bigInt.compareTo(getMaxValue()) > 0) {
				throw DynamicError.cant_cast(null);
			}

			return new XSInteger(bigInt);
		} catch (NumberFormatException e) {
			throw DynamicError.invalidLexicalValue(e);
		}

	}

	/**
	 * Gets the minimum value (inclusive) which can be represented by this integer.
	 *
	 * @return The minimum value which can be represented by this integer type, or {@code null} if there is no minimum.
	 */
	protected BigInteger getMinValue() {
		return null;
	}

	/**
	 * Gets the maximum value (inclusive) which can be represented by this integer.
	 *
	 * @return The maximum value which can be represented by this integer type, or {@code null} if there is no maximum.
	 */
	protected BigInteger getMaxValue() {
		return null;
	}

	private BigInteger castInteger(Item aat) {
		if (aat instanceof XSBoolean) {
			if (aat.getStringValue().equals("true")) {
				return BigInteger.ONE;
			} else {
				return BigInteger.ZERO;
			}
		}
		
		if (aat instanceof XSDecimal || aat instanceof XSFloat ||
				aat instanceof XSDouble) {
				BigDecimal bigDec =  new BigDecimal(aat.getStringValue());
				return bigDec.toBigInteger();
		}
		
		return new BigInteger(aat.getStringValue().trim());
	}
	
	private boolean isCastable(Item aat) throws DynamicError {
		if (aat instanceof XSString || aat instanceof XSUntypedAtomic ||
			aat instanceof NodeType) {
			if (isLexicalValue(aat.getStringValue().trim())) {
				return true;
			} else {
				return false;
			}
		}
		if (aat instanceof XSBoolean || aat instanceof NumericType) {
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean isLexicalValue(String value) {
		
		try {
			new BigInteger(value);
		} catch (NumberFormatException ex) {
			return false;
		}
		
		return true;
	}

	/**
	 * Retrieves the actual integer value stored
	 * 
	 * @return The actual integer value stored
	 */
	public BigInteger int_value() {
		return _value;
	}

	/**
	 * Sets the integer stored to that supplied
	 * 
	 * @param x
	 *            Integer to be stored
	 */
	public void set_int(BigInteger x) {
		_value = x;
		set_double(x.intValue());
	}

	/**
	 * Mathematical addition operator between this XSInteger and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform an addition with
	 * @return A XSInteger consisting of the result of the mathematical
	 *         addition.
	 */
	@Override
	public ResultSequence plus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		Item at = get_single_arg(carg);
		if (!(at instanceof XSInteger))
			throw DynamicError.throw_type_error();
		
		XSInteger val = (XSInteger)at;

		return new XSInteger(int_value().add(val.int_value()));
		
	}

	
	private ResultSequence convertResultSequence(ResultSequence arg)
			throws DynamicError {
		ResultSequence carg = arg;
		Iterator<Item> it = carg.iterator();
		while (it.hasNext()) {
			AnyType type = (AnyType) it.next();
			if (type.string_type().equals("xs:untypedAtomic") ||
				type.string_type().equals("xs:string")) {
				throw DynamicError.invalidType();
			}
		}
		carg = constructor(carg);
		return carg;
	}	
	
	/**
	 * Mathematical subtraction operator between this XSInteger and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform a subtraction with
	 * @return A XSInteger consisting of the result of the mathematical
	 *         subtraction.
	 */
	@Override
	public ResultSequence minus(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		XSInteger val = get_single_type(carg, XSInteger.class);
		
		return new XSInteger(int_value().subtract(val.int_value()));
	}

	/**
	 * Mathematical multiplication operator between this XSInteger and the
	 * supplied ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform a multiplication with
	 * @return A XSInteger consisting of the result of the mathematical
	 *         multiplication.
	 */
	@Override
	protected ResultSequence timesImpl(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSInteger val = get_single_type(carg, XSInteger.class);
		
		return new XSInteger(int_value().multiply(val.int_value()));
	}

	/**
	 * Mathematical modulus operator between this XSInteger and the supplied
	 * ResultSequence. 
	 * 
	 * @param arg
	 *            The ResultSequence to perform a modulus with
	 * @return A XSInteger consisting of the result of the mathematical modulus.
	 */
	@Override
	public ResultSequence mod(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);

		XSInteger val = get_single_type(carg, XSInteger.class);
		if (val.zero()) {
			throw DynamicError.div_zero(null);
		}

		BigInteger result = int_value().remainder(val.int_value());
		
		return new XSInteger(result);
	}

	/**
	 * Negates the integer stored
	 * 
	 * @return New XSInteger representing the negation of the integer stored
	 */
	@Override
	public ResultSequence unary_minus() {
		return new XSInteger(int_value().multiply(BigInteger.valueOf(-1)));
	}

	/**
	 * Absolutes the integer stored
	 * 
	 * @return New XSInteger representing the absolute of the integer stored
	 */
	@Override
	public NumericType abs() {
		return new XSInteger(int_value().abs());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal#gt(org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType)
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		Item carg = convertArg(arg);
        XSInteger val = get_single_type(carg, XSInteger.class);
        
        int compareResult = int_value().compareTo(val.int_value());
		
		return compareResult > 0;
	}
	
	@Override
	protected Item convertArg(AnyType arg) throws DynamicError {
		ResultSequence rs = constructor(arg);
		Item carg = rs.first();
		return carg;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal#lt(org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType)
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		Item carg = convertArg(arg);
        XSInteger val = get_single_type(carg, XSInteger.class);
        
        int compareResult = int_value().compareTo(val.int_value());
		
		return compareResult < 0;
	}
	
	@Override
	public ResultSequence div(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence carg = convertResultSequence(arg);
		
		XSDecimal val = get_single_type(carg, XSDecimal.class);

		if (val.zero()) {
			throw DynamicError.div_zero(null);
		}
		
		BigDecimal result = getValue().divide(val.getValue(), 18, BigDecimal.ROUND_HALF_EVEN);
		return new XSDecimal(result);
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_INTEGER;
	}
}
