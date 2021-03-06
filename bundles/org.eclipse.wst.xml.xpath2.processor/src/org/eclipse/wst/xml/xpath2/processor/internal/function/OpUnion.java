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

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Support for Union operation.
 */
public class OpUnion extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for OpUnion.
	 */
	public OpUnion() {
		super(new QName("union"), 2);
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

		return op_union(args);
	}

	/**
	 * Op-Union operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of operation.
	 */
	public static ResultSequence op_union(Collection<ResultSequence> args) throws DynamicError {
		ResultBuffer rs = new ResultBuffer();

		// convert arguments
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arguments
		Iterator<ResultSequence> iter = cargs.iterator();
		ResultSequence one = iter.next();
		ResultSequence two = iter.next();

		// XXX i don't fink u've ever seen anything lamer than this
		rs.concat(one);
		rs.concat(two);
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
