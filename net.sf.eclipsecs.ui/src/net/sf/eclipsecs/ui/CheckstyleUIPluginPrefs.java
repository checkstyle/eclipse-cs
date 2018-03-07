//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui;

import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Initialize the plugin preferences.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleUIPluginPrefs extends AbstractPreferenceInitializer {

  /**
   * Preference name indication if the user should be warned of possibly losing
   * fileset configurations if he switches from advanced to simple fileset
   * configuration.
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
   * Preference name indicating if the checkstyle tokens within the module
   * editor should be sorted.
   */
  public static final String PREF_SORT_TOKENS = "translate.sort.tokens"; //$NON-NLS-1$

  /**
   * Preference name indicating if the module editor should be opened when
   * adding a module.
   */
  public static final String PREF_OPEN_MODULE_EDITOR = "open.module.editor.on.add"; //$NON-NLS-1$

  /**
   * Preference name indicating the minimum amount of lines that is used for the
   * checker analysis.
   */
  public static final String PREF_DUPLICATED_CODE_MIN_LINES = "checker.strictDuplicatedCode.minLines"; //$NON-NLS-1$

  /**
   * Default value for the minimum amount of lines that is used for the checker
   * analysis.
   */
  public static final int DUPLICATED_CODE_MIN_LINES = 20;

  /**
   * Preference name indicating if all categories should be shown in the
   * statistics views.
   */
  public static final String PREF_STATS_SHOW_ALL_CATEGORIES = "show_all_categories"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeDefaultPreferences() {

    IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CheckstyleUIPlugin.PLUGIN_ID);

    prefs.putBoolean(PREF_FILESET_WARNING, true);
    prefs.put(PREF_ASK_BEFORE_REBUILD, MessageDialogWithToggle.PROMPT);
    prefs.putBoolean(PREF_TRANSLATE_TOKENS, true);
    prefs.putBoolean(PREF_SORT_TOKENS, false);
    prefs.putBoolean(PREF_OPEN_MODULE_EDITOR, true);
    prefs.putInt(PREF_DUPLICATED_CODE_MIN_LINES, DUPLICATED_CODE_MIN_LINES);
    prefs.putBoolean(PREF_STATS_SHOW_ALL_CATEGORIES, false);

    try {
      prefs.flush();
    } catch (BackingStoreException e) {
      CheckstyleLog.log(e);
    }
  }

  /**
   * Returns a string preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @return the string result
   */
  public static String getString(String prefId) {

    IPreferencesService prefs = Platform.getPreferencesService();
    return prefs.getString(CheckstyleUIPlugin.PLUGIN_ID, prefId, null, null);
  }

  /**
   * Returns a boolean preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @return the boolean result
   */
  public static boolean getBoolean(String prefId) {

    IPreferencesService prefs = Platform.getPreferencesService();
    return prefs.getBoolean(CheckstyleUIPlugin.PLUGIN_ID, prefId, false, null);
  }

  /**
   * Returns an integer preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @return the integer result
   */
  public static int getInt(String prefId) {

    IPreferencesService prefs = Platform.getPreferencesService();
    return prefs.getInt(CheckstyleUIPlugin.PLUGIN_ID, prefId, 0, null);
  }

  /**
   * Set a string preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @param value
   *          the string value
   * @throws BackingStoreException
   *           if this operation cannot be completed due to a failure in the
   *           backing store, or inability to communicate with it.
   */
  public static void setString(String prefId, String value) throws BackingStoreException {

    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CheckstyleUIPlugin.PLUGIN_ID);
    prefs.put(prefId, value);
    prefs.flush();
  }

  /**
   * Set a boolean preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @param value
   *          the boolean value
   * @throws BackingStoreException
   *           if this operation cannot be completed due to a failure in the
   *           backing store, or inability to communicate with it.
   */
  public static void setBoolean(String prefId, boolean value) throws BackingStoreException {

    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CheckstyleUIPlugin.PLUGIN_ID);
    prefs.putBoolean(prefId, value);
    prefs.flush();
  }

  /**
   * Set a int preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @param value
   *          the boolean value
   * @throws BackingStoreException
   *           if this operation cannot be completed due to a failure in the
   *           backing store, or inability to communicate with it.
   */
  public static void setInt(String prefId, int value) throws BackingStoreException {

    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CheckstyleUIPlugin.PLUGIN_ID);
    prefs.putInt(prefId, value);
    prefs.flush();
  }
}
