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

package com.atlassw.tools.eclipse.checkstyle.preferences;

//=================================================
// Imports from java namespace
//=================================================
import java.util.List;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/** 
 *  Provides the content for the rule configuration list display.
 */
public class RuleConfigurationProvider implements IStructuredContentProvider
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

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * Default constructor.
     */
    public RuleConfigurationProvider()
    {}

    //=================================================
    // Methods.
    //=================================================

    /**
     * @see IStructuredContentProvider#getElements
     */
    public Object[] getElements(Object input)
    {
        Object[] result = null;
        if (input instanceof List)
        {
            List ruleConfigs = (List)input;
            result = ruleConfigs.toArray();
        }

        return result;
    }

    /**
     * @see IStructuredContentProvider#inputChanged
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {}

    /**
     * @see IStructuredContentProvider#dispose
     */
    public void dispose()
    {}

}