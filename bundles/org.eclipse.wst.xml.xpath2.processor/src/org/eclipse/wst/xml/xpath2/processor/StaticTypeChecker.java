/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eclipse.wst.xml.xpath2.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.wst.xml.xpath2.api.AtomicItemType;
import org.eclipse.wst.xml.xpath2.api.Function;
import org.eclipse.wst.xml.xpath2.api.FunctionLibrary;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.typesystem.NodeItemType;
import org.eclipse.wst.xml.xpath2.processor.internal.SeqType;
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
import org.eclipse.wst.xml.xpath2.processor.internal.ast.MinusExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ModExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.MulExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.NameTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.OrExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PITest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ParExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PipeExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.PlusExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.QuantifiedExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.RangeExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.ReverseStep;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SchemaAttrTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SchemaElemTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SequenceType;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SingleType;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.StringLiteral;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.SubExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.TextTest;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.TreatAsExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.UnionExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.VarExprPair;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.VarRef;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathNode;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathVisitor;
import org.eclipse.wst.xml.xpath2.processor.internal.function.Constructor;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AttrType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.CommentType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.PIType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.QName;
import org.eclipse.wst.xml.xpath2.processor.internal.types.TextType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Node;

/**
 *
 */
public class StaticTypeChecker implements StaticChecker {
	private final org.eclipse.wst.xml.xpath2.api.StaticContext _staticContext;

	public StaticTypeChecker(org.eclipse.wst.xml.xpath2.api.StaticContext staticContext) {
		_staticContext = staticContext;
	}

	@Override
	public void check(XPathNode root) throws StaticError {
		Visitor visitor = new Visitor(_staticContext);
		root.accept(visitor);
	}

	private static boolean disjointOccurrences(int a, int b) {
		if (a == SeqType.OCC_EMPTY) {
			return b != SeqType.OCC_EMPTY && b != SeqType.OCC_STAR;
		} else if (b == SeqType.OCC_EMPTY) {
			return a != SeqType.OCC_EMPTY && a != SeqType.OCC_STAR;
		}

		return false;
	}

	private static boolean disjointTypes(Class<? extends AnyType> a, Class<? extends AnyType> b) {
		if (a.isInterface() || b.isInterface()) {
			return false;
		}

		if (a.isAssignableFrom(b)) {
			return false;
		}

		if (b.isAssignableFrom(a)) {
			return false;
		}

		return true;
	}

	private static boolean disjointTypes(Collection<SeqType> a, Collection<SeqType> b) {
		for (SeqType typeA : a) {
			for (SeqType typeB : b) {
				if (!disjointOccurrences(typeA.occurence(), typeB.occurence())
					&& !disjointTypes(typeA.getTypeClass(), typeB.getTypeClass())) {
					return false;
				}
			}
		}

		return true;
	}

	private static int unionOccurrences(int a, int b) {
		final int EMPTY = 1 << 0;
		final int ONE = 1 << 1;
		final int MORE = 1 << 2;
		final int INVALID = -1;
		final int[] RESULT = new int[(EMPTY | ONE | MORE) + 1];
		RESULT[EMPTY + ONE + MORE] = SeqType.OCC_STAR;
		RESULT[EMPTY + ONE] = SeqType.OCC_QMARK;
		RESULT[EMPTY + MORE] = INVALID;
		RESULT[EMPTY] = SeqType.OCC_EMPTY;
		RESULT[ONE + MORE] = SeqType.OCC_PLUS;
		RESULT[ONE] = SeqType.OCC_NONE;
		RESULT[MORE] = INVALID;
		RESULT[0] = INVALID;

		int index = 0;
		switch (a) {
		case SeqType.OCC_EMPTY:
			index |= EMPTY;
			break;

		case SeqType.OCC_QMARK:
			index |= (EMPTY | ONE);
			break;

		case SeqType.OCC_NONE:
			index |= ONE;
			break;

		case SeqType.OCC_STAR:
			index |= (EMPTY | ONE | MORE);
			break;

		case SeqType.OCC_PLUS:
			index |= (ONE | MORE);
			break;

		default:
			assert false;
			index |= (EMPTY | ONE | MORE);
			break;
		}

		switch (b) {
		case SeqType.OCC_EMPTY:
			index |= EMPTY;
			break;

		case SeqType.OCC_QMARK:
			index |= (EMPTY | ONE);
			break;

		case SeqType.OCC_NONE:
			index |= ONE;
			break;

		case SeqType.OCC_STAR:
			index |= (EMPTY | ONE | MORE);
			break;

		case SeqType.OCC_PLUS:
			index |= (ONE | MORE);
			break;

		default:
			assert false;
			index |= (EMPTY | ONE | MORE);
			break;
		}

		assert RESULT[index] != INVALID;
		return RESULT[index];
	}

