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

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  A simple configuration consisting of a name/value pair.
 */
public class ConfigProperty implements XMLTags, Comparable
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================

	//=================================================
	// Instance member variables.
	//=================================================
    
    private String mName;
    
    private String mValue;

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    /**
     *  Constructor.
     */
    public ConfigProperty()
    {
    }
    
    /**
     *  Constructor.
     * 
     *  @param name  Property name.
     * 
     *  @param value  Property value.
     */
    public ConfigProperty(String name, String value)
    {
        setName(name);
        setValue(value);
    }
    
    /**
     *  Construct from a config file DOM node.
     * 
     *  @param node  The DOM node containing this property.
     * 
     *  @throws CheckstyleException  Error during processing.
     */
    ConfigProperty(Node node) throws CheckstylePluginException
    {        
        String name  = XMLUtil.getNodeAttributeValue(node, NAME_TAG);
        if (name == null)
        {
            String message = "ConfigProperty missing name attribute";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        else
        {
            setName(name.trim());
        }
        
        String value = XMLUtil.getNodeAttributeValue(node, VALUE_TAG);
        if (value == null)
        {
            String message = "ConfigProperty missing value attribute";
            CheckstyleLog.warning(message);
            throw new CheckstylePluginException(message);
        }
        else
        {
            mValue = value.trim();
        }
    }

	//=================================================
	// Methods.
	//=================================================
    
    /**
     *  Get the property's name.
     * 
     *  @return The name
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     *  Set the property's name.
     * 
     *  @param  name  The new name.
     */
    public void setName(String name)
    {
        mName = name;
    }
    
	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue()
	{
		return mValue;
	}

	/**
	 * Sets the value.
	 * @param value The value to set
	 */
	public void setValue(String value)
	{
		mValue = value;
	}

    String getConfigItemTypeTag()
    {
        return CONFIG_PROPERTY_TAG;
    }
    
    Node toDOMNode(Document doc)
    {
        Element cfgPropertyNode = doc.createElement(CONFIG_PROPERTY_TAG);
        cfgPropertyNode.setAttribute(NAME_TAG,  getName());
        cfgPropertyNode.setAttribute(VALUE_TAG, mValue);
        
        return cfgPropertyNode;
    }
    
    Node toCSDOMNode(Document doc)
    {
        Element cfgPropertyNode = doc.createElement(PROPERTY_TAG);
        cfgPropertyNode.setAttribute(NAME_TAG,  getName());
        cfgPropertyNode.setAttribute(VALUE_TAG, mValue);
        
        return cfgPropertyNode;
    }
    
    public int compareTo(Object obj)
    {
        return this.mName.compareTo(((ConfigProperty)obj).mName);
    }
}
