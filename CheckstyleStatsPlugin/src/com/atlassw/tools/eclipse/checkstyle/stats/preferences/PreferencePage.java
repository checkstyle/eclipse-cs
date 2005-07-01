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
package com.atlassw.tools.eclipse.checkstyle.stats.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassw.tools.eclipse.checkstyle.stats.Messages;
import com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePlugin;

/**
 * Préférences du plugin.
 * 
 * @author Fabrice BELLINGARD
 */

public class PreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
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
     * Constructeur.
     */
    public PreferencePage()
    {
        super(GRID);
        setPreferenceStore(StatsCheckstylePlugin.getDefault()
            .getPreferenceStore());
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults()
    {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(PROPS_SHOW_JAVADOC_ERRORS, StatsCheckstylePlugin
            .getDefault().getPluginPreferences().getBoolean(
                PROPS_SHOW_JAVADOC_ERRORS));
        store.setDefault(PROPS_SHOW_ALL_CATEGORIES, StatsCheckstylePlugin
            .getDefault().getPluginPreferences().getBoolean(
                PROPS_SHOW_ALL_CATEGORIES));
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */

    public void createFieldEditors()
    {
        addField(new BooleanFieldEditor(PROPS_SHOW_JAVADOC_ERRORS,
            Messages.PreferencePage_displayJavadocErrors,
            getFieldEditorParent()));

        addField(new BooleanFieldEditor(PROPS_SHOW_ALL_CATEGORIES,
            Messages.PreferencePage_displayAllCategories,
            getFieldEditorParent()));
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }
}