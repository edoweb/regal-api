/*
 * Copyright 2012 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package archive.fedora;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class XmlUtils {

    @SuppressWarnings({ "javadoc", "serial" })
    public static class XPathException extends RuntimeException {

	public XPathException(Throwable cause) {
	    super(cause);
	}
    }

    @SuppressWarnings({ "javadoc", "serial" })
    public static class ReadException extends RuntimeException {
	public ReadException(String message) {
	    super(message);
	}

	public ReadException(Throwable cause) {
	    super(cause);
	}
    }

    @SuppressWarnings({ "javadoc", "serial" })
    public static class StreamNotClosedException extends RuntimeException {
	public StreamNotClosedException(String message) {
	    super(message);
	}

	public StreamNotClosedException(Throwable cause) {
	    super(cause);
	}
    }

    final static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    /**
     * @param digitalEntityFile
     *            the xml file
     * @return the root element as org.w3c.dom.Element
     * @throws XmlException
     *             RuntimeException if something goes wrong
     */
    public static Element getDocument(File digitalEntityFile) {
	try {
	    return getDocument(new FileInputStream(digitalEntityFile));
	} catch (FileNotFoundException e) {
	    throw new XmlException(e);
	}
    }

    /**
     * @param xmlString
     *            a xml string
     * @return the root element as org.w3c.dom.Element
     * @throws XmlException
     *             RuntimeException if something goes wrong
     */
    public static Element getDocument(String xmlString) {
	return getDocument(new ByteArrayInputStream(xmlString.getBytes()));

    }

    /**
     * @param file
     *            file to store the string in
     * @param str
     *            the string will be stored in file
     * @return a file containing the string
     */
    @Deprecated
    public static File stringToFile(File file, String str) {
	try {
	    file.createNewFile();
	    try (FileOutputStream writer = new FileOutputStream(file)) {
		// TODO uhh prevent memory overload
		writer.write(str.replace("\n", " ").replace("  ", " ")
			.getBytes("utf-8"));
	    }
	} catch (IOException e) {
	    throw new ReadException(e);
	}
	str = null;
	return file;
    }

    /**
     * @param file
     *            file to store the string in
     * @param str
     *            the string will be stored in file
     * @return a file containing the string
     */
    public static File newStringToFile(File file, String str) {
	try {
	    file.createNewFile();
	    try (FileOutputStream writer = new FileOutputStream(file)) {
		writer.write(str.getBytes("utf-8"));
	    }
	} catch (IOException e) {
	    throw new ReadException(e);
	}
	str = null;
	return file;
    }

    /**
     * @param file
     *            the contents of this file will be converted to a string
     * @return a string with the content of the file
     */
    public static String fileToString(File file) {
	if (file == null || !file.exists()) {
	    throw new ReadException("");
	}
	byte[] buffer = new byte[(int) file.length()];
	try (BufferedInputStream f = new BufferedInputStream(
		new FileInputStream(file))) {
	    f.read(buffer);
	} catch (IOException e) {
	    throw new ReadException(e);
	}
	return new String(buffer);
    }

    /**
     * @param xPathStr
     *            a xpath expression
     * @param root
     *            the xpath is applied to this element
     * @param nscontext
     *            a NamespaceContext
     * @return a list of elements
     */
    public static List<Element> getElements(String xPathStr, Element root,
	    NamespaceContext nscontext) {
	XPathFactory xpathFactory = XPathFactory.newInstance();
	XPath xpath = xpathFactory.newXPath();
	if (nscontext != null)
	    xpath.setNamespaceContext(nscontext);
	NodeList elements;
	try {
	    elements = (NodeList) xpath.evaluate(xPathStr, root,
		    XPathConstants.NODESET);

	    List<Element> result = new Vector<Element>();
	    for (int i = 0; i < elements.getLength(); i++) {
		try {
		    Element element = (Element) elements.item(i);
		    result.add(element);
		} catch (ClassCastException e) {
		    logger.warn(e.getMessage());
		}
	    }
	    return result;
	} catch (XPathExpressionException e1) {
	    throw new XPathException(e1);
	}

    }

    /**
     * @param inputStream
     *            the xml stream
     * @return the root element as org.w3c.dom.Element
     * @throws XmlException
     *             RuntimeException if something goes wrong
     */
    public static Element getDocument(InputStream inputStream) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    // factory.setNamespaceAware(true);
	    // factory.isValidating();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document doc = docBuilder
		    .parse(new BufferedInputStream(inputStream));
	    Element root = doc.getDocumentElement();
	    root.normalize();
	    return root;
	} catch (FileNotFoundException e) {
	    throw new XmlException(e);
	} catch (SAXException e) {
	    throw new XmlException(e);
	} catch (IOException e) {
	    throw new XmlException(e);
	} catch (ParserConfigurationException e) {
	    throw new XmlException(e);
	}

    }

    /**
     * Validates an xml String
     * 
     * @param oaidc
     *            xml String
     * @param schema
     *            a schema to validate against
     */
    public static void validate(InputStream oaidc, InputStream schema) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    factory.setNamespaceAware(true);
	    // "Valid" means valid to a DTD. We want to valid against a schema,
	    // so we turn of dtd validation here
	    factory.setValidating(false);

	    SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    Schema s = schemaFactory.newSchema(new StreamSource(schema));

	    factory.setSchema(s);
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    docBuilder.setErrorHandler(new ErrorHandler() {
		public void fatalError(SAXParseException exception)
			throws SAXException {
		    throw new XmlException(exception);
		}

		public void error(SAXParseException exception)
			throws SAXException {
		    throw new XmlException(exception);
		}

		public void warning(SAXParseException exception)
			throws SAXException {
		    throw new XmlException(exception);
		}
	    });
	    docBuilder.parse(oaidc);

	} catch (FileNotFoundException e) {
	    throw new XmlException(e);
	} catch (SAXException e) {
	    throw new XmlException(e);
	} catch (IOException e) {
	    throw new XmlException(e);
	} catch (ParserConfigurationException e) {
	    throw new XmlException(e);
	}

    }

    /**
     * Creates a plain xml string of the node and of all it's children. The xml
     * string has no XML declaration.
     * 
     * @param node
     *            a org.w3c.dom.Node
     * @return a plain string representation of the node it's children
     */
    public static String nodeToString(Node node) {
	try {
	    TransformerFactory transFactory = TransformerFactory.newInstance();
	    Transformer transformer = transFactory.newTransformer();
	    StringWriter buffer = new StringWriter(1024);
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
		    "yes");

	    transformer
		    .transform(new DOMSource(node), new StreamResult(buffer));
	    String str = buffer.toString();
	    return str;
	} catch (Exception e) {
	    e.printStackTrace();
	} catch (Error error) {
	    error.printStackTrace();
	}
	return "";
    }

    /**
     * @param file
     *            an xml file
     * @return the root element in a namespace aware manner
     */
    public static Element getNamespaceAwareDocument(File file) {
	try {
	    return getNamespaceAwareDocument(new FileInputStream(file));
	} catch (FileNotFoundException e) {
	    throw new XmlException(e);
	}
    }

    private static Element getNamespaceAwareDocument(InputStream inputStream) {
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    factory.setNamespaceAware(true);
	    factory.isValidating();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    Document doc = docBuilder
		    .parse(new BufferedInputStream(inputStream));
	    Element root = doc.getDocumentElement();
	    root.normalize();
	    return root;
	} catch (FileNotFoundException e) {
	    throw new XmlException(e);
	} catch (SAXException e) {
	    throw new XmlException(e);
	} catch (IOException e) {
	    throw new XmlException(e);
	} catch (ParserConfigurationException e) {
	    throw new XmlException(e);
	}

    }

}
