/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 276134 - improvements to schema aware primitive type support
 *                                 for attribute/element nodes 
 *     Jesper Moller- bug 281159 - we were missing out on qualified attributes 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.apache.xerces.dom.PSVIAttrNSImpl;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.function.XSCtrLibrary;
import org.w3c.dom.Attr;

/**
 * A representation of the AttributeType datatype
 */
public class AttrType extends NodeType {
	private Attr _value;

	// constructor only usefull for string_type()
	// XXX needs to be fixed in future
	/**
	 * Initialises to null
	 */
	public AttrType() {
		this(null, 0);
	}

	/**
	 * Initialises according to the supplied parameters
	 * 
	 * @param v
	 *            The attribute being represented
	 * @param doc_order
	 *            The document order
	 */
	public AttrType(Attr v, int doc_order) {
		super(v, doc_order);
		_value = v;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "attribute" which is the datatype's full pathname
	 */
	@Override
	public String string_type() {
		return "attribute";
	}

	/**
	 * Retrieves a String representation of the attribute being stored
	 * 
	 * @return String representation of the attribute being stored
	 */
	@Override
	public String string_value() {
		return _value.getValue();
	}

	/**
	 * Creates a new ResultSequence consisting of the attribute being stored
	 * 
	 * @return New ResultSequence consisting of the attribute being stored
	 */
	@Override
	public ResultSequence typed_value() {
		ResultSequence rs = ResultSequenceFactory.create_new();
		
		PSVIAttrNSImpl psviAttr = (PSVIAttrNSImpl)_value;
		XSTypeDefinition typeDef = psviAttr.getTypeDefinition();
		
		if (typeDef != null && typeDef.getNamespace().equals(XSCtrLibrary.XML_SCHEMA_NS)) {
		  Object schemaTypeValue = getTypedValueForPrimitiveType(typeDef);
		  if (schemaTypeValue != null) {
			rs.add((AnyType)schemaTypeValue);  
		  }
		  else {
			rs.add(new XSUntypedAtomic(string_value()));  
		  }
	    } else {
		   rs.add(new XSUntypedAtomic(string_value()));	
		}
		
		return rs;
	}

	/**
	 * Retrieves the name of the node
	 * 
	 * @return Name of the node
	 */
	@Override
	public QName node_name() {
		QName name = new QName(_value.getPrefix(), _value.getLocalName(), _value.getNamespaceURI());

		return name;
	}
}
