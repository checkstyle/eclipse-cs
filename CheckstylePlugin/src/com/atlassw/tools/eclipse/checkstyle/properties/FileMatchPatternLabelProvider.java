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

package com.atlassw.tools.eclipse.checkstyle.properties;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileMatchPattern;

/**
 * Provides the labels for the FileSet list display.
 */
class FileMatchPatternLabelProvider extends LabelProvider implements ITableLabelProvider
{

    /**
     * @see ITableLabelProvider#getColumnText(Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        String result = element.toString();
        if (element instanceof FileMatchPattern)
        {
            FileMatchPattern pattern = (FileMatchPattern) element;
            switch (columnIndex)
            {
                case 0:
                    result = new String();
                    break;

                case 1:
                    result = pattern.getMatchPattern();
                    break;

                default:
                    break;
            }
        }
        return result;
    }

    /**
     * @see ITableLabelProvider#getColumnImage(Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        return null;
    }
}