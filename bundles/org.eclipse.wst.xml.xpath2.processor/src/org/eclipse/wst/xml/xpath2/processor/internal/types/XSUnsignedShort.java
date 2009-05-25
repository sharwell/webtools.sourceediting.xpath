/*******************************************************************************
 * Copyright (c) 2009 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - bug 277645 - Initial API and implementation, of xs:unsignedShort
 *                                 data type.
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigInteger;

import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;

public class XSUnsignedShort extends XSUnsignedInt {
	
	/**
	 * Initializes a representation of 0
	 */
	public XSUnsignedShort() {
	  this(BigInteger.valueOf(0));
	}
	
	/**
	 * Initializes a representation of the supplied unsignedShort value
	 * 
	 * @param x
	 *            unsignedShort to be stored
	 */
	public XSUnsignedShort(BigInteger x) {
		super(x);
	}
	
	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:unsignedShort" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "xs:unsignedShort";
	}
	
	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "unsignedShort" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "unsignedShort";
	}
	
	/**
	 * Creates a new ResultSequence consisting of the extractable unsignedShort
	 * in the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which the unsignedShort is to be extracted
	 * @return New ResultSequence consisting of the 'unsignedShort' supplied
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		ResultSequence rs = ResultSequenceFactory.create_new();

		if (arg.empty())
			return rs;

		// the function conversion rules apply here too. Get the argument
		// and convert it's string value to a unsignedShort.
		AnyType aat = arg.first();

		try {
			BigInteger bigInt = new BigInteger(aat.string_value());
			
			// doing the range checking
			// min value is 0
			// max value is 65535
			BigInteger min = BigInteger.valueOf(0);
			BigInteger max = BigInteger.valueOf(65535L);

			if (bigInt.compareTo(min) < 0 || bigInt.compareTo(max) > 0) {
			   // invalid input
			   DynamicError.throw_type_error();	
			}
			
			rs.add(new XSUnsignedShort(bigInt));
			
			return rs;
		} catch (NumberFormatException e) {
			throw DynamicError.cant_cast(null);
		}

	}

}
