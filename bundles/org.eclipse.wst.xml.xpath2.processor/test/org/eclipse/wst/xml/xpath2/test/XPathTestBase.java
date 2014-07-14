package org.eclipse.wst.xml.xpath2.test;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.wst.xml.xpath2.api.Item;
import org.eclipse.wst.xml.xpath2.api.ResultBuffer;
import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.internal.ChildAxis;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AttrType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.DocType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.NodeType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.TextType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.hamcrest.Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author sam
 */
public abstract class XPathTestBase {

	private ZipFile _zipFile;

	public static <T> Matcher<T> xmlEqualTo(T operand) {
		return IsXMLEqual.xmlEqualTo(operand);
	}

	public static <T> Matcher<T> xmlFragmentEqualTo(T operand) {
		return IsXMLEqual.xmlFragmentEqualTo(operand);
	}

	private synchronized ZipFile getZipFile() throws IOException {
		if (_zipFile == null) {
			File zipFile;
			try {
				zipFile = new File(getClass().getClassLoader().getResource("XQTS_1_0_3.zip").toURI());
			} catch (URISyntaxException ex) {
				FileNotFoundException fileNotFoundException = new FileNotFoundException();
				fileNotFoundException.initCause(ex);
				throw fileNotFoundException;
			}

			ZipFile sourceFile = new ZipFile(zipFile);
			_zipFile = sourceFile;
			return sourceFile;
		}

		return _zipFile;
	}

	public String readFile(String filePath, String fileName, String extension) throws IOException {
		ZipFile sourceFile = getZipFile();
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
			inputStreamReader.close();
		}
	}

	public String serializeResultSequence(ResultSequence result) throws TransformerException, ParserConfigurationException {
		ResultSequence normalized = normalizeSequence(result);

		assert normalized.size() == 1;
		DocType normalizedDocument = (DocType)normalized.item(0);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "");
//		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, null);
//		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, null);
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(normalizedDocument.value()), new StreamResult(writer));
		return writer.toString();
	}

	private final DocumentBuilder _documentBuilder;
	private final Document _dummyDocument;
	{
		try {
			_documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}

		_dummyDocument = _documentBuilder.newDocument();
	}

	private ResultSequence normalizeSequence(ResultSequence sequence) throws ParserConfigurationException {
		/* 1. If the sequence that is input to serialization is empty, create a
		 * sequence S1 that consists of a zero-length string. Otherwise, copy
		 * each item in the sequence that is input to serialization to create
		 * the new sequence S1.
		 */
		ResultSequence s1 = sequence.empty() ? new XSString("") : sequence;

		/* 2. For each item in S1, if the item is atomic, obtain the lexical
		 * representation of the item by casting it to an xs:string and copy the
		 * string representation to the new sequence; otherwise, copy the item,
		 * which will be a node, to the new sequence. The new sequence is S2.
		 */
		ResultSequence s2;
		{
			ResultBuffer buffer = new ResultBuffer();
			for (Iterator<Item> itemIt = s1.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				if (item instanceof AnyAtomicType) {
					buffer.append(new XSString(item.getStringValue()));
				} else {
					buffer.append(item);
				}
			}

			s2 = buffer.getSequence();
		}

		/* 3. For each subsequence of adjacent strings in S2, copy a single
		 * string to the new sequence equal to the values of the strings in the
		 * subsequence concatenated in order, each separated by a single space.
		 * Copy all other items to the new sequence. The new sequence is S3.
		 */
		ResultSequence s3;
		{
			ResultBuffer buffer = new ResultBuffer();
			StringBuilder builder = null;
			for (Iterator<Item> itemIt = s2.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				if (item instanceof XSString) {
					if (builder == null) {
						builder = new StringBuilder(item.getStringValue());
					} else {
						builder.append(' ').append(item.getStringValue());
					}
				} else {
					if (builder != null) {
						buffer.append(new XSString(builder.toString()));
					}

					buffer.append(item);
				}
			}

			if (builder != null) {
				buffer.append(new XSString(builder.toString()));
			}

			s3 = buffer.getSequence();
		}

		/* 4. For each item in S3, if the item is a string, create a text node
		 * in the new sequence whose string value is equal to the string;
		 * otherwise, copy the item to the new sequence. The new sequence is S4.
		 */
		ResultSequence s4;
		{
			ResultBuffer buffer = new ResultBuffer();
			for (Iterator<Item> itemIt = s3.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				if (item instanceof XSString) {
					buffer.append(createTextNode(item.getStringValue()));
				} else {
					buffer.append(item);
				}
			}

			s4 = buffer.getSequence();
		}

		/* 5. For each item in S4, if the item is a document node, copy its
		 * children to the new sequence; otherwise, copy the item to the new
		 * sequence. The new sequence is S5.
		 */
		ResultSequence s5;
		{
			ResultBuffer buffer = new ResultBuffer();
			for (Iterator<Item> itemIt = s4.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				if (item instanceof DocType) {
					ChildAxis childAxis = new ChildAxis();
					childAxis.iterate((DocType)item, buffer, null);
				} else {
					buffer.append(item);
				}
			}

			s5 = buffer.getSequence();
		}

		/* 6. For each subsequence of adjacent text nodes in S5, copy a single
		 * text node to the new sequence equal to the values of the text nodes
		 * in the subsequence concatenated in order. Any text nodes with values
		 * of zero length are dropped. Copy all other items to the new sequence.
		 * The new sequence is S6.
		 */
		ResultSequence s6;
		{
			ResultBuffer buffer = new ResultBuffer();
			StringBuilder builder = null;
			for (Iterator<Item> itemIt = s5.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				if (item instanceof TextType) {
					if (builder == null) {
						builder = new StringBuilder(((TextType)item).getStringValue());
					} else {
						builder.append(item.getStringValue());
					}
				} else {
					if (builder != null && builder.length() > 0) {
						buffer.append(createTextNode(builder.toString()));
					}

					buffer.append(item);
				}
			}

			if (builder != null && builder.length() > 0) {
				buffer.append(createTextNode(builder.toString()));
			}

			s6 = buffer.getSequence();
		}

		/* 7. It is a serialization error [err:SENR0001] if an item in S6 is an
		 * attribute node or a namespace node. Otherwise, construct a new
		 * sequence, S7, that consists of a single document node and copy all
		 * the items in the sequence, which are all nodes, as children of that
		 * document node.
		 */
		ResultSequence s7;
		{
			Document document = _documentBuilder.newDocument();
			document.setStrictErrorChecking(false);
			for (Iterator<Item> itemIt = s6.iterator(); itemIt.hasNext(); ) {
				Item item = itemIt.next();
				assert item instanceof NodeType;
				if (item instanceof AttrType) {
					throw new DynamicError("SENR0001", "It is an error if an item in S6 in sequence normalization is an attribute node or a namespace node.", null);
				}

				Node imported = document.importNode(((NodeType)item).node_value(), true);
				document.appendChild(imported);
			}

			s7 = new DocType(document, null);
		}

		return s7;
	}

	private TextType createTextNode(String text) throws ParserConfigurationException {
		return createTextNode(_dummyDocument, text);
	}

	private static TextType createTextNode(Document document, String text) {
		Text textNode = document.createTextNode(text);
		return new TextType(textNode, null);
	}
}
