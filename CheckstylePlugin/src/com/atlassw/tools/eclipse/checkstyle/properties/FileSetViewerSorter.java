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

package com.atlassw.tools.eclipse.checkstyle.properties;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.FileSet;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;


/**
 *  Sorts CheckConfiguration objects into their display order.
 */
public class FileSetViewerSorter extends ViewerSorter
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
	public FileSetViewerSorter()
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
		
        if ((e1 instanceof FileSet) && (e2 instanceof FileSet))
        {
            FileSet fileSet1 = (FileSet)e1;
            FileSet fileSet2 = (FileSet)e2;
            
            String name1 = fileSet1.getName();
            String name2 = fileSet2.getName();
            
            result = name1.compareTo(name2);
        }
		
		return result;
	}
}

