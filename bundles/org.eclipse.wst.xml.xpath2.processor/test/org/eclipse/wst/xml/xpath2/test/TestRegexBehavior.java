package org.eclipse.wst.xml.xpath2.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

public class TestRegexBehavior extends XPathTestBase {

	@Test
	public void testReplacementReferenceLeadingZero() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		String query = "fn:replace(\"abc\", \"abc\", \"x$00\")";
		if (isDebug()) {
			System.out.println(query);
		}

		XPath2Expression expression = getEngine().parseExpression(query, staticContext);
		Node[] contextItems = new Node[] { };
		ResultSequence result = expression.evaluate(dynamicContext, contextItems);

		String actual = serializeResultSequence(result);
		Assert.assertThat(actual, equalTo("xabc"));
	}

	@Test
	public void testReplacementReferenceLeadingZeroHigh() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		for (int i = 1; i <= 9; i++) {
			String query = "fn:replace(\"abc\", \"abc\", \"x$0" + String.valueOf(i) + "\")";
			if (isDebug()) {
				System.out.println(query);
			}

			XPath2Expression expression = getEngine().parseExpression(query, staticContext);
			Node[] contextItems = new Node[] { };
			ResultSequence result = expression.evaluate(dynamicContext, contextItems);

			String actual = serializeResultSequence(result);
			Assert.assertThat(actual, equalTo("x"));
		}
	}

	@Test
	public void testReplacementReferenceHigh() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		for (int i = 1; i <= 9; i++) {
			String query = "fn:replace(\"abc\", \"abc\", \"x$" + String.valueOf(i) + "\")";
			if (isDebug()) {
				System.out.println(query);
			}

			XPath2Expression expression = getEngine().parseExpression(query, staticContext);
			Node[] contextItems = new Node[] { };
			ResultSequence result = expression.evaluate(dynamicContext, contextItems);

			String actual = serializeResultSequence(result);
			Assert.assertThat(actual, equalTo("x"));
		}
	}

	@Test
	public void testReplacementReferenceHighDoubleDigit() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		for (int i = 1; i <= 9; i++) {
			String query = "fn:replace(\"abc\", \"abc\", \"x$" + String.valueOf(i) + "0\")";
			if (isDebug()) {
				System.out.println(query);
			}

			XPath2Expression expression = getEngine().parseExpression(query, staticContext);
			Node[] contextItems = new Node[] { };
			ResultSequence result = expression.evaluate(dynamicContext, contextItems);

			String actual = serializeResultSequence(result);
			Assert.assertThat(actual, equalTo("x0"));
		}
	}

	@Test
	public void testReplacementReference23() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		String query = "fn:replace(\"abcd\", \"(a)(b)(c)(d)\", \"x$23\")";
		if (isDebug()) {
			System.out.println(query);
		}

		XPath2Expression expression = getEngine().parseExpression(query, staticContext);
		Node[] contextItems = new Node[] { };
		ResultSequence result = expression.evaluate(dynamicContext, contextItems);

		String actual = serializeResultSequence(result);
		Assert.assertThat(actual, equalTo("xb3"));
	}

	/**
	 * This is a regression test for https://github.com/sharwell/webtools.sourceediting.xpath/issues/159.
	 */
	@Test
	public void testReplaceExpressionCannotMatchEmpty() throws TransformerException, ParserConfigurationException {
		StaticContext staticContext = createStaticContextBuilder();
		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext);

		String query = "fn:replace(\"abc\", \"\", \"a\")";
		if (isDebug()) {
			System.out.println(query);
		}

		XPath2Expression expression = getEngine().parseExpression(query, staticContext);
		Node[] contextItems = new Node[] { };

		try {
			expression.evaluate(dynamicContext, contextItems);
			fail("Expected an exception: FORX0003");
		} catch (DynamicError ex) {
			assertEquals("FORX0003", ex.code());
		}
	}

}
