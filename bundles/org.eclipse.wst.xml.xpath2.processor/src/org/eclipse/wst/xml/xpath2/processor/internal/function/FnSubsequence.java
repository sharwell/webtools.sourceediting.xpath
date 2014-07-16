/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver - bug 262765 - eased restriction on data type...convert numerics to XSDouble.
 *     Jesper S Moller - bug 285806 - fixed fn:subsequence for indexes starting before 1
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Mukul Gandhi - bug 338999 - improving compliance of function 'fn:subsequence'. implementing full arity support.
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NumericType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Returns the contiguous sequence of items in the value of $sourceSeq beginning
 * at the position indicated by the value of $startingLoc and continuing for the
 * number of items indicated by the value of $length. More specifically, returns
 * the items in $sourceString whose position $p obeys: - fn:round($startingLoc)
 * <= $p < fn:round($startingLoc) + fn:round($length)
 */
public class FnSubsequence extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnSubsequence.
	 */
	public FnSubsequence() {
		super(new QName("subsequence"), 2, 3);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		return subsequence(args);
	}

	/**
	 * Subsequence operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:subsequence operation.
	 */
	public static ResultSequence subsequence(Collection<ResultSequence> args) throws DynamicError {		
		ResultBuffer rs = new ResultBuffer();

		List<ResultSequence> convertedArgs = convert_arguments(args, expected_args());

		// get args
		ResultSequence sourceSeq = convertedArgs.get(0);
		XSDouble startingLoc = (XSDouble)convertedArgs.get(1).first();
		XSDouble length = convertedArgs.size() >= 3 ? (XSDouble)(!convertedArgs.get(2).empty() ? convertedArgs.get(2).first() : null) : null;

		if (sourceSeq.empty())
			return ResultBuffer.EMPTY;

		int start = (int)((XSDouble)FnRound.fn_round(startingLoc)).double_value() - 1;

		int end;
		if (length == null) {
			end = sourceSeq.size();
		} else {
			int lengthValue = (int)((XSDouble)FnRound.fn_round(length)).double_value();
			end = start + lengthValue;
		}

		start = Math.max(start, 0);
		end = Math.min(end, sourceSeq.size());

		if (start == 0 && end == sourceSeq.size()) {
			return sourceSeq;
		} else if (end <= start) {
			return ResultBuffer.EMPTY;
		}

		ResultBuffer result = new ResultBuffer();
		int index = 0;
		for (Iterator<Item> seqIter = sourceSeq.iterator(); seqIter.hasNext(); ) {
			Item item = seqIter.next();
			if (index >= start && index < end) {
				result.add(item);
			}

			index++;
		}

		return result.getSequence();
//		XSDouble startLoc = (XSDouble)convert_argument(citer.next(), new SeqType(XSDouble.class, SeqType.OCC_NONE));
//		ResultSequence length = convert_argument(citer.next(), new SeqType(XSDouble.class, SeqType.OCC_QMARK));
//
//		XSDouble rounded = (XSDouble)FnRound.fn_round(startLoc);
//		int start = (int)rounded.double_value();
//		int end = Integer.MAX_VALUE;
//		if (!length.empty()) {
//			end = (int)((XSDouble)FnRound.fn_round(ResultBuffer.wrap(length.first()))).double_value();
//		}
//
//		int pos = 1; // index running parallel to the iterator
//		for (Iterator<Item> seqIter = seq.iterator(); seqIter.hasNext();) {
//			Item item = seqIter.next();
//			if (start <= pos && pos < end) {
//				rs.add(item);
//			}
//
//			pos++;
//		}
//		
//		return rs.getSequence();
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(new XSString(), SeqType.OCC_QMARK);
			_expected_args.add(new SeqType(AnyType.class, SeqType.OCC_STAR));
			_expected_args.add(new SeqType(new XSDouble(), SeqType.OCC_NONE));
			_expected_args.add(new SeqType(new XSDouble(), SeqType.OCC_QMARK));
		}

		return _expected_args;
	}

}
