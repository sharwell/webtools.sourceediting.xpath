/*******************************************************************************
 * Copyright (c) 2005, 2009 Andrea Bittau, University College London, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sam Harwell - parser implementation using ANTLR
 *******************************************************************************/

package org.eclipse.wst.xml.xpath2.processor.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.AddExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.AndExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.AnyKindTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.AttributeTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.AxisStep;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.CastExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.CastableExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.CmpExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.CntxItemExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.CommentTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.DecimalLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.DivExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.DocumentTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.DoubleLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ElementTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ExceptExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.Expr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.FilterExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ForExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ForwardStep;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.FunctionCall;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.IDivExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.IfExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.InstOfExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.IntegerLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.IntersectExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ItemType;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.KindTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.Literal;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.MinusExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ModExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.MulExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.NameTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.NodeTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.NumericLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.OrExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PITest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ParExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PipeExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PlusExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PrimaryExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.QuantifiedExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.RangeExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ReverseStep;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SchemaAttrTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SchemaElemTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SequenceType;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SingleType;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.StepExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.StringLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SubExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.TextTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.TreatAsExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.UnionExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.VarExprPair;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.VarRef;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.LiteralUtils;

/**
 *
 * @author sam
 */
public class XPathBuilderVisitor extends AbstractParseTreeVisitor<Object> implements XPathParserVisitor<Object> {
	public static final XPathBuilderVisitor INSTANCE = new XPathBuilderVisitor();

	@Override
	public AnyKindTest visitAnyKindTest(XPathParser.AnyKindTestContext ctx) {
		return new AnyKindTest();
	}

	@Override
	public DecimalLiteral visitDecimalLiteral(XPathParser.DecimalLiteralContext ctx) {
		return new DecimalLiteral(new BigDecimal(ctx.DECIMAL().getText()));
	}

	@Override
	public String visitUnreservedNCName(XPathParser.UnreservedNCNameContext ctx) {
		return ctx.getChild(0).getText();
	}

	@Override
	public Expr visitInstanceOfExpr(XPathParser.InstanceOfExprContext ctx) {
		Expr treatExpr = visitTreatExpr(ctx.treatExpr());
		if (ctx.INSTANCE() == null) {
			return treatExpr;
		}

		return new InstOfExpr(treatExpr, visitSequenceType(ctx.sequenceType()));
	}

	@Override
	public Integer visitForwardAxis(XPathParser.ForwardAxisContext ctx) {
		switch (((TerminalNode)ctx.getChild(0)).getSymbol().getType()) {
		case XPathLexer.CHILD:
			return ForwardStep.CHILD;
		case XPathLexer.DESCENDANT:
			return ForwardStep.DESCENDANT;
		case XPathLexer.ATTRIBUTE:
			return ForwardStep.ATTRIBUTE;
		case XPathLexer.SELF:
			return ForwardStep.SELF;
		case XPathLexer.DESCENDANT_OR_SELF:
			return ForwardStep.DESCENDANT_OR_SELF;
		case XPathLexer.FOLLOWING_SIBLING:
			return ForwardStep.FOLLOWING_SIBLING;
		case XPathLexer.FOLLOWING:
			return ForwardStep.FOLLOWING;
		case XPathLexer.NAMESPACE:
			return ForwardStep.NAMESPACE;
		default:
			assert false;
			return ForwardStep.NONE;
		}
	}

	@Override
	public ElementTest visitElementTest(XPathParser.ElementTestContext ctx) {
		if (ctx.elementNameOrWildcard() != null) {
			QName elementNameOrWildcard = visitElementNameOrWildcard(ctx.elementNameOrWildcard());
			boolean wildcard = elementNameOrWildcard == null;
			if (ctx.typeName() != null) {
				QName typeName = visitTypeName(ctx.typeName());
				if (ctx.QUESTIONMARK() != null) {
					return new ElementTest(elementNameOrWildcard, wildcard, typeName, true);
				}
				else {
					return new ElementTest(elementNameOrWildcard, wildcard, typeName);
				}
			}
			else {
				return new ElementTest(elementNameOrWildcard, wildcard);
			}
		}
		else {
			return new ElementTest();
		}
	}

