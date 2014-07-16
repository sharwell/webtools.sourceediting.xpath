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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.custommonkey.xmlunit.XMLAssert;
import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.Engine;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.internal.function.FnCollection;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSAnyURI;
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

    <xsl:if test="x:input-URI | x:defaultCollection">
      Map&lt;String, List&lt;Document&gt;&gt; collections = new HashMap&lt;String, List&lt;Document&gt;&gt;();
    </xsl:if>

    StaticContext staticContext = createStaticContextBuilder()<xsl:for-each select="x:input-URI">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new XSAnyURI().getItemType())</xsl:for-each><xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new ElementType().getItemType())</xsl:for-each>;

<xsl:apply-templates select="x:input-file | x:input-URI | x:defaultCollection" mode="read"/>

    DynamicContext dynamicContext = createDynamicContextBuilder(staticContext)<xsl:for-each select="x:input-URI">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new XSAnyURI("<xsl:apply-templates select="." mode="withVariable"/>"))</xsl:for-each><xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new DocType(<xsl:value-of select="replace(@variable,'-','_')"/>, staticContext.getTypeModel()))</xsl:for-each><xsl:if test="x:input-URI | x:defaultCollection">
          .withCollections(collections)</xsl:if>;

<xsl:apply-templates select="x:query">
  <xsl:with-param name="testName" select="@name"/>
  <xsl:with-param name="compare" select="x:output-file/@compare"/>
  <xsl:with-param name="outputName" select="x:output-file/text()"/>
  <xsl:with-param name="expectedError" select="x:expected-error/text()"/>
</xsl:apply-templates>
  }
  </xsl:template>

  <xsl:template match="x:input-URI" mode="read">
    <xsl:variable name="inputId" select="text()"/>
    <xsl:variable name="collection" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:collection[@ID=$inputId]"/>
    <xsl:if test="$collection">
      <xsl:call-template name="loadCollection">
        <xsl:with-param name="variableName" select="replace(@variable,'-','_')"/>
        <xsl:with-param name="collection" select="$collection"/>
        <xsl:with-param name="collectionName">
          <xsl:text>"</xsl:text>
          <xsl:value-of select="text()"/>
          <xsl:text>"</xsl:text>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="x:input-URI" mode="withVariable">
    <xsl:variable name="inputId" select="text()"/>
    <xsl:variable name="source" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:source[@ID=$inputId]"/>
    <xsl:variable name="collection" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:collection[@ID=$inputId]"/>
    <xsl:choose>
      <xsl:when test="$collection">
        <xsl:value-of select="$inputId"/>
      </xsl:when>
      <xsl:when test="$source">
        <xsl:value-of select="$source/@FileName"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:defaultCollection" mode="read">
    <xsl:variable name="inputId" select="text()"/>
    <xsl:call-template name="loadCollection">
      <xsl:with-param name="variableName" select="'defaultCollection'"/>
      <xsl:with-param name="collection" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:collection[@ID=$inputId]"/>
      <xsl:with-param name="collectionName" select="'FnCollection.DEFAULT_COLLECTION_URI'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="loadCollection">
    <xsl:param name="variableName"/>
    <xsl:param name="collection"/>
    <xsl:param name="collectionName"/>
    List&lt;Document&gt; <xsl:value-of select="$variableName"/> = new ArrayList&lt;Document&gt;();
    <xsl:for-each select="$collection/x:input-document">
      <xsl:variable name="inputDocumentVariable" select="replace(text(),'-','_')"/>
      <xsl:variable name="inputDocumentId" select="text()"/>
      <xsl:variable name="source" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:source[@ID=$inputDocumentId]"/>
      <xsl:variable name="fileName" select="$source/@FileName"/>
      <xsl:variable name="schema">
        <xsl:choose>
          <xsl:when test="$source/@schema">
            <xsl:variable name="schemaNode" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:schema[@ID=($source/@schema)]"/>
            <xsl:value-of select="$schemaNode/@FileName"/>
          </xsl:when>
          <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="ends-with($fileName, '.xml')">
          String raw_<xsl:value-of select="$inputDocumentVariable"/> = readFile("<xsl:value-of select="$fileName"/>");
          Document <xsl:value-of select="$inputDocumentVariable"/> = getDocumentBuilder("<xsl:value-of select="$schema"/>").parse(new ByteArrayInputStream(raw_<xsl:value-of select="$inputDocumentVariable"/>.getBytes("UTF-8")), "<xsl:value-of select="$fileName"/>");
          <xsl:value-of select="$variableName"/>.add(<xsl:value-of select="$inputDocumentVariable"/>);
        </xsl:when>
        <xsl:otherwise>
          Assert.fail("Not yet implemented.");
          <!--String <xsl:value-of select="$inputDocumentVariable"/> = readFile("<xsl:value-of select="$fileName"/>");-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
    collections.put(staticContext.getBaseUri().resolve(<xsl:value-of select="$collectionName"/>).toString(), <xsl:value-of select="$variableName"/>);
  </xsl:template>

  <xsl:template match="x:input-file" mode="read">
    <xsl:variable name="variableName" select="replace(@variable,'-','_')"/>
    <xsl:variable name="inputId" select="text()"/>
    <xsl:variable name="source" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:source[@ID=$inputId]"/>
    <xsl:variable name="fileName" select="$source/@FileName"/>
    <xsl:variable name="schema">
      <xsl:choose>
        <xsl:when test="$source/@schema">
          <xsl:variable name="schemaNode" select="doc('../XQTSCatalog.xml')/x:test-suite/x:sources/x:schema[@ID=($source/@schema)]"/>
          <xsl:value-of select="$schemaNode/@FileName"/>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="ends-with($fileName, '.xml')">
        String raw_<xsl:value-of select="$variableName"/> = readFile("<xsl:value-of select="$fileName"/>");
        Document <xsl:value-of select="$variableName"/> = getDocumentBuilder("<xsl:value-of select="$schema"/>").parse(new ByteArrayInputStream(raw_<xsl:value-of select="$variableName"/>.getBytes("UTF-8")), "<xsl:value-of select="$fileName"/>");
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
    query = query.replaceAll("import\\s+schema\\s+namespace\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*\"[^\"]*\"\\s*;", "");<xsl:if test="$outputName">
    List&lt;Matcher&lt;? super String&gt;&gt; expectedMatchers = new ArrayList&lt;Matcher&lt;? super String&gt;&gt;();<xsl:for-each select="$outputName"><xsl:choose><xsl:when test="$compare='XML'">
    expectedMatchers.add(CoreMatchers.is(xmlEqualTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Fragment' or $compare='Inspect'">
    expectedMatchers.add(CoreMatchers.is(xmlFragmentEqualTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Text'">
    expectedMatchers.add(CoreMatchers.is(CoreMatchers.equalTo(readFile("ExpectedTestResults/" + filePath + "<xsl:value-of select="."/>"))));</xsl:when><xsl:when test="$compare='Ignore'"/><xsl:otherwise>
    Assert.fail("Shouldn't reach this point.");</xsl:otherwise></xsl:choose></xsl:for-each></xsl:if><xsl:if test="$compare='Inspect'">
    if (!checkInspections()) {
      expectedMatchers.add(CoreMatchers.anything());
    }</xsl:if>

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
