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

//=================================================
// Imports from org namespace
//=================================================

/**
 *  Represents a configuration property who value must be resolved.
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
    
    private String   mVariableName;
    
    private String   mCorrelationTag;
    
    private String   mValue = "";

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * @return
     */
    public String getCorrelationTag()
    {
        return mCorrelationTag;
    }

    /**
     * @return
     */
    public String getValue()
    {
        return mValue;
    }

    /**
     * @return
     */
    public String getVariableName()
    {
        return mVariableName;
    }

    /**
     * @param string
     */
    public void setCorrelationTag(String string)
    {
        mCorrelationTag = string;
    }

    /**
     * @param string
     */
    public void setValue(String string)
    {
        mValue = string;
    }

    /**
     * @param string
     */
    public void setVariableName(String string)
    {
        mVariableName = string;
    }

}
