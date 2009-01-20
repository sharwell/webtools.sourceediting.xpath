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
 * Base class for all static errors as defined by the XPath 2.0 specification
 *
 */
public class StaticError extends XPathException {
	// errorcode specified in http://www.w3.org/2004/10/xqt-errors i fink
	private String _code;

	/**
	 * Constructor for a generic static error
	 *
	 * @param code The error code as specified in XPath 2.0
	 * @param err Humar readable error message
	 */
	public StaticError(String code, String err) {
		super(err);
		_code = code;
	}
	
	/**
	 * @return error code which represents the error
	 */
	public String code() { return _code; }
}
