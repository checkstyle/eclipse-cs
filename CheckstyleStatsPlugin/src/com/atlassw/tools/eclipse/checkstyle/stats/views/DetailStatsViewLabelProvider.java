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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;

/**
 * Label provider for the view that displays the list of the markers for a
 * specific error category.
 * 
 * @author Fabrice BELLINGARD
 */

class DetailStatsViewLabelProvider extends LabelProvider implements
    ITableLabelProvider
{
    /**
     * See method below.
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object obj, int index)
    {
        IMarker marker = (IMarker) obj;
        String text = null;

        try
        {
            switch (index)
            {
                case 0:
                    text = marker.getResource().getFullPath().toString();
                    break;
                case 1:
                    text = marker.getAttribute(IMarker.LINE_NUMBER).toString();
                    break;
                case 2:
                    text = marker.getAttribute(IMarker.MESSAGE).toString();
                    break;

                default:
                    text = ""; //$NON-NLS-1$
                    break;
            }
        }
        catch (CoreException e)
        {
            // Can't do anything: let's put a default value
            text = Messages.DetailStatsViewLabelProvider_unknown;
        }

        return text;
    }

    /**
     * See method below.
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object obj, int index)
    {
        return null;
    }
}