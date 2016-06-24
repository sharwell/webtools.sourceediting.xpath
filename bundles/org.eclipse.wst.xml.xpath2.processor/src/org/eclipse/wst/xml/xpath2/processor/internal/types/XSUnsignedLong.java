/*******************************************************************************
 * Copyright (c) 2009 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - bug 277629 - Initial API and implementation, of xs:unsignedLong
 *                                 data type.
 *     David Carver (STAR) - bug 262765 - fixed abs value tests.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigInteger;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

public class XSUnsignedLong extends XSNonNegativeInteger {
	
	private static final String XS_UNSIGNED_LONG = "xs:unsignedLong";
	private static final BigInteger MIN_VALUE = BigInteger.ZERO;
	private static final BigInteger MAX_VALUE = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

	/**
	 * Initializes a representation of 0
	 */
	public XSUnsignedLong() {
	  this(BigInteger.valueOf(0));
	}
	
	/**
	 * Initializes a representation of the supplied unsignedLong value
	 * 
	 * @param x
	 *            unsignedLong to be stored
	 */
	public XSUnsignedLong(BigInteger x) {
		super(x);
	}
	
	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:unsignedLong" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_UNSIGNED_LONG;
	}
	
	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "unsignedLong" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "unsignedLong";
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable unsignedLong
	 * in the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the unsignedLong is to be extracted
	 * @return New ResultSequence consisting of the 'unsignedLong' supplied
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		ResultSequence result = super.constructor(arg);
		if (result.empty()) {
			return result;
		}

		XSInteger integer = (XSInteger)result;
		// range is already validated via getMinValue() and/or getMaxValue()
		return new XSUnsignedLong(integer.int_value());
	}

	@Override
	protected BigInteger getMinValue() {
		return MIN_VALUE;
	}

	@Override
	protected BigInteger getMaxValue() {
		return MAX_VALUE;
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_UNSIGNEDLONG;
	}

}
