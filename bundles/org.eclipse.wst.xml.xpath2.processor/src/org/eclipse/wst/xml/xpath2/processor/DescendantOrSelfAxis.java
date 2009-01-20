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

package org.eclipse.wst.xml.xpath2.processor;

import org.w3c.dom.*;
import org.eclipse.wst.xml.xpath2.processor.types.*;

/**
 * The descendant-or-self axis contains the context node and the descendants of the context
 * node.
 */
// multiple inheretance might be cool here =D
public class DescendantOrSelfAxis extends ForwardAxis {

	/**
	 * Retrieve the the descendants of the context node and the context node itself.
	 * @param node is the type of node.
	 * @param dc is the dynamic context.
	 * @return The context node and its descendants.
	 */
	public ResultSequence iterate(NodeType node, DynamicContext dc) {
		ResultSequence rs = ResultSequenceFactory.create_new();

		// add self
		rs.add(node);

		// add descendants
		DescendantAxis da = new DescendantAxis();
		ResultSequence desc = da.iterate(node, dc);
		rs.concat(desc);

		return rs;
	}

}
