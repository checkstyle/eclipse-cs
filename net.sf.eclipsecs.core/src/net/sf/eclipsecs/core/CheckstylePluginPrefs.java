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

package net.sf.eclipsecs.core;

import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Class for handling preferences of the <code>net.sf.eclipsecs.core</code> plugin.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstylePluginPrefs extends AbstractPreferenceInitializer {

  /**
   * Preference name indicating if rule names are to be included in violation messages.
   */
  public static final String PREF_INCLUDE_RULE_NAMES = "include.rule.names"; //$NON-NLS-1$

  /**
   * Preference name indicating if module ids are to be included in violation messages.
   */
  public static final String PREF_INCLUDE_MODULE_IDS = "include.module.ids"; //$NON-NLS-1$

  /**
   * Preference name indicating if the number of checkstyle warning generated per file should be
   * limited.
   */
  public static final String PREF_LIMIT_MARKERS_PER_RESOURCE = "limit.markers.per.resource"; //$NON-NLS-1$

  /**
   * Preference name for the preference that stores the limit of markers per resource.
   */
  public static final String PREF_MARKER_AMOUNT_LIMIT = "marker.amount.limit"; //$NON-NLS-1$

  /**
   * Preference name for the preference to execute Checkstyle on full builds in the background.
   */
  public static final String PREF_BACKGROUND_FULL_BUILD = "background.full.build"; //$NON-NLS-1$

  /** Default value for the marker limitation. */
  public static final int MARKER_LIMIT = 100;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeDefaultPreferences() {

    IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CheckstylePlugin.PLUGIN_ID);
    prefs.putBoolean(PREF_INCLUDE_RULE_NAMES, false);
    prefs.putBoolean(PREF_INCLUDE_MODULE_IDS, false);
    prefs.putBoolean(PREF_LIMIT_MARKERS_PER_RESOURCE, false);
    prefs.putInt(PREF_MARKER_AMOUNT_LIMIT, MARKER_LIMIT);
    prefs.putBoolean(PREF_BACKGROUND_FULL_BUILD, false);

    try {
      prefs.flush();
    } catch (BackingStoreException e) {
      CheckstyleLog.log(e);
    }
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
    return prefs.getBoolean(CheckstylePlugin.PLUGIN_ID, prefId, false, null);
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
    return prefs.getInt(CheckstylePlugin.PLUGIN_ID, prefId, 0, null);
  }

  /**
   * Set a boolean preference for the given preference id.
   * 
   * @param prefId
   *          the preference id
   * @param value
   *          the boolean value
   * @throws BackingStoreException
   *           if this operation cannot be completed due to a failure in the backing store, or
   *           inability to communicate with it.
   */
  public static void setBoolean(String prefId, boolean value) throws BackingStoreException {

    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CheckstylePlugin.PLUGIN_ID);
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
   *           if this operation cannot be completed due to a failure in the backing store, or
   *           inability to communicate with it.
   */
  public static void setInt(String prefId, int value) throws BackingStoreException {

    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CheckstylePlugin.PLUGIN_ID);
    prefs.putInt(prefId, value);
    prefs.flush();
  }
}
