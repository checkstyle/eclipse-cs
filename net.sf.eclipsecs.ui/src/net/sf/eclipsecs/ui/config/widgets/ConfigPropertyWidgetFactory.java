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
    } else if (type.equals(ConfigPropertyType.String)) {
      widget = new ConfigPropertyWidgetString(parent, prop);
    } else if (type.equals(ConfigPropertyType.StringArray)) {
      widget = new ConfigPropertyWidgetString(parent, prop);
    } else if (type.equals(ConfigPropertyType.Integer)) {
      widget = new ConfigPropertyWidgetInteger(parent, prop);
    } else if (type.equals(ConfigPropertyType.SingleSelect)) {
      widget = new ConfigPropertyWidgetSingleSelect(parent, prop);
    } else if (type.equals(ConfigPropertyType.Boolean)) {
      widget = new ConfigPropertyWidgetBoolean(parent, prop);
    } else if (type.equals(ConfigPropertyType.MultiCheck)) {
      widget = new ConfigPropertyWidgetMultiCheck(parent, prop);
    } else if (type.equals(ConfigPropertyType.Hidden)) {
      widget = new ConfigPropertyWidgetHidden(parent, prop);
    } else if (type.equals(ConfigPropertyType.File)) {
      widget = new ConfigPropertyWidgetFile(parent, prop);
    } else if (type.equals(ConfigPropertyType.Regex)) {
      widget = new ConfigPropertyWidgetRegex(parent, prop);
    }

    widget.initialize();
    return widget;
  }
}
