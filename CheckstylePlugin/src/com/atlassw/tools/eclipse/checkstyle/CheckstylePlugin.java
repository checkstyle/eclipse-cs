//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstylePlugin extends AbstractUIPlugin
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Identifier of the plug-in */
    public static final String PLUGIN_ID = "com.atlassw.tools.eclipse.checkstyle";
    
    /** Preference name indicating if rule names are to be included in violation messages. */
    public static final String PREF_INCLUDE_RULE_NAMES = "include.rule.names";

    //=================================================
    // Static class variables.
    //=================================================

    /**  The shared instance. */
    private static CheckstylePlugin sPlugin;

    //=================================================
    // Instance member variables.
    //=================================================

    /**  Resource bundle. */
    private ResourceBundle mResourceBundle;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    /**
     *  The constructor.
     *
     *  @param descriptor  Plug-in descriptor.
     */
    public CheckstylePlugin(IPluginDescriptor descriptor)
    {
        super(descriptor);
        sPlugin = this;

        try
        {
            mResourceBundle =
                ResourceBundle.getBundle(
                    "com.atlassw.tools.eclipse.checkstyle.CheckstylePluginResources");
        }
        catch (MissingResourceException x)
        {
            mResourceBundle = null;
        }
    }

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Returns the shared instance.
     *
     *  @return The shared plug-in instance.
     */
    public static CheckstylePlugin getDefault()
    {
        return sPlugin;
    }

    /**
     *  Returns the workspace instance.
     *
     *  @return  Workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     *  Returns the string from the plugin's resource bundle,
     *  or 'key' if not found.
     *
     *  @param  key  Resource key.
     *
     *  @return The requested resource.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = CheckstylePlugin.getDefault().getResourceBundle();

        try
        {
            return bundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }

    /**
     *  Returns the plugin's resource bundle.
     *
     *  @return  The plug-in's resource bundle.
     */
    public ResourceBundle getResourceBundle()
    {
        return mResourceBundle;
    }
    
    /**
     * Initialize the default preferences.
     */
	protected void initializeDefaultPluginPreferences()
	{
		IPreferenceStore prefStore = getPreferenceStore();
		
		prefStore.setDefault(PREF_INCLUDE_RULE_NAMES, false);
	}
}
