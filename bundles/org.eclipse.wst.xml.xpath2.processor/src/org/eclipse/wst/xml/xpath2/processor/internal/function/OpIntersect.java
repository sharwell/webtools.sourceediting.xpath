/*******************************************************************************
 * Copyright (c) 2005, 2011 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Jesper Steen Moller  - bug 316988 - Removed O(n^2) performance for large results
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Support for Intersect operation.
 */
public class OpIntersect extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for OpIntersect.
	 */
	public OpIntersect() {
		super(new QName("intersect"), 2);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, org.eclipse.wst.xml.xpath2.api.EvaluationContext ec) throws DynamicError {
		assert args.size() >= min_arity() && args.size() <= max_arity();

		return op_intersect(args);
	}

	/**
	 * Op-Intersect operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	public static ResultSequence op_intersect(Collection<ResultSequence> args)
			throws DynamicError {
		// convert arguments
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arguments
		Iterator<ResultSequence> iter = cargs.iterator();
		ResultSequence one = iter.next();
		ResultSequence two = iter.next();

		Set<Item> leftSide = new HashSet<Item>();
		for (int i = 0; i < one.size();  ++i) {
			leftSide.add(one.item(i));
		}
		TreeSet<NodeType> inBoth = new TreeSet<NodeType>(NodeType.NODE_COMPARATOR);
		for (int j = 0; j < two.size();  ++j) {
			if (leftSide.contains(two.item(j))) {
				// insert AND sort
				inBoth.add((NodeType)two.item(j));
			}
		}
		return new ResultBuffer().concat(inBoth).getSequence();
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();

			SeqType st = new SeqType(SeqType.OCC_STAR);

			_expected_args.add(st);
			_expected_args.add(st);
		}
		return _expected_args;
	}
}
