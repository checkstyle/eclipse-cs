//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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
package com.atlassw.tools.eclipse.checkstyle.preferences.duplicates;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

/**
 * Preference page for the duplicated code checker.
 * 
 * @author Fabrice BELLINGARD
 */
public class DuplicatedCodePreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage 
{

    /**
     * Constructeur.
     */
    public DuplicatedCodePreferencePage() 
    {
        super(GRID);
        setPreferenceStore(CheckstylePlugin.getDefault().getPreferenceStore());
        setDescription("Duplicated code checker settings:");
        initializeDefaults();
    }

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults() 
    {
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(CheckstylePlugin.PREF_DUPLICATED_CODE_MIN_LINES, CheckstylePlugin
                .getDefault().getPluginPreferences().getInt(
                		CheckstylePlugin.PREF_DUPLICATED_CODE_MIN_LINES));
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors() 
    {
        addField(new IntegerFieldEditor(CheckstylePlugin.PREF_DUPLICATED_CODE_MIN_LINES,
                "Minimum number of lines to consider code as duplicated: ",
                getFieldEditorParent()));
    }

    /**
     * Cf. overriden method documentation.
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) 
    {
    }
}