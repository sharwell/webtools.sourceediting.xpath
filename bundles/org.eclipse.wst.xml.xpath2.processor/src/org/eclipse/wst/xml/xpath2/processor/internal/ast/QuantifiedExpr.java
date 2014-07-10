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

import java.util.Collection;
import java.util.Iterator;

// mite wanna do two separate classes at the end
/**
 * Support for Quantified expressions.
 */
public class QuantifiedExpr extends Expr {
	/**
	 * Set internal value for SOME.
	 */
	public static final int SOME = 0;
	/**
	 * Set internal value for ALL.
	 */
	public static final int ALL = 1;

	private final Collection<VarExprPair> _var_expr_pairs;
	private Expr _return;
	private final int _type;

	/**
	 * Constructor for QuantifiedExpr.
	 * 
	 * @param type
	 *            Type (0 for SOME, 1 for ALL).
	 * @param varexp
	 *            Expressions.
	 * @param ret
	 *            Returned expression.
	 */
	public QuantifiedExpr(int type, Collection<VarExprPair> varexp, Expr ret) {
		_type = type;
		_var_expr_pairs = varexp;
		_return = ret;
	}

	/**
	 * Support for Visitor interface.
	 * 
	 * @return Result of Visitor operation.
	 */
	@Override
	public <T> T accept(XPathVisitor<T> v) {
		return v.visit(this);
	}

	/**
	 * Support for Integer interface.
	 * 
	 * @return Result of Int operation.
	 */
	public int type() {
		return _type;
	}

	/**
	 * Support for Iterator inteface.
	 * 
	 * @return Result of Iterator operation.
	 */
	public Iterator<VarExprPair> iterator() {
		return _var_expr_pairs.iterator();
	}

	/**
	 * Support for Expression interface.
	 * 
	 * @return Result of Expr operation.
	 */
	public Expr expr() {
		return _return;
	}

	/**
	 * Set next expression.
	 * 
	 * @param e
	 *            Expression.
	 */
	public void set_expr(Expr e) {
		_return = e;
	}

	// used for normalization... basically just keep a "simple for"... no
	// pairs... collection will always have 1 element
	/**
	 * Normalization of expression pairs.
	 */
	public void truncate_pairs() {
		boolean first = true;

		for (Iterator<VarExprPair> i = _var_expr_pairs.iterator(); i.hasNext();) {
			i.next();
			if (!first)
				i.remove();

			first = false;
		}
	}

	/**
	 * Support for Collection interface.
	 * 
	 * @return Expression pairs.
	 */
	public Collection<VarExprPair> ve_pairs() {
		return _var_expr_pairs;
	}
}