	@Override
	public Integer visitReverseAxis(XPathParser.ReverseAxisContext ctx) {
		switch (((TerminalNode)ctx.getChild(0)).getSymbol().getType()) {
		case XPathLexer.PARENT:
			return ReverseStep.PARENT;
		case XPathLexer.ANCESTOR:
			return ReverseStep.ANCESTOR;
		case XPathLexer.PRECEDING_SIBLING:
			return ReverseStep.PRECEDING_SIBLING;
		case XPathLexer.PRECEDING:
			return ReverseStep.PRECEDING;
		case XPathLexer.ANCESTOR_OR_SELF:
			return ReverseStep.ANCESTOR_OR_SELF;
		default:
			assert false;
			return ReverseStep.DOTDOT;
		}
	}

	@Override
	public String visitPrefix(XPathParser.PrefixContext ctx) {
		return visitNCName(ctx.nCName());
	}

	@Override
	public Expr visitTreatExpr(XPathParser.TreatExprContext ctx) {
		Expr castableExpr = visitCastableExpr(ctx.castableExpr());
		if (ctx.TREAT() == null) {
			return castableExpr;
		}

		return new TreatAsExpr(castableExpr, visitSequenceType(ctx.sequenceType()));
	}

	@Override
	public NameTest visitNameTest(XPathParser.NameTestContext ctx) {
		if (ctx.qName() != null) {
			return new NameTest(visitQName(ctx.qName()));
		}
		else {
			return new NameTest(visitWildcard(ctx.wildcard()));
		}
	}

	@Override
	public CntxItemExpr visitContextItemExpr(XPathParser.ContextItemExprContext ctx) {
		return new CntxItemExpr();
	}

	@Override
	public Expr visitAdditiveExpr(XPathParser.AdditiveExprContext ctx) {
		Expr multiplicativeExpr = visitMultiplicativeExpr(ctx.multiplicativeExpr());
		if (ctx.additiveExpr() == null) {
			return multiplicativeExpr;
		}

		if (ctx.PLUS() != null) {
			return new AddExpr(visitAdditiveExpr(ctx.additiveExpr()), multiplicativeExpr);
		}
		else {
			return new SubExpr(visitAdditiveExpr(ctx.additiveExpr()), multiplicativeExpr);
		}
	}

	@Override
	public VarRef visitVarRef(XPathParser.VarRefContext ctx) {
		return new VarRef(visitVarName(ctx.varName()));
	}

	@Override
	public QName visitElementName(XPathParser.ElementNameContext ctx) {
		return visitQName(ctx.qName());
	}

	@Override
	public Collection<Expr> visitPredicate(XPathParser.PredicateContext ctx) {
		return visitExpr(ctx.expr());
	}

	@Override
	public Collection<Collection<Expr>> visitPredicateList(XPathParser.PredicateListContext ctx) {
		Collection<Collection<Expr>> result = new ArrayList<Collection<Expr>>();
		for (XPathParser.PredicateContext predicate : ctx.predicate()) {
			result.add(visitPredicate(predicate));
		}

		return result;
	}

	@Override
	public SchemaAttrTest visitSchemaAttributeTest(XPathParser.SchemaAttributeTestContext ctx) {
		return new SchemaAttrTest(visitAttributeDeclaration(ctx.attributeDeclaration()));
	}

	@Override
	public Collection<Expr> visitParenthesizedExpr(XPathParser.ParenthesizedExprContext ctx) {
		if (ctx.expr() == null) {
			return new ArrayList<Expr>();
		}

		return visitExpr(ctx.expr());
	}

	@Override
	public NumericLiteral visitNumericLiteral(XPathParser.NumericLiteralContext ctx) {
		if (ctx.integerLiteral() != null) {
			return visitIntegerLiteral(ctx.integerLiteral());
		}
		else if (ctx.decimalLiteral() != null) {
			return visitDecimalLiteral(ctx.decimalLiteral());
		}
		else {
			return visitDoubleLiteral(ctx.doubleLiteral());
		}
	}

