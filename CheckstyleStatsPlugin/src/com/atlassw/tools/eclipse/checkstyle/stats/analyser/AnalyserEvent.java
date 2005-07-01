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
package com.atlassw.tools.eclipse.checkstyle.stats.analyser;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.atlassw.tools.eclipse.checkstyle.stats.data.Stats;

/**
 * Evènement associé aux mises à jour des stats Checkstyle.
 * 
 * @author Fabrice BELLINGARD
 */
public class AnalyserEvent
{
    // TODO : mettre aussi le WorkbenchPage où la sélection a été effectuée
    /**
     * Ressources sur lesquelles les stats ont été effectuées.
     */
    private IStructuredSelection mSelection;

    /**
     * Les stats qui ont été modifiées.
     */
    private Stats mStats;

    /**
     * Constructeur.
     */
    public AnalyserEvent()
    {
        super();
    }

    /**
     * Constructeur.
     * 
     * @param stats :
     *            les stats
     * @param selection :
     *            Ressources sur lesquelles les stats ont été effectuées
     */
    public AnalyserEvent(Stats stats, IStructuredSelection selection)
    {
        super();
        this.mStats = stats;
        this.mSelection = selection;
    }

    /**
     * Returns the markerStatsCollection.
     * 
     * @return Returns the markerStatsCollection.
     */
    public Stats getStats()
    {
        return mStats;
    }

    /**
     * Returns the selection.
     * 
     * @return Returns the selection.
     */
    public IStructuredSelection getSelection()
    {
        return mSelection;
    }
}
