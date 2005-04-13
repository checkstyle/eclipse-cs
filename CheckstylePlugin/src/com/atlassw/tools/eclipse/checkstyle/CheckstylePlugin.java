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
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstylePlugin extends AbstractUIPlugin
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Identifier of the plug-in. */
    public static final String PLUGIN_ID = "com.atlassw.tools.eclipse.checkstyle";

    /**
     * Preference name indicating if rule names are to be included in violation
     * messages.
     */
    public static final String PREF_INCLUDE_RULE_NAMES = "include.rule.names";

    /**
     * Preference name indication if the user should be warned of possibly
     * losing fileset configurations if he switches from advanced to simple
     * fileset configuration.
     */
    public static final String PREF_FILESET_WARNING = "warn.before.losing.filesets";

    /**
     * Preference name indicating the minimum amount of lines that is used for the checker
     * analysis.
     */
    public static final String PREF_DUPLICATED_CODE_MIN_LINES = "checker.strictDuplicatedCode.minLines";
    
    /**
     * Default value for the minimum amount of lines that is used for the checker
     * analysis.
     */
    public static final int DUPLICATED_CODE_MIN_LINES = 20;
    
    /** constant for the plugin properties. */
    private static final String PROPERTIES = "plugin.properties";

    //=================================================
    // Static class variables.
    //=================================================

    /** The shared instance. */
    private static CheckstylePlugin sPlugin;

    /** Resource bundle. */
    private static ResourceBundle sResourceBundle;

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     * The constructor.
     */
    public CheckstylePlugin()
    {
        super();
        sPlugin = this;
    }

    //=================================================
    // Methods.
    //=================================================

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
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     * 
     * @param key Resource key.
     * 
     * @return The requested resource.
     */
    public static String getResourceString(String key)
    {
        if (sResourceBundle == null)
        {
            try
            {
                URL url = CheckstylePlugin.getDefault().find(new Path(PROPERTIES));
                sResourceBundle = new PropertyResourceBundle(url.openStream());
            }
            catch (IOException ioe)
            {
                CheckstyleLog.error(ioe.getLocalizedMessage(), ioe);
            }
        }

        try
        {
            return sResourceBundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }

    /**
     * Initialize the default preferences.
     */
    protected void initializeDefaultPluginPreferences()
    {
        IPreferenceStore prefStore = getPreferenceStore();

        prefStore.setDefault(PREF_INCLUDE_RULE_NAMES, false);
        prefStore.setDefault(PREF_FILESET_WARNING, true);
        prefStore.setDefault(PREF_DUPLICATED_CODE_MIN_LINES, DUPLICATED_CODE_MIN_LINES);
    }
}