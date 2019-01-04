//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class containing all configurations for a eclipse-formatter-profile.
 * 
 * @author Lukas Frena
 */
public class FormatterConfiguration {
  /** Map containing all eclipse editor-settings. */
  private final Map<String, String> mCleanupSettings = new HashMap<>();

  /** Map containing all eclipse formatter-settings. */
  private final Map<String, String> mFormatterSettings = new HashMap<>();

  /**
   * Creates new instance of class FormatterConfiguration.
   */
  public FormatterConfiguration() {
  }

  /**
   * Method for adding a new global setting.
   * 
   * @param setting
   *          The global setting.
   * @param val
   *          The value of this setting.
   */
  public void addCleanupSetting(final String setting, final String val) {
    mCleanupSettings.put(setting, val);
  }

  /**
   * Method for adding a new local setting.
   * 
   * @param setting
   *          The local setting.
   * @param val
   *          The value of this setting.
   */
  public void addFormatterSetting(final String setting, final String val) {
    mFormatterSettings.put(setting, val);
  }

  /**
   * Method for returning the stored global Settings.
   * 
   * @return Returns the global settings.
   */
  public Map<String, String> getCleanupSettings() {
    return mCleanupSettings;
  }

  /**
   * Method for returning the stored local Settings.
   * 
   * @return Returns the local settings.
   */
  public Map<String, String> getFormatterSettings() {
    return mFormatterSettings;
  }

  /**
   * Method for adding new configuration parameters.
   * 
   * @param settings
   *          A eclipse-formatter-configuration.
   */
  public void addConfiguration(final FormatterConfiguration settings) {
    // add local settings
    final Map<String, String> localSettings = settings.getFormatterSettings();
    final Collection<String> localKeys = localSettings.keySet();
    final Iterator<String> localIt = localKeys.iterator();
    String local;
    while (localIt.hasNext()) {
      local = localIt.next();
      if (mFormatterSettings.containsKey(local)) {
        if (!mFormatterSettings.get(local).equals(localSettings.get(local))) {
          // Logger.writeln("already containing local rule " + local
          // + " with other attributes, it gets overwritten!");
        }
      }
      addFormatterSetting(local, localSettings.get(local));
    }

    // add global settings
    final Map<String, String> globalSettings = settings.getCleanupSettings();
    final Collection<String> globalKeys = globalSettings.keySet();
    final Iterator<String> globalIt = globalKeys.iterator();
    String global;
    while (globalIt.hasNext()) {
      global = globalIt.next();
      if (mCleanupSettings.containsKey(global)) {
        if (!getCleanupSettings().get(global).equals(globalSettings.get(global))) {
          // Logger.writeln("already containing global rule " + global
          // + " with other attributes, it gets overwritten!");
        }
      }
      addCleanupSetting(global, settings.getCleanupSettings().get(global));
    }
  }
}
