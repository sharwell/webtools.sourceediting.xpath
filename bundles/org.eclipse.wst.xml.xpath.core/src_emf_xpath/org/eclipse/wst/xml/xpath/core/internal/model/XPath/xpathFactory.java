/**
 *  Copyright (c) 2008 Standards for Technology in Automotive Retail and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      David Carver - initial API and implementation
 *
 * $Id: xpathFactory.java,v 1.1 2008/04/22 21:07:29 dacarver Exp $
 */
package org.eclipse.wst.xml.xpath.core.internal.model.XPath;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.wst.xml.xpath.core.internal.model.XPath.xpathPackage
 * @generated
 */
public interface xpathFactory extends EFactory {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String copyright = " Copyright (c) 2008 Standards for Technology in Automotive Retail and others.\n All rights reserved. This program and the accompanying materials\n are made available under the terms of the Eclipse Public License v1.0\n which accompanies this distribution, and is available at\n http://www.eclipse.org/legal/epl-v10.html\n\n Contributors:\n     David Carver - initial API and implementation";

	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	xpathFactory eINSTANCE = org.eclipse.wst.xml.xpath.core.internal.model.XPath.impl.xpathFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>XPath</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>XPath</em>'.
	 * @generated
	 */
	XPath createXPath();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	xpathPackage getxpathPackage();

} //xpathFactory