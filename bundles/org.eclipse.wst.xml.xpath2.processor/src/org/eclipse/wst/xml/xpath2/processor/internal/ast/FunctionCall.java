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

import org.eclipse.wst.xml.xpath2.api.Function;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Class for Function Call support.
 */
public class FunctionCall extends PrimaryExpr {
	private final QName _name;
	private final Collection<Expr> _args;
	private Function _function;

	/**
	 * Constructor for FunctionCall.
	 * 
	 * @param name
	 *            QName.
	 * @param args
	 *            Collection of arguments.
	 */
	public FunctionCall(QName name, Collection<Expr> args) {
		_name = name;
		_args = args;
	}
	
	public Function function() {
		return _function;
	}
	
	public void set_function(Function _function) {
		this._function = _function;
	}

	/**
	 * Support for Visitor interface.
	 * 
	 * @return Result of Visitor operation.
	 */
	public <T> T accept(XPathVisitor<T> v) {
		return v.visit(this);
	}

	/**
	 * Support for QName interface.
	 * 
	 * @return Result of QName operation.
	 */
	public QName name() {
		return _name;
	}

	/**
	 * Support for Iterator interface.
	 * 
	 * @return Result of Iterator operation.
	 */
	public Iterator<Expr> iterator() {
		return _args.iterator();
	}

	/**
	 * Support for Arity interface.
	 * 
	 * @return Result of Arity operation.
	 */
	public int arity() {
		return _args.size();
	}
}
