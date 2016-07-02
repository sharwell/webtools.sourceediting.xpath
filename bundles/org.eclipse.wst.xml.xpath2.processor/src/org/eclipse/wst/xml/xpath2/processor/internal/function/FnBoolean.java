/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     Jesper Steen Moller  - bug 262765 - use correct 'effective boolean value'
 *     David Carver (STAR) - bug 262765 - fix checking of data types.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.CalendarType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;

/**
 * Computes the effective boolean value of the sequence $arg. If $arg is the
 * empty sequence, returns false. If $arg contains a single atomic value, then
 * the function returns false if $arg is: - The singleton xs:boolean value
 * false. - The singleton value "" (zero-length string) of type xs:string or
 * xdt:untypedAtomic. - A singleton numeric value that is numerically equal to
 * zero. - The singleton xs:float or xs:double value NaN. In all other cases,
 * returns true.
 */
public class FnBoolean extends Function {
	/**
	 * Constructor for FnBoolean.
	 */
	public FnBoolean() {
		super(new QName("boolean"), 1);
	}

	/**
	 * Evaluate arguments.
	 * 
	 * @param args
	 *            argument expressions.
	 * @return Result of evaluation.
	 */
	@Override
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) {
		// 1 argument only!
		assert args.size() >= min_arity() && args.size() <= max_arity();

		ResultSequence argument = args.iterator().next();

		return fn_boolean(argument);
	}

	/**
	 * Boolean operation.
	 * 
	 * @param arg
	 *            Result from the expressions evaluation.
	 * @return Result of fn:boolean operation.
	 * @throws DynamicError 
	 */
	public static XSBoolean fn_boolean(ResultSequence arg) throws DynamicError {
		// 1. If $arg is the empty sequence, fn:boolean returns false.
		if (arg.empty()) {
			return XSBoolean.FALSE;
		}

		// 2. If $arg is a sequence whose first item is a node, fn:boolean returns true.
		if (arg.item(0) instanceof NodeType) {
			return XSBoolean.TRUE;
		}

		// 3. If $arg is a singleton value of type xs:boolean or a derived from xs:boolean, fn:boolean returns $arg.
		if (arg instanceof XSBoolean) {
			return (XSBoolean) arg;
		}

		// 4. If $arg is a singleton value of type xs:string or a type derived from xs:string, xs:anyURI or a type
		// derived from xs:anyURI or xs:untypedAtomic, fn:boolean returns false if the operand value has zero length;
		// otherwise it returns true.
		if (arg instanceof XSString
				|| arg instanceof XSAnyURI
				|| arg instanceof XSUntypedAtomic) {
			return "".equals(((AnyType) arg).getStringValue()) ? XSBoolean.FALSE : XSBoolean.TRUE;
		}

		// 5. If $arg is a singleton value of any numeric type or a type derived from a numeric type, fn:boolean returns
		// false if the operand value is NaN or is numerically equal to zero; otherwise it returns true.
		if (arg instanceof NumericType) {
			if (arg instanceof XSDouble) {
				XSDouble value = (XSDouble) arg;
				if (value.zero() || value.negativeZero() || value.nan()) {
					return XSBoolean.FALSE;
				}
			} else if (arg instanceof XSFloat) {
				XSFloat value = (XSFloat) arg;
				if (value.zero() || value.negativeZero() || value.nan()) {
					return XSBoolean.FALSE;
				}
			} else if (((NumericType) arg).zero()) {
				return XSBoolean.FALSE;
			}

			return XSBoolean.TRUE;
		}

		// 6. In all other cases, fn:boolean raises a type error [err:FORG0006].
		throw DynamicError.argument_type_error(arg.getClass());
	}

}
