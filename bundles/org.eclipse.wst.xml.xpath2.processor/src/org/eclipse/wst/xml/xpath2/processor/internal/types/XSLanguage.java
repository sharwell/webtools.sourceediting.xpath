/*******************************************************************************
 * Copyright (c) 2011 Mukul Gandhi, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.util.regex.Pattern;

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the language datatype
 */
public class XSLanguage extends XSToken {
	private static final String XS_NAME = "xs:language";
	private static final Pattern _lexicalPattern = Pattern.compile("[a-zA-Z]{1,8}(?:-[a-zA-Z0-9]{1,8})*");

	/**
	 * Initialises using the supplied String
	 * 
	 * @param x
	 *            String to be stored
	 */
	public XSLanguage(String x) {
		super(x);
	}

	/**
	 * Initialises to null
	 */
	public XSLanguage() {
		this(null);
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:language" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_NAME;
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "language" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "language";
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable language within
	 * the supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which to extract the language
	 * @return New ResultSequence consisting of the language supplied
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		ResultSequence result = super.constructor(arg);
		if (result.empty()) {
			return result;
		}

		XSToken token = (XSToken)result;
		String value = token.getStringValue();
		if (!_lexicalPattern.matcher(value).matches()) {
			throw DynamicError.cant_cast(null);
		}

		return new XSLanguage(value);
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_LANGUAGE;
	}
}
