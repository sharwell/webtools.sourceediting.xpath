/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 276134 - improvements to schema aware primitive type support
 *                                 for attribute/element nodes 
 *     David Carver - bug 262765 - fixed comparison on sequence range values.
 *     Jesper S Moller - bug 283214 - fix eq for untyped atomic values
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     Jesper Steen Moeller - bug 280555 - Add pluggable collation support
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDayTimeDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSYearMonthDuration;

/**
 * Class for the Equality function.
 */
public class FsEq extends Function {

	/**
	 * Constructor for FsEq.
	 */
	public FsEq() {
		super(new QName("eq"), 2);
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

		return fs_eq_value(args, evaluationContext);
	}

	/**
	 * Converts arguments to values.
	 * 
	 * @param args
	 *            Result from expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of conversion.
	 */
	private static Collection<Item> value_convert_args(Collection<ResultSequence> args)
			throws DynamicError {
		Collection<Item> result = new ArrayList<Item>(args.size());

		// atomize arguments
		for (ResultSequence rs : args) {
			//FnData.fast_atomize(rs);
			rs = FnData.atomize(rs);
//			rs = FsConvertOperand.convert_operand(Arrays.<ResultSequence>asList(rs, new XSString()));

			if (rs.empty())
				return new ArrayList<Item>();

			if (rs.size() > 1)
				throw new DynamicError(TypeError.invalid_type(null));

			Item arg = rs.first();

//			if (arg instanceof XSUntypedAtomic)
//				arg = new XSString(arg.getStringValue());

			result.add(arg);
		}

		return result;
	}

	/**
	 * Conversion operation for the values of the arguments.
	 * 
	 * @param args
	 *            Result from convert value operation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of conversion.
	 */
	public static ResultSequence fs_eq_value(Collection<ResultSequence> args, EvaluationContext evaluationContext)
			throws DynamicError {
		CmpValueOp<CmpEq> op = new CmpValueOp<CmpEq>() {
			@Override
			public Class<? extends CmpEq> getType() {
				return CmpEq.class;
			}

			@Override
			public boolean execute(CmpEq obj, AnyType arg, EvaluationContext evaluationContext) throws DynamicError {
				return obj.eq(arg, evaluationContext);
			}
		};
		return do_cmp_value_op(args, op, evaluationContext);
	}

	/**
	 * A fast Equality operation, no conversion for the inputs performed.
	 * 
	 * @param one
	 *            input1 of any type.
	 * @param two
	 *            input2 of any type.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of Equality operation.
	 */
	public static boolean fs_eq_fast(AnyType one, AnyType two, EvaluationContext evaluationContext)
			throws DynamicError {

		one = FnData.atomize((Item)one);
		two = FnData.atomize((Item)two);

		if (one instanceof XSUntypedAtomic)
			one = new XSString(one.getStringValue());

		if (two instanceof XSUntypedAtomic)
			two = new XSString(two.getStringValue());

		if (!(one instanceof CmpEq))
			throw DynamicError.throw_type_error();

		CmpEq cmpone = (CmpEq) one;

		return cmpone.eq(two, evaluationContext);
	}

	/**
	 * Making sure that the types are the same before comparing the inputs.
	 * 
	 * @param a
	 *            input1 of any type.
	 * @param b
	 *            input2 of any type.
	 * @param dc
	 *              Dynamic Context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of Equality operation.
	 */
	private static boolean do_general_pair(AnyType a, AnyType b,
			CmpGeneralOp op, EvaluationContext evaluationContext) throws DynamicError {

		if (a instanceof XSUntypedAtomic && b instanceof XSUntypedAtomic) {
			a = new XSString(a.getStringValue());
			b = new XSString(b.getStringValue());
		} else if (a instanceof XSUntypedAtomic) {
			if (b instanceof NumericType) {
				a = (XSDouble)new XSDouble().constructor(new XSString(a.getStringValue()));
			} else if (b instanceof XSDayTimeDuration) {
				a = (XSDayTimeDuration)new XSDayTimeDuration().constructor(new XSString(a.getStringValue()));
			} else if (b instanceof XSYearMonthDuration) {
				a = (XSYearMonthDuration)new XSYearMonthDuration().constructor(new XSString(a.getStringValue()));
			} else {
				a = (AnyType)((CtrType)b).constructor(new XSString(a.getStringValue()));
			}
		} else if (b instanceof XSUntypedAtomic) {
			if (a instanceof NumericType) {
				b = (XSDouble)new XSDouble().constructor(new XSString(b.getStringValue()));
			} else if (a instanceof XSDayTimeDuration) {
				b = (XSDayTimeDuration)new XSDayTimeDuration().constructor(new XSString(b.getStringValue()));
			} else if (a instanceof XSYearMonthDuration) {
				b = (XSYearMonthDuration)new XSYearMonthDuration().constructor(new XSString(b.getStringValue()));
			} else {
				b = (AnyType)((CtrType)a).constructor(new XSString(b.getStringValue()));
			}
		}

		ResultSequence one = a;
		ResultSequence two = b;

		Collection<ResultSequence> args = new ArrayList<ResultSequence>();
		args.add(one);
		args.add(two);

//		ResultSequence arg1 = FsConvertOperand.convert_operand(Arrays.<ResultSequence>asList(a, b));
//		ResultSequence arg2 = FsConvertOperand.convert_operand(Arrays.<ResultSequence>asList(b, a));
//		Collection<ResultSequence> args = Arrays.<ResultSequence>asList(a, b);

		ResultSequence result = op.execute(args, evaluationContext);

		if (((XSBoolean) result.first()).value())
			return true;

		return false;
	}

