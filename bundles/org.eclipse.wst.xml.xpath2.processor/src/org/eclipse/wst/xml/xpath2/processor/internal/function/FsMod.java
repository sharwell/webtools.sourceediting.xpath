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
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Class for Modular function.
 */
public class FsMod extends Function {
	/**
	 * Constructor for FsMod.
	 */
	public FsMod() {
		super(new QName("mod"), 2);
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

		return fs_mod(args, evaluationContext);
	}

	/**
	 * General operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_mod(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		FsPlus.MathOp<MathMod> op = new FsPlus.MathOp<MathMod>() {
			@Override
			public Class<? extends MathMod> getType() {
				return MathMod.class;
			}

			@Override
			public ResultSequence execute(MathMod obj, ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
				return obj.mod(arg, evaluationContext);
			}
		};
		return FsPlus.do_math_op(args, op, evaluationContext);
	}
}
