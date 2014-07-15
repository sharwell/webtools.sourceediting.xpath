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
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSIDREF;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Returns the sequence of element nodes that have an ID value matching the value of one
 * or more of the IDREF values supplied in $arg .
 */
public class FnID extends Function {
	private static Collection<SeqType> _expected_args = null;
	
	/**
	 * Constructor for FnInsertBefore.
	 */
	public FnID() {
		super(new QName("id"), 1, 2);
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
		return id(args, ec);
	}

	/**
	 * Returns the sequence of element nodes that have an ID value matching the
	 * value of one or more of the IDREF values supplied in $arg.
	 *
	 * <p>
	 * Note:</p>
	 *
	 * <p>
	 * This function does not have the desired effect when searching a document
	 * in which elements of type xs:ID are used as identifiers. To preserve
	 * backwards compatibility, a new function fn:element-with-id is therefore
	 * being introduced; it behaves the same way as fn:id in the case of
	 * ID-valued attributes.</p>
	 *
	 * <p>
	 * The function returns a sequence, in document order with duplicates
	 * eliminated, containing every element node E that satisfies all the
	 * following conditions:</p>
	 *
	 * <ol>
	 * <li>E is in the target document. The target document is the document
	 * containing $node, or the document containing the context item (.) if the
	 * second argument is omitted. The behavior of the function if $node is
	 * omitted is exactly the same as if the context item had been passed as
	 * $node. If $node, or the context item if the second argument is omitted,
	 * is a node in a tree whose root is not a document node [err:FODC0001] is
	 * raised. If the second argument is the context item, or is omitted, the
	 * following errors may be raised: if there is no context item,
	 * [err:XPDY0002]XP; if the context item is not a node
	 * [err:XPTY0004]XP.</li>
	 * <li>E has an ID value equal to one of the candidate IDREF values, where:
	 * <ul>
	 * <li>An element has an ID value equal to V if either or both of the
	 * following conditions are true:
	 * <ul>
	 * <li>The is-id property (See Section 5.5 is-id AccessorDM.) of the element
	 * node is true, and the typed value of the element node is equal to V under
	 * the rules of the eq operator using the Unicode code point collation
	 * (http://www.w3.org/2005/xpath-functions/collation/codepoint).</li>
	 * <li>The element has an attribute node whose is-id property (See Section
	 * 5.5 is-id AccessorDM.) is true and whose typed value is equal to V under
	 * the rules of the eq operator using the Unicode code point collation
	 * (http://www.w3.org/2005/xpath-functions/collation/codepoint).</li>
	 * </ul></li>
	 * <li>Each xs:string in $arg is treated as a whitespace-separated sequence
	 * of tokens, each token acting as an IDREF. These tokens are then included
	 * in the list of candidate IDREF values. If any of the tokens is not a
	 * lexically valid IDREF (that is, if it is not lexically an xs:NCName), it
	 * is ignored. Formally, the candidate IDREF values are the strings in the
	 * sequence given by the expression:
	 * <pre>
	 * for $s in $arg return fn:tokenize(fn:normalize-space($s), ' ')
	 *     [. castable as xs:IDREF]
	 * </pre></li>
	 * </ul></li>
	 * <li>If several elements have the same ID value, then E is the one that is
	 * first in document order.</li>
	 * </ol>
	 *
	 * <p>
	 * Notes:</p>
	 *
	 * <p>
	 * If the data model is constructed from an Infoset, an attribute will have
	 * the is-id property if the corresponding attribute in the Infoset had an
	 * attribute type of ID: typically this means the attribute was declared as
	 * an ID in a DTD.</p>
	 *
	 * <p>
	 * If the data model is constructed from a PSVI, an element or attribute
	 * will have the is-id property if its typed value is a single atomic value
	 * of type xs:ID or a type derived by restriction from xs:ID.</p>
	 *
	 * <p>
	 * No error is raised in respect of a candidate IDREF value that does not
	 * match the ID of any element in the document. If no candidate IDREF value
	 * matches the ID value of any element, the function returns the empty
	 * sequence.</p>
	 *
	 * <p>
	 * It is not necessary that the supplied argument should have type xs:IDREF
	 * or xs:IDREFS, or that it should be derived from a node with the is-idrefs
	 * property.</p>
	 *
	 * <p>
	 * An element may have more than one ID value. This can occur with synthetic
	 * data models or with data models constructed from a PSVI where the element
	 * and one of its attributes are both typed as xs:ID.</p>
	 *
	 * <p>
	 * If the source document is well-formed but not valid, it is possible for
	 * two or more elements to have the same ID value. In this situation, the
	 * function will select the first such element.</p>
	 *
	 * <p>
	 * It is also possible in a well-formed but invalid document to have an
	 * element or attribute that has the is-id property but whose value does not
	 * conform to the lexical rules for the xs:ID type. Such a node will never
	 * be selected by this function.</p>
	 *
	 * @param args Result from the expressions evaluation.
	 * @throws DynamicError Dynamic error.
	 * @return Result of fn:id operation.
	 */
	public static ResultSequence id(Collection<ResultSequence> args, EvaluationContext context) throws DynamicError {
		Collection<ResultSequence> cargs = Function.convert_arguments(args, expected_args());

		ResultBuffer rs = new ResultBuffer();
		
		Iterator<ResultSequence> argIt = cargs.iterator();
		ResultSequence idrefRS = argIt.next();
		String[] idrefst = idrefRS.first().getStringValue().split(" ");

		ArrayList<XSIDREF> idrefs = createIDRefs(idrefst);
		ResultSequence nodeArg = null;
		NodeType nodeType = null;
		if (argIt.hasNext()) {
			nodeArg = argIt.next();
			nodeType = (NodeType)nodeArg.first();
		} else {
			if (context.getContextItem() == null) {
				throw DynamicError.contextUndefined();
			}
			if (!(context.getContextItem() instanceof NodeType)) {
				throw new DynamicError(TypeError.invalid_type(null));
			}
			nodeType = (NodeType) context.getContextItem();
			if (nodeType.node_value().getOwnerDocument() == null) {
				throw DynamicError.contextUndefined();
			}
		}
		
		Node node = nodeType.node_value();
//		if (node.getOwnerDocument() == null) {
//			// W3C Test suite seems to want XPDY0002
//			throw DynamicError.contextUndefined();
//			//throw DynamicError.noContextDoc();
//		}
		
		if (hasIDREF(idrefs, node)) {
			ElementType element = new ElementType((Element) node, context.getStaticContext().getTypeModel());
			rs.add(element);
		}
		
		processAttributes(node, idrefs, rs, context);
		processChildNodes(node, idrefs, rs, context);

		return rs.getSequence();
	}
	
