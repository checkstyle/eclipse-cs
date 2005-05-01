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

package com.atlassw.tools.eclipse.checkstyle;

//=================================================
// Imports from java namespace
//=================================================
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstylePlugin extends AbstractUIPlugin
{
    // =================================================
    // Public static final variables.
    // =================================================

    /** Identifier of the plug-in. */
    public static final String PLUGIN_ID = "com.atlassw.tools.eclipse.checkstyle"; //$NON-NLS-1$

    /**
     * Preference name indicating if rule names are to be included in violation
     * messages.
     */
    public static final String PREF_INCLUDE_RULE_NAMES = "include.rule.names"; //$NON-NLS-1$

    /**
     * Preference name indication if the user should be warned of possibly
     * losing fileset configurations if he switches from advanced to simple
     * fileset configuration.
     */
    public static final String PREF_FILESET_WARNING = "warn.before.losing.filesets"; //$NON-NLS-1$

    /**
     * Preference name indication if the user should be asked before rebuilding
     * projects.
     */
    public static final String PREF_ASK_BEFORE_REBUILD = "ask.before.rebuild"; //$NON-NLS-1$

    /**
     * Preference name indication if the checkstyle tokens within the module
     * editor should be translated.
     */
    public static final String PREF_TRANSLATE_TOKENS = "translate.checkstyle.tokens"; //$NON-NLS-1$

    /**
     * Preference name indicating the minimum amount of lines that is used for
     * the checker analysis.
     */
    public static final String PREF_DUPLICATED_CODE_MIN_LINES = "checker.strictDuplicatedCode.minLines"; //$NON-NLS-1$

    /**
     * Default value for the minimum amount of lines that is used for the
     * checker analysis.
     */
    public static final int DUPLICATED_CODE_MIN_LINES = 20;

    // =================================================
    // Static class variables.
    // =================================================

    /** The shared instance. */
    private static CheckstylePlugin sPlugin;

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * The constructor.
     */
    public CheckstylePlugin()
    {
        super();
        sPlugin = this;
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Returns the shared instance.
     * 
     * @return The shared plug-in instance.
     */
    public static CheckstylePlugin getDefault()
    {
        return sPlugin;
    }

    /**
     * Returns the workspace instance.
     * 
     * @return Workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the checkstyle logo.
     * 
     * @return the checkstyle logo
     */
    public static Image getLogo()
    {
        return CheckstylePlugin.imageDescriptorFromPlugin(PLUGIN_ID, "icons/logo.png") //$NON-NLS-1$
                .createImage();
    }

    /**
     * Initialize the default preferences.
     */
    protected void initializeDefaultPluginPreferences()
    {
        IPreferenceStore prefStore = getPreferenceStore();

        prefStore.setDefault(PREF_INCLUDE_RULE_NAMES, false);
        prefStore.setDefault(PREF_FILESET_WARNING, true);
        prefStore.setDefault(PREF_ASK_BEFORE_REBUILD, MessageDialogWithToggle.PROMPT);
        prefStore.setDefault(PREF_TRANSLATE_TOKENS, true);
        prefStore.setDefault(PREF_DUPLICATED_CODE_MIN_LINES, DUPLICATED_CODE_MIN_LINES);
    }
}