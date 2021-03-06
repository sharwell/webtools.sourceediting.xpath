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

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Returns an xs:boolean indicating whether the argument node is "nilled". If
 * the argument is not an element node, returns the empty sequence.
 */
public class FnNilled extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnNilled.
	 */
	public FnNilled() {
		super(new QName("nilled"), 1);
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
		return nilled(args);
	}

	/**
	 * Returns an {@code xs:boolean} indicating whether the argument node is
	 * "nilled". If the argument is not an element node, returns the empty
	 * sequence. If the argument is the empty sequence, returns the empty
	 * sequence.
	 *
	 * <pre>
	 * fn:nilled($arg as node()?) as xs:boolean?
	 * </pre>
	 *
	 * @param args Result from the expressions evaluation.
	 * @throws DynamicError Dynamic error.
	 * @return Result of fn:nilled operation.
	 */
	public static ResultSequence nilled(Collection<ResultSequence> args) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultSequence arg1 = cargs.iterator().next();
		if (arg1.empty()) {
			return arg1;
		}

		NodeType nt = (NodeType) arg1.first();
		return nt.nilled();
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			_expected_args.add(new SeqType(NodeType.class, SeqType.OCC_QMARK));
		}

		return _expected_args;
	}
}