	@Override
	public QName visitElementDeclaration(XPathParser.ElementDeclarationContext ctx) {
		return visitElementName(ctx.elementName());
	}

	@Override
	public Expr visitExprSingle(XPathParser.ExprSingleContext ctx) {
		if (ctx.forExpr() != null) {
			return visitForExpr(ctx.forExpr());
		}
		else if (ctx.quantifiedExpr() != null) {
			return visitQuantifiedExpr(ctx.quantifiedExpr());
		}
		else if (ctx.ifExpr() != null) {
			return visitIfExpr(ctx.ifExpr());
		}
		else {
			return visitOrExpr(ctx.orExpr());
		}
	}

	@Override
	public Expr visitOrExpr(XPathParser.OrExprContext ctx) {
		Expr andExpr = visitAndExpr(ctx.andExpr());
		if (ctx.OR() == null) {
			return andExpr;
		}

		return new OrExpr(visitOrExpr(ctx.orExpr()), andExpr);
	}

	@Override
	public Integer visitOccurrenceIndicator(XPathParser.OccurrenceIndicatorContext ctx) {
		if (ctx.QUESTIONMARK() != null) {
			return SequenceType.QUESTION;
		}
		else if (ctx.STAR() != null) {
			return SequenceType.STAR;
		}
		else {
			return SequenceType.PLUS;
		}
	}

	@Override
	public KindTest visitKindTest(XPathParser.KindTestContext ctx) {
		if (ctx.documentTest() != null) {
			return visitDocumentTest(ctx.documentTest());
		}
		else if (ctx.elementTest() != null) {
			return visitElementTest(ctx.elementTest());
		}
		else if (ctx.attributeTest() != null) {
			return visitAttributeTest(ctx.attributeTest());
		}
		else if (ctx.schemaElementTest() != null) {
			return visitSchemaElementTest(ctx.schemaElementTest());
		}
		else if (ctx.schemaAttributeTest() != null) {
			return visitSchemaAttributeTest(ctx.schemaAttributeTest());
		}
		else if (ctx.pITest() != null) {
			return visitPITest(ctx.pITest());
		}
		else if (ctx.commentTest() != null) {
			return visitCommentTest(ctx.commentTest());
		}
		else if (ctx.textTest() != null) {
			return visitTextTest(ctx.textTest());
		}
		else {
			return visitAnyKindTest(ctx.anyKindTest());
		}
	}

	@Override
	public StringLiteral visitStringLiteral(XPathParser.StringLiteralContext ctx) {
		return new StringLiteral(LiteralUtils.unquote(ctx.STRING().getText()));
	}

	@Override
	public Expr visitPathExpr(XPathParser.PathExprContext ctx) {
		if (ctx.relativePathExpr() != null) {
			XPathExpr relativePathExpr = visitRelativePathExpr(ctx.relativePathExpr());
			if (ctx.FORWARD_SLASH() != null) {
				relativePathExpr.set_slashes(1);
			}
			else if (ctx.FORWARD_SLASHSLASH() != null) {
				relativePathExpr.set_slashes(2);
			}

			return relativePathExpr;
		}
		else {
			return new XPathExpr(1, null);
		}
	}

	@Override
	public FunctionCall visitFunctionCall(XPathParser.FunctionCallContext ctx) {
		if (ctx.functionCallMiddle() != null) {
			return new FunctionCall(visitUnreservedQName(ctx.unreservedQName()), visitFunctionCallMiddle(ctx.functionCallMiddle()));
		}
		else {
			return new FunctionCall(visitUnreservedQName(ctx.unreservedQName()), new ArrayList<Expr>());
		}
	}

	@Override
	public DoubleLiteral visitDoubleLiteral(XPathParser.DoubleLiteralContext ctx) {
		return new DoubleLiteral(new Double(ctx.DOUBLE().getText()));
	}

