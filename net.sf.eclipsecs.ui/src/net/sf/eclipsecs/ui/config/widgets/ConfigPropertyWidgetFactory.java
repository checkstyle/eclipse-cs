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

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyType;

/**
 * Create <code>ConfigPropertyWidget</code> instances based on provided
 * metadata.
 */
public final class ConfigPropertyWidgetFactory {

  private static final Map<ConfigPropertyType, ConfigPropertyWidgetBuilder> REGISTRY = Map
          .of(ConfigPropertyType.STRING, ConfigPropertyWidgetString::create,
          ConfigPropertyType.STRING_ARRAY, ConfigPropertyWidgetStringArray::create,
          ConfigPropertyType.INTEGER, ConfigPropertyWidgetInteger::create,
          ConfigPropertyType.SINGLE_SELECT, ConfigPropertyWidgetSingleSelect::create,
          ConfigPropertyType.BOOLEAN, ConfigPropertyWidgetBoolean::create,
          ConfigPropertyType.MULTI_CHECK, ConfigPropertyWidgetMultiCheck::create,
          ConfigPropertyType.HIDDEN, ConfigPropertyWidgetHidden::create,
          ConfigPropertyType.FILE, ConfigPropertyWidgetFile::create,
          ConfigPropertyType.REGEX, ConfigPropertyWidgetRegex::create);

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
  public static ConfigPropertyWidget createWidget(Composite parent, ConfigProperty prop,
          Shell shell) {
    ConfigPropertyWidget widget = null;

    ConfigPropertyType type = prop.getMetaData().getDatatype();

    if (prop.isPropertyReference()) {
      widget = ConfigPropertyWidgetString.create(parent, prop);
    } else {
      widget = getWidgetForConfigPropertyType(parent, prop, type);
    }

    widget.initialize();
    return widget;
  }

  private static ConfigPropertyWidget getWidgetForConfigPropertyType(Composite parent,
          ConfigProperty prop, ConfigPropertyType type) {
    return REGISTRY.getOrDefault(type, ConfigPropertyWidgetString::create).create(parent, prop);
  }

  public interface ConfigPropertyWidgetBuilder {
    ConfigPropertyWidget create(Composite parent, ConfigProperty prop);
  }
}
