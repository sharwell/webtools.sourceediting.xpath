/*******************************************************************************
 * Copyright (c) 2005, 2012 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 274805 - improvements to xs:integer data type 
 *     Jesper Moller - bug 281028 - fix promotion rules for fn:sum
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *    Lukasz Wycisk - bug 361060 - Aggregations with nil=’true’ throw exceptions.
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.ScalarTypePromoter;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.TypePromoter;

/**
 * Returns a value obtained by adding together the values in $arg. If the
 * single-argument form of the function is used, then the value returned for an
 * empty sequence is the xs:integer value 0. If the two-argument form is used,
 * then the value returned for an empty sequence is the value of the $zero
 * argument.
 */
public class FnSum extends Function {

	private static final XSInteger ZERO = new XSInteger(BigInteger.ZERO);

	/**
	 * Constructor for FnSum.
	 */
	public FnSum() {
		super(new QName("sum"), 1, 2);
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
		Iterator<ResultSequence> argIterator = args.iterator();
		ResultSequence argSequence = argIterator.next();
		ResultSequence zero = ZERO;
		if (argIterator.hasNext()) {
			ResultSequence zeroSequence = argIterator.next();
			if (argSequence.empty()) {
				return zeroSequence;
			}

			if (zeroSequence.size() > 1)
				throw new DynamicError(TypeError.invalid_type(null));
			if (zeroSequence.size() == 1 && ! (zeroSequence.first() instanceof AnyAtomicType))
				throw new DynamicError(TypeError.invalid_type(zeroSequence.first().getStringValue()));
			zero = zeroSequence;
		}
		return sum(argSequence, zero, evaluationContext);
	}

	/**
	 * Sum operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:sum operation.
	 */
	public static ResultSequence sum(ResultSequence arg, ResultSequence zeroSequence, EvaluationContext evaluationContext) throws DynamicError {
		if (arg.empty())
			return zeroSequence;

		MathPlus total = null;

		TypePromoter tp = new ScalarTypePromoter();
		tp.considerSequence(arg);

		for (Iterator<Item> i = arg.iterator(); i.hasNext();) {
			Item item = i.next();
			AnyAtomicType conv = tp.promote((AnyType) item);
			if(conv == null){
				throw DynamicError.argument_type_error(item.getClass());
			}
			
			if (conv instanceof XSDouble && ((XSDouble)conv).nan() || conv instanceof XSFloat && ((XSFloat)conv).nan()) {
				return tp.promote(new XSFloat(Float.NaN));
			}
			if (total == null) {
				total = (MathPlus)conv; 
			} else {
				total = (MathPlus)total.plus(conv, evaluationContext).first();
			}
		}
		
		return (AnyType) total;
	}
}
