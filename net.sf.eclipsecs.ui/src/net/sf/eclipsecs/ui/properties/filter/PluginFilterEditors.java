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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Register for the filter editors thats use the
 * <i>net.sf.eclipsecs.ui.filtereditors </i> extension point.
 *
 */
public final class PluginFilterEditors {

  /** constant for the extension point id. */
  private static final String FILTER_EXTENSION_POINT = "net.sf.eclipsecs.ui.filtereditors"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_FILTER = "filter"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

  /** the filter prototypes configured to the extension point. */
  private static Map<String, Class<? extends IFilterEditor>> sFilterEditorClasses;

  /**
   * Initialize the configured to the filter extension point.
   */
  static {

    sFilterEditorClasses = new HashMap<>();

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(FILTER_EXTENSION_POINT);

    for (int i = 0; i < elements.length; i++) {

      try {

        String filter = elements[i].getAttribute(ATTR_FILTER);

        IFilterEditor editor = (IFilterEditor) elements[i].createExecutableExtension(ATTR_CLASS);
        sFilterEditorClasses.put(filter, editor.getClass());
      } catch (Exception ex) {
        CheckstyleLog.log(ex);
      }
    }
  }

  /** Hidden default constructor. */
  private PluginFilterEditors() {
    // NOOP
  }

  /**
   * Determines if a given filter has an editor.
   *
   * @param filter
   *          the filter
   * @return <code>true</code> if the filter has an editor, <code>false</code>
   *         otherwise.
   */
  public static boolean hasEditor(IFilter filter) {
    return sFilterEditorClasses.containsKey(filter.getInternalName());
  }

  /**
   * Creates the filter editor for a given filter.
   *
   * @param filter
   *          the filter
   * @return the filter editor
   * @throws CheckstylePluginException
   *           if the filter editor could not be instantiated.
   */
  public static IFilterEditor getNewEditor(IFilter filter) throws CheckstylePluginException {

    Class<? extends IFilterEditor> editorClass = sFilterEditorClasses.get(filter.getInternalName());

    if (editorClass != null) {

      try {
        return editorClass.getDeclaredConstructor().newInstance();
      } catch (ClassCastException | ReflectiveOperationException | SecurityException ex) {
        CheckstylePluginException.rethrow(ex);
      }
    }

    return null;
  }
}
