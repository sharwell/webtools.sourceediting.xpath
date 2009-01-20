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

package org.eclipse.wst.xml.xpath2.processor.ast;

import org.eclipse.wst.xml.xpath2.processor.types.*;
/**
 * Class for Name test operation.
 */
public class NameTest extends NodeTest {
	private QName _name;
	/**
	 * Constructor for NameTest.
	 * @param name QName to test.
	 */
	public NameTest(QName name) {
		_name = name;
	}
	/**
	 * Support for Visitor interface.
 	 * @return Result of Visitor operation.
 	 */
	@Override
	public Object accept(XPathVisitor v) {
		return v.visit(this);
	}
	/**
	 * Support for QName interface.
	 * @return Resulf of QName operation.
	 */
	public QName name() { return _name; }
}
