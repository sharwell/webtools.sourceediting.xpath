/*******************************************************************************
 * Copyright (c) 2005, 2012 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Moller - bug 280555 - Add pluggable collation support
 *     David Carver (STAR) - bug 262765 - fixed promotion issue 
 *     Jesper Moller - bug 281028 - fix promotion rules for fn:min
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *    Lukasz Wycisk - bug 361060 - Aggregations with nil=’true’ throw exceptions.
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.ComparableTypePromoter;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.TypePromoter;

/**
 * selects an item from the input sequence $arg whose value is less than or
 * equal to the value of every other item in the input sequence. If there are
 * two or more such items, then the specific item whose value is returned is
 * implementation independent.
 */
public class FnMin extends Function {
	/**
	 * Constructor for FnMin.
	 */
	public FnMin() {
		super(new QName("min"), 1, 2);
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
		return min(args, ec);
	}

	/**
	 * Min operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param dynamic 
	 *            Dynamic context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:min operation.
	 */
	public static ResultSequence min(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		ResultSequence arg = FnMax.get_arg(args, CmpLt.class);
		Comparator<String> collation = FnMax.getCollation(args, evaluationContext);
		if (arg.empty())
			return ResultBuffer.EMPTY;

		CmpLt max = null;

		TypePromoter tp = new ComparableTypePromoter();
		tp.considerSequence(arg);

		boolean nan = false;
		for (Iterator<Item> i = arg.iterator(); i.hasNext();) {
			Item item = i.next();
			AnyAtomicType conv = tp.promote((AnyType) item);
			
			if( !nan && conv != null ){
				
				if (conv instanceof XSDouble && ((XSDouble)conv).nan() || conv instanceof XSFloat && ((XSFloat)conv).nan()) {
					nan = true;
				}

				if (max == null) {
					max = (CmpLt)conv;
					continue;
				}

				boolean lt;
				if (conv instanceof XSString || conv instanceof XSAnyURI) {
					lt = collation.compare(conv.getStringValue(), ((AnyType)max).getStringValue()) < 0;
				} else {
					lt = ((CmpLt)conv).lt((AnyType)max, evaluationContext);
				}

				if (lt) {
					max = (CmpLt)conv;
				}
			}
		}

		if (nan) {
			return tp.promote(new XSFloat(Float.NaN));
		}

		return (AnyType) max;
	}

}