	@Override
	public ForwardStep visitForwardStep(XPathParser.ForwardStepContext ctx) {
		if (ctx.forwardAxis() != null) {
			return new ForwardStep(visitForwardAxis(ctx.forwardAxis()), visitNodeTest(ctx.nodeTest()));
		}
		else {
			return visitAbbrevForwardStep(ctx.abbrevForwardStep());
		}
	}

	@Override
	public SequenceType visitSequenceType(XPathParser.SequenceTypeContext ctx) {
		if (ctx.itemType() != null) {
			ItemType itemType = visitItemType(ctx.itemType());
			if (ctx.occurrenceIndicator() != null) {
				return new SequenceType(visitOccurrenceIndicator(ctx.occurrenceIndicator()), itemType);
			}
			else {
				return new SequenceType(SequenceType.NONE, itemType);
			}
		}
		else {
			return new SequenceType(SequenceType.EMPTY, null);
		}
	}

	@Override
	public PITest visitPITest(XPathParser.PITestContext ctx) {
		if (ctx.nCName() != null) {
			return new PITest(visitNCName(ctx.nCName()));
		}
		else if (ctx.stringLiteral() != null) {
			return new PITest(visitStringLiteral(ctx.stringLiteral()).string());
		}
		else {
			return new PITest();
		}
	}

	@Override
	public QName visitQName(XPathParser.QNameContext ctx) {
		if (ctx.COLON() == null) {
			return new QName(visitNCName(ctx.nCName(0)));
		}
		else {
			return new QName(visitNCName(ctx.nCName(0)), visitNCName(ctx.nCName(1)));
		}
	}

	@Override
	public ItemType visitItemType(XPathParser.ItemTypeContext ctx) {
		if (ctx.atomicType() != null) {
			return new ItemType(ItemType.QNAME, visitAtomicType(ctx.atomicType()));
		}
		else if (ctx.kindTest() != null) {
			return new ItemType(ItemType.KINDTEST, visitKindTest(ctx.kindTest()));
		}
		else {
			return new ItemType(ItemType.ITEM, null);
		}
	}

	@Override
	public QName visitAtomicType(XPathParser.AtomicTypeContext ctx) {
		return visitQName(ctx.qName());
	}

	@Override
	public QName visitVarName(XPathParser.VarNameContext ctx) {
		return visitQName(ctx.qName());
	}

	@Override
	public Collection<VarExprPair> visitQuantifiedExprMiddle(XPathParser.QuantifiedExprMiddleContext ctx) {
		Collection<VarExprPair> result;
		if (ctx.quantifiedExprMiddle() != null) {
			result = visitQuantifiedExprMiddle(ctx.quantifiedExprMiddle());
		}
		else {
			result = new ArrayList<VarExprPair>();
		}

		result.add(new VarExprPair(visitVarName(ctx.varName()), visitExprSingle(ctx.exprSingle())));
		return result;
	}

	@Override
	public NodeTest visitNodeTest(XPathParser.NodeTestContext ctx) {
		if (ctx.kindTest() != null) {
			return visitKindTest(ctx.kindTest());
		}
		else {
			return visitNameTest(ctx.nameTest());
		}
	}

	@Override
	public SchemaElemTest visitSchemaElementTest(XPathParser.SchemaElementTestContext ctx) {
		return new SchemaElemTest(visitElementDeclaration(ctx.elementDeclaration()));
	}

	@Override
	public Expr visitCastExpr(XPathParser.CastExprContext ctx) {
		Expr unaryExpr = visitUnaryExpr(ctx.unaryExpr());
		if (ctx.CAST() == null) {
			return unaryExpr;
		}

		return new CastExpr(unaryExpr, visitSingleType(ctx.singleType()));
	}

	@Override
	public QName visitTypeName(XPathParser.TypeNameContext ctx) {
		return visitQName(ctx.qName());
	}

	@Override
	public QName visitUnreservedQName(XPathParser.UnreservedQNameContext ctx) {
		if (ctx.COLON() != null) {
			return new QName(visitNCName(ctx.nCName(0)), visitNCName(ctx.nCName(1)));
		}
		else {
			return new QName(visitUnreservedNCName(ctx.unreservedNCName()));
		}
	}

