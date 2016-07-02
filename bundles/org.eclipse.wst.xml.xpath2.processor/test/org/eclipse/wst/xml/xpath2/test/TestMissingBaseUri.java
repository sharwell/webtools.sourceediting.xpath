package org.eclipse.wst.xml.xpath2.test;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;
import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

public class TestMissingBaseUri extends XPathTestBase {

	@Test
	public void testFnDocWithoutBaseUri() throws Exception {
		StaticContext staticContext = createStaticContextBuilder()
				.withVariable(new QName("input-context"), new XSAnyURI().getItemType());

		DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)
				.withVariable(new QName("input-context"), new XSAnyURI("TestSources/SpaceBracket.xml"));

		String query = "fn:doc($input-context)";
		if (isDebug()) {
			System.out.println(query);
		}

		List<Matcher<? super String>> matchers = new ArrayList<Matcher<? super String>>();
		matchers.add(CoreMatchers.is(CoreMatchers.equalTo("FONS0005")));

		try {
			XPath2Expression expression = getEngine().parseExpression(query, staticContext);
			Node[] contextItems = new Node[]{};
			ResultSequence result = expression.evaluate(dynamicContext, contextItems);
			Assert.fail("Expected an exception: FONS0005");
		} catch (DynamicError ex) {
			assertThat(ex.code(), CoreMatchers.anyOf(matchers));
		} catch (StaticError ex) {
			assertThat(ex.code(), CoreMatchers.anyOf(matchers));
		}
	}

	protected StaticContextBuilder createStaticContextBuilder() {
		return new StaticContextBuilder()
				.withNamespace("fn", "http://www.w3.org/2005/xpath-functions")
				.withNamespace("xs", "http://www.w3.org/2001/XMLSchema")
				.withNamespace("xml", "http://www.w3.org/XML/1998/namespace");
	}

}
