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
 *     Jesper Steen Mooller - bug 280555 - Add pluggable collation support
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;

/**
 * Class for the Greater than or equal to function.
 */
public class FsGe extends Function {
	/**
	 * Constructor for FsGe.
	 */
	public FsGe() {
		super(new QName("ge"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_ge_value(args, ec.getDynamicContext());
	}

	/**
	 * Greater than or equal to operation on the values of the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param dc 
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_ge_value(Collection<ResultSequence> args, DynamicContext dc)
			throws DynamicError {
		ResultSequence greater = FsGt.fs_gt_value(args, dc);

		if (((XSBoolean) greater.first()).value())
			return greater;

		ResultSequence equal = FsEq.fs_eq_value(args, dc);

		if (((XSBoolean) equal.first()).value())
			return equal;

		return new XSBoolean(false);
	}

	/**
	 * General greater than or equal to operation.
	 * 
	 * @param args
	 *            input arguments.
	 * @param dc 
	 *             The dynamic context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_ge_general(Collection<ResultSequence> args, DynamicContext dc)
			throws DynamicError {
		FsEq.CmpGeneralOp op = new FsEq.CmpGeneralOp() {
			@Override
			public ResultSequence execute(Collection<ResultSequence> args, DynamicContext dynamicContext) throws DynamicError {
				return fs_ge_value(args, dynamicContext);
			}
		};
		return FsEq.do_cmp_general_op(args, op, dc);
	}
}
