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
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

//=================================================
// Imports from org namespace
//=================================================
import org.w3c.dom.Node;


/**
 *  This class represents a possible value of a rule property
 *  when the set of legal values is a limited set defined by 
 *  an enumeration.
 */
public class ConfigPropertyValueMetadata implements Cloneable
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
    
    private String mValue = "";

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    ConfigPropertyValueMetadata(Node valueNode) throws CheckstylePluginException
    {
        //
        //  Get the value attribute.
        //
        String temp = XMLUtil.getNodeAttributeValue(valueNode, XMLTags.VALUE_TAG);
        if (temp != null)
        {
            mValue = temp.trim();
        }
    }

	//=================================================
	// Methods.
	//=================================================

	/**
	 * Returns the value.
	 * @return String
	 */
    public String getValue()
    {
        return mValue;
    }
    
    /**
     *  Clone the object.
     *  
     *  @return  The cloned object
     * 
     *  @throws  CloneNotSupportedException  The object can not be cloned.
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
