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

package com.atlassw.tools.eclipse.checkstyle.config;

//=================================================
// Imports from java namespace
//=================================================
import java.util.LinkedList;
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

import com.puppycrawl.tools.checkstyle.api.Configuration; 

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * The class is responsible for managining the serializing 
 * and deserializing checkstyle of checksytle configurations
 * 
 * @author dnehring
 */
public class CheckstyleConfigurationSerializer implements XMLTags
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================
    
    private static final String DOCTYPE =
        "<!DOCTYPE module PUBLIC " +
        "\"-//Puppy Crawl//DTD Check Configuration 1.1//EN\" " +
        "\"http://www.puppycrawl.com/dtds/configuration_1_1.dtd\">" +
        System.getProperty("line.separator");
        
	//=================================================
	// Instance member variables.
	//=================================================

	//=================================================
	// Constructors & finalizer.
	//=================================================

	//=================================================
	// Methods.
	//=================================================

	private CheckstyleConfigurationSerializer()
	{
	}
	
	public static String serialize(CheckConfiguration config)
        throws CheckstylePluginException
	{
		String xml = null;
		try
        {
        	Document configDoc = XMLUtil.newDocument();
            Element root = configDoc.createElement(MODULE_TAG);
            configDoc.appendChild(root);
            
            //
            //  Set the name of the root element.
            //
            root.setAttribute(NAME_TAG, config.getName());
            Configuration[] configurations = config.getChildren();
            serializeConfiguration(configurations, configDoc, root);
            xml = XMLUtil.serializeDocument(configDoc, true);
            xml = insertDocType(xml);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            String message = "Failed to write check configuration file: " +
                             e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }
        
        return xml;
		
	}
	
	public static List deserialize(Document configDoc) throws CheckstylePluginException
	{
		List checkConfigs = new LinkedList();

		if (configDoc == null)
		{
			String message = "Failed to read and parse check configurations";
			CheckstyleLog.warning(message);
			throw new CheckstylePluginException(message);
		}

		Node rootNode = configDoc.getDocumentElement();
		String checkConfigName = "";
		checkConfigName = XMLUtil.getNodeAttributeValue(rootNode, CHECK_CONFIG_NAME_TAG);

		NodeList children = rootNode.getChildNodes();
		int count = children.getLength();
		for (int i = 0; i < count; i++)
		{
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				if (node.getNodeName().equals(MODULE_TAG))
				{

					Node treewalkerNode = children.item(i);
					if (treewalkerNode.getNodeType() == Node.ELEMENT_NODE)
					{

						CheckConfiguration config =
							new CheckConfiguration(treewalkerNode, checkConfigName, true);
						if (config == null)
						{
							CheckstyleLog.warning("Failed to create CheckConfiguration, ignoring");
						}
						else
						{
							checkConfigs.add(config);
						}
					}
				}
			}
		}

		return checkConfigs;
	}
	
	/**
	 * Serializes a Configuration[] into xml
	 * 
	 */
	private static void serializeConfiguration(Configuration[] configurations, Document configDoc, Node node)
	{
		
		if (configurations == null || configurations.length < 1)
		{
			return;
		}
		for (int i = 0; i < configurations.length; i++)
		{
			Configuration configuration = configurations[i];
			Node childNode = serializeConfiguration(configuration, configDoc, node);
			Configuration[] childConfiguration = configuration.getChildren();
			serializeConfiguration(childConfiguration, configDoc, childNode);
		}
	}
	
	/**
	 * Serializes a Configuration into xml
	 */
	private static Node serializeConfiguration(Configuration configuration,
                                                 Document configDoc, Node node)
	{
		
		if (configuration == null)
		{
			return null;
		}
		String configurationName = configuration.getName();
		String [] attributes = configuration.getAttributeNames();
		Element configElement = configDoc.createElement(MODULE_TAG);
		configElement.setAttribute(NAME_TAG, configurationName);
		
		for (int i = 0; i < attributes.length; i++)
		{
			try
			{
				String name = attributes[i];
				String value = configuration.getAttribute(name);
				Node propertyNode = toPropertyNode(configDoc, name, value);
				configElement.appendChild(propertyNode);
				
			}
			catch (com.puppycrawl.tools.checkstyle.api.CheckstyleException cse)
			{
				//  TODO
			}
		}
		node.appendChild(configElement);
		return configElement; 
		
	}
	
	private static Node toPropertyNode(Document doc, String name, String value)
    {
        Element propertyNode = doc.createElement(PROPERTY_TAG);
        propertyNode.setAttribute(NAME_TAG,  name);
        propertyNode.setAttribute(VALUE_TAG, value);
        
        return propertyNode;
    }
	
    /**
     *  Insert the doctype into the XML string.  This has to be done this way
     *  since the DOM2 API does not provide a way to specify it.
     */
    private static String insertDocType(String xml)
    {
        String result = null;
        
        //
        //  Find the close of the XML opening line.
        //
        int index = xml.indexOf("?>");
        
        result = xml.substring(0, index+3) + DOCTYPE + xml.substring(index+3);
        
        return result;
    }
}
