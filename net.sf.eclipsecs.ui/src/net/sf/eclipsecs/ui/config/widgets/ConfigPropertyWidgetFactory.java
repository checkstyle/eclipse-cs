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

package net.sf.eclipsecs.ui.config.widgets;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyType;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Create <code>ConfigPropertyWidget</code> instances based on provided
 * metadata.
 */
public final class ConfigPropertyWidgetFactory {

  private ConfigPropertyWidgetFactory() {
  }

  /**
   * Creates a property widget for the given property.
   *
   * @param parent
   *          the parent component
   * @param prop
   *          the property
   * @param shell
   *          the parent shell
   * @return the widget or <code>null</code> if the property type is unknown
   */
  public static IConfigPropertyWidget createWidget(Composite parent, ConfigProperty prop,
          Shell shell) {
    IConfigPropertyWidget widget = null;

    ConfigPropertyType type = prop.getMetaData().getDatatype();

    if (prop.isPropertyReference()) {
      widget = new ConfigPropertyWidgetString(parent, prop);
    } else {
      widget = getWidgetForConfigPropertyType(parent, prop, type);
    }

    widget.initialize();
    return widget;
  }

  private static IConfigPropertyWidget getWidgetForConfigPropertyType(Composite parent,
          ConfigProperty prop, ConfigPropertyType type) {
    switch (type) {
    case String:
      return new ConfigPropertyWidgetString(parent, prop);
    case StringArray:
      return new ConfigPropertyWidgetStringArray(parent, prop);
    case Integer:
      return new ConfigPropertyWidgetInteger(parent, prop);
    case SingleSelect:
      return new ConfigPropertyWidgetSingleSelect(parent, prop);
    case Boolean:
      return new ConfigPropertyWidgetBoolean(parent, prop);
    case MultiCheck:
      return new ConfigPropertyWidgetMultiCheck(parent, prop);
    case Hidden:
      return new ConfigPropertyWidgetHidden(parent, prop);
    case File:
      return new ConfigPropertyWidgetFile(parent, prop);
    case Regex:
      return new ConfigPropertyWidgetRegex(parent, prop);
    default:
      return new ConfigPropertyWidgetString(parent, prop);
    }
  }
}