	@Override
	public String visitNCName(XPathParser.NCNameContext ctx) {
		return ctx.getChild(0).getText();
	}

	@Override
	public Integer visitGeneralComp(XPathParser.GeneralCompContext ctx) {
		switch (((TerminalNode)ctx.getChild(0)).getSymbol().getType()) {
		case XPathLexer.EQUALS:
			return CmpExpr.EQUALS;
		case XPathLexer.NOTEQUALS:
			return CmpExpr.NOTEQUALS;
		case XPathLexer.LESSTHAN:
			return CmpExpr.LESSTHAN;
		case XPathLexer.LESSEQUAL:
			return CmpExpr.LESSEQUAL;
		case XPathLexer.GREATER:
			return CmpExpr.GREATER;
		case XPathLexer.GREATEREQUAL:
			return CmpExpr.GREATEREQUAL;
		default:
			assert false;
			return 0;
		}
	}

	@Override
	public QName visitWildcard(XPathParser.WildcardContext ctx) {
		if (ctx.COLON() == null) {
			return new QName("*", "*");
		}
		else if (ctx.getChild(0) instanceof TerminalNode) {
			return new QName("*", visitNCName(ctx.nCName()));
		}
		else {
			return new QName(visitNCName(ctx.nCName()), "*");
		}
	}

	@Override
	public Literal visitLiteral(XPathParser.LiteralContext ctx) {
		if (ctx.numericLiteral() != null) {
			return visitNumericLiteral(ctx.numericLiteral());
		}
		else {
			return visitStringLiteral(ctx.stringLiteral());
		}
	}

	@Override
	public ForwardStep visitAbbrevForwardStep(XPathParser.AbbrevForwardStepContext ctx) {
		NodeTest nodeTest = visitNodeTest(ctx.nodeTest());
		if (ctx.AT_SYM() != null) {
			return new ForwardStep(ForwardStep.AT_SYM, nodeTest);
		}
		else {
			return new ForwardStep(ForwardStep.NONE, nodeTest);
		}
	}

	@Override
	public ForExpr visitForExpr(XPathParser.ForExprContext ctx) {
		return new ForExpr(visitSimpleForClause(ctx.simpleForClause()), visitExprSingle(ctx.exprSingle()));
	}

	@Override
	public Expr visitUnaryExpr(XPathParser.UnaryExprContext ctx) {
		if (ctx.MINUS() != null) {
			return new MinusExpr(visitUnaryExpr(ctx.unaryExpr()));
		}
		else if (ctx.PLUS() != null) {
			return new PlusExpr(visitUnaryExpr(ctx.unaryExpr()));
		}
		else {
			return visitValueExpr(ctx.valueExpr());
		}
	}

	@Override
	public Expr visitValueExpr(XPathParser.ValueExprContext ctx) {
		return visitPathExpr(ctx.pathExpr());
	}

	@Override
	public PrimaryExpr visitPrimaryExpr(XPathParser.PrimaryExprContext ctx) {
		if (ctx.literal() != null) {
			return visitLiteral(ctx.literal());
		}
		else if (ctx.varRef() != null) {
			return visitVarRef(ctx.varRef());
		}
		else if (ctx.parenthesizedExpr() != null) {
			return new ParExpr(visitParenthesizedExpr(ctx.parenthesizedExpr()));
		}
		else if (ctx.contextItemExpr() != null) {
			return visitContextItemExpr(ctx.contextItemExpr());
		}
		else {
			return visitFunctionCall(ctx.functionCall());
		}
	}

	@Override
	public Collection<VarExprPair> visitSimpleForClause(XPathParser.SimpleForClauseContext ctx) {
		Collection<VarExprPair> result;
		if (ctx.simpleForClause() != null) {
			result = visitSimpleForClause(ctx.simpleForClause());
		}
		else {
			result = new ArrayList<VarExprPair>();
		}

		result.add(new VarExprPair(visitVarName(ctx.varName()), visitExprSingle(ctx.exprSingle())));
		return result;
	}

