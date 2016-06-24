/*******************************************************************************
 * Copyright (c) 2009 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - bug 277599 - Initial API and implementation, of xs:nonPositiveInteger
 *                                 data type.
 *     David Carver (STAR) - bug 262765 - fixed abs value tests.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigInteger;

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

public class XSNonPositiveInteger extends XSInteger {
	
	private static final String XS_NON_POSITIVE_INTEGER = "xs:nonPositiveInteger";
	private static final BigInteger MAX_VALUE = BigInteger.ZERO;

	/**
	 * Initializes a representation of 0
	 */
	public XSNonPositiveInteger() {
	  this(BigInteger.valueOf(0));
	}
	
	/**
	 * Initializes a representation of the supplied nonPositiveInteger value
	 * 
	 * @param x
	 *            nonPositiveInteger to be stored
	 */
	public XSNonPositiveInteger(BigInteger x) {
		super(x);
	}
	
	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:nonPositiveInteger" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_NON_POSITIVE_INTEGER;
	}
	
	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "nonPositiveInteger" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "nonPositiveInteger";
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable nonPositiveInteger
	 * in the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the nonPositiveInteger is to be extracted
	 * @return New ResultSequence consisting of the 'nonPositiveInteger' supplied
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
		return new XSNonPositiveInteger(integer.int_value());
	}

	@Override
	protected BigInteger getMinValue() {
		return null;
	}

	@Override
	protected BigInteger getMaxValue() {
		return MAX_VALUE;
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_NONPOSITIVEINTEGER;
	}

}
