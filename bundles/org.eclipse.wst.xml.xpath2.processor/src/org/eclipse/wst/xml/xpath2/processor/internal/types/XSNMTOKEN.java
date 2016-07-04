/*******************************************************************************
 * Copyright (c) 2011 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mukul Gandhi - initial API and implementation
 *     Mukul Gandhi - bug 334842 - improving support for the data types Name, NCName, ENTITY, 
 *                                 ID, IDREF and NMTOKEN. 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.apache.xerces.util.XMLChar;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the NMTOKEN datatype
 */
public class XSNMTOKEN extends XSToken {
	private static final String XS_NMTOKEN = "xs:NMTOKEN";

	/**
	 * Initialises using the supplied String
	 * 
	 * @param x
	 *            String to be stored
	 */
	public XSNMTOKEN(String x) {
		super(x);
	}

	/**
	 * Initialises to null
	 */
	public XSNMTOKEN() {
		this(null);
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:NMTOKEN" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_NMTOKEN;
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "NMTOKEN" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "NMTOKEN";
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable NMTOKEN within
	 * the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which to extract the NMTOKEN
	 * @return New ResultSequence consisting of the NMTOKEN supplied
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyType aat = FnData.atomize(arg.first());
		String strValue = aat.getStringValue();
		
		if (!XMLChar.isValidNmtoken(strValue)) {
			// invalid input
			throw DynamicError.cant_cast(null);
		}

		return new XSNMTOKEN(strValue);
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_NMTOKEN;
	}
	
}
