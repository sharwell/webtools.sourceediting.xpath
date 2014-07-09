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
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSUntypedAtomic;

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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return fs_eq_value(args, ec.getDynamicContext());
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

			if (rs.empty())
				return new ArrayList<Item>();

			if (rs.size() > 1)
				throw new DynamicError(TypeError.invalid_type(null));

			Item arg = rs.first();

			if (arg instanceof XSUntypedAtomic)
				arg = new XSString(arg.getStringValue());

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
	public static ResultSequence fs_eq_value(Collection<ResultSequence> args, DynamicContext context)
			throws DynamicError {
		CmpValueOp<CmpEq> op = new CmpValueOp<CmpEq>() {
			@Override
			public Class<? extends CmpEq> getType() {
				return CmpEq.class;
			}

			@Override
			public boolean execute(CmpEq obj, AnyType arg, DynamicContext context) throws DynamicError {
				return obj.eq(arg, context);
			}
		};
		return do_cmp_value_op(args, op, context);
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
	public static boolean fs_eq_fast(AnyType one, AnyType two, DynamicContext context)
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

		return cmpone.eq(two, context);
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
			CmpGeneralOp op, DynamicContext ec) throws DynamicError {

		// section 3.5.2

		// rule a
		// if one is untyped and other is numeric, cast untyped to
		// double
		if ((a instanceof XSUntypedAtomic && b instanceof NumericType)
				|| (b instanceof XSUntypedAtomic && a instanceof NumericType)) {
			if (a instanceof XSUntypedAtomic)
				a = new XSDouble(a.getStringValue());
			else
				b = new XSDouble(b.getStringValue());

		}

		// rule b
		// if one is untyped and other is string or untyped, then cast
		// untyped to string
		else if ((a instanceof XSUntypedAtomic
				&& (b instanceof XSString || b instanceof XSUntypedAtomic) || (b instanceof XSUntypedAtomic && (a instanceof XSString || a instanceof XSUntypedAtomic)))) {

			if (a instanceof XSUntypedAtomic)
				a = new XSString(a.getStringValue());
			if (b instanceof XSUntypedAtomic)
				b = new XSString(b.getStringValue());
		}

		// rule c
		// if one is untyped and other is not string,untyped,numeric
		// cast untyped to dynamic type of other

		// XXX?
		// TODO: This makes no sense as implemented before
		else if (a instanceof XSUntypedAtomic) {
//			ResultSequence converted = ResultSequenceFactory.create_new(a);
//			assert converted.size() == 1;
//			a = converted.first();
		} else if (b instanceof XSUntypedAtomic) {
//			ResultSequence converted = ResultSequenceFactory.create_new(b);
//			assert converted.size() == 1;
//			b = converted.first();
		}

		// rule d
		// if value comparison is true, return true.

		ResultSequence one = a;
		ResultSequence two = b;

		Collection<ResultSequence> args = new ArrayList<ResultSequence>();
		args.add(one);
		args.add(two);

		ResultSequence result = op.execute(args, ec);

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
	public static ResultSequence fs_eq_general(Collection<ResultSequence> args, DynamicContext dc)
			{
		CmpGeneralOp op = new CmpGeneralOp() {
			@Override
			public ResultSequence execute(Collection<ResultSequence> args, DynamicContext dynamicContext) throws DynamicError {
				return FsEq.fs_eq_value(args, dynamicContext);
			}
		};
		return do_cmp_general_op(args, op, dc);
	}

	public interface CmpGeneralOp {
		ResultSequence execute(Collection<ResultSequence> args, DynamicContext dynamicContext) throws DynamicError;
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
	public static ResultSequence do_cmp_general_op(Collection<ResultSequence> args, CmpGeneralOp op,
			DynamicContext dc) throws DynamicError {

		// sanity check args and get them
		if (args.size() != 2)
			throw DynamicError.throw_type_error();

		Iterator<ResultSequence> argiter = args.iterator();

		ResultSequence one = argiter.next();
		ResultSequence two = argiter.next();

		// XXX ?
		if (one.empty() || two.empty())
			return XSBoolean.FALSE;

		// atomize
		one = FnData.atomize(one);
		two = FnData.atomize(two);

		// we gotta find a pair that satisfied the condition
		for (Iterator<Item> i = one.iterator(); i.hasNext();) {
			AnyType a = (AnyType) i.next();
			for (Iterator<Item> j = two.iterator(); j.hasNext();) {
				AnyType b = (AnyType) j.next();

				if (do_general_pair(a, b, op, dc))
					return XSBoolean.TRUE;
			}
		}

		return XSBoolean.FALSE;
	}

	public interface CmpValueOp<T> {
		Class<? extends T> getType();

		boolean execute(T obj, AnyType arg, DynamicContext dynamicContext) throws DynamicError;
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
	public static <T> ResultSequence do_cmp_value_op(Collection<ResultSequence> args, CmpValueOp<T> op,
			DynamicContext context) throws DynamicError {

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

		if (!(op.getType().isInstance(arg)))
			throw DynamicError.throw_type_error();

		boolean cmpres = op.execute(op.getType().cast(arg), (AnyType)arg2.first(), context);
		return XSBoolean.valueOf(cmpres);
	}
}
