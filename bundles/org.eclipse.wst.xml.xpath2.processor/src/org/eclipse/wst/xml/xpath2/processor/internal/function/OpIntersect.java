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
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
	@Override
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
		ResultBuffer rs = new ResultBuffer();

		// convert arguments
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arguments
		Iterator<ResultSequence> iter = cargs.iterator();
		ResultSequence one = iter.next();
		ResultSequence two = iter.next();

		// XXX lame
		for (Iterator<Item> i = one.iterator(); i.hasNext();) {
			NodeType node = (NodeType) i.next();
			boolean found = false;

			// death
			for (Iterator<Item> j = two.iterator(); j.hasNext();) {
				NodeType node2 = (NodeType) j.next();

				if (node.node_value() == node2.node_value()) {
					found = true;
					break;
				}

			}
			if (found)
				rs.add(node);
		}
		rs = NodeType.linarize(rs);

		return rs.getSequence();
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();

			SeqType st = new SeqType(NodeType.class, SeqType.OCC_STAR);

			_expected_args.add(st);
			_expected_args.add(st);
		}
		return _expected_args;
	}
}