	/**
	 * A general equality function.
	 * 
	 * @param args
	 *            input arguments.
	 * @param dc
	 *         Dynamic context 
	 * @return Result of general equality operation.
	 */
	public static ResultSequence fs_eq_general(Collection<ResultSequence> args, EvaluationContext evaluationContext) {
		CmpGeneralOp op = new CmpGeneralOp() {
			@Override
			public ResultSequence execute(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
				return FsEq.fs_eq_value(args, evaluationContext);
			}
		};
		return do_cmp_general_op(args, op, evaluationContext);
	}

	public interface CmpGeneralOp {
		ResultSequence execute(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError;
	}

	// voodoo 3
	/**
	 * Actual equality operation for fs_eq_general.
	 * 
	 * @param args
	 *            input arguments.
	 * @param type
	 *            type of the arguments.
	 * @param mname
	 *            Method name for template simulation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static ResultSequence do_cmp_general_op(Collection<ResultSequence> args, CmpGeneralOp op, EvaluationContext evaluationContext) throws DynamicError {

		// sanity check args and get them
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Iterator<ResultSequence> argiter = args.iterator();

		ResultSequence one = argiter.next();
		ResultSequence two = argiter.next();

		// XXX ?
		if (one.empty() || two.empty())
			return XSBoolean.FALSE;

		// apply fn:data to each of the arguments
		one = FnData.atomize(one);
		two = FnData.atomize(two);

		// we gotta find a pair that satisfied the condition
		for (Iterator<Item> i = one.iterator(); i.hasNext();) {
			AnyType a = (AnyType) i.next();
			for (Iterator<Item> j = two.iterator(); j.hasNext();) {
				AnyType b = (AnyType) j.next();

				if (do_general_pair(a, b, op, evaluationContext))
					return XSBoolean.TRUE;
			}
		}

		return XSBoolean.FALSE;
	}

	public interface CmpValueOp<T> {
		Class<? extends T> getType();

		boolean execute(T obj, AnyType arg, EvaluationContext evaluationContext) throws DynamicError;
	}

	// voodoo 2
	/**
	 * Actual equality operation for fs_eq_value.
	 * 
	 * @param args
	 *            input arguments.
	 * @param type
	 *            type of the arguments.
	 * @param mname
	 *            Method name for template simulation.
	 * @param dynamicContext 
	 *             Dynamic error.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of the operation.
	 */
	public static <T> ResultSequence do_cmp_value_op(Collection<ResultSequence> args, CmpValueOp<T> op, EvaluationContext evaluationContext) throws DynamicError {

		// sanity check args + convert em
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Collection<Item> cargs = value_convert_args(args);

		if (cargs.size() == 0)
			return ResultBuffer.EMPTY;

		// make sure arugments are comparable by equality
		Iterator<Item> argi = cargs.iterator();
		Item arg = ((ResultSequence) argi.next()).first();
		ResultSequence arg2 = (ResultSequence) argi.next();

		if (arg2.size() != 1)
			throw DynamicError.throw_type_error();

		AnyType[] promoted = promoteValueComparisonOperands((CtrType)arg, (CtrType)arg2.first());
		if (promoted[0].getClass() != promoted[1].getClass()) {
//			throw DynamicError.cant_cast(String.format("Cannot apply value comparison to arguments of type %s and %s", ((CtrType)arg).string_type(), ((CtrType)arg2.first()).string_type()));
			throw DynamicError.invalidType();
		}

		arg = promoted[0];
		arg2 = promoted[1];

		if (!(op.getType().isInstance(arg)))
			throw DynamicError.throw_type_error();

		boolean cmpres = op.execute(op.getType().cast(arg), (AnyType)arg2.first(), evaluationContext);
		return XSBoolean.valueOf(cmpres);
	}

//	private static AnyType[] promoteGeneralComparisonOperands(CtrType arg1, CtrType arg2) {
//		if (arg1 instanceof XSUntypedAtomic && arg2 instanceof XSUntypedAtomic) {
//			return new AnyType[] { new XSString(arg1.getStringValue()), new XSString(arg2.getStringValue()) };
//		}
//
//		if (arg1.getClass() == arg2.getClass()) {
//			// trivial case
//			return new AnyType[] { arg1, arg2 };
//		}
//
//		if (arg2 instanceof XSUntypedAtomic) {
//			AnyType[] swappedResult = promoteGeneralComparisonOperands(arg2, arg1);
//			AnyType tmp = swappedResult[0];
//			swappedResult[0] = swappedResult[1];
//			swappedResult[1] = tmp;
//			return swappedResult;
//		}
//
//		AnyType[] result = new AnyType[2];
//		if (arg1.getClass().isAssignableFrom(arg2.getClass())) {
//			result[0] = arg1;
//			result[1] = (AnyType)arg1.constructor(arg2);
//		} else if (arg2.getClass().isAssignableFrom(arg1.getClass())) {
//			result[0] = (AnyType)arg2.constructor(arg1);
//			result[1] = arg2;
//		} else if (arg1 instanceof XSDouble) {
//			if (arg2 instanceof XSFloat || arg2 instanceof XSDecimal) {
//				result[0] = arg1;
//				result[1] = (AnyType)arg1.constructor(arg2);
//			}
//		} else if (arg1 instanceof XSFloat) {
//			if (arg2 instanceof XSDouble) {
//				result[0] = (AnyType)arg2.constructor(arg1);
//				result[1] = arg2;
//			} else if (arg2 instanceof XSDecimal) {
//				result[0] = arg1;
//				result[1] = (AnyType)arg1.constructor(arg2);
//			}
//		} else if (arg1 instanceof XSDecimal) {
//			if (arg2 instanceof XSDouble || arg2 instanceof XSFloat) {
//				result[0] = (AnyType)arg2.constructor(arg1);
//				result[1] = arg2;
//			}
//		} else if (arg1 instanceof XSUntypedAtomic) {
//			result[0] = (AnyType)arg2.constructor(arg1);
//			result[1] = arg2;
//		} else if (arg1 instanceof XSAnyURI) {
//			if (arg2 instanceof XSString) {
//				result[0] = (AnyType)arg2.constructor(arg1);
//				result[1] = arg2;
//			}
//		} else if (arg2 instanceof XSAnyURI) {
//			if (arg1 instanceof XSString) {
//				result[0] = arg1;
//				result[1] = (AnyType)arg1.constructor(arg2);
//			}
//		}
//
//		if (result[0] == null || result[1] == null) {
//			Class<? extends CtrType> commonParent = findCommonParent(arg1, arg2);
//			if (commonParent != null && commonParent != CtrType.class) {
//				CtrType instance = null;
//				try {
//					instance = commonParent.newInstance();
//				} catch (InstantiationException ex) {
//				} catch (IllegalAccessException ex) {
//				}
//
//				if (instance != null) {
//					result[0] = (AnyType)instance.constructor(arg1);
//					result[1] = (AnyType)instance.constructor(arg2);
//				}
//			}
//		}
//
//		if (result[0] == null || result[1] == null) {
//			// couldn't promote types; leave as they were
//			result[0] = arg1;
//			result[1] = arg2;
//		}
//
//		return result;
//	}

	private static AnyType[] promoteValueComparisonOperands(CtrType arg1, CtrType arg2) {
		if (arg1 instanceof XSUntypedAtomic) {
			arg1 = new XSString(arg1.getStringValue());
		}

		if (arg2 instanceof XSUntypedAtomic) {
			arg2 = new XSString(arg2.getStringValue());
		}

		if (arg1.getClass() == arg2.getClass()) {
			// trivial case
			return new AnyType[] { arg1, arg2 };
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
