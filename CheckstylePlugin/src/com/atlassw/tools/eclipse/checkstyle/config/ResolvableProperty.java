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

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Represents a configuration property who's value must be resolved.
 */
public class ResolvableProperty
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
    
    private String   mPropertyName;
    
    private String   mCorrelationTag;
    
    private String   mValue;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return  The correlation tag for the property.
     */
    public String getCorrelationTag()
    {
        return mCorrelationTag;
    }

    /**
     * @return  The value of the property.
     */
    public String getValue()
    {
        return mValue;
    }

    /**
     * @return  The property's name.
     */
    public String getPropertyName()
    {
        return mPropertyName;
    }

    /**
     * @param string Correlation tag value.
     */
    public void setCorrelationTag(String string)
    {
        mCorrelationTag = string;
    }

    /**
     * @param string  Value for the property.
     */
    public void setValue(String string)
    {
        mValue = string;
    }

    /**
     * @param string  The property's name.
     */
    public void setPropertyName(String string)
    {
        mPropertyName = string;
    }

}
