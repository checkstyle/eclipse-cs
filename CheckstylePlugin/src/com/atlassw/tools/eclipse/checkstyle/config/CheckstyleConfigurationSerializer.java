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

package com.atlassw.tools.eclipse.checkstyle.config;

//=================================================
// Imports from java namespace
//=================================================

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

/**
 * The class is responsible for managining the serializing 
 * and deserializing checkstyle of checksytle configurations.
 * 
 * @author dnehring
 */
public final class CheckstyleConfigurationSerializer
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final String DOCTYPE =
        "<!DOCTYPE module PUBLIC "
            + "\"-//Puppy Crawl//DTD Check Configuration 1.1//EN\" "
            + "\"http://www.puppycrawl.com/dtds/configuration_1_1.dtd\">"
            + System.getProperty("line.separator");

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
    {}

    /**
     * Serialize a check configuration into a standard Checkstyle configuration file format.
     * 
     * @param  config  The configuration to serialize
     * 
     * @return  The XML for the standard Checkstyle configuration file.
     * 
     * @throws CheckstylePluginException  Error during processing.
     */
    public static String serialize(CheckConfiguration config) throws CheckstylePluginException
    {
        String xml = null;
        try
        {
            Document configDoc = XMLUtil.newDocument();
            Element root = configDoc.createElement(XMLTags.MODULE_TAG);
            configDoc.appendChild(root);

            //
            //  Set the name of the root element.
            //
            root.setAttribute(XMLTags.NAME_TAG, config.getName());
            Configuration[] configurations = config.getChildren();
            serializeConfiguration(configurations, configDoc, root);
            xml = XMLUtil.serializeDocument(configDoc, true);
            xml = insertDocType(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String message = "Failed to write check configuration file: " + e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }

        return xml;

    }

    /**
     * Serializes a Configuration[] into xml.
     */
    private static void serializeConfiguration(
        Configuration[] configurations,
        Document configDoc,
        Node node)
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
     * Serializes a Configuration into xml.
     */
    private static Node serializeConfiguration(
        Configuration configuration,
        Document configDoc,
        Node node)
    {
        if (configuration == null)
        {
            return null;
        }
        String configurationName = configuration.getName();
        String[] attributes = configuration.getAttributeNames();
        Element configElement = configDoc.createElement(XMLTags.MODULE_TAG);
        configElement.setAttribute(XMLTags.NAME_TAG, configurationName);

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
                String message = "Error serializing check attributes";
                CheckstyleLog.warning(message, cse);
            }
        }
        node.appendChild(configElement);
        return configElement;
    }

    private static Node toPropertyNode(Document doc, String name, String value)
    {
        Element propertyNode = doc.createElement(XMLTags.PROPERTY_TAG);
        propertyNode.setAttribute(XMLTags.NAME_TAG, name);
        propertyNode.setAttribute(XMLTags.VALUE_TAG, value);

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

        result = xml.substring(0, index + 3) + DOCTYPE + xml.substring(index + 3);

        return result;
    }

}