	@Override
	public QName visitAttributeDeclaration(XPathParser.AttributeDeclarationContext ctx) {
		return visitAttributeName(ctx.attributeName());
	}

	@Override
	public String visitLocalPart(XPathParser.LocalPartContext ctx) {
		return visitNCName(ctx.nCName());
	}

	@Override
	public SingleType visitSingleType(XPathParser.SingleTypeContext ctx) {
		QName atomicType = visitAtomicType(ctx.atomicType());
		if (ctx.QUESTIONMARK() != null) {
			return new SingleType(atomicType, true);
		}
		else {
			return new SingleType(atomicType);
		}
	}

	@Override
	public Integer visitNodeComp(XPathParser.NodeCompContext ctx) {
		if (ctx.IS() != null) {
			return CmpExpr.IS;
		}
		else if (ctx.LESS_LESS() != null) {
			return CmpExpr.LESS_LESS;
		}
		else {
			return CmpExpr.GREATER_GREATER;
		}
	}

	@Override
	public QName visitElementNameOrWildcard(XPathParser.ElementNameOrWildcardContext ctx) {
		if (ctx.elementName() != null) {
			return visitElementName(ctx.elementName());
		}
		else {
			return null;
		}
	}

	@Override
	public IfExpr visitIfExpr(XPathParser.IfExprContext ctx) {
		Collection<Expr> condition = visitExpr(ctx.expr());
		Expr then = visitExprSingle(ctx.exprSingle(0));
		Expr els = visitExprSingle(ctx.exprSingle(1));
		return new IfExpr(condition, then, els);
	}

	@Override
	public Collection<Expr> visitExpr(XPathParser.ExprContext ctx) {
		Collection<Expr> result = new ArrayList<Expr>();
		for (XPathParser.ExprSingleContext ex : ctx.exprSingle()) {
			result.add(visitExprSingle(ex));
		}

		return result;
	}

	@Override
	public QName visitAttributeName(XPathParser.AttributeNameContext ctx) {
		return visitQName(ctx.qName());
	}

	@Override
	public ReverseStep visitReverseStep(XPathParser.ReverseStepContext ctx) {
		if (ctx.reverseAxis() != null) {
			return new ReverseStep(visitReverseAxis(ctx.reverseAxis()), visitNodeTest(ctx.nodeTest()));
		}
		else {
			return visitAbbrevReverseStep(ctx.abbrevReverseStep());
		}
	}

	@Override
	public Expr visitCastableExpr(XPathParser.CastableExprContext ctx) {
		Expr castExpr = visitCastExpr(ctx.castExpr());
		if (ctx.CASTABLE() == null) {
			return castExpr;
		}

		return new CastableExpr(castExpr, visitSingleType(ctx.singleType()));
	}

	@Override
	public QuantifiedExpr visitQuantifiedExpr(XPathParser.QuantifiedExprContext ctx) {
		if (ctx.SOME() != null) {
			return new QuantifiedExpr(QuantifiedExpr.SOME, visitQuantifiedExprMiddle(ctx.quantifiedExprMiddle()), visitExprSingle(ctx.exprSingle()));
		}
		else {
			return new QuantifiedExpr(QuantifiedExpr.ALL, visitQuantifiedExprMiddle(ctx.quantifiedExprMiddle()), visitExprSingle(ctx.exprSingle()));
		}
	}

	@Override
	public Expr visitComparisonExpr(XPathParser.ComparisonExprContext ctx) {
		if (ctx.rangeExpr(1) == null) {
			return visitRangeExpr(ctx.rangeExpr(0));
		}

		if (ctx.valueComp() != null) {
			return new CmpExpr(visitRangeExpr(ctx.rangeExpr(0)), visitRangeExpr(ctx.rangeExpr(1)), visitValueComp(ctx.valueComp()));
		}
		else if (ctx.generalComp() != null) {
			return new CmpExpr(visitRangeExpr(ctx.rangeExpr(0)), visitRangeExpr(ctx.rangeExpr(1)), visitGeneralComp(ctx.generalComp()));
		}
		else {
			return new CmpExpr(visitRangeExpr(ctx.rangeExpr(0)), visitRangeExpr(ctx.rangeExpr(1)), visitNodeComp(ctx.nodeComp()));
		}
	}

