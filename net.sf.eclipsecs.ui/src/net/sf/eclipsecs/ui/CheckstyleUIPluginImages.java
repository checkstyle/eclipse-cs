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

package net.sf.eclipsecs.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Manages and caches images for the plugin.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class CheckstyleUIPluginImages {

  /** Image descriptor for the plugin logo. */
  public static final ImageDescriptor PLUGIN_LOGO;

  /** Image descriptor for the error marker. */
  public static final ImageDescriptor MARKER_ERROR;

  /** Image descriptor for the warning marker. */
  public static final ImageDescriptor MARKER_WARNING;

  /** Image descriptor for the info marker. */
  public static final ImageDescriptor MARKER_INFO;

  /** Image descriptor for the help icon. */
  public static final ImageDescriptor HELP_ICON;

  /** Image descriptor for the add correction icon. */
  public static final ImageDescriptor CORRECTION_ADD;

  /** Image descriptor for the change correction icon. */
  public static final ImageDescriptor CORRECTION_CHANGE;

  /** Image descriptor for the remove correction icon. */
  public static final ImageDescriptor CORRECTION_REMOVE;

  /** Image descriptor for the tick icon. */
  public static final ImageDescriptor TICK_ICON;

  /** Image descriptor for the filter icon. */
  public static final ImageDescriptor FILTER_ICON;

  /** Image descriptor for the Checkstyle violation view icon. */
  public static final ImageDescriptor LIST_VIEW_ICON;

  /** Image descriptor for the graph view icon. */
  public static final ImageDescriptor GRAPH_VIEW_ICON;

  /** Image descriptor for the graph view icon. */
  public static final ImageDescriptor EXPORT_REPORT_ICON;

  /** Image descriptor for the module group icon. */
  public static final ImageDescriptor MODULEGROUP_ICON;

  /** Image descriptor for the ticked module group icon. */
  public static final ImageDescriptor MODULEGROUP_TICKED_ICON;

  /** Image descriptor for the module icon. */
  public static final ImageDescriptor MODULE_ICON;

  /** Image descriptor for the ticked module icon. */
  public static final ImageDescriptor MODULE_TICKED_ICON;

  /** Image descriptor for the refresh icon. */
  public static final ImageDescriptor REFRESH_ICON;

  /** Image cache. */
  private static final Map<ImageDescriptor, Image> CACHED_IMAGES = new HashMap<>();

  static {

    PLUGIN_LOGO = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/eclipse-cs-little.png"); //$NON-NLS-1$
    MARKER_ERROR = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/checkstyle_error.gif"); //$NON-NLS-1$
    MARKER_WARNING = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/checkstyle_warning.gif"); //$NON-NLS-1$
    MARKER_INFO = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/checkstyle_info.gif"); //$NON-NLS-1$
    HELP_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/help.gif"); //$NON-NLS-1$
    CORRECTION_ADD = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/add_correction.gif"); //$NON-NLS-1$
    CORRECTION_CHANGE = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/correction_change.gif"); //$NON-NLS-1$
    CORRECTION_REMOVE = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/remove_correction.gif"); //$NON-NLS-1$
    TICK_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/tick.gif"); //$NON-NLS-1$

    FILTER_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/filter_16.gif"); //$NON-NLS-1$
    LIST_VIEW_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/listingView.gif"); //$NON-NLS-1$
    GRAPH_VIEW_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/graphView.gif"); //$NON-NLS-1$
    EXPORT_REPORT_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/exportReport.gif"); //$NON-NLS-1$

    MODULEGROUP_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/modulegroup.gif"); //$NON-NLS-1$
    MODULEGROUP_TICKED_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID, "icons/modulegroup_used.gif"); //$NON-NLS-1$
    MODULE_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/module.gif"); //$NON-NLS-1$
    MODULE_TICKED_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/module_used.gif"); //$NON-NLS-1$

    REFRESH_ICON = AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
            "icons/refresh.gif"); //$NON-NLS-1$
  }

  /**
   * Hidden default constructor.
   */
  private CheckstyleUIPluginImages() {
    // NOOP
  }

  /**
   * Gets an image from a given descriptor.
   * 
   * @param descriptor
   *          the descriptor
   * @return the image
   */
  public static Image getImage(ImageDescriptor descriptor) {

    Image image = CACHED_IMAGES.get(descriptor);
    if (image == null) {
      image = descriptor.createImage();
      CACHED_IMAGES.put(descriptor, image);
    }
    return image;
  }

  /**
   * Disposes the cached images and clears the cache.
   */
  public static void clearCachedImages() {

    for (Image image : CACHED_IMAGES.values()) {
      image.dispose();
    }

    CACHED_IMAGES.clear();
  }
}
