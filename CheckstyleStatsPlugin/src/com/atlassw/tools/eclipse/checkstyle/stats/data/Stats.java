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
package com.atlassw.tools.eclipse.checkstyle.stats.data;

import java.util.Collection;

/**
 * 
 * Classe qui véhicule les statistiques Checkstyle.
 * 
 * Elle contient notamment la liste des différentes erreurs avec leur comptage.
 * 
 * @author Fabrice BELLINGARD
 */
public class Stats
{
    /**
     * Liste des différentes erreurs.
     */
    private Collection mMarkerStats;

    /**
     * Nombre de marqueurs scannés.
     */
    private int mMarkerCount;

    /**
     * Constructeur.
     * 
     * @param markerStats
     *            la liste des MarkerStats
     * @param markerCount
     *            le nombre de marqueurs scannés
     */
    public Stats(Collection markerStats, int markerCount)
    {
        super();
        this.mMarkerStats = markerStats;
        this.mMarkerCount = markerCount;
    }

    /**
     * Returns the markerStats.
     * 
     * @return Returns the markerStats.
     */
    public Collection getMarkerStats()
    {
        return mMarkerStats;
    }

    /**
     * Returns the markerCount.
     * 
     * @return Returns the markerCount.
     */
    public int getMarkerCount()
    {
        return mMarkerCount;
    }

    /**
     * The markerCount to set.
     * 
     * @param markerCount
     *            The markerCount to set.
     */
    public void setMarkerCount(int markerCount)
    {
        this.mMarkerCount = markerCount;
    }
}
