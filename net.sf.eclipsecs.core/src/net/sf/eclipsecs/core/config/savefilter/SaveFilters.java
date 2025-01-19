//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.config.savefilter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Register for the filters thats use the <i>net.sf.eclipsecs.core.checkstyleFilter </i> extension
 * point. Checkstyle filters can be enabled per project.
 *
 */
public final class SaveFilters {

  /** constant for the extension point id. */
  private static final String FILTER_EXTENSION_POINT = CheckstylePlugin.PLUGIN_ID + ".saveFilters"; //$NON-NLS-1$

  /** constant for the class attribute. */
  private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

  /** the filter prototypes configured to the extension point. */
  private static final ISaveFilter[] SAVE_FILTERS;

  /**
   * Initialize the configured to the filter extension point.
   */
  static {

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(FILTER_EXTENSION_POINT);

    List<ISaveFilter> filters = new ArrayList<>();

    for (int i = 0; i < elements.length; i++) {

      try {

        ISaveFilter filter = (ISaveFilter) elements[i].createExecutableExtension(ATTR_CLASS);
        filters.add(filter);
      } catch (Exception ex) {
        CheckstyleLog.log(ex);
      }
    }

    SAVE_FILTERS = filters.toArray(new ISaveFilter[filters.size()]);
  }

  /** Hidden default constructor. */
  private SaveFilters() {
    // NOOP
  }

  /**
   * Passes the configured modules through the known save filters.
   *
   * @param configuredModules
   *          the configured modules of a configuration to be written
   */
  public static void process(List<Module> configuredModules) {
    for (int i = 0; i < SAVE_FILTERS.length; i++) {
      SAVE_FILTERS[i].postProcessConfiguredModules(configuredModules);
    }
  }
}
