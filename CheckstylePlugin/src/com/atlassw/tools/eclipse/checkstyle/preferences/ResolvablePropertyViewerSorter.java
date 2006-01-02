//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;

/**
 * Sorts AuditConfiguration objects into their display order.
 */
public class ResolvablePropertyViewerSorter extends ViewerSorter
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
    public ResolvablePropertyViewerSorter()
    {
        super();
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     * {@inheritDoc}
     */
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        int result = 0;

        if ((e1 instanceof ResolvableProperty) && (e2 instanceof ResolvableProperty))
        {
            String string1 = ((ResolvableProperty) e1).getPropertyName();
            String string2 = ((ResolvableProperty) e2).getPropertyName();

            result = string1.compareToIgnoreCase(string2);
        }

        return result;
    }
}