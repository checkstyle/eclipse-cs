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

package net.sf.eclipsecs.ui.config.configtypes;

import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Register for the configuration types ui thats use the
 * <i>net.sf.eclipsecs.ui.configtypesui </i> extension point.
 * 
 * @author Lars Ködderitzsch
 */
public final class ConfigurationTypesUI {

  //
  // constants
  //

  /** constant for the extension point id. */
  private static final String CONFIGTYPES_EXTENSION_POINT = CheckstyleUIPlugin.PLUGIN_ID
          + ".configtypesui"; //$NON-NLS-1$

  /** constant for the name attribute. */
  private static final String ATTR_NAME = "configtypename"; //$NON-NLS-1$

  /** constant for the class attribute. */
  private static final String ATTR_CLASS = "editorclass"; //$NON-NLS-1$

  /** constant for the creatable attribute. */
  private static final String ATTR_ICON = "icon"; //$NON-NLS-1$

  /** the configuration types configured to the extension point. */
  private static final Map<String, Class<? extends ICheckConfigurationEditor>> CONFIGURATION_TYPE_EDITORS;

  /** Map of icon paths for the configuration types. */
  private static final Map<String, String> CONFIGATION_TYPE_ICONS;

  /**
   * Initialize the configured to the filter extension point.
   */
  static {

    CONFIGURATION_TYPE_EDITORS = new HashMap<>();
    CONFIGATION_TYPE_ICONS = new HashMap<>();

    IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

    IConfigurationElement[] elements = pluginRegistry
            .getConfigurationElementsFor(CONFIGTYPES_EXTENSION_POINT);

    for (int i = 0; i < elements.length; i++) {

      try {

        String internalName = elements[i].getAttribute(ATTR_NAME);

        ICheckConfigurationEditor editor = (ICheckConfigurationEditor) elements[i]
                .createExecutableExtension(ATTR_CLASS);

        String iconPath = elements[i].getAttribute(ATTR_ICON);

        CONFIGURATION_TYPE_EDITORS.put(internalName, editor.getClass());
        CONFIGATION_TYPE_ICONS.put(internalName, iconPath);
      } catch (Exception e) {
        CheckstyleLog.log(e);
      }
    }
  }

  /** Hidden default constructor. */
  private ConfigurationTypesUI() {
    // NOOP
  }

  /**
   * Creates the editor for a given configuration type.
   * 
   * @param configType
   *          the configuration type
   * @return the editor
   * @throws CheckstylePluginException
   *           if the filter editor could not be instantiated.
   */
  public static ICheckConfigurationEditor getNewEditor(IConfigurationType configType)
          throws CheckstylePluginException {

    Class<? extends ICheckConfigurationEditor> editorClass = CONFIGURATION_TYPE_EDITORS
            .get(configType.getInternalName());

    if (editorClass != null) {

      try {
        ICheckConfigurationEditor editor = editorClass.newInstance();
        return editor;
      } catch (InstantiationException e) {
        CheckstylePluginException.rethrow(e);
      } catch (IllegalAccessException e) {
        CheckstylePluginException.rethrow(e);
      } catch (ClassCastException e) {
        CheckstylePluginException.rethrow(e);
      }
    }

    return null;
  }

  /**
   * Return an image for the given configuration type.
   * 
   * @param configType
   *          the configuration type
   * @return the image representing the configuration type or <code>null</code>
   *         if no image is registered
   */
  public static Image getConfigurationTypeImage(IConfigurationType configType) {

    String iconPath = CONFIGATION_TYPE_ICONS.get(configType.getInternalName());

    if (iconPath != null) {
      ImageDescriptor descriptor = AbstractUIPlugin
              .imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID, iconPath);
      return CheckstyleUIPluginImages.getImage(descriptor);
    }
    return null;
  }
}