	@Override
	public FilterExpr visitFilterExpr(XPathParser.FilterExprContext ctx) {
		return new FilterExpr(visitPrimaryExpr(ctx.primaryExpr()), visitPredicateList(ctx.predicateList()));
	}

	@Override
	public Expr visitUnionExpr(XPathParser.UnionExprContext ctx) {
		Expr intersectExceptExpr = visitIntersectExceptExpr(ctx.intersectExceptExpr());
		if (ctx.unionExpr() == null) {
			return intersectExceptExpr;
		}

		if (ctx.UNION() != null) {
			return new UnionExpr(visitUnionExpr(ctx.unionExpr()), intersectExceptExpr);
		}
		else {
			return new PipeExpr(visitUnionExpr(ctx.unionExpr()), intersectExceptExpr);
		}
	}

	@Override
	public AxisStep visitAxisStep(XPathParser.AxisStepContext ctx) {
		if (ctx.forwardStep() != null) {
			return new AxisStep(visitForwardStep(ctx.forwardStep()), visitPredicateList(ctx.predicateList()));
		}
		else {
			return new AxisStep(visitReverseStep(ctx.reverseStep()), visitPredicateList(ctx.predicateList()));
		}
	}

	@Override
	public ReverseStep visitAbbrevReverseStep(XPathParser.AbbrevReverseStepContext ctx) {
		return new ReverseStep(ReverseStep.DOTDOT, null);
	}

	@Override
	public AttributeTest visitAttributeTest(XPathParser.AttributeTestContext ctx) {
		if (ctx.attribNameOrWildcard() != null) {
			QName attribNameOrWildcard = visitAttribNameOrWildcard(ctx.attribNameOrWildcard());
			boolean wildcard = attribNameOrWildcard == null;
			if (ctx.typeName() != null) {
				return new AttributeTest(attribNameOrWildcard, wildcard, visitTypeName(ctx.typeName()));
			}
			else {
				return new AttributeTest(attribNameOrWildcard, wildcard);
			}
		}
		else {
			return new AttributeTest();
		}
	}

	@Override
	public XPathExpr visitRelativePathExpr(XPathParser.RelativePathExprContext ctx) {
		StepExpr stepExpr = visitStepExpr(ctx.stepExpr());
		if (ctx.FORWARD_SLASH() != null) {
			XPathExpr relativePathExpr = visitRelativePathExpr(ctx.relativePathExpr());
			relativePathExpr.add_tail(1, stepExpr);
			return relativePathExpr;
		}
		else if (ctx.FORWARD_SLASHSLASH() != null) {
			XPathExpr relativePathExpr = visitRelativePathExpr(ctx.relativePathExpr());
			relativePathExpr.add_tail(2, stepExpr);
			return relativePathExpr;
		}
		else {
			return new XPathExpr(0, stepExpr);
		}
	}

	@Override
	public CommentTest visitCommentTest(XPathParser.CommentTestContext ctx) {
		return new CommentTest();
	}

	@Override
	public QName visitAttribNameOrWildcard(XPathParser.AttribNameOrWildcardContext ctx) {
		if (ctx.attributeName() != null) {
			return visitAttributeName(ctx.attributeName());
		}
		else {
			return null;
		}
	}

	@Override
	public TextTest visitTextTest(XPathParser.TextTestContext ctx) {
		return new TextTest();
	}

	@Override
	public Expr visitIntersectExceptExpr(XPathParser.IntersectExceptExprContext ctx) {
		Expr instanceOfExpr = visitInstanceOfExpr(ctx.instanceOfExpr());
		if (ctx.intersectExceptExpr() == null) {
			return instanceOfExpr;
		}

		if (ctx.INTERSECT() != null) {
			return new IntersectExpr(visitIntersectExceptExpr(ctx.intersectExceptExpr()), instanceOfExpr);
		}
		else {
			return new ExceptExpr(visitIntersectExceptExpr(ctx.intersectExceptExpr()), instanceOfExpr);
		}
	}

