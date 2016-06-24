package org.eclipse.wst.xml.xpath2.test;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeItemTypeImpl;
import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TestNonStandardPrefix extends XPathTestBase {

    /**
     * This is a regression test for a special case identified during code review of
     * https://github.com/sharwell/webtools.sourceediting.xpath/pull/44.
     *
     * <p>The namespace normally given the prefix 'xs' could actually be given a different prefix, such as 'xss'.</p>
     */
    @Test
    public void nonStandardPrefix_NotationConstructor() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String filePath = "Expressions/exprSeqTypes/SeqExprCast/";

        StaticContext staticContext = createStaticContextBuilder()
                .withVariable(new QName("input-context"), new NodeItemTypeImpl(Node.DOCUMENT_NODE));

        String raw_input_context = readFile("TestSources/emptydoc.xml");
        Document input_context = getDocumentBuilder("").parse(new ByteArrayInputStream(raw_input_context.getBytes("UTF-8")), staticContext.getBaseUri().toString() + "TestSources/emptydoc.xml");

        DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
                .withVariable(new QName("input-context"), new DocType(input_context, staticContext.getTypeModel()));

        String query = readFile("Queries/XQuery/" + filePath + "K-SeqExprCast-65.xq");

        // Special case: replace "xs:" with "xss:"
        query = query.replace("xs:", "xss:");
        assertThat(query, containsString("xss:NOTATION"));
        assertThat(query, not(containsString("xs:")));

        if (isDebug()) {
            System.out.println(query);
        }
        query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
        query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");

        List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
        matchers.add(CoreMatchers.is(CoreMatchers.equalTo("XPST0017")));

        try {
            XPath2Expression expression = getEngine().parseExpression(query, staticContext);
            Node[] contextItems = new Node[] { };
            ResultSequence result = expression.evaluate(dynamicContext, contextItems);
            Assert.fail("Expected an exception: XPST0017");
        } catch (DynamicError ex) {
            assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        } catch (StaticError ex) {
            assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        }
    }

    /**
     * This is a regression test for a special case identified during code review of
     * https://github.com/sharwell/webtools.sourceediting.xpath/pull/44.
     *
     * <p>The namespace normally given the prefix 'xs' could actually be given a different prefix, such as 'xss'.</p>
     */
    @Test
    public void nonStandardPrefix_CastAsNotation() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String filePath = "Expressions/exprSeqTypes/SeqExprCast/";

        StaticContext staticContext = createStaticContextBuilder()
                .withVariable(new QName("input-context"), new NodeItemTypeImpl(Node.DOCUMENT_NODE));

        String raw_input_context = readFile("TestSources/emptydoc.xml");
        Document input_context = getDocumentBuilder("").parse(new ByteArrayInputStream(raw_input_context.getBytes("UTF-8")), staticContext.getBaseUri().toString() + "TestSources/emptydoc.xml");

        DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
                .withVariable(new QName("input-context"), new DocType(input_context, staticContext.getTypeModel()));

        String query = readFile("Queries/XQuery/" + filePath + "K-SeqExprCast-61.xq");

        // Special case: replace "xs:" with "xss:"
        query = query.replace("xs:", "xss:");
        assertThat(query, containsString("cast as xss:NOTATION"));
        assertThat(query, not(containsString("xs:")));

        if (isDebug()) {
            System.out.println(query);
        }
        query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
        query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");

        List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
        matchers.add(CoreMatchers.is(CoreMatchers.equalTo("XPST0080")));

        try {
            XPath2Expression expression = getEngine().parseExpression(query, staticContext);
            Node[] contextItems = new Node[] { };
            ResultSequence result = expression.evaluate(dynamicContext, contextItems);
            Assert.fail("Expected an exception: XPST0080");
        } catch (DynamicError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        } catch (StaticError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        }
    }

    /**
     * This is a regression test for a special case identified during code review of
     * https://github.com/sharwell/webtools.sourceediting.xpath/pull/44.
     *
     * <p>The namespace normally given the prefix 'xs' could actually be given a different prefix, such as 'xss'.</p>
     */
    @Test
    public void nonStandardPrefix_CastAsAnyAtomicType() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String filePath = "Expressions/exprSeqTypes/SeqExprCast/";

        StaticContext staticContext = createStaticContextBuilder()
                .withVariable(new QName("input-context"), new NodeItemTypeImpl(Node.DOCUMENT_NODE));

        String raw_input_context = readFile("TestSources/emptydoc.xml");
        Document input_context = getDocumentBuilder("").parse(new ByteArrayInputStream(raw_input_context.getBytes("UTF-8")), staticContext.getBaseUri().toString() + "TestSources/emptydoc.xml");

        DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
                .withVariable(new QName("input-context"), new DocType(input_context, staticContext.getTypeModel()));

        String query = readFile("Queries/XQuery/" + filePath + "K-SeqExprCast-6.xq");

        // Special case: replace "xs:" with "xss:"
        query = query.replace("xs:", "xss:");
        assertThat(query, containsString("cast as xss:anyAtomicType"));
        assertThat(query, not(containsString("xs:")));

        if (isDebug()) {
            System.out.println(query);
        }
        query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
        query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");

        List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
        matchers.add(CoreMatchers.is(CoreMatchers.equalTo("XPST0080")));

        try {
            XPath2Expression expression = getEngine().parseExpression(query, staticContext);
            Node[] contextItems = new Node[] { };
            ResultSequence result = expression.evaluate(dynamicContext, contextItems);
            Assert.fail("Expected an exception: XPST0080");
        } catch (DynamicError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        } catch (StaticError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        }
    }

    /**
     * This is a regression test for a special case identified during code review of
     * https://github.com/sharwell/webtools.sourceediting.xpath/pull/44.
     *
     * <p>The namespace normally given the prefix 'xs' could actually be given a different prefix, such as 'xss'.</p>
     */
    @Test
    public void nonStandardPrefix_CastableAsNotation() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String filePath = "Expressions/exprSeqTypes/SeqExprCastable/";

        StaticContext staticContext = createStaticContextBuilder()
                .withVariable(new QName("input-context"), new NodeItemTypeImpl(Node.DOCUMENT_NODE));

        String raw_input_context = readFile("TestSources/emptydoc.xml");
        Document input_context = getDocumentBuilder("").parse(new ByteArrayInputStream(raw_input_context.getBytes("UTF-8")), staticContext.getBaseUri().toString() + "TestSources/emptydoc.xml");

        DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
                .withVariable(new QName("input-context"), new DocType(input_context, staticContext.getTypeModel()));

        String query = readFile("Queries/XQuery/" + filePath + "K-SeqExprCastable-10.xq");

        // Special case: replace "xs:" with "xss:"
        query = query.replace("xs:", "xss:");
        assertThat(query, containsString("castable as xss:NOTATION"));
        assertThat(query, not(containsString("xs:")));

        if (isDebug()) {
            System.out.println(query);
        }
        query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
        query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");

        List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
        matchers.add(CoreMatchers.is(CoreMatchers.equalTo("XPST0080")));

        try {
            XPath2Expression expression = getEngine().parseExpression(query, staticContext);
            Node[] contextItems = new Node[] { };
            ResultSequence result = expression.evaluate(dynamicContext, contextItems);
            Assert.fail("Expected an exception: XPST0080");
        } catch (DynamicError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        } catch (StaticError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        }
    }

    /**
     * This is a regression test for a special case identified during code review of
     * https://github.com/sharwell/webtools.sourceediting.xpath/pull/44.
     *
     * <p>The namespace normally given the prefix 'xs' could actually be given a different prefix, such as 'xss'.</p>
     */
    @Test
    public void nonStandardPrefix_CastableAsAnyAtomicType() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String filePath = "Expressions/exprSeqTypes/SeqExprCastable/";

        StaticContext staticContext = createStaticContextBuilder()
                .withVariable(new QName("input-context"), new NodeItemTypeImpl(Node.DOCUMENT_NODE));

        String raw_input_context = readFile("TestSources/emptydoc.xml");
        Document input_context = getDocumentBuilder("").parse(new ByteArrayInputStream(raw_input_context.getBytes("UTF-8")), staticContext.getBaseUri().toString() + "TestSources/emptydoc.xml");

        DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
                .withVariable(new QName("input-context"), new DocType(input_context, staticContext.getTypeModel()));

        String query = readFile("Queries/XQuery/" + filePath + "K-SeqExprCastable-7.xq");

        // Special case: replace "xs:" with "xss:"
        query = query.replace("xs:", "xss:");
        assertThat(query, containsString("castable as xss:anyAtomicType"));
        assertThat(query, not(containsString("xs:")));

        if (isDebug()) {
            System.out.println(query);
        }
        query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
        query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");

        List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
        matchers.add(CoreMatchers.is(CoreMatchers.equalTo("XPST0080")));

        try {
            XPath2Expression expression = getEngine().parseExpression(query, staticContext);
            Node[] contextItems = new Node[] { };
            ResultSequence result = expression.evaluate(dynamicContext, contextItems);
            Assert.fail("Expected an exception: XPST0080");
        } catch (DynamicError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        } catch (StaticError ex) {
            Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));
        }
    }

    protected StaticContextBuilder createStaticContextBuilder() {
        try {
            return new StaticContextBuilder()
                    .withNamespace("fn", "http://www.w3.org/2005/xpath-functions")
                    .withNamespace("xss", "http://www.w3.org/2001/XMLSchema")
                    .withNamespace("xml", "http://www.w3.org/XML/1998/namespace")
                    .withBaseUri("jar:" + getClass().getClassLoader().getResource("XQTS_1_0_3.zip").toURI().toString() + "!/");
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
