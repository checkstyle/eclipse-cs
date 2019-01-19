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

package net.sf.eclipsecs.core.config.configtypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Register for the configuration types thats use the <i>net.sf.eclipsecs.core.configurationtypes
 * </i> extension point.
 *
 * @author Lars Ködderitzsch
 */
public final class ConfigurationTypes {

  /** constant for the extension point id. */
  private static final String CONFIGTYPES_EXTENSION_POINT = CheckstylePlugin.PLUGIN_ID
          + ".configurationtypes"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_NAME = "name"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_INTERNAL_NAME = "internal-name"; //$NON-NLS-1$

  /** constant for the class attribute. */
  private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

  /** constant for the creatable attribute. */
  private static final String ATTR_CREATABLE = "creatable"; //$NON-NLS-1$

  /** constant for the creatable attribute. */
  private static final String ATTR_EDITABLE = "editable"; //$NON-NLS-1$

  /** constant for the creatable attribute. */
  private static final String ATTR_CONFIGURABLE = "configurable"; //$NON-NLS-1$

  /** the configuration types configured to the extension point. */
  private static final Map<String, IConfigurationType> CONFIGURATION_TYPES;

  /**
   * Initialize the configured to the filter extension point.
   */
  static {

    CONFIGURATION_TYPES = new LinkedHashMap<>();

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(CONFIGTYPES_EXTENSION_POINT);

    for (int i = 0; i < elements.length; i++) {

      try {

        String name = elements[i].getAttribute(ATTR_NAME);
        String internalName = elements[i].getAttribute(ATTR_INTERNAL_NAME);

        String definingPluginId = elements[i].getDeclaringExtension().getNamespaceIdentifier();
        boolean isCreatable = Boolean.valueOf(elements[i].getAttribute(ATTR_CREATABLE))
                .booleanValue();
        boolean isEditable = Boolean.valueOf(elements[i].getAttribute(ATTR_EDITABLE))
                .booleanValue();
        boolean isConfigurable = Boolean.valueOf(elements[i].getAttribute(ATTR_CONFIGURABLE))
                .booleanValue();

        IConfigurationType configType = (IConfigurationType) elements[i]
                .createExecutableExtension(ATTR_CLASS);
        configType.initialize(name, internalName, definingPluginId, isCreatable, isEditable,
                isConfigurable);

        CONFIGURATION_TYPES.put(internalName, configType);
      } catch (Exception e) {
        CheckstyleLog.log(e);
      }
    }
  }

  /** Hidden default constructor. */
  private ConfigurationTypes() {
    // NOOP
  }

  /**
   * Returns the available configuration types.
   *
   * @return the configuration types.
   */
  public static IConfigurationType[] getCreatableConfigTypes() {
    List<IConfigurationType> creatableTypes = new ArrayList<>();

    for (IConfigurationType type : CONFIGURATION_TYPES.values()) {
      if (type.isCreatable()) {
        creatableTypes.add(type);
      }
    }

    return creatableTypes.toArray(new IConfigurationType[creatableTypes.size()]);
  }

  /**
   * Returns the available configuration types that can be configured, mean the configuration file
   * can by principle be written as it lays in the local filesystem.
   *
   * @return the configurable configuration types.
   */
  public static IConfigurationType[] getConfigurableConfigTypes() {

    List<IConfigurationType> configurableTypes = new ArrayList<>();
    configurableTypes.addAll(Arrays.asList(getCreatableConfigTypes()));

    IConfigurationType remoteType = getByInternalName("remote"); //$NON-NLS-1$
    configurableTypes.remove(remoteType);

    return configurableTypes.toArray(new IConfigurationType[configurableTypes.size()]);
  }

  /**
   * Gets the configuration type by its internal name.
   *
   * @param name
   *          the configuration type internal name
   * @return the configuration type or <code>null</code>
   */
  public static IConfigurationType getByInternalName(String name) {
    return CONFIGURATION_TYPES.get(name);
  }

  /**
   * Gets the configuration type by its name.
   *
   * @param name
   *          the configuration type name
   * @return the configuration type or <code>null</code>
   */
  public static IConfigurationType getByName(String name) {

    for (IConfigurationType type : CONFIGURATION_TYPES.values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    return null;
  }
}
