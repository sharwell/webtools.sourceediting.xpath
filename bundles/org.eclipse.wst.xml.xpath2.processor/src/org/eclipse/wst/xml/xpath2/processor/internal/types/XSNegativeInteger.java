/*******************************************************************************
 * Copyright (c) 2009 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - bug 277602 - Initial API and implementation, of xs:negativeInteger
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

public class XSNegativeInteger extends XSNonPositiveInteger {
	
	private static final String XS_NEGATIVE_INTEGER = "xs:negativeInteger";
	private static final BigInteger MAX_VALUE = BigInteger.valueOf(-1);

	/**
	 * Initializes a representation of -1
	 */
	public XSNegativeInteger() {
	  this(BigInteger.valueOf(-1));
	}
	
	/**
	 * Initializes a representation of the supplied negativeInteger value
	 * 
	 * @param x
	 *            negativeInteger to be stored
	 */
	public XSNegativeInteger(BigInteger x) {
		super(x);
	}
	
	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:negativeInteger" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_NEGATIVE_INTEGER;
	}
	
	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "negativeInteger" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "negativeInteger";
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable negativeInteger
	 * in the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the negativeInteger is to be extracted
	 * @return New ResultSequence consisting of the 'negativeInteger' supplied
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
		return new XSNegativeInteger(integer.int_value());
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
		return BuiltinTypeLibrary.XS_NEGATIVEINTEGER;
	}
	
}
