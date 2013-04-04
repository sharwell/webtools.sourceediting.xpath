/*******************************************************************************
 * Copyright (c) 2013 Jesper Steen Moeller, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Steen Moeller - initial API and implementation 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.ast;

/**
 * The concatenation operator sequences as operands and concatenates them as strings
 */
public class ConcatExpr extends BinExpr {

	/**
	 * Constructor for ConcatExpr.
	 * 
	 * @param l
	 *            input1 xpath expression/variable.
	 * @param r
	 *            unput2 xpath expression/variable.
	 */
	public ConcatExpr(Expr l, Expr r) {
		super(l, r);
	}

	/**
	 * Support for Visitor interface.
	 * 
	 * @return Result of Visitor operation.
	 */
	public Object accept(XPathVisitor v) {
		return v.visit(this);
	}
}