	private static ArrayList<XSIDREF> createIDRefs(String[] idReftokens) {
		ArrayList<XSIDREF> xsidRef = new ArrayList<XSIDREF>();
		for (String idReftoken : idReftokens) {
			XSIDREF idref = new XSIDREF(idReftoken);
			xsidRef.add(idref);
		}
		return xsidRef;
	}
	
	private static void processChildNodes(Node node, List<XSIDREF> idrefs, ResultBuffer rs, EvaluationContext context) {
		if (!node.hasChildNodes()) {
			return;
		}
		
		NodeList nodeList = node.getChildNodes();
		for (int nodecnt = 0; nodecnt < nodeList.getLength(); nodecnt++) {
			Node childNode = nodeList.item(nodecnt);
			if (childNode.getNodeType() == Node.ELEMENT_NODE && !isDuplicate(childNode, rs)) {
				ElementType element = new ElementType((Element)childNode, context.getStaticContext().getTypeModel());
				if (element.isID()) {
					if (hasIDREF(idrefs, childNode)) {
						rs.add(element);
					}
				} 
				processAttributes(childNode, idrefs, rs, context);
				processChildNodes(childNode, idrefs, rs, context);
			}
		}

	}
	
	private static void processAttributes(Node node, List<XSIDREF> idrefs, ResultBuffer rs, EvaluationContext context) {
		if (!node.hasAttributes()) {
			return;
		}
		
		NamedNodeMap attributeList = node.getAttributes();
		for (int atsub = 0; atsub < attributeList.getLength(); atsub++) {
			Attr atNode = (Attr) attributeList.item(atsub);
			NodeType atType = new AttrType(atNode, context.getStaticContext().getTypeModel());
			if (atType.isID()) {
				if (hasIDREF(idrefs, atNode)) {
					if (!isDuplicate(node, rs)) {
						ElementType element = new ElementType((Element)node, context.getStaticContext().getTypeModel());
						rs.add(element);
					}
				}
			}
		}
	}
	
	private static boolean hasIDREF(List<XSIDREF> idrefs, Node node) {
		for (XSIDREF idref : idrefs) {
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
