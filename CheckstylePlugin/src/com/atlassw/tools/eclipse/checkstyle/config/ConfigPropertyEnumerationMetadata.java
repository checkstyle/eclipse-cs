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

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *  This represents an enumeration of possible legal values for a
 *  rule's configuration property.
 */
public class ConfigPropertyEnumerationMetadata implements Cloneable, XMLTags
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
    
    private List  mValueMetadata = new LinkedList();

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    /**
     *  Constructor.
     * 
     *  @param enumNode  DOM node containing the enumeration's data
     * 
     *  @throws CheckstyleException  Error during processing.
     */
	ConfigPropertyEnumerationMetadata(Node enumNode) throws CheckstylePluginException
	{
		NodeList children = enumNode.getChildNodes();
		int count = children.getLength();
		for (int i = 0; i < count; i++)
		{
			Node node = children.item(i);
			if (node.getNodeName().equals(PROPERTY_VALUE_OPTIONS_TAG))
			{
				ConfigPropertyValueMetadata valueMeta = null;
				try
				{
					valueMeta = new ConfigPropertyValueMetadata(node);
				}
				catch (CheckstylePluginException e)
				{
					CheckstyleLog.warning(
						"Failed to get configuration property metadata, " + e.getMessage(),
						e);
				}
				if (valueMeta != null)
				{
					mValueMetadata.add(valueMeta);
				}
			}
		}
	}

	//=================================================
	// Methods.
	//=================================================
    
    /**
     *  Get a list of the possible values for a property.
     * 
     *  @return  list of <code>ConfigPropertyValueMetadata</code>
     *            objects.
     */
    public List getValueMetadata()
    {
        return mValueMetadata;
    }
    
    /**
     *  Clone a copy of the object.
     * 
     *  @return A copy of the object.
     * 
     *  @throws CloneNotSupportedException  Object can not be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
