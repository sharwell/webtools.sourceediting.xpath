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
 * Class for the Division function.
 */
public class FsDiv extends Function {
	/**
	 * Constructor for FsDiv.
	 */
	public FsDiv() {
		super(new QName("div"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_div(args);
	}

	/**
	 * Div operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fs:div operation.
	 */
	public static ResultSequence fs_div(Collection<ResultSequence> args) throws DynamicError {
		FsPlus.MathOp<MathDiv> op = new FsPlus.MathOp<MathDiv>() {
			@Override
			public Class<? extends MathDiv> getType() {
				return MathDiv.class;
			}

			@Override
			public ResultSequence execute(MathDiv obj, ResultSequence arg) throws DynamicError {
				return obj.div(arg);
			}
		};
		return FsPlus.do_math_op(args, op);
	}
}
