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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Objet qui donne des statistiques sur les marqueurs.
 * 
 * @author Fabrice BELLINGARD
 */
public class MarkerStat implements Comparable<MarkerStat> {

  /**
   * Identifiant du marqueur : dans notre cas, il s'agit du message du marqueur
   * Checkstyle.
   */
  private String mIdentifiant;

  /**
   * List of the markers of this categories.
   */
  private Collection<IMarker> mMarkers;

  /**
   * The maximum severity for this marker group.
   */
  private int mMaxSeverity;

  /**
   * Crée un MarkerStat pour un marqueur Checkstyle correspondant à
   * l'identifiant passé en paramètre.
   * 
   * @param identifiant
   *          : le message du marqueur Checkstyle
   */
  public MarkerStat(String identifiant) {
    super();
    this.mIdentifiant = identifiant;
    mMarkers = new ArrayList<>();
  }

  /**
   * Reference the marker as one fo this category.
   * 
   * @param marker
   *          : the marker to add to this category
   */
  public void addMarker(IMarker marker) {
    mMarkers.add(marker);

    int severity = MarkerUtilities.getSeverity(marker);
    if (severity > mMaxSeverity) {
      mMaxSeverity = severity;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(MarkerStat stat) {
    return mIdentifiant.compareTo(stat.getIdentifiant());
  }

  /**
   * Returns the list of markers for this category.
   * 
   * @return a collection of IMarker
   */
  public Collection<IMarker> getMarkers() {
    return mMarkers;
  }

  /**
   * Retourne le nombre d 'occurence.
   * 
   * @return Returns the count.
   */
  public int getCount() {
    return mMarkers.size();
  }

  /**
   * Returns the maximum severity level occurring in this group.
   * 
   * @return the maximum severity level
   */
  public int getMaxSeverity() {
    return mMaxSeverity;
  }

  /**
   * Retourne l'identifiant (i.e. le message Checkstyle) de ce MarkerStat.
   * 
   * @return Returns the identifiant.
   */
  public String getIdentifiant() {
    return mIdentifiant;
  }
}
