/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpEq;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpGt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.CmpLt;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathDiv;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathIDiv;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathMinus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathMod;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathPlus;
import org.eclipse.wst.xml.xpath2.processor.internal.function.MathTimes;

/**
 * A representation of the NumericType datatype
 */
public abstract class NumericType extends CtrType

implements CmpEq, CmpGt, CmpLt,

MathPlus, MathMinus, MathTimes, MathDiv, MathIDiv, MathMod {

	// XXX needed for fn:boolean
	/**
	 * Check whether node represnts 0
	 * 
	 * @return True if node represnts 0. False otherwise
	 */
	public abstract boolean zero();

	/**
	 * Creates a new ResultSequence representing the negation of the number
	 * stored
	 * 
	 * @return New ResultSequence representing the negation of the number stored
	 */
	public abstract ResultSequence unary_minus();

	// numeric functions
	/**
	 * Absolutes the number stored
	 * 
	 * @return New NumericType representing the absolute of the number stored
	 */
	public abstract NumericType abs();

	/**
	 * Returns the smallest integer greater than the number stored
	 * 
	 * @return A NumericType representing the smallest integer greater than the
	 *         number stored
	 */
	public abstract NumericType ceiling();

	/**
	 * Returns the largest integer smaller than the number stored
	 * 
	 * @return A NumericType representing the largest integer smaller than the
	 *         number stored
	 */
	public abstract NumericType floor();

	/**
	 * Returns the closest integer of the number stored.
	 * 
	 * @return A NumericType representing the closest long of the number stored.
	 */
	public abstract NumericType round();

	/**
	 * Returns the closest integer of the number stored.
	 * 
	 * @return A NumericType representing the closest long of the number stored.
	 */
	public final NumericType round_half_to_even() {
		return round_half_to_even(0);
	}
	
	public abstract NumericType round_half_to_even(int precision);

	protected Item get_single_arg(ResultSequence rs) throws DynamicError {
		if (rs.size() != 1)
			throw DynamicError.throw_type_error();

		return rs.first();
	}

	/***
	 * Check whether the supplied node is of the supplied type
	 * 
	 * @param at
	 *            The node being tested
	 * @param type
	 *            The type expected
	 * @return The node being tested
	 * @throws DynamicError
	 *             If node being tested is not of expected type
	 */
	public static <T extends AnyType> T get_single_type(Item at, Class<T> type) throws DynamicError {
		try {
			return type.cast(at);
		} catch (ClassCastException ex) {
			throw DynamicError.throw_type_error();
		}
	}

	public static <T extends AnyType> T get_single_type(AnyType at, Class<T> type) throws DynamicError {
		return get_single_type((Item)at, type);
	}

	/***
	 * Check whether first node in supplied ResultSequence is of the supplied
	 * type
	 * 
	 * @param rs
	 *            The node being tested
	 * @param type
	 *            The type expected
	 * @return The node being tested
	 * @throws DynamicError
	 *             If node being tested is not of expected type
	 */
	public static <T extends AnyType> T get_single_type(ResultSequence rs, Class<T> type) throws DynamicError {
		if (rs.size() != 1)
			throw DynamicError.throw_type_error();

		return get_single_type(rs.first(), type);
	}

	@Override
	public final ResultSequence times(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.size() == 1) {
			Item first = arg.first();
			if (first instanceof XSDuration && first instanceof MathTimes) {
				// This is a special case for xs:dayTimeDuration and xs:yearMonthDuration
				// https://www.w3.org/TR/xpath20/#mapping
				return ((MathTimes)first).times(this, evaluationContext);
			}
		}

		return timesImpl(arg, evaluationContext);
	}

	protected abstract ResultSequence timesImpl(ResultSequence arg, EvaluationContext evaluationContext) throws DynamicError;
}
