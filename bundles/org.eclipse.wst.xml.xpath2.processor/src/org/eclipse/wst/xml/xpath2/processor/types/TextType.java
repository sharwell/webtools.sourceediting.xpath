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

package org.eclipse.wst.xml.xpath2.processor.types;

import org.w3c.dom.*;
import org.eclipse.wst.xml.xpath2.processor.*;
import org.eclipse.wst.xml.xpath2.processor.ast.*;
/**
 * A representation of the TextType datatype
 */
public class TextType extends NodeType {
	private Text _value;
	/**
	 * Initialises using the supplied parameters
	 * @param v The value of the TextType node
	 * @param doc_type The document ordering
	 */
	public TextType(Text v, int doc_type) {
		super(v, doc_type);
		_value = v;
	}
	/**
	 * Retrieves the datatype's name
	 * @return "text" which is the datatype's name
	 */
	public String string_type() { return "text"; }
	/**
	 * Retrieves a String representation of the actual value stored
	 * @return String representation of the actual value stored
	 */
	public String string_value() {
		return _value.getNodeValue();
	}
	/**
	 * Creates a new ResultSequence consisting of the Text value stored
	 * @return New ResultSequence consisting of the Text value stored
	 */
	public ResultSequence typed_value() {
		ResultSequence rs = ResultSequenceFactory.create_new();

		rs.add(new UntypedAtomic(_value.getData()));

		return rs;
	}
	/**
	 * Unsupported method for this nodetype. 
	 * @return null (no user defined name for this node gets defined)
	 */
	public QName node_name() {
		return null;
	}
}
