//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package net.sf.eclipsecs.ui.stats.data;

import java.util.Collection;

/**
 * Classe qui véhicule les statistiques Checkstyle. Elle contient notamment la
 * liste des différentes erreurs avec leur comptage.
 * 
 * @author Fabrice BELLINGARD
 */
public class Stats {

  /** Liste des différentes erreurs. */
  private Collection<MarkerStat> mMarkerStats;

  /**
   * Nombre de marqueurs scannés.
   */
  private int mMarkerCount;

  /** The number of all markers in the workspace. */
  private int mMarkerCountWhole;

  /**
   * Constructeur.
   * 
   * @param markerStats
   *          la liste des MarkerStats
   * @param markerCount
   *          le nombre de marqueurs scannés
   * @param markerCountWhole
   *          the number of all checkstyle markers in the workspace
   */
  public Stats(Collection<MarkerStat> markerStats, int markerCount, int markerCountWhole) {
    super();
    this.mMarkerStats = markerStats;
    this.mMarkerCount = markerCount;
    this.mMarkerCountWhole = markerCountWhole;
  }

  /**
   * Returns the markerStats.
   * 
   * @return Returns the markerStats.
   */
  public Collection<MarkerStat> getMarkerStats() {
    return mMarkerStats;
  }

  /**
   * Returns the markerCount.
   * 
   * @return Returns the markerCount.
   */
  public int getMarkerCount() {
    return mMarkerCount;
  }

  /**
   * Returns the number of all Checkstyle markers in the workspace.
   * 
   * @return the number of all Checkstyle markers
   */
  public int getMarkerCountAll() {
    return mMarkerCountWhole;
  }
}
