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
import org.w3c.dom.Node;


/**
 *  This class represents metadata about one of a rule's properties.
 */
public class ConfigPropertyMetadata implements Cloneable
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
    
    private ConfigPropertyType                 mDatatype = null;
    
    private String                             mName = "";
    
    private String                             mDefaultValue = "";
    
    private String                             mDescription = "";
    
    private boolean                            mHidden = false;
    
    private ConfigPropertyEnumerationMetadata  mEnumeration = null;

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    /**
     *  Constructor.
     * 
     *  @param propNode  DOM node containing the property's data
     * 
     *  @throws CheckstyleException  Error during processing.
     */
    ConfigPropertyMetadata(Node propNode) throws CheckstylePluginException
    {
        //
        //  Get the name attribute.
        //
        String temp = XMLUtil.getNodeAttributeValue(propNode, XMLTags.NAME_TAG);
        if (temp != null)
        {
            mName = temp.trim();
        }
        
        //
        //  Get the default value attribute.
        //
        temp = XMLUtil.getNodeAttributeValue(propNode, XMLTags.DEFAULT_VALUE_TAG);
        if (temp != null)
        {
            mDefaultValue = temp.trim();
        }
        
        //
        //  Get the data type.
        //
        temp = XMLUtil.getNodeAttributeValue(propNode, XMLTags.DATATYPE_TAG);
        if (temp != null)
        {
            mDatatype = ConfigPropertyType.getConfigPropertyType(temp.trim());
        }
        
        //
        //  Get the description node.
        //
        Node descNode = XMLUtil.getChildNode(propNode, XMLTags.DESCRIPTION_TAG);
        if (descNode != null)
        {
            temp = XMLUtil.getNodeTextValue(descNode);
            if (temp != null)
            {
                mDescription = temp.trim();
            }
        }
        
		//
		//  Look for an enumeration node.
		//
        Node enumNode = XMLUtil.getChildNode(propNode, XMLTags.ENUMERATION_TAG);
        if (enumNode != null)
		{
			ConfigPropertyEnumerationMetadata enumMeta = null;
			try
			{
				enumMeta = new ConfigPropertyEnumerationMetadata(enumNode);
			}
			catch (CheckstylePluginException e)
			{
				CheckstyleLog.warning(
					"Failed to get configuration property metadata, " + e.getMessage(),
					e);
			}
			if (enumMeta != null)
			{
				mEnumeration = enumMeta;
			}
		}
    }
    
    ConfigPropertyMetadata(ConfigPropertyType type, String name, String defaultValue)
    {
    	mDatatype = type;
    	mName = name;
    	mDefaultValue = defaultValue;
    	mDescription = "No Description Available";
    }

	//=================================================
	// Methods.
	//=================================================
    
    /**
     *  Get the property's datatype
     * 
     *  @return  The datatype
     */
    public ConfigPropertyType getDatatype()
    {
        return mDatatype;
    }
    
    /**
     * Get the property's name
     * 
     * @return  The name
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     * Get the property's description.
     * 
     * @return  The description
     */
    public String getDescription()
    {
        return mDescription;
    }
    
    /**
     * Get the default value
     * 
     * @return  The default value
     */
    public String getDefaultValue()
    {
        return mDefaultValue;
    }
    
    /**
     * Get the enumeration of allowable values.
     * 
     * @return  Enumeration of values
     */
    public ConfigPropertyEnumerationMetadata getPropertyEnumeration()
    {
        return mEnumeration;
    }
    
    /**
     * Clone the object.
     * 
     * @return  The cloned object.
     * 
     * @throws  CloneNotSupportedException  The object can not be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    
	/**
	 * Returns the hidden.
	 * @return boolean
	 */
	public boolean isHidden()
	{
		return mHidden;
	}

	/**
	 * Sets the hidden.
	 * @param hidden The hidden to set
	 */
	public void setHidden(boolean hidden)
	{
		mHidden = hidden;
	}

}
