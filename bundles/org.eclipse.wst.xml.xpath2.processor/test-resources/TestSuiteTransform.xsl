<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:x="http://www.w3.org/2005/02/query-test-XQTSCatalog" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:template match="/">
    <xsl:apply-templates select="x:test-group[x:test-case[@is-XPath2='true']]"/>
  </xsl:template>

  <xsl:template match="x:test-group">
    <xsl:variable name="classname" select="concat('Test_',replace(@name,'-','_'))"/>
    <xsl:result-document href="org/eclipse/wst/xml/xpath2/test/{$classname}.java" method="text">
      <xsl:text><![CDATA[package org.eclipse.wst.xml.xpath2.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

]]></xsl:text>

      <xsl:if test="x:test-case[@is-XPath2='true']/x:output-file[@compare='XML' or @compare='Fragment' or @compare='Inspect']">
        <xsl:text><![CDATA[import org.custommonkey.xmlunit.XMLAssert;
]]></xsl:text>
      </xsl:if>
<xsl:text><![CDATA[import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;]]></xsl:text><xsl:if test="x:test-case[@is-XPath2='true']/x:expected-error"><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.DynamicError;]]></xsl:text></xsl:if><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.Engine;]]></xsl:text><xsl:if test="x:test-case[@is-XPath2='true']/x:expected-error"><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.StaticError;]]></xsl:text></xsl:if><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;]]></xsl:text><xsl:if test="x:test-case[@is-XPath2='true']/x:input-URI"><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;]]></xsl:text></xsl:if><xsl:text><![CDATA[
import org.eclipse.wst.xml.xpath2.processor.util.DynamicContextBuilder;
import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ]]></xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:text><![CDATA[ extends XPathTestBase {

]]></xsl:text>
      <xsl:apply-templates select="x:test-case[@is-XPath2='true']"/>
      <xsl:text><![CDATA[
}
]]></xsl:text>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="x:test-case">
    <xsl:variable name="methodName" select="replace(@name,'-','_')"/>
  @Test
  public void <xsl:value-of select="$methodName"/>() throws IOException, ParserConfigurationException, SAXException, TransformerException {
    String filePath = "<xsl:value-of select="@FilePath"/>";

<xsl:apply-templates select="x:input-file | x:input-URI" mode="read"/>

    StaticContext staticContext = createStaticContextBuilder()<xsl:for-each select="x:input-URI">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new XSAnyURI().getItemType())</xsl:for-each><xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new ElementType().getItemType())</xsl:for-each>;
    DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)<xsl:for-each select="x:input-URI">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new XSAnyURI(<xsl:value-of select="replace(@variable,'-','_')"/>))</xsl:for-each><xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new DocType(<xsl:value-of select="replace(@variable,'-','_')"/>, staticContext.getTypeModel()))</xsl:for-each>;

<xsl:apply-templates select="x:query">
  <xsl:with-param name="testName" select="@name"/>
  <xsl:with-param name="compare" select="x:output-file/@compare"/>
  <xsl:with-param name="outputName" select="x:output-file/text()"/>
  <xsl:with-param name="expectedError" select="x:expected-error/text()"/>
</xsl:apply-templates>
  }
  </xsl:template>

  <xsl:template match="x:input-URI" mode="read">
    <xsl:variable name="variableName" select="replace(@variable,'-','_')"/>
    String <xsl:value-of select="$variableName"/> = "<xsl:value-of select="text()"/>.xml";
  </xsl:template>

  <xsl:template match="x:input-file" mode="read">
    <xsl:variable name="variableName" select="replace(@variable,'-','_')"/>
    <xsl:variable name="inputId" select="text()"/>
    <xsl:variable name="fileName" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:source[@ID=$inputId]/@FileName"/>
    <xsl:choose>
      <xsl:when test="ends-with($fileName, '.xml')">
        String raw_<xsl:value-of select="$variableName"/> = readFile("<xsl:value-of select="$fileName"/>");
        Document <xsl:value-of select="$variableName"/> = getDocumentBuilder().parse(new ByteArrayInputStream(raw_<xsl:value-of select="$variableName"/>.getBytes("UTF-8")));
      </xsl:when>
      <xsl:otherwise>
        String <xsl:value-of select="$variableName"/> = readFile("<xsl:value-of select="$fileName"/>");
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:query">
    <xsl:param name="testName"/>
    <xsl:param name="outputName"/>
    <xsl:param name="compare"/>
    <xsl:param name="expectedError"/>
    String query = readFile("Queries/XQuery/" + filePath + "<xsl:value-of select="@name"/>.xq");
    if (isDebug()) {
      System.out.println(query);
    }
    query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
    query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s+=\\s+\"[^\"]*\"\\s*;", "");<xsl:if test="$outputName">
    List&lt;Matcher&lt;? super String&gt;&gt; expectedMatchers = new ArrayList&lt;Matcher&lt;? super String&gt;&gt;();<xsl:for-each select="$outputName"><xsl:choose><xsl:when test="$compare='XML'">
    expectedMatchers.add(CoreMatchers.is(xmlEqualTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Fragment' or $compare='Inspect'">
    expectedMatchers.add(CoreMatchers.is(xmlFragmentEqualTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Text'">
    expectedMatchers.add(CoreMatchers.is(CoreMatchers.equalTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Ignore'"/><xsl:otherwise>
    Assert.fail("Shouldn't reach this point.");</xsl:otherwise></xsl:choose></xsl:for-each></xsl:if>

<xsl:choose>
<xsl:when test="$expectedError"><xsl:if test="$expectedError!='*'">
	List&lt;Matcher&lt;? super String&gt;&gt; matchers = new ArrayList&lt;Matcher&lt;? super String&gt;&gt;();<xsl:for-each select="$expectedError">
	matchers.add(CoreMatchers.is(CoreMatchers.equalTo("<xsl:value-of select="."/>")));</xsl:for-each></xsl:if>
	try {
		XPath2Expression expression = getEngine().parseExpression(query, staticContext);
		Node[] contextItems = new Node[] { };
		ResultSequence result = expression.evaluate(dynamicContext, contextItems);<xsl:choose><xsl:when test="$outputName">

		String actual = serializeResultSequence(result);
<xsl:if test="$compare != 'Ignore'">
		Assert.assertThat(actual, CoreMatchers.anyOf(expectedMatchers));
</xsl:if>
		</xsl:when><xsl:otherwise>
		Assert.fail("Expected an exception: <xsl:for-each select="$expectedError"><xsl:value-of select="."/><xsl:if test="position() != last()"> or </xsl:if></xsl:for-each>");</xsl:otherwise></xsl:choose>
	} catch (DynamicError ex) {<xsl:if test="$expectedError!='*'">
		Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));</xsl:if>
	} catch (StaticError ex) {<xsl:if test="$expectedError!='*'">
		Assert.assertThat(ex.code(), CoreMatchers.anyOf(matchers));</xsl:if>
	}
</xsl:when>
<xsl:otherwise>
    XPath2Expression expression = getEngine().parseExpression(query, staticContext);
    Node[] contextItems = new Node[] { };
    ResultSequence result = expression.evaluate(dynamicContext, contextItems);

    String actual = serializeResultSequence(result);
<xsl:if test="$compare != 'Ignore'">
		Assert.assertThat(actual, CoreMatchers.anyOf(expectedMatchers));
</xsl:if>
</xsl:otherwise>
</xsl:choose>
  </xsl:template>
  <!-- no output for anything else -->
  <xsl:template match="node()|@*"/>
</xsl:stylesheet>