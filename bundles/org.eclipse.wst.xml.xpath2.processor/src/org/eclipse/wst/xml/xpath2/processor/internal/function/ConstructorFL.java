/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.Hashtable;

import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.CtrType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;

/**
 * Constructor class for the functions library.
 */
public class ConstructorFL extends FunctionLibrary {

	private Hashtable<QName, AnyAtomicType> _types;

	/**
	 * Constructor for ConstructorFL.
	 * 
	 * @param ns
	 *            input string.
	 */
	public ConstructorFL(String ns) {
		super(ns);

		_types = new Hashtable<QName, AnyAtomicType>();
	}

	/**
	 * Adds a type into the functions library.
	 * 
	 * @param at
	 *            input of any atomic type.
	 */
	public void add_type(CtrType at) {
		QName name = new QName(at.type_name());
		name.set_namespace(namespace());

		_types.put(name, at);

		add_function(new Constructor(at));
	}

	/**
	 * Adds a type into the functions library as an abstract type.
	 * 
	 * @param at
	 *            input of any atomic type.
	 */
	public void add_abstract_type(String localName, AnyAtomicType at) {
		QName name = new QName(localName);
		name.set_namespace(namespace());

		_types.put(name, at);
	}
	
	/**
	 * Support for QName interface.
	 * 
	 * @param name
	 *            variable name.
	 * @return type of input variable.
	 */
	public AnyAtomicType atomic_type(QName name) {
		return _types.get(name);
	}
}
