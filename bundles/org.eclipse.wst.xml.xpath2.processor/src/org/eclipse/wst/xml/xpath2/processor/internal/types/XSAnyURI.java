/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug274719 - implementation of equality of xs:anyURI values
 *     David Carver (STAR) - bug 282223 - fixed casting to xs:anyURI only string,
 *         untypedAtomic, and anyURI are allowed.
 *     David Carver (STAR) - bug 283777 - implemented gt, lt comparison code.
 *     Jesper Steen Moller - bug 281159 - added promotion of xs:anyURI to string (reverse case) 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * Represents a Universal Resource Identifier (URI) reference
 */
public class XSAnyURI extends CtrType implements CmpEq, CmpGt, CmpLt {

	private static final String XS_ANY_URI = "xs:anyURI";
	private String _value;

	/**
	 * Arity 1 Constructor
	 * 
	 * @param x
	 *            String representation of the URI
	 */
	public XSAnyURI(String x) {
		_value = x;
	}

	/**
	 * Arity 0 Constructor. Initiates URI to null.
	 */
	public XSAnyURI() {
		this(null);
	}

	/**
	 * Retrieve full type pathname of this datatype
	 * 
	 * @return "xs:anyURI", the full type pathname of this datatype
	 */
	@Override
	public String string_type() {
		return XS_ANY_URI;
	}

	/**
	 * Retrieve type name of this datatype
	 * 
	 * @return "anyURI", the type name of this datatype
	 */
	@Override
	public String type_name() {
		return "anyURI";
	}

	/**
	 * Transforms and retrieves the URI value of this URI datatype in String
	 * format
	 * 
	 * @return the URI value held by this instance of the URI datatype as a
	 *         String
	 */
	@Override
	public String string_value() {
		return _value;
	}

	/**
	 * Creation of a result sequence consisting of a URI from a previous result
	 * sequence.
	 * 
	 * @param arg
	 *            previous result sequence
	 * @throws DynamicError
	 * @return new result sequence consisting of the URI supplied
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyType aat = FnData.atomize(arg.first());

		if (!(aat.string_type().equals("xs:string")
				|| aat.string_type().equals(XS_ANY_URI) || aat.string_type()
				.equals("xs:untypedAtomic"))) {
			throw DynamicError.invalidType();
		}

		String uri = aat.string_value().trim().replaceAll("\\s+", " ");

		try {
			// Escape the input text prior to validation as a URI
			byte[] encodedInput = uri.getBytes("UTF-8");
			StringBuilder builder = new StringBuilder();
			for (byte b : encodedInput) {
				int i = (int)b & 0xFF;
				// start by checking control and non-ASCII characters
				boolean needEscape = i <= 0x1F || i >= 0x7F;
				if (!needEscape) {
					switch (i) {
					// space
					case ' ':
					// delims
					case '<':
					case '>':
					case '"':
					// unwise
					case '{':
					case '}':
					case '|':
					case '\\':
					case '^':
					case '`':
						needEscape = true;
						break;

					default:
						break;
					}
				}

				if (needEscape) {
					builder.append('%');
					if (i < 0x10) {
						builder.append('0');
					}

					builder.append(Integer.toHexString(i));
				} else {
					builder.append((char)i);
				}
			}

			// The URI constructor does not allow raw spaces. Escape them to ensure they do not cause the xs:anyURI
			// constructor to fail.
			new URI(builder.toString());
		} catch (UnsupportedEncodingException ex) {
			// This shouldn't be reachable since all JREs support UTF-8
			throw DynamicError.cant_cast(uri, ex);
		} catch (URISyntaxException ex) {
			throw DynamicError.cant_cast(uri, ex);
		}

		return new XSAnyURI(uri);
	}

	/**
	 * Equality comparison between this and the supplied representation which
	 * must be of type xs:anyURI (or, by promotion of this, xs:string)
	 * 
	 * @param arg
	 *            The representation to compare with
	 * @return True if the two representation are of the same String. False
	 *         otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg instanceof XSAnyURI || arg instanceof XSString) {
			if (this.string_value().equals(arg.string_value())) {
				return true;
			}
		} else {
			throw DynamicError.throw_type_error();
		}

		return false;
	}

	/**
	 * Greater than comparison between this and the supplied representation which
	 * must be of type xs:anyURI (or, by promotion of this, xs:string)
	 * @since 1.1
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		if (!(arg instanceof XSAnyURI || arg instanceof XSString)) {
			throw DynamicError.throw_type_error();	
		}
		
		String anyURI = this.string_value();
		String compareToURI = arg.string_value();
		if (anyURI.compareTo(compareToURI) > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Less than comparison between this and the supplied representation which
	 * must be of type xs:anyURI (or, by promotion of this, xs:string)
	 * 
	 * @since 1.1
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		if (!(arg instanceof XSAnyURI || arg instanceof XSString)) {
			throw DynamicError.throw_type_error();
		}
		
		String anyURI = this.string_value();
		String compareToURI = arg.string_value();
		if (anyURI.compareTo(compareToURI) < 0) {
			return true;
		}

		return false;
	}

	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_ANYURI;
	}

	@Override
	public String getStringValue() {
		return _value;
	}
}
