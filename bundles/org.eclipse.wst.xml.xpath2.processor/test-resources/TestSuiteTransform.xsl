<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:x="http://www.w3.org/2005/02/query-test-XQTSCatalog" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
    <xsl:apply-templates select="x:test-group[x:test-case[@is-XPath2='true']]"/>
  </xsl:template>

  <xsl:template match="x:test-group">
    <xsl:variable name="classname" select="concat('Test_',replace(@name,'-','_'))"/>
    <xsl:result-document href="org/eclipse/wst/xml/xpath2/test/{$classname}.java" method="text">
      <xsl:text><![CDATA[package org.eclipse.wst.xml.xpath2.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLAssert;
import org.eclipse.wst.xml.xpath2.api.DynamicContext;
import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.StaticContext;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.Engine;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.util.DynamicContextBuilder;
import org.eclipse.wst.xml.xpath2.processor.util.StaticContextBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ]]></xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:text><![CDATA[ {

]]></xsl:text>
      <xsl:apply-templates select="x:test-case[@is-XPath2='true']"/>
      <xsl:text><![CDATA[

	private String readFile(String filePath, String fileName, String extension) throws IOException {
		File zipFile;
		try {
			zipFile = new File(getClass().getClassLoader().getResource("XQTS_1_0_3.zip").toURI());
		} catch (URISyntaxException ex) {
			FileNotFoundException fileNotFoundException = new FileNotFoundException();
			fileNotFoundException.initCause(ex);
			throw fileNotFoundException;
		}

		ZipFile sourceFile = new ZipFile(zipFile);
		ZipEntry entry = sourceFile.getEntry(filePath + fileName + extension);
		InputStreamReader inputStreamReader = new InputStreamReader(sourceFile.getInputStream(entry), "UTF-8");
		try {
			char[] data = new char[(int)entry.getSize()];
			int read = inputStreamReader.read(data);
			if (read < data.length) {
				data = Arrays.copyOf(data, read);
			}

			return new String(data);
		} finally {
			sourceFile.close();
			inputStreamReader.close();
		}
	}
}
]]></xsl:text>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="x:test-case">
    <xsl:variable name="methodName" select="replace(@name,'-','_')"/>
  @Test<xsl:if test="x:expected-error">(expected = NullPointerException.class)</xsl:if>
  public void <xsl:value-of select="$methodName"/>() throws IOException, ParserConfigurationException, SAXException, TransformerException {
    String filePath = "<xsl:value-of select="@FilePath"/>";
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

<xsl:apply-templates select="x:input-file" mode="read"/>

    Engine engine = new Engine();
    StaticContext staticContext = new StaticContextBuilder()
          .withNamespace("fn", "http://www.w3.org/2005/xpath-functions")
          .withNamespace("xs", "http://www.w3.org/2001/XMLSchema")
        <xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new ElementType().getItemType())
        </xsl:for-each>;
    DynamicContext dynamicContext = new DynamicContextBuilder(staticContext)
        <xsl:for-each select="x:input-file">
          .withVariable(new QName("<xsl:value-of select="@variable"/>"), new DocType(<xsl:value-of select="replace(@variable,'-','_')"/>, staticContext.getTypeModel()))
        </xsl:for-each>;

<xsl:apply-templates select="x:query">
  <xsl:with-param name="testName" select="@name"/>
  <xsl:with-param name="compare" select="x:output-file/@compare"/>
</xsl:apply-templates>
  }
  </xsl:template>

  <xsl:template match="x:input-file" mode="read">
    <xsl:variable name="variableName" select="replace(@variable,'-','_')"/>
    String raw_<xsl:value-of select="$variableName"/> = readFile("TestSources/", "<xsl:value-of select="text()"/>", ".xml");
    Document <xsl:value-of select="$variableName"/> = documentBuilder.parse(new ByteArrayInputStream(raw_<xsl:value-of select="$variableName"/>.getBytes("UTF-8")));
  </xsl:template>

  <xsl:template match="x:query">
    <xsl:param name="testName"/>
    <xsl:param name="compare"/>
    {
      String query = readFile("Queries/XQuery/" + filePath, "<xsl:value-of select="@name"/>", ".xq");
      query = query.replaceAll("declare\\s+variable\\s+\\$[a-zA-Z_-][a-zA-Z0-9_-]*\\s+external\\s*;", "");
      String expected = readFile("ExpectedTestResults/" + filePath, "<xsl:value-of select="$testName"/>", ".txt");

      XPath2Expression expression = engine.parseExpression(query, staticContext);
      Node[] contextItems = new Node[] { };
      ResultSequence result = expression.evaluate(dynamicContext, contextItems);

      StringWriter writer = new StringWriter();
      for (Iterator&lt;Item&gt; itemIt = result.iterator(); itemIt.hasNext(); ) {
        Item item = itemIt.next();
        if (item instanceof NodeType) {
          Node node = ((NodeType)item).node_value();
          transformer.transform(new DOMSource(node), new StreamResult(writer));
        } else {
          writer.append(item.getStringValue());
        }
      }

      String actual = writer.toString();
<xsl:choose>
  <xsl:when test="$compare='XML'">
      XMLAssert.assertXMLEqual(expected, actual);
  </xsl:when>
  <xsl:when test="$compare='Fragment'">
      XMLAssert.assertXMLEqual("&lt;dummy&gt;" + expected + "&lt;/dummy&gt;", "&lt;dummy&gt;" + actual + "&lt;/dummy&gt;");
  </xsl:when>
  <xsl:when test="$compare='Text'">
      Assert.assertEquals(expected, actual);
  </xsl:when>
  <xsl:when test="$compare='Inspect'">
      XMLAssert.assertXMLEqual("&lt;dummy&gt;" + expected + "&lt;/dummy&gt;", "&lt;dummy&gt;" + actual + "&lt;/dummy&gt;");
  </xsl:when>
  <xsl:when test="$compare='Ignore'">
  </xsl:when>
  <xsl:otherwise>
      Assert.fail("Shouldn't reach this point.");
  </xsl:otherwise>
</xsl:choose>
    }
  </xsl:template>
  <!-- no output for anything else -->
  <xsl:template match="node()|@*"/>
</xsl:stylesheet>
