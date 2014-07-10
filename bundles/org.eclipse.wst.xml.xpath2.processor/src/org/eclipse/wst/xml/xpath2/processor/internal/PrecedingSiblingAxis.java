/*******************************************************************************
 * Copyright (c) 2005, 2012 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Lukasz Wycisk - bug 361803 - NodeType:dom_to_xpath and null value
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.w3c.dom.Node;

/**
 * the preceding axis contains all nodes that are descendants of the root of the
 * tree in which the context node is found including the context node itself
 */
public class PrecedingSiblingAxis extends ReverseAxis {
	// XXX again, unify with following

	/**
	 * returns preceding nodes of the context node
	 * 
	 * @param node
	 *            is the node type.
	 */
	@Override
	public void iterate(NodeType node, ResultBuffer copyInto, Node limitNode) {
		// XXX check for attribute / namespace node... if so return
		// empty sequence

		int pos = copyInto.size();
		Node iterNode = node.node_value();
		// get the children of the parent [siblings]
		do {
			iterNode = iterNode.getPreviousSibling();
			if (iterNode != null) {
				NodeType nodeType = NodeType.dom_to_xpath(iterNode, node.getTypeModel());
				if(nodeType != null) {
					copyInto.addAt(pos, nodeType);
				}
			}
		} while (iterNode != null);
	}

	@Override
	public String name() {
		return "preceding-sibling";
	}

}
