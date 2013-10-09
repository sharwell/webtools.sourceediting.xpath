/*******************************************************************************
 * Copyright (c) 2005, 2010 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.ast;

/**
 * Class for binary operation Add, takes 2 inputs and returns the combined
 * value.
 */
public class AddExpr extends BinExpr {

	/**
	 * Constructor for AddExpr
	 * 
	 * @param l
	 *            input1 xpath expression/variable.
	 * @param r
	 *            input2 xpath expression/variable.
	 */
	public AddExpr(Expr l, Expr r) {
		super(l, r);
	}

	/**
	 * Support for Visitor interface.
	 * 
	 * @return Result of Visitor operation.
	 */
	public <T> T accept(XPathVisitor<T> v) {
		return v.visit(this);
	}
}
