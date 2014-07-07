/*******************************************************************************
 * Copyright (c) 2005, 2012 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0
 *     Mukul Gandhi - bug 276134 - improvements to schema aware primitive type support
 *                                  for attribute/element nodes
 *     Jesper Moller - bug 275610 - Avoid big time and memory overhead for externals
 *     David Carver  - bug 281186 - implementation of fn:id and fn:idref
 *     David Carver (STAR) - bug 289304 - fix schema awarness of types on elements
 *     Jesper Moller - bug 297958 - Fix fn:nilled for elements
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *     Mukul Gandhi - bug 323900 - improving computing the typed value of element &
 *                                 attribute nodes, where the schema type of nodes
 *                                 are simple, with varieties 'list' and 'union'.
 *     Lukasz Wycisk - bug 361659 - ElemntType typed value in case of nil=’true’
 *	   Mukul Gandhi	- bug 393904 - improvements to computing typed value of element nodes                                  
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.types;

import org.apache.xerces.dom.PSVIElementNSImpl;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeDefinition;
import org.eclipse.wst.xml.xpath2.api.typesystem.TypeModel;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.builtin.BuiltinTypeLibrary;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;

/**
 * A representation of the ElementType datatype
 */
public class ElementType extends NodeType {
	private static final String ELEMENT = "element";

	private static final String SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String NIL_ATTRIBUTE = "nil";
	private static final String TRUE_VALUE = "true";

	private Element _value;

	private String _string_value;

	/**
	 * Initialises to a null element
	 */
	public ElementType() {
		this(null, null);
	}

	/**
	 * Initialises according to the supplied parameters
	 * 
	 * @param v
	 *            The element being represented
	 */
	public ElementType(Element v, TypeModel tm) {
		super(v, tm);
		_value = v;

		_string_value = null;
	}

	/**
	 * Retrieves the actual element value being represented
	 * 
	 * @return Actual element value being represented
	 */
	public Element value() {
		return _value;
	}

	/**
	 * Retrieves the datatype's full pathname
	 * 
	 * @return "element" which is the datatype's full pathname
	 */
	public String string_type() {
		return ELEMENT;
	}

	/**
	 * Retrieves a String representation of the element being stored
	 * 
	 * @return String representation of the element being stored
	 */
	public String getStringValue() {
		// XXX can we cache ?
		if (_string_value != null)
			return _string_value;

		_string_value = textnode_strings(_value);

		return _string_value;
	}

	/**
	 * Creates a new ResultSequence consisting of the element stored
	 * 
	 * @return New ResultSequence consisting of the element stored
	 */
	public ResultSequence typed_value() {
		
		TypeDefinition typeDef = getType();

		if (!isNilled(_value)) {
			if (typeDef != null) {
				return getXDMTypedValue(typeDef, typeDef.getSimpleTypes(_value));
		    	 /*catch(DynamicError err) {
		    		if ("FOTY0012".equals(err.code())) {
		    			// upon encountering an element-only content, perform following additional checks
		    			if (isDescendantElementValidatedByWildCard(_value)) {
		    			   // find the typed value, as if it were compleType 'mixed' content model
		    			   rs.add(new XSUntypedAtomic(string_value()));
		    			}
		    			else {
		    			   // this error indicates that a typed-value cannot be computed, and the dynamic error
		    			   // FOTY0012 will be shown to the caller of XPath engine. 
		    			   throw err;
		    			}
		    		}
		    	 } */
			}
			else {
				return new XSUntypedAtomic(getStringValue());
			}
		}
		return ResultBuffer.EMPTY;
	}

	private boolean isNilled(Element _value2) {
		return TRUE_VALUE.equals(_value2.getAttributeNS(SCHEMA_INSTANCE, NIL_ATTRIBUTE));
	}

	/*
	 * Check if a descendant element of this element node, is validated by a wild-card.
	 * Check the following additional conditions:
	 * a) If validated by a 'skip' wild-card, return 'true'.
	 * b) If validated by a 'lax' wild-card, but an element declaration for the wild-card didn't exist 
	 *    in the schema return 'true' else return 'false'.
	 * c) If validated by a 'strict' wild-card, return 'false'.
	 */
	private boolean isDescendantElementValidatedByWildCard(Element elemNode) {
		boolean isDescElemValByWildCard = false;
		
		NodeList childNodes = ((PSVIElementNSImpl) elemNode).getChildNodes();
		for (int ndIdex = 0; ndIdex < childNodes.getLength(); ndIdex++) {
			Node node = childNodes.item(ndIdex);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
			   PSVIElementNSImpl psviElem = (PSVIElementNSImpl)node;
			   XSTypeDefinition elemType = psviElem.getTypeDefinition();
			   if (elemType == null || "anyType".equals(elemType.getName()) || isDescendantElementValidatedByWildCard((Element)node)) {
				  // this element instance was likely validated by a 'skip', or 'lax' (which didn't 
				  // find an element declaration) wild-card. otherwise, continue checking other nodes
				  // in this tree recursively.
				  isDescElemValByWildCard = true;
				  break;
			   }
			}			
		}
		
		return isDescElemValByWildCard;
		
	} // isDescendantElementValidatedByWildCard

	// recursively concatenate TextNode strings
	/**
	 * Recursively concatenate TextNode strings
	 * 
	 * @param node
	 *            Node to recurse
	 * @return String representation of the node supplied
	 */
	public static String textnode_strings(Node node) {
		String result = "";

		if (node.getNodeType() == Node.TEXT_NODE) {
			Text tn = (Text) node;
			result += tn.getData();
		}

		NodeList nl = node.getChildNodes();

		StringBuffer buf = new StringBuffer(result);
		// concatenate children
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);

			buf.append(textnode_strings(n));
		}

		result = buf.toString();
		return result;
	}

	/**
	 * Retrieves the name of the node
	 * 
	 * @return QName representation of the name of the node
	 */
	public QName node_name() {
		QName name = new QName(_value.getPrefix(), _value.getLocalName(), _value.getNamespaceURI());

		return name;
	}

	public ResultSequence nilled() {

		if (_value instanceof PSVIElementNSImpl) {
			PSVIElementNSImpl psviElement = (PSVIElementNSImpl) _value;
			return XSBoolean.valueOf(psviElement.getNil());
		}
		else {
			return XSBoolean.FALSE;
		}
	}

	/**
	 * @since 1.1
	 */
	public boolean isID() {
		return isElementType(SCHEMA_TYPE_ID);
	}

	/**
	 * @since 1.1
	 */
	public boolean isIDREF() {
		return isElementType(SCHEMA_TYPE_IDREF);
	}

	protected boolean isElementType(String typeName) {
		TypeInfo typeInfo = _value.getSchemaTypeInfo();
		return isType(typeInfo, typeName);
	}
	
	public TypeDefinition getTypeDefinition() {
		return BuiltinTypeLibrary.XS_UNTYPED;
	}
}
