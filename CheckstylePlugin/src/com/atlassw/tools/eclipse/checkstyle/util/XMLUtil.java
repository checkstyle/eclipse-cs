//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.util;

//=================================================
// Imports from java namespace
//=================================================
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.EmptyStackException;
import java.util.Stack;

//=================================================
// Imports from javax namespace
//=================================================
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
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
public final class XMLUtil
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static Stack                  sDocBuilderCache      = new Stack();

    private static DocumentBuilderFactory sDocBuilderFactory    = DocumentBuilderFactory
                                                                        .newInstance();

    private static SAXParserFactory       sSAXParserFactory     = SAXParserFactory.newInstance();

    private static TransformerFactory     sTransformerFactory   = TransformerFactory.newInstance();

    private static final int              MAX_DOC_BUILDER_CACHE = 10;

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Private constructor to prevent instances.
     */
    private XMLUtil()
    {}

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get a named child node. If there is more then one child node with the
     * given name the child returned is undefined.
     * 
     * @param parent The parent node.
     * 
     * @param childName The node name of the child node.
     * 
     * @return The requested child node or <code>null</code> if no child with
     *         the requested name is found.
     */
    public static Node getChildNode(Node parent, String childName)
    {
        Node result = null;

        if (parent != null)
        {
            NodeList childList = parent.getChildNodes();
            int childCount = childList.getLength();
            for (int i = 0; i < childCount; i++)
            {
                Node child = childList.item(i);
                String nodeName = child.getNodeName();
                if (nodeName.equals(childName))
                {
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
     * 
     * @return Text value of the tag element
     */
    public static String getNodeTextValue(Node node)
    {
        String nodeValue = null;

        if (node != null)
        {
            NodeList childNodes = node.getChildNodes();

            if (childNodes.getLength() > 0)
            {
                Node childNode = childNodes.item(0);

                if (childNode.getNodeType() == Node.TEXT_NODE
                        || childNode.getNodeType() == Node.CDATA_SECTION_NODE)
                {
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
     * 
     * @param attrName Name of the attribute.
     * 
     * @return Value of the attribute or <code>null</code> if the attribute
     *         was not found.
     */
    public static String getNodeAttributeValue(Node node, String attrName)
    {
        String result = null;
        if (node == null)
        {
            return result;
        }

        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs.getNamedItem(attrName);

        if (attr instanceof Attr)
        {
            result = ((Attr) attr).getValue();
        }
        else
        {
            result = getNodeTextValue(attr);
        }

        return result;
    }

    /**
     * Add a new Element and its value to an input Document.
     * 
     * @param document Document to add to
     * 
     * @param parent Parent element to add new element to
     * 
     * @param tagName Element tag name to add
     * 
     * @param value Value of new Element
     * 
     * @return Element Newly added Element
     */
    public static Element addElementAndValue(Document document, Element parent, String tagName,
            String value)
    {
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
     * 
     * @param parent Parent element to add new element to
     * 
     * @param tagName Element tag name to add
     * 
     * @return Element Newly added Element
     */
    public static Element addElement(Document document, Element parent, String tagName)
    {
        Element element = document.createElement(tagName);
        parent.appendChild(element);

        return element;
    }

    /**
     * Create a new Document.
     * 
     * @throws CheckstylePluginException An error occurred during creation.
     * 
     * @return Document Newly created Document
     */
    public static Document newDocument() throws CheckstylePluginException
    {
        Document document = null;
        try
        {
            DocumentBuilder docBuilder = getDocumentBuilder();
            document = docBuilder.newDocument();
            releaseDocumentBuilder(docBuilder);
        }
        catch (ParserConfigurationException ex)
        {
            throw new CheckstylePluginException("ParserConfigurationException" + ex.getMessage());
        }
        catch (DOMException ex)
        {
            throw new CheckstylePluginException("Caught DOMException " + ex.getMessage());
        }

        return document;
    }

    /**
     * Converts the specified String into an XML Document. If the String can't
     * be parsed, then null is returned.
     * 
     * @param xmlString The String to parse into an XML Document
     * @return Document The parsed Docuement
     */
    public static Document newDocument(String xmlString)
    {
        //
        //  Parse the document.
        //        
        Document document = null;
        try
        {
            ByteArrayInputStream bs = new ByteArrayInputStream(xmlString.getBytes());
            DocumentBuilder docBuilder = getDocumentBuilder();
            document = docBuilder.parse(bs);
            releaseDocumentBuilder(docBuilder);
        }
        catch (ParserConfigurationException ex)
        {
            //you get nothing-
            document = null;
            CheckstyleLog.warning("Exception while parsing XML", ex);
        }
        catch (DOMException ex)
        {
            //you get nothing-
            document = null;
            CheckstyleLog.warning("Exception while parsing XML", ex);
        }
        catch (Exception e)
        {
            //humm?
            document = null;
            CheckstyleLog.warning("Exception while parsing XML", e);
        }

        return document;
    }

    /**
     * Create a document from the contents of a stream.
     * 
     * @param inStream Stream to read from.
     * 
     * @return Resulting Document.
     */
    public static Document newDocument(InputStream inStream)
    {
        Document result = null;
        try
        {
            DocumentBuilder docBuilder = getDocumentBuilder();
            result = docBuilder.parse(inStream);
            releaseDocumentBuilder(docBuilder);
        }
        catch (ParserConfigurationException ex)
        {
            result = null;
            CheckstyleLog.warning("Exception while parsing XML", ex);
        }
        catch (IOException ex)
        {
            result = null;
            CheckstyleLog.warning("Exception while reading XML", ex);
        }
        catch (SAXException e)
        {
            result = null;
            CheckstyleLog.warning("Exception while parsing XML file", e);
        }

        return result;
    }

    /**
     * Serialize Document into String Rep.
     * 
     * @param doc - Document to be serialized
     * 
     * @param indent - boolean indicating whether or not to indent tags
     * 
     * @throws CheckstylePluginException - An error occurred during
     *             serialization
     * 
     * @return String - Serialized string representation of doc
     */
    public static String serializeDocument(Document doc, boolean indent)
            throws CheckstylePluginException
    {

        String result = null;

        try
        {
            final Document document = doc;
            final StringWriter writer = new StringWriter();

            Source theSource = new DOMSource(doc);
            Result theResult = new StreamResult(writer);

            //A transformer without stylesheet does identity transformation
            Transformer transformer = sTransformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

            transformer.transform(theSource, theResult);

            result = writer.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String errMsg = "Exception occurred while serializing document: " + e.getMessage();
            throw new CheckstylePluginException(errMsg);
        }
        return result;
    }

    /**
     * Serialize Document into String Rep, delegates to method with two
     * arguments (indent set to false).
     * 
     * @param doc - Document to be serialized
     * 
     * @throws CheckstylePluginException - An error occurred during
     *             serialization.
     * 
     * @return String - Serialized string representation of doc
     */
    public static String serializeDocument(Document doc) throws CheckstylePluginException
    {
        return serializeDocument(doc, false);
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
            throws ParserConfigurationException, SAXException, IOException
    {

        SAXParser parser = sSAXParserFactory.newSAXParser();
        parser.parse(in, handler);
    }

    /**
     * Creates a transformer handler that writes to the given output stream. You
     * can send sax events to the transformer and receive a similar output.
     * 
     * @param out the output stream the handler writes to
     * @return the transformer handler where sax events can be sent to.
     * @throws TransformerConfigurationException error creating the transformer
     */
    public static TransformerHandler writeWithSax(OutputStream out)
            throws TransformerConfigurationException
    {

        SAXTransformerFactory saxFactory = (SAXTransformerFactory) sTransformerFactory;

        //uses identity transformation (in==out)
        Transformer transformer = saxFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setErrorListener(new ErrorListener()
        {

            public void warning(TransformerException exception) throws TransformerException
            {
                exception.printStackTrace();

            }

            public void error(TransformerException exception) throws TransformerException
            {
                exception.printStackTrace();

            }

            public void fatalError(TransformerException exception) throws TransformerException
            {
                exception.printStackTrace();
            }

        });

        StreamResult result = new StreamResult(out);

        TransformerHandler handler = saxFactory.newTransformerHandler();
        handler.setResult(result);

        return handler;
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilder builder = null;
        try
        {
            builder = (DocumentBuilder) sDocBuilderCache.pop();
        }
        catch (EmptyStackException e)
        {
            builder = createDocumentBuilder();
        }

        return builder;
    }

    private static void releaseDocumentBuilder(DocumentBuilder builder)
    {
        if (sDocBuilderCache.size() < MAX_DOC_BUILDER_CACHE)
        {
            sDocBuilderCache.push(builder);
        }
    }

    private static synchronized DocumentBuilder createDocumentBuilder()
            throws ParserConfigurationException
    {
        return sDocBuilderFactory.newDocumentBuilder();
    }

}