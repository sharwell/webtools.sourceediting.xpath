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

import org.w3c.dom.Document;
import java.io.InputStream;

/**
 * The DOM loader loads the XML document.
 */
public interface DOMLoader {

	/**
 	 * The DOM loader loads the XML docuemnt 
	 * @param in is the input stream.
	 * @throws DOMLoaderException DOM loader exception.
	 * @return The loaded document.
 	 */
	public Document load(InputStream in) throws DOMLoaderException;

	/**
 	 * Set validating boolean.
	 * @param val is the validating boolean.
 	 */
	// XXX: default is false ?! [document it ?]
	public void set_validating(boolean val);

	// XXX: set namespace aware!?
}
