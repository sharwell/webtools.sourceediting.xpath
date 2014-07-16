/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver - STAR - bug 262765 - fixed implementation of fn:local-name according to spec.  
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 * Returns the local part of the name of $arg as an xs:string that will either
 * be the zero-length string or will have the lexical form of an xs:NCName.
 */
public class FnLocalName extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnLocalName.
	 */
	public FnLocalName() {
		super(new QName("local-name"), 0, 1);
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
		return local_name(args, ec);
	}

	/**
	 * Local-Name operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:local-name operation.
	 */
	public static ResultSequence local_name(Collection<ResultSequence> args, EvaluationContext context)
			throws DynamicError {

		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arg
		ResultSequence arg1 = null;
		
		if (cargs.isEmpty()) {
			if (context.getContextItem() == null)
				throw DynamicError.contextUndefined();
			else {
				arg1 = (AnyType) context.getContextItem();
			}
		} else {
			arg1 = cargs.iterator().next();

		}
		
		if (arg1.empty()) {
			return new XSString("");
		}

		if (!(arg1 instanceof NodeType)) {
			throw DynamicError.invalidType();
		}

		NodeType an = (NodeType) arg1.first();
		

		QName name = an.node_name();

		String sname = "";
		if (name != null)
			sname = name.local();

		return new XSString(sname);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(NodeType.class, SeqType.OCC_QMARK);
			_expected_args.add(arg);
		}

		return _expected_args;
	}
}
