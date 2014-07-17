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
 *     Jesper Steen Moeller - bug 262765 - fixes float handling for fn:number 
 *     Mukul Gandhi - bug 298519 - improvements to fn:number implementation,
 *                                 catering to node arguments. 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;

/**
 * Returns the value indicated by $arg or, if $arg is not specified, the context
 * item after atomization, converted to an xs:double. If $arg or the context
 * item cannot be converted to an xs:double, the xs:double value NaN is
 * returned. If the context item is undefined an error is raised:
 * [err:FONC0001].
 */
public class FnNumber extends Function {
	/**
	 * Constructor for FnNumber.
	 */
	public FnNumber() {
		super(new QName("number"), 0, 1);
	}

	/**
	 * Evaluate arguments.
	 * 
	 * @param args
	 *            argument expressions.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of evaluation.
	 */
	@Override
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {

		assert args.size() >= min_arity() && args.size() <= max_arity();

		ResultSequence argument;
		if (args.isEmpty()) {
			Item contextItem = ec.getContextItem();
			if (contextItem == null) {
				throw DynamicError.contextUndefined();
			}

			argument = ResultBuffer.wrap(contextItem);
		} else {
			argument = args.iterator().next();
		}

		return fn_number(argument, ec);
	}

	/**
	 * Number operation.
	 * 
	 * @param arg
	 *            Result from the expressions evaluation.
	 * @param dc
	 *            Result of dynamic context operation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:number operation.
	 */
	public static XSDouble fn_number(ResultSequence arg, EvaluationContext ec)
			throws DynamicError {

		if (arg.empty()) {
			return new XSDouble(Double.NaN);
		}

		try {
			return (XSDouble)new XSDouble().constructor(arg);
		} catch (DynamicError ex) {
			return new XSDouble(Double.NaN);
		}
	}

}
