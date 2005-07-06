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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.data.MarkerStat;

/**
 * Implémentation d'un PieDataset pour fournir les données à afficher au graph.
 * 
 * Copié de DefaultPieDataset car celui-ci ne permettait pas d'être vidé de ses
 * données dynamiquement. Le code ajouté est balisé en début de classe.
 * 
 * @see org.jfree.data.general.DefaultPieDataset
 * @author Fabrice BELLINGARD
 */
public class GraphPieDataset extends AbstractDataset implements PieDataset,
    Cloneable, Serializable
{

    // --------------- RAJOUT : début ---------------
    /**
     * Pourcentage minimum en dessous duquel une catégorie d'erreur est mise
     * dans la catégorie "Autres" opur l'affichage du camember.
     */
    private static final int POURCENTAGE_MIN = 2;

    /**
     * Pourcentage pour le calcul.
     */
    private static final int CENT = 100;

    /**
     * Afficher ou pas les Javadoc.
     */
    private boolean mShowJavadoc;

    /**
     * Afficher ou pas toutes les catégories.
     */
    private boolean mShowAllCategories;

    /**
     * La collection à afficher.
     */
    private Collection mMarkerStatCollection;

    /**
     * Le nombre de marqueurs analysés.
     */
    private int mMarkerCount;

    /**
     * Remplit le Dataset avec les valeurs de la collection de MarkerStat.
     * 
     * @param markerStatCollection :
     *            la collection d'objet MarkerStat
     * @param markerCount :
     *            le nombre de marqueurs analysés
     */
    public void setMarkerStatCollection(Collection markerStatCollection,
        int markerCount)
    {
        this.mMarkerStatCollection = markerStatCollection;
        this.mMarkerCount = markerCount;
        mData = new DefaultKeyedValues();
        // markers que l'on comptera dans une catégorie "Autres" car ils
        // représentent trop peu de %
        int leftCount = 0;
        float mCount = new Float(markerCount).floatValue();
        // et on remplit
        for (Iterator iter = markerStatCollection.iterator(); iter.hasNext();)
        {
            MarkerStat markerStat = (MarkerStat) iter.next();
            // on vérifie si on affiche le Javadoc
            if (!mShowJavadoc)
            {
                String id = markerStat.getIdentifiant();
                // look for the word Javadoc (or javadoc) or @
                if (id.indexOf("avadoc") > -1 || id.indexOf("@") > -1) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    // c'est une erreur Javadoc : on ne l'affiche pas
                    continue;
                }
            }
            // on calcule le %
            float percentage = CENT * markerStat.getCount() / mCount;
            if (mShowAllCategories)
            {
                setValue(markerStat.getIdentifiant(), percentage);
            }
            else
            {
                // on ne veut pas montrer toutes les catégories : on fait le tri
                if (percentage > POURCENTAGE_MIN)
                {
                    setValue(markerStat.getIdentifiant(), percentage);
                }
                else
                {
                    leftCount += markerStat.getCount();
                }
            }
        }
        if (!mShowAllCategories && leftCount != 0)
        {
            // on ne veut pas montrer toutes les catégories, et certaines
            // n'ont pas été prises en compte : on les mets dans "Autres"
            setValue(Messages.GraphPieDataset_otherCategories, CENT * leftCount
                / mCount);
        }
        fireDatasetChanged();
    }

    /**
     * Remet à zéro les données du graphe en enlevant tout.
     */
    public void removeValues()
    {
        mData = new DefaultKeyedValues();
        fireDatasetChanged();
    }

    /**
     * The showJavadoc to set.
     * 
     * @param showJavadoc
     *            The showJavadoc to set.
     */
    public void setShowJavadoc(boolean showJavadoc)
    {
        this.mShowJavadoc = showJavadoc;
    }

    /**
     * The showAllCategories to set.
     * 
     * @param showAllCategories
     *            The showAllCategories to set.
     */
    public void setShowAllCategories(boolean showAllCategories)
    {
        this.mShowAllCategories = showAllCategories;
    }

    // ---------------- RAJOUT : fin ----------------

    /** Storage for the data. */
    private DefaultKeyedValues mData;

    /**
     * Constructs a new dataset, initially empty.
     */
    public GraphPieDataset()
    {

        this.mData = new DefaultKeyedValues();

    }

    /**
     * Creates a new dataset that uses the data from a {@link KeyedValues}
     * instance.
     * 
     * @param data
     *            the data.
     */
    public GraphPieDataset(final KeyedValues data)
    {

        this.mData = new DefaultKeyedValues();
        for (int i = 0; i < data.getItemCount(); i++)
        {
            this.mData.addValue(data.getKey(i), data.getValue(i));
        }
    }

    /**
     * Returns the number of items in the dataset.
     * 
     * @return the item count.
     */
    public int getItemCount()
    {
        return this.mData.getItemCount();
    }

    /**
     * Returns the categories in the dataset. The returned list is unmodifiable.
     * 
     * @return the categories in the dataset.
     */
    public List getKeys()
    {
        return Collections.unmodifiableList(this.mData.getKeys());
    }

    /**
     * Returns the key for an item.
     * 
     * @param item
     *            the item index (zero-based).
     * 
     * @return the category.
     */
    public Comparable getKey(final int item)
    {

        Comparable result = null;
        if (getItemCount() > item)
        {
            result = this.mData.getKey(item);
        }
        return result;

    }

    /**
     * Returns the index for a key.
     * 
     * @param key
     *            the key.
     * 
     * @return the key index.
     */
    public int getIndex(final Comparable key)
    {

        return this.mData.getIndex(key);

    }

    /**
     * Returns a value.
     * 
     * @param item
     *            the value index.
     * 
     * @return the value (possibly <code>null</code>).
     */
    public Number getValue(final int item)
    {

        Number result = null;
        if (getItemCount() > item)
        {
            result = this.mData.getValue(item);
        }
        return result;

    }

    /**
     * Returns the data value associated with a key.
     * 
     * @param key
     *            the key (<code>null</code> not permitted).
     * 
     * @return the value (possibly <code>null</code>).
     */
    public Number getValue(final Comparable key)
    {

        // check arguments...
        if (key == null)
        {
            throw new IllegalArgumentException(
                "PieDataset: null key not allowed."); //$NON-NLS-1$
        }

        // fetch the value...
        return this.mData.getValue(key);

    }

    /**
     * Sets the data value for a key.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value.
     */
    public void setValue(final Comparable key, final Number value)
    {

        this.mData.setValue(key, value);
        fireDatasetChanged();

    }

    /**
     * Sets the data value for a key.
     * 
     * @param key
     *            the key.
     * @param value
     *            the value.
     */
    public void setValue(final Comparable key, final double value)
    {

        setValue(key, new Double(value));

    }

    /**
     * Tests if this object is equal to another.
     * 
     * @param o
     *            the other object.
     * 
     * @return A boolean.
     */
    public boolean equals(final Object o)
    {

        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof PieDataset))
        {
            return false;
        }
        final PieDataset pd = (PieDataset) o;
        final int count = getItemCount();
        if (pd.getItemCount() != count)
        {
            return false;
        }

        for (int i = 0; i < count; i++)
        {
            final Comparable k1 = getKey(i);
            final Comparable k2 = pd.getKey(i);
            if (!k1.equals(k2))
            {
                return false;
            }

            final Number v1 = getValue(i);
            final Number v2 = pd.getValue(i);
            if (v1 == null)
            {
                if (v2 != null)
                {
                    return false;
                }
            }
            else
            {
                if (!v1.equals(v2))
                {
                    return false;
                }
            }
        }
        return true;

    }

    /**
     * Returns a hash code.
     * 
     * @return a hash code.
     */
    public int hashCode()
    {
        return this.mData.hashCode();
    }

    /**
     * Returns a clone.
     * 
     * @return A clone.
     * 
     * @throws CloneNotSupportedException
     *             This class will not throw this exception, but subclasses (if
     *             any) might.
     */
    public Object clone() throws CloneNotSupportedException
    {
        final GraphPieDataset clone = (GraphPieDataset) super.clone();
        clone.mData = (DefaultKeyedValues) this.mData.clone();
        return clone;
    }

}
