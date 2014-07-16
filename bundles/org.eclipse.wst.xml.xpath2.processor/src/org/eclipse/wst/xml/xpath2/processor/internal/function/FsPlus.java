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
import java.util.List;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.CtrType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSYearMonthDuration;

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
	 * @param sc 
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of conversion.
	 */
	private static Collection<ResultSequence> convert_args(Collection<ResultSequence> args) throws DynamicError {
		Collection<ResultSequence> result = new ArrayList<ResultSequence>();

		// Keep track of numeric types for promotion
		boolean has_float = false;
		boolean has_double = false;
		boolean has_duration = false;
		
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
			if (arg instanceof XSDayTimeDuration || arg instanceof XSYearMonthDuration) has_duration = true;
			result.add(arg);
		}

		if (has_double) has_float = false;
		
		if (has_double || has_float || has_duration) {
			Collection<ResultSequence> result2 = new ArrayList<ResultSequence>();

			// promote arguments
			for (ResultSequence rs : result) {
				Item arg = rs.item(0);
				
				if ((has_double || has_duration) && (arg instanceof XSFloat)) {
					arg = new XSDouble(((XSFloat)arg).float_value());
				} else if ((has_double || has_duration) && (arg instanceof XSDecimal)) {
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
	 * @param sc 
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
		if (arg.empty()) {
			return ResultBuffer.EMPTY;
		}

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
	 * @param sc 
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	public static <T> ResultSequence do_math_op(Collection<ResultSequence> args, MathOp<T> op, EvaluationContext evaluationContext) throws DynamicError {

		// sanity check args + convert em
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Collection<ResultSequence> cargs = convert_args(args);
		if (cargs.isEmpty())
			return ResultBuffer.EMPTY;

		cargs = promoteMathOperands(cargs);

		// make sure arugments are good [at least the first one]
		Iterator<ResultSequence> argi = cargs.iterator();

		ResultSequence argSequence = argi.next();
		if (argSequence == null) {
			throw DynamicError.throw_type_error();
		}

		Item arg = argSequence.item(0);
		if (!(op.getType().isInstance(arg)))
			throw DynamicError.throw_type_error();

		ResultSequence arg2 = argi.next();
		return op.execute(op.getType().cast(arg), arg2, evaluationContext);
	}

	private static Collection<ResultSequence> promoteMathOperands(Collection<ResultSequence> args) {
		List<ResultSequence> result = new ArrayList<ResultSequence>(args);

		boolean changed;
		do {
			changed = false;
			for (int i = 1; i < result.size(); i++) {
				AnyType[] promoted = promoteMathOperands((CtrType)result.get(0), (CtrType)result.get(i));
				if (promoted[0] != result.get(0)) {
					changed = true;
					result.set(0, promoted[0]);
				}

				if (promoted[1] != result.get(1)) {
					changed = true;
					result.set(1, promoted[1]);
				}
			}
		} while (changed);

		return result;
	}

	private static AnyType[] promoteMathOperands(CtrType arg1, CtrType arg2) {
		if (arg1 == null || arg2 == null) {
			return new AnyType[] { arg1, arg2 };
		}

		if (arg1 instanceof XSUntypedAtomic && arg2 instanceof XSUntypedAtomic) {
			return new AnyType[] { new XSString(arg1.getStringValue()), new XSString(arg2.getStringValue()) };
		}

		if (arg1.getClass() == arg2.getClass()) {
			// trivial case
			return new AnyType[] { arg1, arg2 };
		}

		if (arg2 instanceof XSUntypedAtomic) {
			AnyType[] swappedResult = promoteMathOperands(arg2, arg1);
			AnyType tmp = swappedResult[0];
			swappedResult[0] = swappedResult[1];
			swappedResult[1] = tmp;
			return swappedResult;
		}

		AnyType[] result = new AnyType[2];
		if (arg1.getClass().isAssignableFrom(arg2.getClass())) {
			result[0] = arg1;
			result[1] = (AnyType)arg1.constructor(arg2);
		} else if (arg2.getClass().isAssignableFrom(arg1.getClass())) {
			result[0] = (AnyType)arg2.constructor(arg1);
			result[1] = arg2;
		} else if (arg1 instanceof XSDouble) {
			if (arg2 instanceof XSFloat || arg2 instanceof XSDecimal) {
				result[0] = arg1;
				result[1] = (AnyType)arg1.constructor(arg2);
			}
		} else if (arg1 instanceof XSFloat) {
			if (arg2 instanceof XSDouble) {
				result[0] = (AnyType)arg2.constructor(arg1);
				result[1] = arg2;
			} else if (arg2 instanceof XSDecimal) {
				result[0] = arg1;
				result[1] = (AnyType)arg1.constructor(arg2);
			}
		} else if (arg1 instanceof XSDecimal) {
			if (arg2 instanceof XSDouble || arg2 instanceof XSFloat) {
				result[0] = (AnyType)arg2.constructor(arg1);
				result[1] = arg2;
			}
		} else if (arg1 instanceof XSUntypedAtomic) {
			result[0] = (AnyType)arg2.constructor(arg1);
			result[1] = arg2;
		} else if (arg1 instanceof XSAnyURI) {
			if (arg2 instanceof XSString) {
				result[0] = (AnyType)arg2.constructor(arg1);
				result[1] = arg2;
			}
		} else if (arg2 instanceof XSAnyURI) {
			if (arg1 instanceof XSString) {
				result[0] = arg1;
				result[1] = (AnyType)arg1.constructor(arg2);
			}
		}

		if (result[0] == null || result[1] == null) {
			Class<? extends CtrType> commonParent = findCommonParent(arg1, arg2);
			if (commonParent != null && commonParent != CtrType.class) {
				CtrType instance = null;
				try {
					instance = commonParent.newInstance();
				} catch (InstantiationException ex) {
				} catch (IllegalAccessException ex) {
				}

				if (instance != null) {
					result[0] = (AnyType)instance.constructor(arg1);
					result[1] = (AnyType)instance.constructor(arg2);
				}
			}
		}

		if (result[0] == null || result[1] == null) {
			// couldn't promote types; leave as they were
			result[0] = arg1;
			result[1] = arg2;
		}

		return result;
	}

	private static Class<? extends CtrType> findCommonParent(CtrType arg1, CtrType arg2) {
		Class<?> type;
		for (type = arg2.getClass(); type != null; type = type.getSuperclass()) {
			if (type.isAssignableFrom(arg1.getClass())) {
				break;
			}
		}

		if (type != null && CtrType.class.isAssignableFrom(type)) {
			return type.asSubclass(CtrType.class);
		}

		return null;
	}
}
