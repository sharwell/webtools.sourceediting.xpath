/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor;

/**
 * This error is thrown when there is a problem with an XPath exception.
 */
public class XPathError extends Error {
	private String _reason;

	/**
 	 * Constructor for XPathError
	 * @param reason Is the reason why the error has been thrown.
 	 */
	public XPathError(String reason) {
		_reason = reason;
	}

	/**
	 * The reason why the error has been thrown.
	 * @return the reason why the error has been throw. 
	 */
	public String reason() {
		return _reason;
	}
}
