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
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;


/**
 *  Sorts CheckConfiguration objects into their display order.
 */
public class CheckConfigurationViewerSorter extends ViewerSorter
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
	public CheckConfigurationViewerSorter()
	{
		super();
	}

    //=================================================
    // Methods.
    //=================================================
	
	/**
	 * @see ViewerSorter#compare
	 */
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int result = 0;
		
        if ((e1 instanceof CheckConfiguration) && (e2 instanceof CheckConfiguration))
        {
            CheckConfiguration cfg1 = (CheckConfiguration)e1;
            CheckConfiguration cfg2 = (CheckConfiguration)e2;
            
            String string1 = cfg1.getConfigName();
            String string2 = cfg2.getConfigName();
            
            result = string1.compareToIgnoreCase(string2);
        }
		
		return result;
	}
}

