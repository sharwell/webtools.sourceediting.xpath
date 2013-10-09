/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrea Bittau - initial API and implementation from the PsychoPath XPath 2.0 
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal.ast;

import org.eclipse.wst.xml.xpath2.processor.ast.XPath;

/**
 * Visitor class for XPath expressions.
 */
public interface XPathVisitor<T> {
	/**
	 * Visit XPath.
	 */
	public T visit(XPath xp);

	/**
	 * Visit ForExpr.
	 */
	public T visit(ForExpr fex);

	/**
	 * Visit QuantifiedExpr.
	 */
	public T visit(QuantifiedExpr qex);

	/**
	 * Visit IfExpr.
	 */
	public T visit(IfExpr ifex);

	/**
	 * Visit OrExpr.
	 */
	public T visit(OrExpr orex);

	/**
	 * Visit AndExpr.
	 */
	public T visit(AndExpr andex);

	/**
	 * Visit CmpExpr.
	 */
	public T visit(CmpExpr cmpex);

	/**
	 * Visit RangeExpr.
	 */
	public T visit(RangeExpr rex);

	/**
	 * Visit AddExpr.
	 */
	public T visit(AddExpr addex);

	/**
	 * Visit SubExpr.
	 */
	public T visit(SubExpr subex);

	/**
	 * Visit MulExpr.
	 */
	public T visit(MulExpr mulex);

	/**
	 * Visit DivExpr.
	 */
	public T visit(DivExpr mulex);

	/**
	 * Visit IDivExpr.
	 */
	public T visit(IDivExpr mulex);

	/**
	 * Visit ModExpr.
	 */
	public T visit(ModExpr mulex);

	/**
	 * Visit UnionExpr.
	 */
	public T visit(UnionExpr unex);

	/**
	 * Visit PipeExpr.
	 */
	public T visit(PipeExpr pipex);

	/**
	 * Visit IntersectExpr.
	 */
	public T visit(IntersectExpr iexpr);

	/**
	 * Visit ExceptExpr.
	 */
	public T visit(ExceptExpr eexpr);

	/**
	 * Visit InstOfExpr.
	 */
	public T visit(InstOfExpr ioexp);

	/**
	 * Visit TreatAsExpr.
	 */
	public T visit(TreatAsExpr taexp);

	/**
	 * Visit CastableExpr.
	 */
	public T visit(CastableExpr cexp);

	/**
	 * Visit CastExpr.
	 */
	public T visit(CastExpr cexp);

	/**
	 * Visit MinusExpr.
	 */
	public T visit(MinusExpr e);

	/**
	 * Visit PlusExpr.
	 */
	public T visit(PlusExpr e);

	/**
	 * Visit XPathExpr.
	 */
	public T visit(XPathExpr e);

	/**
	 * Visit ForwardStep.
	 */
	public T visit(ForwardStep e);

	/**
	 * Visit ReverseStep.
	 */
	public T visit(ReverseStep e);

	/**
	 * Visit NameTest.
	 */
	public T visit(NameTest e);

	/**
	 * Visit VarRef.
	 */
	public T visit(VarRef e);

	/**
	 * Visit StringLiteral.
	 */
	public T visit(StringLiteral e);

	/**
	 * Visit IntegerLiteral.
	 */
	public T visit(IntegerLiteral e);

	/**
	 * Visit DoubleLiteral.
	 */
	public T visit(DoubleLiteral e);

	/**
	 * Visit DecimalLiteral.
	 */
	public T visit(DecimalLiteral e);

	/**
	 * Visit ParExpr.
	 */
	public T visit(ParExpr e);

	/**
	 * Visit CntxItemExpr.
	 */
	public T visit(CntxItemExpr e);

	/**
	 * Visit FunctionCall.
	 */
	public T visit(FunctionCall e);

	/**
	 * Visit SingleType.
	 */
	public T visit(SingleType e);

	/**
	 * Visit SequenceType.
	 */
	public T visit(SequenceType e);

	/**
	 * Visit ItemType.
	 */
	public T visit(ItemType e);

	/**
	 * Visit AnyKindTest.
	 */
	public T visit(AnyKindTest e);

	/**
	 * Visit DocumentTest.
	 */
	public T visit(DocumentTest e);

	/**
	 * Visit TextTest.
	 */
	public T visit(TextTest e);

	/**
	 * Visit CommentTest.
	 */
	public T visit(CommentTest e);

	/**
	 * Visit PITest.
	 */
	public T visit(PITest e);

	/**
	 * Visit AttributeTest.
	 */
	public T visit(AttributeTest e);

	/**
	 * Visit SchemaAttrTest.
	 */
	public T visit(SchemaAttrTest e);

	/**
	 * Visit ElementTest.
	 */
	public T visit(ElementTest e);

	/**
	 * Visit SchemElemTest.
	 */
	public T visit(SchemaElemTest e);

	/**
	 * Visit AxisStep.
	 */
	public T visit(AxisStep e);

	/**
	 * Visit FilterExpr.
	 */
	public T visit(FilterExpr e);
}
