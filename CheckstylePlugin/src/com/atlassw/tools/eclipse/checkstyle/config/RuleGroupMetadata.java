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

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  This class describes a collection of check rules 
 *  that are logicaly grouped together.
 */
public class RuleGroupMetadata implements Cloneable
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
    
    private String    mGroupName;
    
    private List      mRuleMetadata = new LinkedList();

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
	RuleGroupMetadata(Node groupNode)
	{
		//
		//  Get the name attribute off of the group node.
		//
		mGroupName = XMLUtil.getNodeAttributeValue(groupNode, XMLTags.NAME_TAG);

		//
		//  Find all the rules and load them.
		//
		NodeList children = groupNode.getChildNodes();
		int count = children.getLength();
		for (int i = 0; i < count; i++)
		{
			Node node = children.item(i);
			if (node.getNodeName().equals(XMLTags.RULE_METADATA_TAG))
			{
                RuleMetadata rule = null;
                try
                {
				    rule = new RuleMetadata(node);
                }
                catch (CheckstylePluginException e)
                {
                    CheckstyleLog.warning("Failed to get rule metadata, " + e.getMessage(), e);
                }
                if (rule != null)
                {
                    mRuleMetadata.add(rule);
                }
			}
		}
	}
	
	RuleGroupMetadata(String groupName)
	{
		mGroupName = groupName;
	}

	//=================================================
	// Methods.
	//=================================================
    
	/**
	 * Returns the group's name.
     * 
	 * @return Group name
	 */
	public final String getGroupName()
	{
		return mGroupName;
	}

    /**
     * Returns a list of the group's rule metadata.
     * 
     * @return  List of <code>RuleMetadata</code> objects.
     */
    public final List getRuleMetadata()
    {
        return mRuleMetadata;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
