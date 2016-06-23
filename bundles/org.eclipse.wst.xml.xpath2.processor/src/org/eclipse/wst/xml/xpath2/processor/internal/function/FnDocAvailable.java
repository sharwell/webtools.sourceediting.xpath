/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Jesper Steen Moller - bug 281159 - fix document loading and resolving URIs 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Sam Harwell - implement fn:doc-available
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;

/**
 *
 * @author Sam Harwell
 */
public class FnDocAvailable extends Function {
	private static Collection<SeqType> _expected_args;

	/**
	 * Constructor for {@link FnDocAvailable}.
	 */
	public FnDocAvailable() {
		super(new QName("doc-available"), 1);
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
		return docAvailable(args, ec);
	}

	/**
	 * The function returns true if and only if the function call
	 * {@code fn:doc($uri)} would return a document node.
	 *
	 * <p>
	 * If {@code $uri} is an empty sequence, this function returns
	 * {@code false}.</p>
	 *
	 * <p>
	 * If a call on {@code fn:doc($uri)} would return a document node, this
	 * function returns {@code true}.</p>
	 *
	 * <p>
	 * If {@code $uri} is not a valid URI according to the rules applied by the
	 * implementation of {@code fn:doc}, an error is raised [err:FODC0005].</p>
	 *
	 * <p>
	 * Otherwise, this function returns {@code false}.</p>
	 *
	 * <p>
	 * If this function returns {@code true}, then calling {@code fn:doc($uri)}
	 * within the same execution scope must return a document node. However, if
	 * non-stable processing has been selected for the {@code fn:doc} function,
	 * this guarantee is lost.</p>
	 *
	 * @param args Result from the expressions evaluation.
	 * @param dc Result of dynamic context operation.
	 * @throws DynamicError Dynamic error.
	 * @return Result of fn:doc-available operation.
	 */
	public static ResultSequence docAvailable(Collection<ResultSequence> args, EvaluationContext evaluationContext) throws DynamicError {
		try {
			ResultSequence doc = FnDoc.doc(args, evaluationContext);
			if (doc.empty()) {
				return XSBoolean.FALSE;
			}

			return XSBoolean.TRUE;
		} catch (DynamicError ex) {
			if ("FODC0002".equals(ex.code())) {
				return XSBoolean.FALSE;
			}

			throw ex;
		}
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
			_expected_args.add(arg);
		}

		return _expected_args;
	}
}
