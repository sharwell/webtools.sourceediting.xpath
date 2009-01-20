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
 * Create a result sequence that contains the context node
 */
public class SelfAxis extends ForwardAxis {

	/**
	 * create new rs and add the context node to it
	 * @param node is the node type
 	 * @param dc is the dynamic context
	 * @return rs containing node
 	 */
	public ResultSequence iterate(NodeType node, DynamicContext dc) {
		ResultSequence rs = ResultSequenceFactory.create_new();

		rs.add(node);

		return rs;
	}

}
