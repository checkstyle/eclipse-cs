//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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
     * Preference name indicating if the checkstyle tokens within the module
     * editor should be translated.
     */
    public static final String PREF_TRANSLATE_TOKENS = "translate.checkstyle.tokens"; //$NON-NLS-1$

    /**
     * Preference name indicating if the module editor should be opened when
     * adding a module.
     */
    public static final String PREF_OPEN_MODULE_EDITOR = "open.module.editor.on.add"; //$NON-NLS-1$

    /**
     * Preference name indicating if the number of checkstyle warning generated
     * per file should be limited.
     */
    public static final String PREF_LIMIT_MARKERS_PER_RESOURCE = "limit.markers.per.resource"; //$NON-NLS-1$

    /**
     * Preference name for the preference that stores the limit of markers per
     * resource.
     */
    public static final String PREF_MARKER_AMOUNT_LIMIT = "marker.amount.limit"; //$NON-NLS-1$

    /**
     * Preference name indicating the minimum amount of lines that is used for
     * the checker analysis.
     */
    public static final String PREF_DUPLICATED_CODE_MIN_LINES = "checker.strictDuplicatedCode.minLines"; //$NON-NLS-1$

    /**
     * Preference name indicating if the project classloader feature should be
     * disabled. This can help with worspace crashes with RAD 6.0.
     */
    public static final String PREF_DISABLE_PROJ_CLASSLOADER = "diable.project.classloader"; //$NON-NLS-1$

    /**
     * Default value for the minimum amount of lines that is used for the
     * checker analysis.
     */
    public static final int DUPLICATED_CODE_MIN_LINES = 20;

    /** Default value for the marker limitation. */
    public static final int MARKER_LIMIT = 100;

    /** Constant for the path of the (custom) package names file. */
    public static final String PACKAGE_NAMES_FILE = "/extension-libraries/checkstyle_packages.xml";

    /** Constant for the path to the extension-libraries directory. */
    public static final String EXTENSION_LIBS_DIR = "/extension-libraries";

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
     * Initialize the default preferences.
     */
    protected void initializeDefaultPluginPreferences()
    {
        IPreferenceStore prefStore = getPreferenceStore();

        prefStore.setDefault(PREF_INCLUDE_RULE_NAMES, false);
        prefStore.setDefault(PREF_FILESET_WARNING, true);
        prefStore.setDefault(PREF_ASK_BEFORE_REBUILD, MessageDialogWithToggle.PROMPT);
        prefStore.setDefault(PREF_TRANSLATE_TOKENS, true);
        prefStore.setDefault(PREF_OPEN_MODULE_EDITOR, true);
        prefStore.setDefault(PREF_DUPLICATED_CODE_MIN_LINES, DUPLICATED_CODE_MIN_LINES);
        prefStore.setDefault(PREF_LIMIT_MARKERS_PER_RESOURCE, false);
        prefStore.setDefault(PREF_MARKER_AMOUNT_LIMIT, MARKER_LIMIT);
        prefStore.setDefault(PREF_DISABLE_PROJ_CLASSLOADER, false);
    }
}