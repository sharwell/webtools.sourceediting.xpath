/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.wst.xml.xpath2.processor.internal;

import java.util.Iterator;
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
import org.eclipse.wst.xml.xpath2.processor.internal.ast.VarRef;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathExpr;
import org.eclipse.wst.xml.xpath2.processor.internal.ast.XPathVisitor;

/**
 *
 * @author Sam Harwell
 */
public class ExpressionPrinterVisitor implements XPathVisitor<String> {
    public static final ExpressionPrinterVisitor DEFAULT = new ExpressionPrinterVisitor();

    @Override
    public String visit(XPath xp) {
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
        return "??";
    }

    @Override
    public String visit(QuantifiedExpr qex) {
        return "??";
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
        return "??";
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
        return "??";
    }

    @Override
    public String visit(TreatAsExpr taexp) {
        return "??";
    }

    @Override
    public String visit(CastableExpr cexp) {
        return "??";
    }

    @Override
    public String visit(CastExpr cexp) {
        return "??";
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

        builder.append(e.expr().accept(this));
        if (e.next() != null) {
            builder.append(", ");
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
            builder.append("??::");
            break;
        }

        builder.append(e.node_test().accept(this));
        return builder.toString();
    }

    @Override
    public String visit(NameTest e) {
        return e.name().toString();
    }

    @Override
    public String visit(VarRef e) {
        return "??";
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
        return "??";
    }

    @Override
    public String visit(FunctionCall e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.name());
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
        return "??";
    }

    @Override
    public String visit(SequenceType e) {
        return "??";
    }

    @Override
    public String visit(ItemType e) {
        return "??";
    }

    @Override
    public String visit(AnyKindTest e) {
        return "??";
    }

    @Override
    public String visit(DocumentTest e) {
        return "??";
    }

    @Override
    public String visit(TextTest e) {
        return "??";
    }

    @Override
    public String visit(CommentTest e) {
        return "??";
    }

    @Override
    public String visit(PITest e) {
        return "??";
    }

    @Override
    public String visit(AttributeTest e) {
        return "??";
    }

    @Override
    public String visit(SchemaAttrTest e) {
        return "??";
    }

    @Override
    public String visit(ElementTest e) {
        return "??";
    }

    @Override
    public String visit(SchemaElemTest e) {
        return "??";
    }

    @Override
    public String visit(AxisStep e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.step().accept(this));
        if (e.predicate_count() > 0) {
            builder.append("[??]");
        }

        return builder.toString();
    }

    @Override
    public String visit(FilterExpr e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.primary().accept(this));
        if (e.predicate_count() > 0) {
            builder.append("[??]");
        }

        return builder.toString();
    }
}
