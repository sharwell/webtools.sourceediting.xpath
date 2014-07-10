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

package org.eclipse.wst.xml.xpath2.processor.internal;

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.w3c.dom.Node;

/**
 * The descendant axis contains the descendants of the context node
 */
public class DescendantAxis extends ChildAxis {

	/**
	 * Using the context node retrieve the descendants of this node
	 * 
	 * @param node
	 *            is the type of node.
	 */
	@Override
	public void iterate(NodeType node, ResultBuffer copyInto, Node limitNode) {
		addChildren(node, copyInto, true);
	}

	@Override
	public String name() {
		return "descendant";
	}
}
