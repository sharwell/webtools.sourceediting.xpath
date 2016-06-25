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
 *     Jesper Steen Moller  - Bug 286062 - Add type promotion for numeric operators  
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;

/**
 * Class for Plus function.
 */
public class FsPlus extends Function {
	/**
	 * Constructor for FsPlus.
	 */
	public FsPlus() {
		super(new QName("plus"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext evaluationContext) {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_plus(args, evaluationContext);
	}

	/**
	 * Convert and promote arguments for operation.
	 * 
	 * @param args
	 *            input arguments.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of conversion.
	 */
	private static Collection<ResultSequence> convert_args(Collection<ResultSequence> args) throws DynamicError {
		Collection<ResultSequence> result = new ArrayList<ResultSequence>();

		// Keep track of numeric types for promotion
		boolean has_float = false;
		boolean has_double = false;
		
		// atomize arguments
		for (Iterator<ResultSequence> i = args.iterator(); i.hasNext();) {
			ResultSequence rs = FnData.atomize(i.next());

			if (rs.empty())
				return new ArrayList<ResultSequence>();

			if (rs.size() > 1)
				throw new DynamicError(TypeError.invalid_type(null));

			AnyType arg = (AnyType) rs.item(0);

			if (arg instanceof XSUntypedAtomic) {
				arg = new XSDouble(arg.getStringValue());
			}

			if (arg instanceof XSDouble) has_double = true;
			if (arg instanceof XSFloat) has_float = true;
			result.add(ResultBuffer.wrap(arg));
		}

		if (has_double) has_float = false;
		
		if (has_double || has_float) {
			Collection<ResultSequence> result2 = new ArrayList<ResultSequence>();

			// promote arguments
			for (ResultSequence rs : result) {
				Item arg = rs.item(0);
				
				if (has_double && (arg instanceof XSFloat)) {
					arg = new XSDouble(((XSFloat)arg).float_value());
				} else if (has_double && (arg instanceof XSDecimal)) {
					arg = new XSDouble(((XSDecimal)arg).getValue().doubleValue());
				} else if (has_float && (arg instanceof XSDecimal)) {
					arg = new XSFloat(((XSDecimal)arg).getValue().floatValue());
				}
				result2.add((ResultSequence)arg);
			}
			return result2;
		}
		
		return result;
	}


	/**
	 * General operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param evaluationContext
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_plus(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		MathOp<MathPlus> op = new MathOp<MathPlus>() {
			@Override
			public Class<? extends MathPlus> getType() {
				return MathPlus.class;
			}

			@Override
			public ResultSequence execute(MathPlus obj, ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
				return obj.plus(arg, evaluationContext);
			}
		};
		return do_math_op(args, op, evaluationContext);
	}

	/**
	 * Unary operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence fs_plus_unary(Collection<ResultSequence> args)
			throws DynamicError {

		// make sure we got only one arg
		if (args.size() != 1)
			throw DynamicError.throw_type_error();
		ResultSequence arg = args.iterator().next();

		// make sure we got only one numeric atom
		if (arg.size() != 1)
			throw DynamicError.throw_type_error();
		Item at = arg.first();
		if (!(at instanceof NumericType))
			throw DynamicError.throw_type_error();

		// no-op
		return arg;
	}

	public interface MathOp<T> {
		Class<? extends T> getType();

		ResultSequence execute(T obj, ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError;
	}

	// voodoo
	/**
	 * Mathematical operation on the arguments.
	 * 
	 * @param args
	 *            input arguments.
	 * @param evaluationContext
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	public static <T> ResultSequence do_math_op(Collection<ResultSequence> args, MathOp<T> op, EvaluationContext evaluationContext) throws DynamicError {

		// sanity check args + convert em
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Collection<ResultSequence> cargs = convert_args(args);

		if (cargs.size() == 0)
			return ResultBuffer.EMPTY;

		// make sure arugments are good [at least the first one]
		Iterator<ResultSequence> argi = cargs.iterator();

		T arg;
		try {
			arg = op.getType().cast(argi.next().item(0));
			if (arg == null) {
				throw DynamicError.throw_type_error();
			}
		} catch (ClassCastException ex) {
			throw DynamicError.throw_type_error();
		}

		ResultSequence arg2 = argi.next();

		return op.execute(arg, arg2, evaluationContext);
	}
}
