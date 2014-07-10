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

package org.eclipse.wst.xml.xpath2.processor;

import org.eclipse.wst.xml.xpath2.processor.internal.InternalXPathParser;

/**
 * JFlexCupParser parses the xpath expression
 */
public class JFlexCupParser implements XPathParser {

	/**
	 * Tries to parse the xpath expression
	 * 
	 * @param xpath
	 *            is the xpath string.
	 * @throws XPathParserException
	 * @return the xpath value.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public org.eclipse.wst.xml.xpath2.processor.ast.XPath parse(String xpath) throws XPathParserException {

		return new InternalXPathParser().parse(xpath, false);
	}
	
	/**
	 * Tries to parse the xpath expression
	 * 
	 * @param xpath
	 *            is the xpath string.
	 * @param isRootlessAccess
	 *            if 'true' then PsychoPath engine can't parse xpath expressions starting with / or //.
	 * @throws XPathParserException
	 * @return the xpath value.
	 * @since 2.0
	 */
	@SuppressWarnings("deprecation")
	@Override
	public org.eclipse.wst.xml.xpath2.processor.ast.XPath parse(String xpath, boolean isRootlessAccess) throws XPathParserException {

		return new InternalXPathParser().parse(xpath, isRootlessAccess);
	}
}
