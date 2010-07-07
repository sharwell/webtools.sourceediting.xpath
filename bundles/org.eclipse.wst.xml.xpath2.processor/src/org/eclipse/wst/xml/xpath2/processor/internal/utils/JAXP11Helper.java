/*******************************************************************************
 * Copyright (c) 2010 Jesper Steen Moller, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Moller - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/
package org.eclipse.wst.xml.xpath2.processor.internal.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

public final class JAXP11Helper {

	public static final short DOCUMENT_POSITION_PRECEDING = 2; // Node.DOCUMENT_POSITION_PRECEDING
	public static final short DOCUMENT_POSITION_FOLLOWING = 4; // Node.DOCUMENT_POSITION_FOLLOWING

	private JAXP11Helper() {
	}

	private static final Method Node_isSameNode = getMethod(Node.class,
			"isSameNode", Node.class);

	public static boolean isSameNode(Node nodeA, Node nodeB) {
		Boolean result = (Boolean) invoke(Node_isSameNode, nodeA, nodeB);
		return result.booleanValue();
	}

	private static final Method Node_compareDocumentPosition = getMethod(
			Node.class, "compareDocumentPosition", Node.class);

	public static short compareDocumentPosition(Node nodeA, Node nodeB) {
		Short result = (Short) invoke(Node_compareDocumentPosition, nodeA,
				nodeB);
		return result.shortValue();
	}

	private static final Method Document_getDocumentURI = getMethod(Document.class,
			"getDocumentURI");

	public static String getDocumentURI(Document doc) {
		return (String) invoke(Document_getDocumentURI, doc);
	}

	private static final Method Node_getBaseURI = getMethod(Node.class,
			"getBaseURI");

	public static String getBaseURI(Node domNode) {
		return (String) invoke(Node_getBaseURI, domNode);
	}

	private static final Method Attr_getSchemaTypeInfo = getMethod(Attr.class,
			"getSchemaTypeInfo");
	public static TypeInfo getSchemaTypeInfo(Attr domNode) {
		return (TypeInfo) invoke(Attr_getSchemaTypeInfo, domNode);
	}

	private static final Method Element_getSchemaTypeInfo = getMethod(Element.class,
			"getSchemaTypeInfo");
	public static TypeInfo getSchemaTypeInfo(Element domNode) {
		return (TypeInfo) invoke(Element_getSchemaTypeInfo, domNode);
	}

	private static final Method Element_isDefaultNamespace = getMethod(
			Element.class, "isDefaultNamespace", String.class);

	public static boolean isDefaultNamespace(Element element,
			String namespaceURI) {
		Boolean result = (Boolean)invoke(Element_isDefaultNamespace, element, namespaceURI);
		return result.booleanValue();
	}

	private static final Method Node_isEqualNode = getMethod(Node.class,
			"isEqualNode", Node.class);
	public static boolean isEqualNode(Node a, Node b) {
		Boolean result = (Boolean)invoke(Node_isEqualNode, a, b);
		return result.booleanValue();
	}

	private static final Method Element_lookupNamespaceURI = getMethod(Element.class,
			"lookupNamespaceURI", String.class);
	public static String lookupNamespaceURI(Element element, String prefix) {
		return (String) invoke(Element_lookupNamespaceURI, element, prefix);
	}

	private static final Method DocumentBuilderFactory_setSchema = getMethod(DocumentBuilderFactory.class,
			"setSchema", Schema.class);
	public static void setSchema(DocumentBuilderFactory factory, Schema _schema) {
		invoke(DocumentBuilderFactory_setSchema, factory, _schema);
	}

	private static final Method Document_setDocumentURI= getMethod(Document.class,
			"setDocumentURI", String.class);
	public static void setDocumentURI(Document doc, String uri) {
		invoke(Document_setDocumentURI, doc, uri);
	}

	// General helper functions
	
	private static Object invoke(Method method, Object thisPtr) {
		return invokeGeneral(method, thisPtr, new Object[] {});
	}

	private static Object invoke(Method method, Object thisPtr, Object arg0) {
		return invokeGeneral(method, thisPtr, new Object[] { arg0 });
	}

	private static Object invokeGeneral(Method method, Object thisPtr,
			Object[] objects) {
		try {
			return method.invoke(thisPtr, objects);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("invokeGeneral(" + method
					+ ") - IllegalArgumentException", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("invokeGeneral(" + method
					+ ") - IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("invokeGeneral(" + method
					+ ") - InvocationTargetException", e);
		}
	}

	private static Method getMethod(Class clazz, String methodName)
			throws SecurityException {
		return getMethodGeneral(clazz, methodName, new Class[] {});
	}

	private static Method getMethod(Class clazz, String methodName, Class class2) {
		return getMethodGeneral(clazz, methodName, new Class[] { class2 });
	}

	private static Method getMethodGeneral(Class clazz, String methodName,
			Class[] args) {
		try {
			return clazz.getMethod(methodName, args);
		} catch (SecurityException e) {
			throw new RuntimeException("getMethodGeneral(" + methodName
					+ ") - SecurityException", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("getMethodGeneral(" + methodName
					+ ") - NoSuchMethodException", e);
		}
	}

}
