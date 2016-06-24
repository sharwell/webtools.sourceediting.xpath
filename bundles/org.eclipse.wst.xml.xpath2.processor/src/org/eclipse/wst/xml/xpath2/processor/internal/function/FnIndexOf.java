/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Moller - bug 280555 - Add pluggable collation support
 *     David Carver (STAR) - bug 262765 - fixed collation and comparison issues.
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDuration;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Returns a sequence of positive integers giving the positions within the
 * sequence $seqParam of items that are equal to $srchParam.
 */
public class FnIndexOf extends AbstractCollationEqualFunction {
	
	private static Collection<SeqType> _expected_args = null;
	
	/**
	 * Constructor for FnIndexOf.
	 */
	public FnIndexOf() {
		super(new QName("index-of"), 2, 3);
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
		return index_of(args, ec);
	}

	/**
	 * Obtain a comparable type.
	 * 
	 * @param at
	 *            expression of any type.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	private static CmpEq get_comparable(AnyType at) throws DynamicError {
		if (at instanceof NodeType) {
			XSString nodeString = new XSString(at.getStringValue());
			return nodeString;
		}
		
		if (!(at instanceof AnyAtomicType))
			throw DynamicError.throw_type_error();
		
		if (!(at instanceof CmpEq))
			throw DynamicError.not_cmp(null);

		return (CmpEq) at;
	}

	/**
	 * Index-Of operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param evaluationContext
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:index-of operation.
	 */
	public static ResultSequence index_of(Collection<ResultSequence> args, EvaluationContext evaluationContext) {
		Function.convert_arguments(args, expected_args());

		// get args
		Iterator<ResultSequence> citer = args.iterator();
		ResultSequence arg1 = citer.next();
		ResultSequence arg2 = citer.next();
		
		if (arg1.empty()) {
			return ResultBuffer.EMPTY;
		}

		// sanity chex
		if (arg2.size() != 1)
			throw DynamicError.throw_type_error();
		
		String collationUri = evaluationContext.getDynamicContext().getCollationProvider().getDefaultCollation();
		if (citer.hasNext()) {
			ResultSequence arg3 = citer.next();
			if (!arg3.empty()) {
				XSString collation = (XSString) arg3.first();
				collationUri = collation.getStringValue();
			}
		}

		ResultBuffer rb = new ResultBuffer();
		AnyAtomicType at = (AnyAtomicType)arg2.first();

		get_comparable(at);

		int index = 1;

		for (Iterator<Item> i = arg1.iterator(); i.hasNext();) {
			AnyType cmptype = (AnyType) i.next();
			get_comparable(cmptype);

			if (!(at instanceof CmpEq))
				continue;
			
			if (isBoolean(cmptype, at)) {
				XSBoolean boolat = (XSBoolean) cmptype;
				if (boolat.eq(at, evaluationContext)) {
 				   rb.add(new XSInteger(BigInteger.valueOf(index)));
				}
			} else 
			
			if (isNumeric(cmptype, at)) {
				NumericType numericat = (NumericType) at;
				if (numericat.eq(cmptype, evaluationContext)) {
					rb.add(new XSInteger(BigInteger.valueOf(index)));
				}
			} else
			
			if (isDuration(cmptype, at)) {
				XSDuration durat = (XSDuration) at;
				if (durat.eq(cmptype, evaluationContext)) {
					rb.add(new XSInteger(BigInteger.valueOf(index)));
				}
			} else
				
			if (at instanceof QName && cmptype instanceof QName ) {
				QName qname = (QName)at;
				if (qname.eq(cmptype, evaluationContext)) {
					rb.add(new XSInteger(BigInteger.valueOf(index)));
				}
			} else 
			
			if (needsStringComparison(cmptype, at)) {
				XSString xstr1 = new XSString(cmptype.getStringValue());
				XSString itemStr = new XSString(at.getStringValue());
				if (FnCompare.compare_string(collationUri, xstr1, itemStr, evaluationContext).equals(BigInteger.ZERO)) {
					rb.add(new XSInteger(BigInteger.valueOf(index)));
				}
			} 
			
			index++;
		}

		return rb.getSequence();
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(AnyType.class, SeqType.OCC_STAR);
			_expected_args.add(arg);
			_expected_args.add(new SeqType(AnyAtomicType.class, SeqType.OCC_NONE));
			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_NONE));
			_expected_args.add(arg);
		}

		return _expected_args;
	}
	
}