	private static Class<? extends AnyType> unionTypes(Class<? extends AnyType> a, Class<? extends AnyType> b) {
		// this rule could be improved
		return AnyType.class;
	}

	private static Collection<SeqType> unionTypes(Collection<SeqType> a, Collection<SeqType> b) {
		return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
//		int occ = unionOccurrences(a.occurence(), b.occurence());
//		Class<? extends AnyType> type = unionTypes(a.getTypeClass(), b.getTypeClass());
//		return new SeqType(type, occ);
	}

	private static Collection<SeqType> intersectTypes(Collection<SeqType> a, Collection<SeqType> b) {
		// this could be improved...
		return a;
	}

	private static class Visitor implements XPathVisitor<Collection<SeqType>> {
		private final org.eclipse.wst.xml.xpath2.api.StaticContext _staticContext;
		private final Deque<Collection<SeqType>> _contextType = new ArrayDeque<Collection<SeqType>>();
		private final Map<QName, Deque<Collection<SeqType>>> _variables = new HashMap<QName, Deque<Collection<SeqType>>>();

		{
			_contextType.push(Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR)));
		}

		public Visitor(org.eclipse.wst.xml.xpath2.api.StaticContext staticContext) {
			this._staticContext = staticContext;
		}

		@Override
		@SuppressWarnings("deprecation")
		public Collection<SeqType> visit(org.eclipse.wst.xml.xpath2.processor.ast.XPath xp) {
			for (Iterator<Expr> exprIt = xp.iterator(); exprIt.hasNext(); ) {
				exprIt.next().accept(this);
			}

			return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
		}

		@Override
		public Collection<SeqType> visit(ForExpr fex) {
			for (VarExprPair pair : fex.ve_pairs()) {
				Collection<SeqType> scopeVariable = pair.expr().accept(this);
				Deque<Collection<SeqType>> variableStack = _variables.get(pair.varname());
				if (variableStack == null) {
					variableStack = new ArrayDeque<Collection<SeqType>>();
					_variables.put(pair.varname(), variableStack);
				}

				variableStack.push(scopeVariable);
			}

			try {
				Collection<SeqType> exprResult = fex.expr().accept(this);
				Collection<SeqType> result = new ArrayList<SeqType>();
				for (SeqType type : exprResult) {
					result.add(new SeqType(type.getTypeClass(), SeqType.OCC_STAR));
				}

				return result;
			} finally {
				for (VarExprPair pair : fex.ve_pairs()) {
					_variables.get(pair.varname()).pop();
				}
			}
		}

		@Override
		public Collection<SeqType> visit(QuantifiedExpr qex) {
			for (VarExprPair pair : qex.ve_pairs()) {
				pair.expr().accept(this);
			}

			qex.expr().accept(this);
			return Collections.singletonList(new SeqType(XSBoolean.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(IfExpr ifex) {
			for (Iterator<Expr> exprIt = ifex.iterator(); exprIt.hasNext(); ) {
				exprIt.next().accept(this);
			}

			Collection<SeqType> thenType = ifex.then_clause().accept(this);
			Collection<SeqType> elseType = ifex.else_clause().accept(this);
			return unionTypes(thenType, elseType);
		}

		@Override
		public Collection<SeqType> visit(OrExpr orex) {
			orex.left().accept(this);
			orex.right().accept(this);
			return Collections.singletonList(new SeqType(XSBoolean.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(AndExpr andex) {
			andex.left().accept(this);
			andex.right().accept(this);
			return Collections.singletonList(new SeqType(XSBoolean.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(CmpExpr cmpex) {
			Collection<SeqType> left = cmpex.left().accept(this);
			Collection<SeqType> right = cmpex.right().accept(this);
			// TODO: check for comparison operator for the operands

			return Collections.singletonList(new SeqType(XSBoolean.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(RangeExpr rex) {
			if (disjointTypes(Collections.singletonList(new SeqType(XSInteger.class, SeqType.OCC_QMARK)), rex.left().accept(this))) {
				throw new StaticTypeError();
			}

			if (disjointTypes(Collections.singletonList(new SeqType(XSInteger.class, SeqType.OCC_QMARK)), rex.right().accept(this))) {
				throw new StaticTypeError();
			}

			return Collections.singletonList(new SeqType(XSInteger.class, SeqType.OCC_STAR));
		}

		@Override
		public Collection<SeqType> visit(AddExpr addex) {
			Collection<SeqType> leftType = addex.left().accept(this);
			Collection<SeqType> rightType = addex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(SubExpr subex) {
			Collection<SeqType> leftType = subex.left().accept(this);
			Collection<SeqType> rightType = subex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(MulExpr mulex) {
			Collection<SeqType> leftType = mulex.left().accept(this);
			Collection<SeqType> rightType = mulex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(DivExpr mulex) {
			Collection<SeqType> leftType = mulex.left().accept(this);
			Collection<SeqType> rightType = mulex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(IDivExpr mulex) {
			Collection<SeqType> leftType = mulex.left().accept(this);
			Collection<SeqType> rightType = mulex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(ModExpr mulex) {
			Collection<SeqType> leftType = mulex.left().accept(this);
			Collection<SeqType> rightType = mulex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(UnionExpr unex) {
			Collection<SeqType> leftType = unex.left().accept(this);
			Collection<SeqType> rightType = unex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(PipeExpr pipex) {
			Collection<SeqType> leftType = pipex.left().accept(this);
			Collection<SeqType> rightType = pipex.right().accept(this);
			return unionTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(IntersectExpr iexpr) {
			Collection<SeqType> leftType = iexpr.left().accept(this);
			Collection<SeqType> rightType = iexpr.right().accept(this);
			return intersectTypes(leftType, rightType);
		}

		@Override
		public Collection<SeqType> visit(ExceptExpr eexpr) {
			Collection<SeqType> leftType = eexpr.left().accept(this);
			Collection<SeqType> rightType = eexpr.right().accept(this);
			return leftType;
		}

		@Override
		public Collection<SeqType> visit(InstOfExpr ioexp) {
			Collection<SeqType> leftType = ioexp.left().accept(this);
			Collection<SeqType> rightType = ioexp.right().accept(this);
			return rightType;
		}

		@Override
		public Collection<SeqType> visit(TreatAsExpr taexp) {
			Collection<SeqType> leftType = taexp.left().accept(this);
			Collection<SeqType> rightType = taexp.right().accept(this);
			if (disjointTypes(leftType, rightType)) {
				throw new StaticTypeError();
			}

			return rightType;
		}

		@Override
		public Collection<SeqType> visit(CastableExpr cexp) {
			cexp.left().accept(this);
			cexp.right().accept(this);
			return Collections.singletonList(new SeqType(XSBoolean.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(CastExpr cexp) {
			cexp.left().accept(this);
			return cexp.right().accept(this);
		}

		@Override
		public Collection<SeqType> visit(MinusExpr e) {
			Collection<SeqType> exprType = e.arg().accept(this);
			if (disjointTypes(Collections.singletonList(new SeqType(XSDecimal.class, SeqType.OCC_NONE)), exprType)
				&& disjointTypes(Collections.singletonList(new SeqType(XSFloat.class, SeqType.OCC_NONE)), exprType)
				&& disjointTypes(Collections.singletonList(new SeqType(XSDouble.class, SeqType.OCC_NONE)), exprType))
			{
				throw new StaticTypeError();
			}

			return exprType;
		}

		@Override
		public Collection<SeqType> visit(PlusExpr e) {
			Collection<SeqType> exprType = e.arg().accept(this);
			if (disjointTypes(Collections.singletonList(new SeqType(XSDecimal.class, SeqType.OCC_NONE)), exprType)
				&& disjointTypes(Collections.singletonList(new SeqType(XSFloat.class, SeqType.OCC_NONE)), exprType)
				&& disjointTypes(Collections.singletonList(new SeqType(XSDouble.class, SeqType.OCC_NONE)), exprType))
			{
				throw new StaticTypeError();
			}

			return exprType;
		}

		@Override
		public Collection<SeqType> visit(XPathExpr e) {
			Collection<SeqType> current = e.expr().accept(this);
			if (e.next() == null) {
				return current;
			}

			try {
				_contextType.push(current);
				return e.next().accept(this);
			} finally {
				_contextType.pop();
			}
		}

		@Override
		public Collection<SeqType> visit(ForwardStep e) {
			boolean canHaveAttribute = false;
			boolean canHaveSelf = false;
			boolean canHaveChildren = false;
			boolean canHaveParent = false;

			for (SeqType contextType : _contextType.peek()) {
				if (ElementType.class.isAssignableFrom(contextType.getTypeClass())) {
					canHaveAttribute = true;
					canHaveSelf = true;
					canHaveParent = true;
					canHaveChildren = true;
				} else if (DocType.class.isAssignableFrom(contextType.getTypeClass())) {
					canHaveChildren = true;
				} else if (contextType.getTypeClass().isAssignableFrom(NodeType.class)) {
					canHaveAttribute = true;
					canHaveSelf = true;
					canHaveParent = true;
					canHaveChildren = true;
				}
			}

			switch (e.axis()) {
			case ForwardStep.ATTRIBUTE:
			case ForwardStep.AT_SYM:
				if (!canHaveAttribute) {
					throw new StaticTypeError("Context cannot have any attribute nodes.", null);
				}

				break;

			case ForwardStep.SELF:
				if (!canHaveSelf) {
					throw new StaticTypeError("Context cannot have a self:: node.", null);
				}

				break;

			case ForwardStep.CHILD:
			case ForwardStep.DESCENDANT:
				if (!canHaveSelf) {
					throw new StaticTypeError("Context cannot have any child nodes.", null);
				}

				break;

			case ForwardStep.DESCENDANT_OR_SELF:
				if (!canHaveSelf && !canHaveChildren) {
					throw new StaticTypeError("Context cannot have any self:: or descendant nodes.", null);
				}

				break;

			default:
				break;
			}

			Collection<SeqType> nodeTestResult = e.node_test().accept(this);
			switch (e.axis()) {
			case ForwardStep.ATTRIBUTE:
			case ForwardStep.AT_SYM:
				return Collections.singletonList(new SeqType(AttrType.class, SeqType.OCC_STAR));

			case ForwardStep.CHILD:
			case ForwardStep.DESCENDANT:
			case ForwardStep.DESCENDANT_OR_SELF:
			case ForwardStep.FOLLOWING:
			case ForwardStep.FOLLOWING_SIBLING:
			case ForwardStep.NAMESPACE:
			case ForwardStep.NONE:
			case ForwardStep.SELF:
			default:
				return nodeTestResult;
			}
		}

		@Override
		public Collection<SeqType> visit(ReverseStep e) {
			if (e.axis() == ReverseStep.PARENT || e.axis() == ReverseStep.DOTDOT || e.axis() == ReverseStep.ANCESTOR) {
				boolean canHaveParent = false;
				for (SeqType contextType : _contextType.peek()) {
					if (AnyAtomicType.class.isAssignableFrom(contextType.getTypeClass())) {
						continue;
					}

					if (DocType.class.isAssignableFrom(contextType.getTypeClass())) {
						continue;
					}

					canHaveParent = true;
				}

				if (!canHaveParent) {
					throw new StaticTypeError("Context cannot have a parent node.", null);
				}
			}

			if (e.node_test() != null) {
				e.node_test().accept(this);
			}

			return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
		}

		@Override
		public Collection<SeqType> visit(NameTest e) {
			return Collections.singletonList(new SeqType(NodeType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(VarRef e) {
			Deque<Collection<SeqType>> variables = _variables.get(e.name());
			if (variables != null && !variables.isEmpty()) {
				return variables.peek();
			}

			org.eclipse.wst.xml.xpath2.api.typesystem.ItemType staticVariable = _staticContext.getInScopeVariables().getVariableType(e.name().asQName());
			if (staticVariable == null) {
				throw new StaticTypeError("Could not locate an variable named $" + e.name().getStringValue() + " in the current scope", null);
			}

			int occ;
			switch (staticVariable.getOccurrence()) {
			case org.eclipse.wst.xml.xpath2.api.typesystem.ItemType.OCCURRENCE_ONE:
				occ = SeqType.OCC_NONE;
				break;

			case org.eclipse.wst.xml.xpath2.api.typesystem.ItemType.OCCURRENCE_OPTIONAL:
				occ = SeqType.OCC_QMARK;
				break;

			case org.eclipse.wst.xml.xpath2.api.typesystem.ItemType.OCCURRENCE_NONE_OR_MANY:
				occ = SeqType.OCC_STAR;
				break;

			case org.eclipse.wst.xml.xpath2.api.typesystem.ItemType.OCCURRENCE_ONE_OR_MANY:
				occ = SeqType.OCC_PLUS;
				break;

			default:
				throw new StaticTypeError("Unknown item occurrence type.", null);
			}

			Class<? extends AnyType> typeClass;
			if (staticVariable instanceof NodeItemType) {
				switch (((NodeItemType)staticVariable).getNodeType()) {
				case Node.ELEMENT_NODE:
					typeClass = ElementType.class;
					break;

				case Node.ATTRIBUTE_NODE:
					typeClass = AttrType.class;
					break;

				case Node.TEXT_NODE:
					typeClass = TextType.class;
					break;

				case Node.CDATA_SECTION_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				case Node.ENTITY_REFERENCE_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				case Node.ENTITY_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				case Node.PROCESSING_INSTRUCTION_NODE:
					typeClass = PIType.class;
					break;

				case Node.COMMENT_NODE:
					typeClass = CommentType.class;
					break;

				case Node.DOCUMENT_NODE:
					typeClass = DocType.class;
					break;

				case Node.DOCUMENT_TYPE_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				case Node.DOCUMENT_FRAGMENT_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				case Node.NOTATION_NODE:
					throw new UnsupportedOperationException("Not yet implemented.");

				default:
					throw new StaticTypeError("Unknown node type.", null);
				}
			} else if (staticVariable instanceof AtomicItemType) {
				Class<?> nativeType = ((AtomicItemType)staticVariable).getTypeDefinition().getNativeType();
				if (nativeType == null) {
					throw new UnsupportedOperationException("Unknown variable type.");
				}

				typeClass = nativeType.asSubclass(AnyType.class);
			} else {
				throw new UnsupportedOperationException("Unknown variable type.");
			}

			return Collections.singletonList(new SeqType(typeClass, occ));
		}

		@Override
		public Collection<SeqType> visit(StringLiteral e) {
			return Collections.singletonList(new SeqType(XSString.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(IntegerLiteral e) {
			return Collections.singletonList(new SeqType(XSInteger.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(DoubleLiteral e) {
			return Collections.singletonList(new SeqType(XSDouble.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(DecimalLiteral e) {
			return Collections.singletonList(new SeqType(XSDecimal.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(ParExpr e) {
			return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
		}

		@Override
		public Collection<SeqType> visit(CntxItemExpr e) {
			return _contextType.peek();
		}

		@Override
		public Collection<SeqType> visit(FunctionCall e) {
			Function function = e.function();
			List<Collection<SeqType>> arguments = new ArrayList<Collection<SeqType>>();
			for (Iterator<Expr> exprIt = e.iterator(); exprIt.hasNext(); ) {
				arguments.add(exprIt.next().accept(this));
			}

			if (function == null) {
				return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
			}

			return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_STAR));
		}

		@Override
		public Collection<SeqType> visit(SingleType e) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Collection<SeqType> visit(SequenceType e) {
			Collection<SeqType> items = e.item_type().accept(this);

			int occ;
			switch (e.occurrence()) {
			case SequenceType.EMPTY:
				occ = SeqType.OCC_EMPTY;
				break;

			case SequenceType.NONE:
				occ = SeqType.OCC_NONE;
				break;

			case SequenceType.PLUS:
				occ = SeqType.OCC_PLUS;
				break;

			case SequenceType.QUESTION:
				occ = SeqType.OCC_QMARK;
				break;

			case SequenceType.STAR:
				occ = SeqType.OCC_STAR;
				break;

			default:
				throw new StaticTypeError("Invalid SequenceType multiplicity.", null);
			}

			Collection<SeqType> result = new ArrayList<SeqType>();
			for (SeqType type : items) {
				result.add(new SeqType(type.getTypeClass(), occ));
			}

			return result;
		}

		@Override
		public Collection<SeqType> visit(ItemType e) {
			switch (e.type()) {
			case ItemType.ITEM:
				return Collections.singletonList(new SeqType(AnyType.class, SeqType.OCC_NONE));

			case ItemType.KINDTEST:
				return e.kind_test().accept(this);

			case ItemType.QNAME:
				if (e.qname().prefix() == null) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				// try to find a constructor for the type
				Function constructor = _staticContext.resolveFunction(e.qname().asQName(), 1);
				if (constructor == null) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				if (constructor instanceof Constructor) {
					return Collections.singletonList(new SeqType(((Constructor)constructor).getAtomicType().getClass(), SeqType.OCC_NONE));
				}

				throw new UnsupportedOperationException("Not supported yet.");

			default:
				throw new StaticTypeError("Unknown item type.", null);
			}
		}

		@Override
		public Collection<SeqType> visit(AnyKindTest e) {
			return Collections.singletonList(new SeqType(NodeType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(DocumentTest e) {
			return Collections.singletonList(new SeqType(DocType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(TextTest e) {
			return Collections.singletonList(new SeqType(TextType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(CommentTest e) {
			return Collections.singletonList(new SeqType(CommentType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(PITest e) {
			return Collections.singletonList(new SeqType(PIType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(AttributeTest e) {
			return Collections.singletonList(new SeqType(AttrType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(SchemaAttrTest e) {
			return Collections.singletonList(new SeqType(AttrType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(ElementTest e) {
			return Collections.singletonList(new SeqType(ElementType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(SchemaElemTest e) {
			return Collections.singletonList(new SeqType(ElementType.class, SeqType.OCC_NONE));
		}

		@Override
		public Collection<SeqType> visit(AxisStep e) {
			Collection<SeqType> stepType = e.step().accept(this);
			for (Iterator<Collection<Expr>> collectionIt = e.iterator(); collectionIt.hasNext(); ) {
				for (Expr expr : collectionIt.next()) {
					expr.accept(this);
				}
			}

			return stepType;
		}

		@Override
		public Collection<SeqType> visit(FilterExpr e) {
			Collection<SeqType> primary = e.primary().accept(this);
			for (Iterator<Collection<Expr>> filterIt = e.iterator(); filterIt.hasNext(); ) {
				for (Expr expr : filterIt.next()) {
					expr.accept(this);
				}
			}

			return primary;
		}
		
	}
}
