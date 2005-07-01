//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats.views;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;

/**
 * Provides content based on collection of objects.
 * 
 * @author Fabrice BELLINGARD
 */

class CollectionContentProvider implements IStructuredContentProvider
{

    /**
     * See method below.
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer v, Object oldInput, Object newInput)
    {
    }

    /**
     * See method below.
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object parent)
    {
        if (parent == null)
        {
            return new Object[0];
        }

        if (parent instanceof Collection)
        {
            return ((Collection) parent).toArray();
        }
        else
        {
            // programming error: log and return empty array
            StatsCheckstylePlugin.log(IStatus.WARNING,
                "The input object should be a Collection for this ContentProvider : " //$NON-NLS-1$
                    + this.getClass(), null);
            return new Object[0];
        }
    }

    /**
     * See method below.
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
    }
}