/*******************************************************************************
 * Copyright (c) 2009 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - bug 277608 - implementation of xs:short data type
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

public class XSShort extends XSInt {
	
	private static final String XS_SHORT = "xs:short";
	private static final BigInteger MIN_VALUE = BigInteger.valueOf(Short.MIN_VALUE);
	private static final BigInteger MAX_VALUE = BigInteger.valueOf(Short.MAX_VALUE);

	/**
	 * Initializes a representation of 0
	 */
	public XSShort() {
	  this(BigInteger.valueOf(0));
	}
	
	/**
	 * Initializes a representation of the supplied short value
	 * 
	 * @param x
	 *            Short to be stored
	 */
	public XSShort(BigInteger x) {
		super(x);
	}
	
	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:short" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_SHORT;
	}
	
	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "short" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "short";
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable 'short' in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the short is to be extracted
	 * @return New ResultSequence consisting of the 'short' supplied
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
		return new XSShort(integer.int_value());
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
		return BuiltinTypeLibrary.XS_SHORT;
	}

	@Override
	public Short getNativeValue() {
		return getValue().shortValue();
	}

}
