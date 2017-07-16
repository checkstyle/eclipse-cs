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

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class for storing all settings of a checkstyle-configuration file.
 *
 * @author Lukas Frena
 *
 */
public class CheckstyleSetting {
  /** Map which holds all checker-modules of the configuration. */
  private final HashMap<String, HashMap<String, String>> mCheckerModules = new HashMap<>();

  /** Map which holds all treewalker-modules of the configuration. */
  private final HashMap<String, HashMap<String, String>> mTreeWalkerModules = new HashMap<>();

  /**
   * Creates new instance of class CheckstyleSetting.
   */
  public CheckstyleSetting() {
  }

  /**
   * Method for adding a new treewalker-module.
   *
   * @param name
   *          The name of the module.
   * @param properties
   *          A hashmap of properties of this module.
   */
  public void addTreeWalkerModule(final String name, final HashMap<String, String> properties) {

    mTreeWalkerModules.put(name, properties);
  }

  /**
   * Method for adding a new checker-module.
   *
   * @param name
   *          The name of the module.
   * @param properties
   *          A hashmap of properties of this module.
   */
  public void addCheckerModule(final String name, final HashMap<String, String> properties) {

    mCheckerModules.put(name, properties);
  }

  /**
   * Method for getting all checker-modules.
   *
   * @return A hashmap containing all checker-modules.
   */
  public HashMap<String, HashMap<String, String>> getmCheckerModules() {
    return mCheckerModules;
  }

  /**
   * Method for getting all treewalker-modules.
   *
   * @return A hashmap containing all treewalker-modules.
   */
  public HashMap<String, HashMap<String, String>> getmTreeWalkerModules() {
    return mTreeWalkerModules;
  }

  /**
   * Method for adding another instance of type CheckstyleSetting.
   *
   * @param setting
   *          The CheckstyleSetting to add.
   */
  public void addSetting(final CheckstyleSetting setting) {
    // add checker-modules
    Iterator<String> modit = setting.getmCheckerModules().keySet().iterator();
    String module;
    while (modit.hasNext()) {
      module = modit.next();
      if (!mCheckerModules.keySet().contains(module)) {
        mCheckerModules.put(module, setting.getmCheckerModules().get(module));
      } else {
        final Iterator<String> propit = setting.getmCheckerModules().get(module).keySet()
                .iterator();
        String property;
        while (propit.hasNext()) {
          property = propit.next();
          if (!mCheckerModules.get(module).containsKey(property)) {
            mCheckerModules.get(module).put(property,
                    setting.getmCheckerModules().get(module).get(property));
          } else {
            if (!mCheckerModules.get(module).get(property)
                    .equals(setting.getmCheckerModules().get(module).get(property))) {
            }
          }
        }
      }
    }

    // add treewalker-modules
    modit = setting.getmTreeWalkerModules().keySet().iterator();
    while (modit.hasNext()) {
      module = modit.next();
      if (!mTreeWalkerModules.keySet().contains(module)) {
        mTreeWalkerModules.put(module, setting.getmTreeWalkerModules().get(module));
      } else {
        final Iterator<String> propit = setting.getmTreeWalkerModules().get(module).keySet()
                .iterator();
        String property;
        while (propit.hasNext()) {
          property = propit.next();
          if (!mTreeWalkerModules.get(module).containsKey(property)) {
            mTreeWalkerModules.get(module).put(property,
                    setting.getmTreeWalkerModules().get(module).get(property));
          } else {
            if (!mTreeWalkerModules.get(module).get(property)
                    .equals(setting.getmTreeWalkerModules().get(module).get(property))) {
            }
          }
        }
      }
    }
  }
}
