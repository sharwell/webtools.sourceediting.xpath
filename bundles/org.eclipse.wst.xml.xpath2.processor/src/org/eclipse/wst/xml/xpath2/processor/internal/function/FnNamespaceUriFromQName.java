/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Mukul Gandhi - bug 361721 - fixes to fn:namespace-uri-from-QName function
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;

/**
 * Returns the namespace URI for $arg as an xs:string. If $arg is the empty
 * sequence, the empty sequence is returned. If $arg is in no namespace, the
 * zero-length string is returned.
 */
public class FnNamespaceUriFromQName extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnNamespaceUriFromQName.
	 */
	public FnNamespaceUriFromQName() {
		super(new QName("namespace-uri-from-QName"), 1);
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
		return namespace(args, ec.getStaticContext());
	}

	/**
	 * Namespace-uri-from-QName operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:namespace-uri-from-QName operation.
	 */
	public static ResultSequence namespace(Collection<ResultSequence> args, StaticContext staticContext) throws DynamicError {

		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arg
		ResultSequence arg1 = cargs.iterator().next();

		if (arg1.empty())
			return ResultBuffer.EMPTY;

		QName qname = (QName) arg1.first();
		if (qname.namespace() == null) {
		   qname = QName.parse_QName(qname.string_value());
		}

		String ns = qname.namespace();

		if (ns == null) {
		   // attempt to find namespace name of QName value, by looking into the dynamic context
		   String prefix = qname.prefix();
		   if (prefix != null && !"".equals(prefix)) {
			  ns = staticContext.getNamespaceContext().getNamespaceURI(prefix);
		   }
		   else {
			  ns = staticContext.getDefaultNamespace();
		   }
		}
		
		if (ns == null) {
		   ns = "";
		}
		
		return new XSAnyURI(ns);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(new QName(), SeqType.OCC_QMARK);
			_expected_args.add(arg);
		}

		return _expected_args;
	}
}
