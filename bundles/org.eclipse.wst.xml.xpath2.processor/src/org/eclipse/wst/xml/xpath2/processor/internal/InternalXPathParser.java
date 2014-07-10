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
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import java.io.StringReader;

import java_cup.runtime.Symbol;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.XPathParserException;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathExpr;

/**
 * JFlexCupParser parses the xpath expression
 */
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
	@SuppressWarnings("deprecation")
	public org.eclipse.wst.xml.xpath2.processor.ast.XPath parse(String xpath, boolean isRootlessAccess) throws XPathParserException {

		XPathFlex lexer = new XPathFlex(new StringReader(xpath));

		try {
			XPathCup p = new XPathCup(lexer); 
			Symbol res = p.parse();
			org.eclipse.wst.xml.xpath2.processor.ast.XPath xPath2 = (org.eclipse.wst.xml.xpath2.processor.ast.XPath) res.value;
			if (isRootlessAccess) {
				xPath2.accept(new DefaultVisitor() {
					@Override
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
		} catch (JFlexError e) {
			throw new XPathParserException("JFlex lexer error: " + e.reason(), e);
		} catch (CupError e) {
			throw new XPathParserException("CUP parser error: " + e.reason(), e);
		} catch (StaticError e) {
			throw new XPathParserException(e.code(), e.getMessage(), e);
		} catch (Exception e) {
			throw new XPathParserException(e.getMessage(), e);
		}
	}
}
