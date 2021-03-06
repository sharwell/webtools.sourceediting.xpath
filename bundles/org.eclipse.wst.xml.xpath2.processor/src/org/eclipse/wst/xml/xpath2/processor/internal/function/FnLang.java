/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     David Carver (STAR) - bug 280972 - fix fn:lang implementation so it matches spec. 
 *     Jesper Steen Moeller - bug 285145 - implement full arity checking
 *     David Carver (STAR) - bug 262765 - correct invalidType to throw XPTY0004 instead of FORG0006
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This function tests whether the language of $node, or the context node if the
 * second argument is omitted, as specified by xml:lang attributes is the same
 * as, or is a sublanguage of, the language specified by $testlang. The language
 * of the argument node, or the context node if the second argument is omitted,
 * is determined by the value of the xml:lang attribute on the node, or, if the
 * node has no such attribute, by the value of the xml:lang attribute on the
 * nearest ancestor of the node that has an xml:lang attribute. If there is no
 * such ancestor, then the function returns false.
 */
public class FnLang extends Function {
	private static Collection<SeqType> _expected_args = null;

	/**
	 * Constructor for FnLang.
	 */
	public FnLang() {
		super(new QName("lang"), 1, 2);
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
		return lang(args, ec);
	}

	/**
	 * Language operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:lang operation.
	 */
	public static ResultSequence lang(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {

		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		// get arg
		Iterator<ResultSequence> citer = cargs.iterator();
		ResultSequence arg1 = citer.next();
		ResultSequence arg2 = null;
		if (cargs.size() == 1) {
			if (ec.getContextItem() == null) {
				throw DynamicError.contextUndefined();
			}
			arg2 = (AnyType) ec.getContextItem();
		} else {
			arg2 = citer.next();
		}
		
		String lang = "";

		if (!arg1.empty()) {
			lang = ((XSString) arg1.first()).value();
		}

		
		if (!(arg2.first() instanceof NodeType) ) {
			throw DynamicError.invalidType();
		}
		
		NodeType an = (NodeType) arg2.first();

		return XSBoolean.valueOf(test_lang(an.node_value(), lang));
	}

	/**
	 * Language test operation.
	 * 
	 * @param node
	 *            Node to test.
	 * @param lang
	 *            Language to test for.
	 * @return Boolean result of operation.
	 */
	private static boolean test_lang(Node node, String lang) {
		NamedNodeMap attrs = node.getAttributes();

		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr attr = (Attr) attrs.item(i);

				if (!"xml:lang".equals(attr.getName()))
					continue;
				
				String xmllangValue = attr.getValue();
				int hyphenIndex = xmllangValue.indexOf('-');
				
				if (hyphenIndex > -1) {
					xmllangValue = xmllangValue.substring(0, hyphenIndex);
				}

				
				String langLanguage = lang;
				if (lang.length() > 2) {
					langLanguage = lang.substring(0, 2);
				}
				
				return xmllangValue.equalsIgnoreCase(langLanguage);
			}
		}

		Node parent = node.getParentNode();
		if (parent == null)
			return false;

		return test_lang(parent, lang);
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();

			_expected_args.add(new SeqType(new XSString(), SeqType.OCC_QMARK));
			_expected_args.add(new SeqType(NodeType.class, SeqType.OCC_NONE));
		}

		return _expected_args;
	}
		
}
