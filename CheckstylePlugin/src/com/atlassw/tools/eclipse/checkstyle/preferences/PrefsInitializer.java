//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.osgi.service.prefs.BackingStoreException;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Initialize the plugin preferences.
 * 
 * @author Lars Ködderitzsch
 */
public class PrefsInitializer extends AbstractPreferenceInitializer
{

    /**
     * {@inheritDoc}
     */
    public void initializeDefaultPreferences()
    {

        IEclipsePreferences prefs = new DefaultScope().getNode(CheckstylePlugin.PLUGIN_ID);

        prefs.putBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, false);
        prefs.putBoolean(CheckstylePlugin.PREF_FILESET_WARNING, true);
        prefs.put(CheckstylePlugin.PREF_ASK_BEFORE_REBUILD, MessageDialogWithToggle.PROMPT);
        prefs.putBoolean(CheckstylePlugin.PREF_TRANSLATE_TOKENS, true);
        prefs.putBoolean(CheckstylePlugin.PREF_SORT_TOKENS, false);
        prefs.putBoolean(CheckstylePlugin.PREF_OPEN_MODULE_EDITOR, true);
        prefs.putInt(CheckstylePlugin.PREF_DUPLICATED_CODE_MIN_LINES,
                CheckstylePlugin.DUPLICATED_CODE_MIN_LINES);
        prefs.putBoolean(CheckstylePlugin.PREF_LIMIT_MARKERS_PER_RESOURCE, false);
        prefs.putInt(CheckstylePlugin.PREF_MARKER_AMOUNT_LIMIT, CheckstylePlugin.MARKER_LIMIT);
        prefs.putBoolean(CheckstylePlugin.PREF_DISABLE_PROJ_CLASSLOADER, false);
        try
        {
            prefs.flush();
        }
        catch (BackingStoreException e)
        {
            CheckstyleLog.log(e);
        }
    }

}