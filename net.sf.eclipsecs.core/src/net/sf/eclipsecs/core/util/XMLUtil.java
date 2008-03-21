//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.core.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides utility methods for XML manipulations.
 */
public final class XMLUtil {

    private static SAXParserFactory sSAXParserFactory = SAXParserFactory.newInstance();

    private static TransformerFactory sTransformerFactory = TransformerFactory.newInstance();

    /**
     * Private constructor to prevent instances.
     */
    private XMLUtil() {}

    /**
     * Get a named child node. If there is more then one child node with the
     * given name the child returned is undefined.
     * 
     * @param parent The parent node.
     * @param childName The node name of the child node.
     * @return The requested child node or <code>null</code> if no child with
     *         the requested name is found.
     */
    public static Node getChildNode(Node parent, String childName) {
        Node result = null;

        if (parent != null) {
            NodeList childList = parent.getChildNodes();
            int childCount = childList.getLength();
            for (int i = 0; i < childCount; i++) {
                Node child = childList.item(i);
                String nodeName = child.getNodeName();
                if (nodeName.equals(childName)) {
                    result = child;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Get the value of the text node that is passed in.
     * 
     * @param node The node to work on
     * @return Text value of the tag element
     */
    public static String getNodeTextValue(Node node) {
        String nodeValue = null;

        if (node != null) {
            NodeList childNodes = node.getChildNodes();

            if (childNodes.getLength() > 0) {
                Node childNode = childNodes.item(0);

                if (childNode.getNodeType() == Node.TEXT_NODE
                        || childNode.getNodeType() == Node.CDATA_SECTION_NODE) {
                    nodeValue = childNode.getNodeValue();
                }
            }
        }
        return nodeValue;
    }

    /**
     * Get the value of a node attribute.
     * 
     * @param node The nade to get the attribute from.
     * @param attrName Name of the attribute.
     * @return Value of the attribute or <code>null</code> if the attribute
     *         was not found.
     */
    public static String getNodeAttributeValue(Node node, String attrName) {
        String result = null;
        if (node == null) {
            return result;
        }

        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs.getNamedItem(attrName);

        if (attr instanceof Attr) {
            result = ((Attr) attr).getValue();
        }
        else {
            result = getNodeTextValue(attr);
        }

        return result;
    }

    /**
     * Add a new Element and its value to an input Document.
     * 
     * @param document Document to add to
     * @param parent Parent element to add new element to
     * @param tagName Element tag name to add
     * @param value Value of new Element
     * @return Element Newly added Element
     */
    public static Element addElementAndValue(Document document, Element parent, String tagName,
            String value) {
        Element element = document.createElement(tagName);
        parent.appendChild(element);
        Text text = document.createTextNode(value);
        element.appendChild(text);

        return element;
    }

    /**
     * Add a new Element to an input Document.
     * 
     * @param document Document to add to
     * @param parent Parent element to add new element to
     * @param tagName Element tag name to add
     * @return Element Newly added Element
     */
    public static Element addElement(Document document, Element parent, String tagName) {
        Element element = document.createElement(tagName);
        parent.appendChild(element);

        return element;
    }

    /**
     * Parses an input stream with a sax parser using the given default handler.
     * 
     * @param in the input stream
     * @param handler the default handler receiving the sax events
     * @throws ParserConfigurationException error creating the sax parser
     * @throws SAXException error parsing the input stream
     * @throws IOException error reading the input stream
     */
    public static void parseWithSAX(InputStream in, DefaultHandler handler)
        throws ParserConfigurationException, SAXException, IOException {

        parseWithSAX(in, handler, false);
    }

    /**
     * Validated and parses an input stream with a sax parser using the given
     * default handler.
     * 
     * @param in the input stream
     * @param handler the default handler receiving the sax events
     * @param validate <code>true</code> if the xml should be validated.
     * @throws ParserConfigurationException error creating the sax parser
     * @throws SAXException error parsing the input stream
     * @throws IOException error reading the input stream
     */
    public static void parseWithSAX(InputStream in, DefaultHandler handler, boolean validate)
        throws ParserConfigurationException, SAXException, IOException {
        sSAXParserFactory.setValidating(validate);
        SAXParser parser = sSAXParserFactory.newSAXParser();

        parser.parse(in, handler);
    }

    /**
     * Creates a transformer handler that writes to the given output stream. You
     * can send sax events to the transformer and receive a similar output.
     * 
     * @param out the output stream the handler writes to
     * @param doctypePublic the public doctype id or <code>null</code>
     * @param doctypeSystem the system doctype id or <code>null</code>
     * @return the transformer handler where sax events can be sent to.
     * @throws TransformerConfigurationException error creating the transformer
     */
    public static TransformerHandler writeWithSax(OutputStream out, String doctypePublic,
            String doctypeSystem) throws TransformerConfigurationException {

        SAXTransformerFactory saxFactory = (SAXTransformerFactory) sTransformerFactory;
        Templates templates = null;

        InputStream in = null;
        try {
            in = new BufferedInputStream(XMLUtil.class.getResourceAsStream("identity.xsl")); //$NON-NLS-1$
            templates = saxFactory.newTemplates(new StreamSource(in));
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        StreamResult result = new StreamResult(out);

        // uses identity transformation (in==out)
        TransformerHandler handler = saxFactory.newTransformerHandler(templates);
        if (doctypePublic != null) {
            handler.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctypePublic);
        }
        if (doctypeSystem != null) {
            handler.getTransformer().setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem);
        }

        handler.setResult(result);

        return handler;
    }

}