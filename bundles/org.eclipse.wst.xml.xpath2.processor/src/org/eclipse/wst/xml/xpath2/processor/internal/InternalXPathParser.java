/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Bug 338494    - prohibiting xpath expressions starting with / or // to be parsed. 
 *     Sam Harwell   - parser implementation using ANTLR
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.XPathParserException;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathExpr;

/**
 * JFlexCupParser parses the xpath expression
 */
@SuppressWarnings("deprecation")
public class InternalXPathParser {

	/**
	 * Tries to parse the xpath expression
	 * 
	 * @param xpath
	 *            is the xpath string.
	 * @param isRootlessAccess
	 *            if 'true' then PsychoPath engine can't parse xpath expressions starting with / or //.
	 * @throws XPathParserException.
	 * @return the xpath value.
	 * @since 2.0
	 */
	public XPath parse(String xpath, boolean isRootlessAccess) throws XPathParserException {

		XPathLexer lexer = new XPathLexer(new ANTLRInputStream(xpath));

		try {
			XPathParser p = new XPathParser(new CommonTokenStream(lexer)); 
			p.setErrorHandler(new BailErrorStrategy());
			XPathParser.XPathContext context = p.xPath();
			XPathBuilderVisitor visitor = XPathBuilderVisitor.INSTANCE;
			XPath xPath2 = visitor.visitXPath(context);
			if (isRootlessAccess) {
				xPath2.accept(new DefaultVisitor() {
					public Object visit(XPathExpr e) {
						if (e.slashes() > 0) {
							throw new XPathParserException("Access to root node is not allowed (set by caller)", null);
						}
						do {
							e.expr().accept(this); // check the single step (may have filter with root access)
							e = e.next(); // follow singly linked list of the path, it's all relative past the first one
						} while (e != null);
						return null;
					}
				});
			}
			return xPath2;
		} catch (RecognitionException e) {
			throw new XPathParserException("ANTLR parser error: " + e.getMessage(), e);
		} catch (StaticError e) {
			throw new XPathParserException(e.code(), e.getMessage(), e);
		} catch (Exception e) {
			throw new XPathParserException(e.getMessage(), e);
		}
	}
}
