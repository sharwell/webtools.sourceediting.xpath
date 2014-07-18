/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.wst.xml.xpath2.processor.internal;

import java.util.Collection;
import java.util.Iterator;

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
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathVisitor;
import org.eclipse.wst.xml.xpath2.processor.internal.utils.LiteralUtils;

/**
 *
 * @author Sam Harwell
 */
public class ExpressionPrinterVisitor implements XPathVisitor<String> {
    public static final ExpressionPrinterVisitor DEFAULT = new ExpressionPrinterVisitor();

    @Override
    @SuppressWarnings("deprecation")
    public String visit(org.eclipse.wst.xml.xpath2.processor.ast.XPath xp) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Iterator<Expr> exprIt = xp.iterator(); exprIt.hasNext(); ) {
            if (!first) {
                builder.append(", ");
            }

            Expr expr = exprIt.next();
            builder.append(expr.accept(this));
            first = false;
        }
        return builder.toString();
    }

	@Override
	public String visit(ForExpr fex) {
		StringBuilder builder = new StringBuilder();
		builder.append("for");
		boolean first = true;
		for (VarExprPair pair : fex.ve_pairs()) {
			if (first) {
				builder.append(" $");
				first = false;
			} else {
				builder.append(", $");
			}

			builder.append(pair.varname().getStringValue());
			builder.append(" in ");
			builder.append(pair.expr().accept(this));
		}

		builder.append(" return ");
		builder.append(fex.expr().accept(this));
		return builder.toString();
	}

	@Override
	public String visit(QuantifiedExpr qex) {
		StringBuilder builder = new StringBuilder();
		switch (qex.type()) {
		case QuantifiedExpr.SOME:
			builder.append("some");
			break;

		case QuantifiedExpr.ALL:
			builder.append("every");
			break;

		default:
			assert false;
			break;
		}

		boolean first = true;
		for (VarExprPair pair : qex.ve_pairs()) {
			if (first) {
				builder.append(" $");
				first = false;
			} else {
				builder.append(", $");
			}

			builder.append(pair.varname().getStringValue());
			builder.append(" in ");
			builder.append(pair.expr().accept(this));
		}

		builder.append(" satisfies ");
		builder.append(qex.expr().accept(this));
		return builder.toString();
	}

    @Override
    public String visit(IfExpr ifex) {
        StringBuilder builder = new StringBuilder();
        builder.append("if ");
        boolean first = true;
        for (Iterator<Expr> exprIt = ifex.iterator(); exprIt.hasNext(); ) {
            if (!first) {
                builder.append(", ");
            }

            Expr expr = exprIt.next();
            builder.append(expr.accept(this));
            first = false;
        }

        builder.append(" then ");
        builder.append(ifex.then_clause().accept(this));
        
        if (ifex.else_clause() != null) {
            builder.append(" else ");
            builder.append(ifex.else_clause().accept(this));
        }

        return builder.toString();
    }

    @Override
    public String visit(OrExpr orex) {
        StringBuilder builder = new StringBuilder();
        builder.append(orex.left().accept(this));
        builder.append(" or ");
        builder.append(orex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(AndExpr andex) {
        StringBuilder builder = new StringBuilder();
        builder.append(andex.left().accept(this));
        builder.append(" and ");
        builder.append(andex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(CmpExpr cmpex) {
        StringBuilder builder = new StringBuilder();
        if (cmpex.left() != null) {
            builder.append(cmpex.left().accept(this));
        } else {
            builder.append("null");
        }

        switch (cmpex.type()) {
        case CmpExpr.EQUALS:
            builder.append("=");
            break;

        case CmpExpr.NOTEQUALS:
            builder.append("!=");
            break;

        case CmpExpr.LESSTHAN:
            builder.append("<");
            break;

        case CmpExpr.LESSEQUAL:
            builder.append("<=");
            break;

        case CmpExpr.GREATER:
            builder.append(">");
            break;

        case CmpExpr.GREATEREQUAL:
            builder.append(">=");
            break;

        case CmpExpr.EQ:
            builder.append(" eq ");
            break;

        case CmpExpr.NE:
            builder.append(" ne ");
            break;

        case CmpExpr.LT:
            builder.append(" lt ");
            break;

        case CmpExpr.LE:
            builder.append(" le ");
            break;

        case CmpExpr.GT:
            builder.append(" gt ");
            break;

        case CmpExpr.GE:
            builder.append(" ge ");
            break;

        case CmpExpr.IS:
            builder.append(" is ");
            break;

        case CmpExpr.LESS_LESS:
            builder.append("<<");
            break;

        case CmpExpr.GREATER_GREATER:
            builder.append(">>");
            break;

        default:
			assert false;
            builder.append("??");
            break;
        }

        if (cmpex.right() != null) {
            builder.append(cmpex.right().accept(this));
        } else {
            builder.append("null");
        }

        return builder.toString();
    }

	@Override
	public String visit(RangeExpr rex) {
		StringBuilder builder = new StringBuilder();
		builder.append(rex.left().accept(this));
		builder.append(" to ");
		builder.append(rex.right().accept(this));
		return builder.toString();
	}

    @Override
    public String visit(AddExpr addex) {
        StringBuilder builder = new StringBuilder();
        builder.append(addex.left().accept(this));
        builder.append(" + ");
        builder.append(addex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(SubExpr subex) {
        StringBuilder builder = new StringBuilder();
        builder.append(subex.left().accept(this));
        builder.append(" - ");
        builder.append(subex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(MulExpr mulex) {
        StringBuilder builder = new StringBuilder();
        builder.append(mulex.left().accept(this));
        builder.append(" * ");
        builder.append(mulex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(DivExpr mulex) {
        StringBuilder builder = new StringBuilder();
        builder.append(mulex.left().accept(this));
        builder.append(" div ");
        builder.append(mulex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(IDivExpr mulex) {
        StringBuilder builder = new StringBuilder();
        builder.append(mulex.left().accept(this));
        builder.append(" idiv ");
        builder.append(mulex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(ModExpr mulex) {
        StringBuilder builder = new StringBuilder();
        builder.append(mulex.left().accept(this));
        builder.append(" mod ");
        builder.append(mulex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(UnionExpr unex) {
        StringBuilder builder = new StringBuilder();
        builder.append(unex.left().accept(this));
        builder.append(" union ");
        builder.append(unex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(PipeExpr pipex) {
        StringBuilder builder = new StringBuilder();
        builder.append(pipex.left().accept(this));
        builder.append(" | ");
        builder.append(pipex.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(IntersectExpr iexpr) {
        StringBuilder builder = new StringBuilder();
        builder.append(iexpr.left().accept(this));
        builder.append(" intersect ");
        builder.append(iexpr.right().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(ExceptExpr eexpr) {
        StringBuilder builder = new StringBuilder();
        builder.append(eexpr.left().accept(this));
        builder.append(" except ");
        builder.append(eexpr.right().accept(this));
        return builder.toString();
    }

	@Override
	public String visit(InstOfExpr ioexp) {
		StringBuilder builder = new StringBuilder();
		builder.append(ioexp.left().accept(this));
		builder.append(" instance of ");
		builder.append(ioexp.right().accept(this));
		return builder.toString();
	}

	@Override
	public String visit(TreatAsExpr taexp) {
		StringBuilder builder = new StringBuilder();
		builder.append(taexp.left().accept(this));
		builder.append(" treat as ");
		builder.append(taexp.right().accept(this));
		return builder.toString();
	}

	@Override
	public String visit(CastableExpr cexp) {
		StringBuilder builder = new StringBuilder();
		builder.append(cexp.left().accept(this));
		builder.append(" castable as ");
		builder.append(cexp.right().accept(this));
		return builder.toString();
	}

	@Override
	public String visit(CastExpr cexp) {
		StringBuilder builder = new StringBuilder();
		builder.append(cexp.left().accept(this));
		builder.append(" cast as ");
		builder.append(cexp.right().accept(this));
		return builder.toString();
	}

    @Override
    public String visit(MinusExpr e) {
        StringBuilder builder = new StringBuilder();
        builder.append("-");
        builder.append(e.arg().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(PlusExpr e) {
        StringBuilder builder = new StringBuilder();
        builder.append("+");
        builder.append(e.arg().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(XPathExpr e) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < e.slashes(); i++) {
            builder.append('/');
        }

        if (e.expr() != null) {
            builder.append(e.expr().accept(this));
        }

        if (e.next() != null) {
            builder.append(e.next().accept(this));
        }

        return builder.toString();
    }

    @Override
    public String visit(ForwardStep e) {
        StringBuilder builder = new StringBuilder();
        switch (e.axis()) {
        case ForwardStep.NONE:
            builder.append("");
            break;

        case ForwardStep.CHILD:
            builder.append("child::");
            break;

        case ForwardStep.DESCENDANT:
            builder.append("descendant::");
            break;

        case ForwardStep.ATTRIBUTE:
            builder.append("attribute::");
            break;

        case ForwardStep.SELF:
            builder.append("self::");
            break;

        case ForwardStep.DESCENDANT_OR_SELF:
            builder.append("descendant-or-self::");
            break;

        case ForwardStep.FOLLOWING_SIBLING:
            builder.append("following-sibling::");
            break;

        case ForwardStep.FOLLOWING:
            builder.append("following::");
            break;

        case ForwardStep.NAMESPACE:
            builder.append("namespace::");
            break;

        case ForwardStep.AT_SYM:
            builder.append("@");
            break;

        default:
			assert false;
            builder.append("??::");
            break;
        }

        builder.append(e.node_test().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(ReverseStep e) {
        StringBuilder builder = new StringBuilder();
        switch (e.axis()) {
        case ReverseStep.PARENT:
            builder.append("parent::");
            break;

        case ReverseStep.ANCESTOR:
            builder.append("ancestor::");
            break;

        case ReverseStep.PRECEDING_SIBLING:
            builder.append("preceding-sibling::");
            break;

        case ReverseStep.PRECEDING:
            builder.append("preceding::");
            break;

        case ReverseStep.ANCESTOR_OR_SELF:
            builder.append("ancestor-or-self::");
            break;

        case ReverseStep.DOTDOT:
            builder.append("..");
            break;

        default:
			assert false;
            builder.append("??::");
            break;
        }

        builder.append(e.node_test().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(NameTest e) {
        return e.name().getStringValue();
    }

    @Override
    public String visit(VarRef e) {
        return "$" + e.name().getStringValue();
    }

    @Override
    public String visit(StringLiteral e) {
        return "'" + e.string() + "'";
    }

    @Override
    public String visit(IntegerLiteral e) {
        return e.value().getStringValue();
    }

    @Override
    public String visit(DoubleLiteral e) {
        return e.value().getStringValue();
    }

    @Override
    public String visit(DecimalLiteral e) {
        return e.value().getStringValue();
    }

    @Override
    public String visit(ParExpr e) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        boolean first = true;
        for (Iterator<Expr> exprIt = e.iterator(); exprIt.hasNext(); ) {
            if (!first) {
                builder.append(", ");
            }

            Expr expr = exprIt.next();
            builder.append(expr.accept(this));
            first = false;
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public String visit(CntxItemExpr e) {
        return ".";
    }

    @Override
    public String visit(FunctionCall e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.name().getStringValue());
        builder.append('(');
        boolean first = true;
        for (Iterator<Expr> exprIt = e.iterator(); exprIt.hasNext(); ) {
            if (!first) {
                builder.append(", ");
            }

            Expr expr = exprIt.next();
            builder.append(expr.accept(this));
            first = false;
        }
        builder.append(')');
        return builder.toString();
    }

	@Override
	public String visit(SingleType e) {
		String result = e.type().getStringValue();
		if (e.qmark()) {
			result += "?";
		}

		return result;
	}

	@Override
	public String visit(SequenceType e) {
		if (e.occurrence() == SequenceType.EMPTY) {
			return "empty-sequence()";
		}

		String itemType = e.item_type().accept(this);
		switch (e.occurrence()) {
		case SequenceType.NONE:
			return itemType;

		case SequenceType.QUESTION:
			return itemType + "?";

		case SequenceType.STAR:
			return itemType + "*";

		case SequenceType.PLUS:
			return itemType + "+";

		default:
			assert false;
			return "??";
		}
	}

	@Override
	public String visit(ItemType e) {
		switch (e.type()) {
		case ItemType.QNAME:
			return e.qname().getStringValue();

		case ItemType.KINDTEST:
			return e.kind_test().accept(this);

		case ItemType.ITEM:
			return "item()";

		default:
			assert false;
			return "??";
		}
	}

	@Override
	public String visit(AnyKindTest e) {
		return "node()";
	}

	@Override
	public String visit(DocumentTest e) {
		if (e.type() == DocumentTest.NONE) {
			return "document()";
		}

		StringBuilder builder = new StringBuilder("document(");
		switch (e.type()) {
		case DocumentTest.ELEMENT:
			builder.append(e.elem_test().accept(this));
			break;

		case DocumentTest.SCHEMA_ELEMENT:
			builder.append(e.schema_elem_test().accept(this));
			break;

		default:
			assert false;
			builder.append("??");
			break;
		}

		builder.append(')');
		return builder.toString();
	}

	@Override
	public String visit(TextTest e) {
		return "text()";
	}

	@Override
	public String visit(CommentTest e) {
		return "comment()";
	}

	@Override
	public String visit(PITest e) {
		if (e.arg() == null) {
			return "processing-instruction()";
		}

		return "processing-instruction(" + LiteralUtils.quote(e.arg()) + ")";
	}

	@Override
	public String visit(AttributeTest e) {
		if (e.type() == null && e.name() == null) {
			if (e.wild()) {
				return "attribute(*)";
			} else {
				return "attribute()";
			}
		} else {
			StringBuilder builder = new StringBuilder("attribute(");
			if (e.wild()) {
				builder.append('*');
			} else {
				assert e.name() != null;
				builder.append(e.name().getStringValue());
			}

			if (e.type() != null) {
				builder.append(", ");
				builder.append(e.type().getStringValue());
			}

			return builder.toString();
		}
	}

	@Override
	public String visit(SchemaAttrTest e) {
		return "schema-attribute(" + e.name().getStringValue() + ")";
	}

	@Override
	public String visit(ElementTest e) {
		if (e.type() == null && e.name() == null) {
			assert !e.qmark();
			if (e.wild()) {
				return "element(*)";
			} else {
				return "element()";
			}
		} else {
			StringBuilder builder = new StringBuilder("element(");
			if (e.wild()) {
				builder.append("*");
			} else {
				assert e.name() != null;
				builder.append(e.name().getStringValue());
			}

			if (e.type() != null) {
				builder.append(", ");
				builder.append(e.type().getStringValue());
				if (e.qmark()) {
					builder.append('?');
				}
			} else {
				assert !e.qmark();
			}

			return builder.toString();
		}
	}

	@Override
	public String visit(SchemaElemTest e) {
		StringBuilder builder = new StringBuilder("schema-element(");
		builder.append(e.name().getStringValue());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public String visit(AxisStep e) {
		StringBuilder builder = new StringBuilder();
		builder.append(e.step().accept(this));
		for (Iterator<Collection<Expr>> exprIt = e.iterator(); exprIt.hasNext(); ) {
			builder.append('[');
			boolean first = true;
			for (Expr expr : exprIt.next()) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}

				builder.append(expr.accept(this));
			}
			builder.append(']');
		}

		return builder.toString();
	}

	@Override
	public String visit(FilterExpr e) {
		StringBuilder builder = new StringBuilder();
		builder.append(e.primary().accept(this));
		for (Iterator<Collection<Expr>> exprIt = e.iterator(); exprIt.hasNext(); ) {
			builder.append('[');
			boolean first = true;
			for (Expr expr : exprIt.next()) {
				if (first) {
					first = false;
				} else {
					builder.append(", ");
				}

				builder.append(expr.accept(this));
			}
			builder.append(']');
		}

		return builder.toString();
	}
}
