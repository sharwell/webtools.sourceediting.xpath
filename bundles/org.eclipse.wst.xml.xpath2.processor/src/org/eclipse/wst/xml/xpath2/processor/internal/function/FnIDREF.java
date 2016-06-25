/*******************************************************************************
 * Copyright (c) 2009, 2010 Standard for Technology in Automotive Retail, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   David Carver (STAR) - bug 281168 - initial API and implementation
 *     David Carver  - bug 281186 - implementation of fn:id and fn:idref
 *     Mukul Gandhi - bug 280798 - PsychoPath support for JDK 1.4
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.xml.xpath2.api.EvaluationContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
import org.eclipse.wst.xml.xpath2.processor.internal.TypeError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AttrType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSID;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class FnIDREF extends Function {
	private static Collection<SeqType> _expected_args = null;
	
	/**
	 * Constructor for FnInsertBefore.
	 */
	public FnIDREF() {
		super(new QName("idref"), 1, 2);
	}

	/**
	 * Evaluate arguments.
	 * 
	 * @param args
	 *            argument expressions.
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of evaluation.
	 */
	@Override
	public ResultSequence evaluate(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		return idref(args, ec);
	}

	/**
	 * Insert-Before operation.
	 * 
	 * @param args
	 *            Result from the expressions evaluation.
	 * @param ec current evaluation context
	 * @throws DynamicError
	 *             Dynamic error.
	 * @return Result of fn:insert-before operation.
	 */
	public static ResultSequence idref(Collection<ResultSequence> args, EvaluationContext ec) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultBuffer rs = new ResultBuffer();
		
		Iterator<ResultSequence> argIt = cargs.iterator();
		ResultSequence idrefRS = argIt.next();
		String[] idst = idrefRS.first().getStringValue().split(" ");

		ArrayList<XSID> ids = createIDs(idst);
		ResultSequence nodeArg = null;
		NodeType nodeType = null;
		if (argIt.hasNext()) {
			nodeArg = argIt.next();
			nodeType = (NodeType)nodeArg.first();
		} else {
			if (ec.getContextItem() == null) {
				throw DynamicError.contextUndefined();
			}
			if (!(ec.getContextItem() instanceof NodeType)) {
				throw new DynamicError(TypeError.invalid_type(null));
			}
			nodeType = (NodeType) ec.getContextItem();
			if (nodeType.node_value().getOwnerDocument() == null) {
				throw DynamicError.contextUndefined();
			}
		}
		
		Node node = nodeType.node_value();
		if (node.getOwnerDocument() == null) {
			// W3C test suite seems to want XPDY0002 here
			throw DynamicError.contextUndefined();
			//throw DynamicError.noContextDoc();
		}
		
		if (hasID(ids, node)) {
			ElementType element = new ElementType((Element) node, ec.getStaticContext().getTypeModel());
			rs.add(element);
		}
		
		rs = processAttributes(node, ids, rs, ec);
		rs = processChildNodes(node, ids, rs, ec);

		return rs.getSequence();
	}
	
	private static ArrayList<XSID> createIDs(String[] idtokens) {
		ArrayList<XSID> xsid = new ArrayList<XSID>();
		for (String idtoken : idtokens) {
			XSID id = new XSID(idtoken);
			xsid.add(id);
		}
		return xsid;
	}
	
	private static ResultBuffer processChildNodes(Node node, List<XSID> ids, ResultBuffer rs, EvaluationContext ec) {
		if (!node.hasChildNodes()) {
			return rs;
		}
		
		NodeList nodeList = node.getChildNodes();
		for (int nodecnt = 0; nodecnt < nodeList.getLength(); nodecnt++) {
			Node childNode = nodeList.item(nodecnt);
			if (childNode.getNodeType() == Node.ELEMENT_NODE && !isDuplicate(childNode, rs)) {
				ElementType element = new ElementType((Element)childNode, ec.getStaticContext().getTypeModel());
				if (element.isIDREF()) {
					if (hasID(ids, childNode)) {
						rs.add(element);
					}
				} 
				rs = processAttributes(childNode, ids, rs, ec);
				rs = processChildNodes(childNode, ids, rs, ec);
			}
		}
		
		return rs;

	}
	
	private static ResultBuffer processAttributes(Node node, List<XSID> idrefs, ResultBuffer rs, EvaluationContext ec) {
		if (!node.hasAttributes()) {
			return rs;
		}
		
		NamedNodeMap attributeList = node.getAttributes();
		for (int atsub = 0; atsub < attributeList.getLength(); atsub++) {
			Attr atNode = (Attr) attributeList.item(atsub);
			NodeType atType = new AttrType(atNode, ec.getStaticContext().getTypeModel());
			if (atType.isID()) {
				if (hasID(idrefs, atNode)) {
					if (!isDuplicate(node, rs)) {
						ElementType element = new ElementType((Element)node, ec.getStaticContext().getTypeModel());
						rs.add(element);
					}
				}
			}
		}
		return rs;
	}
	
	private static boolean hasID(List<XSID> ids, Node node) {
		for (XSID idref : ids) {
			if (idref.getStringValue().equals(node.getNodeValue())) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isDuplicate(Node node, ResultBuffer rs) {
		Iterator<Item> it = rs.iterator();
		while (it.hasNext()) {
			if (it.next().equals(node)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Obtain a list of expected arguments.
	 * 
	 * @return Result of operation.
	 */
	public synchronized static Collection<SeqType> expected_args() {
		if (_expected_args == null) {
			_expected_args = new ArrayList<SeqType>();
			SeqType arg = new SeqType(new XSString(), SeqType.OCC_STAR);
			_expected_args.add(arg);
			_expected_args.add(new SeqType(SeqType.OCC_NONE));
		}

		return _expected_args;
	}
	
}
