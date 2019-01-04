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

package net.sf.eclipsecs.core.projectconfig;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Register for the filters thats use the <i>net.sf.eclipsecs.core.checkstyleFilter </i> extension
 * point. Checkstyle filters can be enabled per project.
 *
 * @author Lars Ködderitzsch
 */
public final class PluginFilters {

  /** constant for the extension point id. */
  private static final String FILTER_EXTENSION_POINT = "net.sf.eclipsecs.core.filters"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_NAME = "name"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_INTERNAL_NAME = "internal-name"; //$NON-NLS-1$

  /** constant for the description attribute. */
  private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$

  /** constant for the class attribute. */
  private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

  /** contant for the readonly attribute. */
  private static final String ATTR_READONLY = "readonly"; //$NON-NLS-1$

  /** constant for the selected attribute. */
  private static final String ATTR_SELECTED = "selected"; //$NON-NLS-1$

  /** constant for the value attribute. */
  private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

  /** constant for the data tag. */
  private static final String TAG_DATA = "data"; //$NON-NLS-1$

  /** the filter prototypes configured to the extension point. */
  private static final IFilter[] FILTER_PROTOTYPES;

  /**
   * Initialize the configured to the filter extension point.
   */
  static {

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(FILTER_EXTENSION_POINT);

    List<IFilter> filters = new ArrayList<>();

    for (int i = 0; i < elements.length; i++) {

      try {

        String name = elements[i].getAttribute(ATTR_NAME);
        String internalName = elements[i].getAttribute(ATTR_INTERNAL_NAME);
        String desc = elements[i].getAttribute(ATTR_DESCRIPTION);
        boolean readOnly = Boolean.valueOf(elements[i].getAttribute(ATTR_READONLY)).booleanValue();

        IFilter filter = (IFilter) elements[i].createExecutableExtension(ATTR_CLASS);
        filter.initialize(name, internalName, desc, readOnly);

        boolean defaultState = Boolean.valueOf(elements[i].getAttribute(ATTR_SELECTED))
                .booleanValue();

        filter.setEnabled(defaultState);

        // Load initial filter data
        List<String> data = new ArrayList<>();
        IConfigurationElement[] dataTags = elements[i].getChildren(TAG_DATA);
        int size = dataTags != null ? dataTags.length : 0;
        for (int j = 0; j < size; j++) {
          data.add(dataTags[j].getAttribute(ATTR_VALUE));
        }
        filter.setFilterData(data);

        filters.add(filter);
      } catch (Exception e) {
        CheckstyleLog.log(e);
      }
    }

    FILTER_PROTOTYPES = filters.toArray(new IFilter[filters.size()]);
  }

  /** Hidden default constructor. */
  private PluginFilters() {
    // NOOP
  }

  /**
   * Returns the available filters.
   *
   * @return the available filters.
   */
  public static IFilter[] getConfiguredFilters() {

    // Copy the prototypes for the client
    IFilter[] mFilter = new IFilter[FILTER_PROTOTYPES.length];

    // Clone and set the state of the filter
    for (int i = 0; i < mFilter.length; i++) {
      mFilter[i] = FILTER_PROTOTYPES[i].clone();
    }

    return mFilter;
  }

  /**
   * Gets a filter prototype by name.
   *
   * @param internalName
   *          the filters internal name
   * @return the filter prototype or <code>null</code>
   */
  public static IFilter getByInternalName(String internalName) {

    IFilter filter = null;

    for (int i = 0; i < FILTER_PROTOTYPES.length; i++) {
      if (FILTER_PROTOTYPES[i].getInternalName().equals(internalName)) {
        filter = FILTER_PROTOTYPES[i].clone();
        break;
      }
    }

    return filter;
  }
}
