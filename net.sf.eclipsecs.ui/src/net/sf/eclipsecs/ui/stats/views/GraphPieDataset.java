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

package net.sf.eclipsecs.ui.stats.views;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;

import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;

/**
 * Implémentation d'un PieDataset pour fournir les données à afficher au graph. Copié de
 * DefaultPieDataset car celui-ci ne permettait pas d'être vidé de ses données dynamiquement. Le
 * code ajouté est balisé en début de classe.
 *
 * @see org.jfree.data.general.DefaultPieDataset
 * @author Fabrice BELLINGARD
 */
public class GraphPieDataset extends AbstractDataset implements PieDataset {

  // --------------- RAJOUT : début ---------------

  /** Serialization support across different class versions. */
  private static final long serialVersionUID = 9010212577897074893L;

  /**
   * Pourcentage minimum en dessous duquel une catégorie d'erreur est mise dans la catégorie
   * "Autres" opur l'affichage du camember.
   */
  private static final int POURCENTAGE_MIN = 1;

  /**
   * Pourcentage pour le calcul.
   */
  private static final int CENT = 100;

  /**
   * Afficher ou pas toutes les catégories.
   */
  private boolean mShowAllCategories;

  /**
   * Remplit le Dataset avec les valeurs de la collection de MarkerStat.
   *
   * @param stats
   *          the Checkstyle violation stats
   */
  public void setStats(Stats stats) {

    Collection<MarkerStat> markerStatCollection = stats != null ? stats.getMarkerStats()
            : Collections.emptyList();
    mData = new DefaultKeyedValues();

    // markers que l'on comptera dans une catégorie "Autres" car ils
    // représentent trop peu de %
    int leftCount = 0;
    float mCount = stats.getMarkerCount();
    // et on remplit
    for (Iterator<MarkerStat> iter = markerStatCollection.iterator(); iter.hasNext();) {
      MarkerStat markerStat = iter.next();

      // on calcule le %
      float percentage = CENT * markerStat.getCount() / mCount;
      if (mShowAllCategories) {
        setValue(markerStat.getIdentifiant(), percentage);
      } else {
        // on ne veut pas montrer toutes les catégories : on fait le tri
        if (percentage > POURCENTAGE_MIN) {
          setValue(markerStat.getIdentifiant(), percentage);
        } else {
          leftCount += markerStat.getCount();
        }
      }
    }
    if (!mShowAllCategories && leftCount != 0) {
      // on ne veut pas montrer toutes les catégories, et certaines
      // n'ont pas été prises en compte : on les mets dans "Autres"
      setValue(Messages.GraphPieDataset_otherCategories, CENT * leftCount / mCount);
    }
    fireDatasetChanged();
  }

  /**
   * Remet à zéro les données du graphe en enlevant tout.
   */
  public void removeValues() {
    mData = new DefaultKeyedValues();
    fireDatasetChanged();
  }

  /**
   * The showAllCategories to set.
   *
   * @param showAllCategories
   *          The showAllCategories to set.
   */
  public void setShowAllCategories(boolean showAllCategories) {
    this.mShowAllCategories = showAllCategories;
  }

  // ---------------- RAJOUT : fin ----------------

  /** Storage for the data. */
  private DefaultKeyedValues mData;

  /**
   * Constructs a new dataset, initially empty.
   */
  public GraphPieDataset() {
    this.mData = new DefaultKeyedValues();
  }

  /**
   * Returns the number of items in the dataset.
   *
   * @return the item count.
   */
  @Override
  public int getItemCount() {
    return this.mData.getItemCount();
  }

  /**
   * Returns the categories in the dataset. The returned list is unmodifiable.
   *
   * @return the categories in the dataset.
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<?> getKeys() {
    return Collections.unmodifiableList(this.mData.getKeys());
  }

  /**
   * Returns the key for an item.
   *
   * @param item
   *          the item index (zero-based).
   *
   * @return the category.
   */
  @Override
  public Comparable<?> getKey(final int item) {

    Comparable<?> result = null;
    if (getItemCount() > item) {
      result = this.mData.getKey(item);
    }
    return result;

  }

  /**
   * Returns the index for a key.
   *
   * @param key
   *          the key.
   *
   * @return the key index.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public int getIndex(final Comparable key) {

    return this.mData.getIndex(key);

  }

  /**
   * Returns a value.
   *
   * @param item
   *          the value index.
   *
   * @return the value (possibly <code>null</code>).
   */
  @Override
  public Number getValue(final int item) {

    Number result = null;
    if (getItemCount() > item) {
      result = this.mData.getValue(item);
    }
    return result;

  }

  /**
   * Returns the data value associated with a key.
   *
   * @param key
   *          the key (<code>null</code> not permitted).
   *
   * @return the value (possibly <code>null</code>).
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Number getValue(final Comparable key) {

    // check arguments...
    if (key == null) {
      throw new IllegalArgumentException("PieDataset: null key not allowed."); //$NON-NLS-1$
    }

    // fetch the value...
    return this.mData.getValue(key);

  }

  /**
   * Sets the data value for a key.
   *
   * @param key
   *          the key.
   * @param value
   *          the value.
   */
  public void setValue(final Comparable<?> key, final Number value) {

    this.mData.setValue(key, value);
    fireDatasetChanged();

  }

  /**
   * Sets the data value for a key.
   *
   * @param key
   *          the key.
   * @param value
   *          the value.
   */
  public void setValue(final Comparable<?> key, final double value) {
    setValue(key, Double.valueOf(value));
  }
}
