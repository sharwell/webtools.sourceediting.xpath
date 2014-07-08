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
 *     Jesper Moller - bug 280555 - Add pluggable collation support
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;

/**
 * Class for Less than or equal to function.
 */
public class FsLe extends Function {
	/**
	 * Constructor for FsLe.
	 */
	public FsLe() {
		super(new QName("le"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_le_value(args, ec.getStaticContext(), ec.getDynamicContext());
	}

	/**
	 * Operation on the values of the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param 
     *         DynamicContext 
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_le_value(Collection<ResultSequence> args, StaticContext staticContext, DynamicContext dynamicContext)
			throws DynamicError {
		ResultSequence less = FsLt.fs_lt_value(args, staticContext, dynamicContext);

		if (((XSBoolean) less.first()).value())
			return less;

		ResultSequence equal = FsEq.fs_eq_value(args, staticContext, dynamicContext);

		if (((XSBoolean) equal.first()).value())
			return equal;

		return ResultSequenceFactory.create_new(new XSBoolean(false));
	}

	/**
	 * General operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param dc 
	 *             The dynamic context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_le_general(Collection<ResultSequence> args, StaticContext staticContext, DynamicContext dynamicContext) {
		FsEq.CmpGeneralOp op = new FsEq.CmpGeneralOp() {
			@Override
			public ResultSequence execute(Collection<ResultSequence> args, StaticContext staticContext, DynamicContext dynamicContext) throws DynamicError {
				return fs_le_value(args, staticContext, dynamicContext);
			}
		};
		return FsEq.do_cmp_general_op(args, op, staticContext, dynamicContext);
	}
}
