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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;

/**
 * Provider de libellé pour la table qui affiche les statistiques Checkstyle.
 * 
 * @author Fabrice BELLINGARD
 */

class MarkerStatsViewLabelProvider extends LabelProvider implements
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
        MarkerStat stat = (MarkerStat) obj;
        String text = null;

        switch (index)
        {
            case 0:
                text = stat.getIdentifiant();
                break;
            case 1:
                text = stat.getCount() + ""; //$NON-NLS-1$
                break;

            default:
                text = ""; //$NON-NLS-1$
                break;
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