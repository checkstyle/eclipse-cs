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
package net.sf.eclipsecs.stats;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

/**
 * The plugins preferences initializer.
 * 
 * @author Fabrice BELLINGARD
 */
public class PrefsInitializer extends AbstractPreferenceInitializer
{

    /**
     * Permet de dire si on veut afficher dans le graphe les erreurs Checkstyle
     * liées au Javadoc.
     */
    public static final String PROPS_SHOW_JAVADOC_ERRORS = "show_javadoc_errors"; //$NON-NLS-1$

    /**
     * Permet de dire si on veut afficher dans le graphe toutes les catégories
     * ou bien regrouper les plus faibles dans une catégorie "Autres".
     */
    public static final String PROPS_SHOW_ALL_CATEGORIES = "show_all_categories"; //$NON-NLS-1$

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences()
    {
        StatsCheckstylePlugin.getDefault().getPluginPreferences().setDefault(
            PROPS_SHOW_JAVADOC_ERRORS, true);
        StatsCheckstylePlugin.getDefault().getPluginPreferences().setDefault(
            PROPS_SHOW_ALL_CATEGORIES, false);
    }

}
