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
import java.util.List;
import java.util.LinkedList;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  This class contains the metadata that describes a check rule.
 */
public class RuleMetadata implements Cloneable, XMLTags
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
    
    private String          mCheckImplClassname;
    
    private String          mName;
    
    private String          mDescription;
    
    private SeverityLevel   mDefaultSeverityLevel = SeverityLevel.WARNING;
    
    private List            mConfigPropMetadata = new LinkedList();
    
    private int             mGroupIndex = -1;

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    RuleMetadata(Node ruleNode) throws CheckstylePluginException
    {
        //
        //  Get the name attribute.
        //
        String temp = XMLUtil.getNodeAttributeValue(ruleNode, NAME_TAG);
        if (temp != null)
        {
            mName = temp.trim();
        }

        //
        //  Get the check implementation classname attribute.
        //
        temp = XMLUtil.getNodeAttributeValue(ruleNode, CLASSNAME_TAG);
        if (temp != null)
        {
            mCheckImplClassname = temp.trim();
        }

        //
        //  Get the default severity attribute.
        //
        temp = XMLUtil.getNodeAttributeValue(ruleNode, DEFAULT_SEVERITY_TAG);
        if (temp != null)
        {
            mDefaultSeverityLevel = SeverityLevel.getInstance(temp.trim());
        }
        
        //
        //  Get the description node.
        //
		Node descNode = XMLUtil.getChildNode(ruleNode, DESCRIPTION_TAG);
		if (descNode != null)
		{
			temp = XMLUtil.getNodeTextValue(descNode);
			if (temp != null)
			{
				mDescription = temp.trim();
			}
		}

        //
        //  Find all the properties and load them.
        //
        NodeList children = ruleNode.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++)
        {
            Node node = children.item(i);
            if (node.getNodeName().equals(PROPERTY_METADATA_TAG))
            {
                ConfigPropertyMetadata propMeta = null;
                try
                {
                    propMeta = new ConfigPropertyMetadata(node);
                }
                catch (CheckstylePluginException e)
                {
                    CheckstyleLog.warning("Failed to get configuration property metadata, " 
                                          + e.getMessage(), e);
                }
                if (propMeta != null)
                {
                    mConfigPropMetadata.add(propMeta);
                }
            }
        }
    }

	//=================================================
	// Methods.
	//=================================================
    
	/**
	 * Returns the default severity level.
     * 
	 * @return  The severity level.
	 */
	public SeverityLevel getDefaultSeverityLevel()
	{
		return mDefaultSeverityLevel;
	}

	/**
	 * Returns the rule's check implementation class name.
     * 
	 * @return Implementation class name.
	 */
	public String getCheckImplClassname()
	{
		return mCheckImplClassname;
	}

	/**
	 * Returns the rule's description.
     * 
	 * @return Rule description
	 */
	public String getDescription()
	{
		return mDescription;
	}

    /**
     * Returns the rule's name
     * @return String
     */
    public String getRuleName()
    {
        return mName;
    }

	/**
	 * Returns the configuration property metadata.
     * 
	 * @return A list of <code>ConfigPropertyMetadata</code> objects.
	 */
	public List getConfigItemMetadata()
	{
		return mConfigPropMetadata;
	}
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

	/**
	 * Returns the groupIndex.
	 * @return int
	 */
	public int getGroupIndex()
	{
		return mGroupIndex;
	}

	/**
	 * Sets the groupIndex.
	 * @param groupIndex The groupIndex to set
	 */
	public void setGroupIndex(int groupIndex)
	{
		mGroupIndex = groupIndex;
	}

}
