/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - improved comparison of xs:string with other XDM types
 *  Jesper S Moller - bug 286061   correct handling of quoted string 
 *  Jesper S Moller - bug 280555 - Add pluggable collation support
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import java.math.BigInteger;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnCompare;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnData;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;

/**
 * A representation of the String datatype
 */
public class XSString extends CtrType implements CmpEq, CmpGt, CmpLt {

	private static final String XS_STRING = "xs:string";
	private String _value;

	/**
	 * Initialises using the supplied String
	 * 
	 * @param x
	 *            The String to initialise to
	 */
	public XSString(String x) {
		_value = x;
	}

	/**
	 * Initialises to null
	 */
	public XSString() {
		this(null);
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "xs:string" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return XS_STRING;
	}

	/**
	 * Retrieves the datatype's name
	 * 
	 * @return "string" which is the datatype's name
	 */
	@Override
	public String type_name() {
		return "string";
	}

	/**
	 * Retrieves a String representation of the string stored. This method is
	 * functionally identical to value()
	 * 
	 * @return The String stored
	 */
	@Override
	public String getStringValue() {
		return _value;
	}

	/**
	 * Retrieves a String representation of the string stored. This method is
	 * functionally identical to string_value()
	 * 
	 * @return The String stored
	 */
	public String value() {
		return getStringValue();
	}

	/**
	 * Creates a new ResultSequence consisting of the extractable String in the
	 * supplied ResultSequence
	 * 
	 * @param arg
	 *            The ResultSequence from which to extract the String
	 * @return New ResultSequence consisting of the supplied String
	 * @throws DynamicError
	 */
	@Override
	public ResultSequence constructor(ResultSequence arg) throws DynamicError {
		if (arg.empty())
			return ResultBuffer.EMPTY;

		AnyType aat = FnData.atomize(arg.first());

		return new XSString(aat.getStringValue());
	}

	// comparisons

	// 666 indicates death [compare returned empty seq]
	private int do_compare(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {

		// XXX: This can't happen, I guess
		if (arg == null) return 666;

		XSString comparand = arg instanceof XSString ? (XSString)arg : new XSString(arg.getStringValue());
		
		BigInteger result = FnCompare.compare_string(evaluationContext.getDynamicContext().getCollationProvider().getDefaultCollation(), this, comparand, evaluationContext);

		return result.intValue();
	}

	/**
	 * Equality comparison between this and the supplied representation which
	 * must be of type String
	 * 
	 * @param arg
	 *            The representation to compare with
	 * @return True if the two representation are of the same String. False
	 *         otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean eq(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		int cmp = do_compare(arg, evaluationContext);

		// XXX im not sure what to do here!!! because eq has to return
		// something i fink....
		if (cmp == 666)
			assert false;

		return cmp == 0;
	}

	/**
	 * Comparison between this and the supplied representation which must be of
	 * type String
	 * 
	 * @param arg
	 *            The representation to compare with
	 * @return True if this String is lexographically greater than that
	 *         supplied. False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean gt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		int cmp = do_compare(arg, evaluationContext);

		assert cmp != 666;

		return cmp > 0;
	}

	/**
	 * Comparison between this and the supplied representation which must be of
	 * type String
	 * 
	 * @param arg
	 *            The representation to compare with
	 * @return True if this String is lexographically less than that supplied.
	 *         False otherwise
	 * @throws DynamicError
	 */
	@Override
	public boolean lt(AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
		int cmp = do_compare(arg, evaluationContext);

		assert cmp != 666;

		return cmp < 0;
	}
	@Override
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_STRING;
	}

	@Override
	public Object getNativeValue() {
		return _value;
	}

}
