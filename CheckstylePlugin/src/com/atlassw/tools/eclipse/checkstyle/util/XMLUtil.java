//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
import java.io.StringWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Stack;
import java.util.EmptyStackException;

//=================================================
// Imports from javax namespace
//=================================================
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.DOMException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


/**
 *  Provides utility methods for XML manipulations.
 */
public class XMLUtil
{
	//=================================================
	// Public static final variables.
	//=================================================
	
	
	//=================================================
	// Static class variables.
	//=================================================
    
    private static Stack sDocBuilderCache = new Stack();
    
    private static DocumentBuilderFactory sDocBuilderFactory 
        = DocumentBuilderFactory.newInstance();
    
    private static final int MAX_DOC_BUILDER_CACHE = 10;
	
	//=================================================
	// Instance member variables.
	//=================================================
	
	
	//=================================================
	// Constructors & finalizer.
	//=================================================
	
	/**
	 *  Private constructor to prevent instances.
	 */
	private XMLUtil()
	{}
	
    //=================================================
    // Methods.
    //=================================================
	
	/**
	 *  Get a named child node.  If there is more then one child node
	 *  with the given name the child returned is undefined.
	 * 
	 *  @param  parent  The parent node.
	 * 
	 *  @param  childName  The node name of the child node.
	 * 
	 *  @return  The requested child node or <code>null</code> if no
	 *           child with the requested name is found.
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
	 *  Get the value of the text node that is passed in.
	 *
	 *  @param  node   The node to work on
	 * 
	 *  @return Text   value of the tag element
	 */
	public static String getNodeTextValue(Node node) 
	{  
		String nodeValue = null;
  
		if ( node != null )
		{
			NodeList childNodes = node.getChildNodes();
  
			if ( childNodes.getLength() > 0 )        
			{            
				Node childNode = childNodes.item(0); 
                   
				if ( childNode.getNodeType() == Node.TEXT_NODE ||
                        childNode.getNodeType() == Node.CDATA_SECTION_NODE)     
				{            
					nodeValue  = childNode.getNodeValue();     
				}   
			}            
		}
		return nodeValue;
	}
    
    /**
     *  Get the value of a node attribute.
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
            result = ((Attr)attr).getValue();
        }
        else
        {
            result = getNodeTextValue(attr);
        }
        
        return result;
    }
	
	/**
	 *  Add a new Element and its value to an input Document
	 *
	 *  @param  document   Document to add to
	 * 
	 *  @param  parent  Parent element to add new element to
	 * 
	 *  @param  tagName  Element tag name to add
	 * 
	 *  @param  value   Value of new Element
	 * 
	 *  @return Element   Newly added Element
	 */	
	public static Element addElementAndValue(Document document, 
	                                         Element parent, 
									         String tagName, 
									         String value)
	{
		Element element 
			= document.createElement(tagName);
		parent.appendChild( element );
		Text text = document.createTextNode( value );
		element.appendChild( text );

		return element;
	}
  
	/**
	 *  Add a new Element to an input Document
	 *
	 *  @param  document   Document to add to
	 * 
	 *  @param  parent  Parent element to add new element to
	 * 
	 *  @param  tagName  Element tag name to add
	 * 
	 *  @return Element   Newly added Element
	 */	
	public static Element addElement(Document document, 
							         Element parent, 
							         String tagName)
	{
		Element element 
			= document.createElement(tagName);
		parent.appendChild( element );

		return element;
	}
	
	/**
	 *  Create a new Document
	 * 
	 *  @throws DocumentCreationException An error
	 *                                    occurred
	 *                                    during
	 *                                    creation 
	 * 
	 *  @return Document   Newly created Document
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
			throw new CheckstylePluginException("ParserConfigurationException" 
									            + ex.getMessage());
		}
		catch (DOMException ex)       
		{
            throw new CheckstylePluginException("Caught DOMException " 
			                                    + ex.getMessage());
		}

		return document;
	}
	
	/**
	 * Converts the specified String into an XML Document.  If the String
	 * can't be parsed, then null is returned.
	 * @param	xmlString	The String to parse into an XML Document
	 * @return	Document	The parsed Docuement
	 */
	public static Document newDocument(String xmlString)
	{        
		//
        //  Parse the document.
        //        
        Document document = null;
        try
		{
			ByteArrayInputStream bs 
                = new ByteArrayInputStream(xmlString.getBytes());
			DocumentBuilder docBuilder = getDocumentBuilder();
			document = docBuilder.parse(bs);
            releaseDocumentBuilder(docBuilder);
		}
		catch (ParserConfigurationException ex)
		{
			//you get nothing-
			document = null;
		}
		catch (DOMException ex)       
		{
			//you get nothing-
			document = null;
		}
		catch (Exception e)
		{
			//humm?
			document = null;
		}
		
		
        return document;
	}
    
    /**
     *  Create a document from the contents of a stream.
     */
	public static Document newDocument(InputStream inStream)
	{
		Document result = null;
		try
		{
			InputStreamReader reader = new InputStreamReader(inStream);
			StringBuffer buffer = new StringBuffer();
			boolean eofReached = false;
			char[] input = new char[inStream.available()];
			while (!eofReached)
			{
				int status = reader.read(input, 0, input.length);
				if (status < 0)
				{
					eofReached = true;
				}
				else
				{
					buffer.append(input, 0, status);
				}
			}

			String xmlString = buffer.toString();
			result = newDocument(xmlString);
		}
		catch (Exception e)
		{}

		return result;
	}
	
	
	/**
	 *  Serialize Document into String Rep
	 * 
	 * @param doc - Document to be serialized
	 * 
	 * @param indent - boolean indicating whether or not to indent
	 *                 tags
	 * 
	 * @throws DocumentSerializationException - An error occurred
	 *                                          during serialization
	 * 
	 * @return String  - Serialized string representation of doc
	 */		
	public static String serializeDocument(Document doc, boolean indent)
	                throws CheckstylePluginException
	{
		String result;
		try
		{
			final Document document = doc;
			final StringWriter writer = new StringWriter();

			OutputFormat outputFormat = new OutputFormat(document);
			outputFormat.setIndenting(indent);
			final XMLSerializer serializer = 
				new XMLSerializer(writer, outputFormat);

			serializer.serialize(document);
			result = writer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String errMsg = "Exception occurred while serializing document: "
			                + e.getMessage();
			throw new CheckstylePluginException(errMsg);
		}
		return result;
	}

	/**
	 *  Serialize Document into String Rep, delegates to method
	 *  with two arguments (indent set to false)
	 * 
	 * @param doc - Document to be serialized
	 * 
	 * @throws DocumentSerializationException - An error occurred
	 *                                          during serialization
	 * 
	 * @return String  - Serialized string representation of doc
	 */		
    public static String serializeDocument(Document doc)
              throws CheckstylePluginException
    {
	    return serializeDocument(doc, false);
	}
    
    private static DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        DocumentBuilder builder = null;
        try
        {
            builder = (DocumentBuilder)sDocBuilderCache.pop();
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




