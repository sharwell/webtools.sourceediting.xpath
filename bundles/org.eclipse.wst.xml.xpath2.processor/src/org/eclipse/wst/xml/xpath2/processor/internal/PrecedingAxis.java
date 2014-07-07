/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *     Mukul Gandhi  - bug 353373 - "preceding" & "following" axes behavior is erroneous
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import java.util.Iterator;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.w3c.dom.Node;

/**
 * the preceding axis contains all nodes that are descendants of the root of the
 * tree in which the context node is found
 */
public class PrecedingAxis extends ReverseAxis {

	/**
	 * returns preceding nodes of the context node
	 * 
	 * @param node
	 *            is the node type.
	 * @throws dc
	 *             is the Dynamic context.
	 */
	public void iterate(NodeType node, ResultBuffer result, Node limitNode) {

		if (limitNode != null && limitNode.isSameNode(node.node_value())) {
			// no further, we have reached the limit node
			return;
		}

		// get the parent
		NodeType parent = null;
		ResultBuffer parentBuffer = new ResultBuffer();
		new ParentAxis().iterate(node, result, limitNode);
		if (parentBuffer.size() == 1)
			parent = (NodeType) parentBuffer.item(0);

		// get the preceding siblings of this node, and add them
		if (parent != null) {
			PrecedingSiblingAxis psa = new PrecedingSiblingAxis();
			ResultBuffer siblingBuffer = new ResultBuffer();
			psa.iterate(node, siblingBuffer, limitNode);
			
			// for each sibling, get all its descendants
			DescendantAxis da = new DescendantAxis();
			for (Iterator<Item> i = siblingBuffer.iterator(); i.hasNext();) {
				// add firstly this node to the result
				NodeType precedingNodeTmp = (NodeType) i.next();
				result.add(precedingNodeTmp);
				// get descendants of this node, and add them to the result
				da.iterate(precedingNodeTmp, result, null);
			}
			
			// add preceding siblings of original context node & their descendants to the result
			siblingBuffer = new ResultBuffer();
			psa.iterate(node, siblingBuffer, limitNode);
			for (Iterator<Item> i = siblingBuffer.iterator(); i.hasNext();) {
				NodeType precedingNodeTmp = (NodeType) i.next();
				result.add(precedingNodeTmp);
				da.iterate(precedingNodeTmp, result, null);
			}
			
			// we gotta repeat the story for the parent and add the results
			iterate(parent, result, limitNode);
		}
	}
	
	public String name() {
		return "preceding";
	}
}