	@Override
	public Integer visitValueComp(XPathParser.ValueCompContext ctx) {
		switch (((TerminalNode)ctx.getChild(0)).getSymbol().getType()) {
		case XPathLexer.EQ:
			return CmpExpr.EQ;
		case XPathLexer.NE:
			return CmpExpr.NE;
		case XPathLexer.LT:
			return CmpExpr.LT;
		case XPathLexer.LE:
			return CmpExpr.LE;
		case XPathLexer.GT:
			return CmpExpr.GT;
		case XPathLexer.GE:
			return CmpExpr.GE;
		default:
			assert false;
			return 0;
		}
	}

	@Override
	public IntegerLiteral visitIntegerLiteral(XPathParser.IntegerLiteralContext ctx) {
		return new IntegerLiteral(new BigInteger(ctx.INTEGER().getText()));
	}

	@Override
	public XPath visitXPath(XPathParser.XPathContext ctx) {
		return new XPath(visitExpr(ctx.expr()));
	}

	@Override
	public Expr visitMultiplicativeExpr(XPathParser.MultiplicativeExprContext ctx) {
		Expr unionExpr = visitUnionExpr(ctx.unionExpr());
		if (ctx.multiplicativeExpr() == null) {
			return unionExpr;
		}

		if (ctx.STAR() != null) {
			return new MulExpr(visitMultiplicativeExpr(ctx.multiplicativeExpr()), unionExpr);
		}
		else if (ctx.DIV() != null) {
			return new DivExpr(visitMultiplicativeExpr(ctx.multiplicativeExpr()), unionExpr);
		}
		else if (ctx.IDIV() != null) {
			return new IDivExpr(visitMultiplicativeExpr(ctx.multiplicativeExpr()), unionExpr);
		}
		else {
			return new ModExpr(visitMultiplicativeExpr(ctx.multiplicativeExpr()), unionExpr);
		}
	}

	@Override
	public DocumentTest visitDocumentTest(XPathParser.DocumentTestContext ctx) {
		if (ctx.elementTest() != null) {
			return new DocumentTest(DocumentTest.ELEMENT, visitElementTest(ctx.elementTest()));
		}
		else if (ctx.schemaElementTest() != null) {
			return new DocumentTest(DocumentTest.SCHEMA_ELEMENT, visitSchemaElementTest(ctx.schemaElementTest()));
		}
		else {
			return new DocumentTest();
		}
	}

	@Override
	public Expr visitRangeExpr(XPathParser.RangeExprContext ctx) {
		if (ctx.TO() == null) {
			return visitAdditiveExpr(ctx.additiveExpr(0));
		}

		return new RangeExpr(visitAdditiveExpr(ctx.additiveExpr(0)), visitAdditiveExpr(ctx.additiveExpr(1)));
	}

	@Override
	public Expr visitAndExpr(XPathParser.AndExprContext ctx) {
		Expr comparisonExpr = visitComparisonExpr(ctx.comparisonExpr());
		if (ctx.AND() == null) {
			return comparisonExpr;
		}

		return new AndExpr(visitAndExpr(ctx.andExpr()), comparisonExpr);
	}

	@Override
	public StepExpr visitStepExpr(XPathParser.StepExprContext ctx) {
		if (ctx.axisStep() != null) {
			return visitAxisStep(ctx.axisStep());
		}
		else {
			return visitFilterExpr(ctx.filterExpr());
		}
	}

	@Override
	public Collection<Expr> visitFunctionCallMiddle(XPathParser.FunctionCallMiddleContext ctx) {
		Collection<Expr> result;
		if (ctx.functionCallMiddle() != null) {
			result = visitFunctionCallMiddle(ctx.functionCallMiddle());
		}
		else {
			result = new ArrayList<Expr>();
		}

		result.add(visitExprSingle(ctx.exprSingle()));
		return result;
	}
	
}
