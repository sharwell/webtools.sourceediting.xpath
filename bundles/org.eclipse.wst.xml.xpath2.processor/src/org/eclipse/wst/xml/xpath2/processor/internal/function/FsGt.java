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
 *     Jesper Steen Moeller - bug 280555 - Add pluggable collation support
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Class for the Greater than function.
 */
public class FsGt extends Function {
	/**
	 * Constructor for FsGt.
	 */
	public FsGt() {
		super(new QName("gt"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_gt_value(args, evaluationContext);
	}

	/**
	 * Operation on the values of the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param evaluationContext
	 *             Evaluation context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_gt_value(Collection<ResultSequence> args, EvaluationContext evaluationContext)
			throws DynamicError {
		FsEq.CmpValueOp<CmpGt> op = new FsEq.CmpValueOp<CmpGt>() {
			@Override
			public Class<? extends CmpGt> getType() {
				return CmpGt.class;
			}

			@Override
			public boolean execute(CmpGt obj, AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
				return obj.gt(arg, evaluationContext);
			}
		};
		return FsEq.do_cmp_value_op(args, op, evaluationContext);
	}

	/**
	 * General operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param evaluationContext
	 *             The evaluation context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_gt_general(Collection<ResultSequence> args, EvaluationContext evaluationContext)
			throws DynamicError {
		FsEq.CmpGeneralOp op = new FsEq.CmpGeneralOp() {
			@Override
			public ResultSequence execute(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
				return fs_gt_value(args, evaluationContext);
			}
		};
		return FsEq.do_cmp_general_op(args, op, evaluationContext);
	}

}
