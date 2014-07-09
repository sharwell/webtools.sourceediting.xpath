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
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Document;

/**
 * Retrieves a document using an xs:anyURI supplied as an xs:string. If $uri is
 * not a valid xs:anyURI, an error is raised [err:FODC0005]. If it is a relative
 * URI Reference, it is resolved relative to the value of the base URI property
 * from the static context. The resulting absolute URI Reference is cast to an
 * xs:string. If the Available documents discussed in Section 2.1.2 Dynamic
 * ContextXP provides a mapping from this string to a document node, the
 * function returns that document node. If the Available documents maps the
 * string to an empty sequence, then the function returns an empty sequence. If
 * the Available documents provides no mapping for the string, an error is
 * raised [err:FODC0005].
 */
public class FnDoc extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnDoc.
	 */
	public FnDoc() {
		super(new QName("doc"), 1);
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
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		return doc(args, ec);
	}

	/**
	 * Doc operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param dc
	 *            Result of dynamic context operation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:doc operation.
	 */
	public static ResultSequence doc(Collection<ResultSequence> args, EvaluationContext ec)
			throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get args
		Iterator<ResultSequence> argiter = cargs.iterator();
		ResultSequence arg1 = argiter.next();

		if (arg1.empty())
			return ResultBuffer.EMPTY;

		String uri = ((XSString) arg1.item(0)).value();

		DynamicContext dc = ec.getDynamicContext();
		URI resolved = dc.resolveUri(uri);
		if (resolved == null)
			throw DynamicError.invalid_doc(null);

		Document doc = dc.getDocument(resolved);
		if (doc == null)
			throw DynamicError.doc_not_found(null, null);

		return new DocType(doc, ec.getStaticContext().getTypeModel());
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
