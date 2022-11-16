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

import java.util.function.Supplier;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Manages and caches images for the plugin.
 *
 * @author Lars Ködderitzsch
 */
public enum CheckstyleUIPluginImages {

  /** Image descriptor for the plugin logo. */
  PLUGIN_LOGO(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/eclipse-cs-little.png")),
  /** Image descriptor for the error marker. */
  MARKER_ERROR(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/checkstyle_error.gif")),
  /** Image descriptor for the warning marker. */
  MARKER_WARNING(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/checkstyle_warning.gif")),
  /** Image descriptor for the info marker. */
  MARKER_INFO(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/checkstyle_info.gif")),
  /** Image descriptor for the help icon. */
  HELP_ICON(() -> PlatformUI.getWorkbench().getSharedImages()
          .getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP)),
  /** Image descriptor for the add correction icon. */
  CORRECTION_ADD(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/add_correction.gif")),
  /** Image descriptor for the change correction icon. */
  CORRECTION_CHANGE(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/correction_change.gif")),
  /** Image descriptor for the remove correction icon. */
  CORRECTION_REMOVE(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/remove_correction.gif")),
  /** Image descriptor for the tick icon. */
  TICK_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/tick.gif")),

  /** Image descriptor for the filter icon. */
  FILTER_ICON(() -> ResourceLocator.imageDescriptorFromBundle("org.eclipse.ui.ide",
          "platform:/plugin/org.eclipse.ui.ide/icons/full/elcl16/filter_ps.png")
          .orElse(MARKER_ERROR.getImageDescriptor())),
  /** Image descriptor for the Checkstyle violation view icon. */
  LIST_VIEW_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/listingView.gif")),
  /** Image descriptor for the graph view icon. */
  GRAPH_VIEW_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/graphView.gif")),
  /** Image descriptor for the graph view icon. */
  EXPORT_REPORT_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/exportReport.gif")),

  /** Image descriptor for the module group icon. */
  MODULEGROUP_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/modulegroup.gif")),
  /** Image descriptor for the ticked module group icon. */
  MODULEGROUP_TICKED_ICON(() -> AbstractUIPlugin
          .imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID, "icons/modulegroup_used.gif")),
  /** Image descriptor for the module icon. */
  MODULE_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/module.gif")),
  /** Image descriptor for the ticked module icon. */
  MODULE_TICKED_ICON(() -> AbstractUIPlugin.imageDescriptorFromPlugin(CheckstyleUIPlugin.PLUGIN_ID,
          "icons/module_used.gif")),
  /** Image descriptor for the refresh icon. */
  REFRESH_ICON(() -> ResourceLocator.imageDescriptorFromBundle("org.eclipse.search",
          "platform:/plugin/org.eclipse.search/icons/full/elcl16/refresh.png")
          .orElse(MARKER_ERROR.getImageDescriptor()));

  /**
   * lazy creation factory
   */
  private Supplier<ImageDescriptor> factory;

  /**
   * image descriptor
   */
  private ImageDescriptor imageDescriptor;

  /**
   * image that got created from the descriptor
   */
  private Image image;

  private CheckstyleUIPluginImages(Supplier<ImageDescriptor> factory) {
    this.factory = factory;
  }

  public ImageDescriptor getImageDescriptor() {
    if (imageDescriptor == null) {
      imageDescriptor = factory.get();
    }
    return imageDescriptor;
  }

  /**
   * Gets an image from a given descriptor.
   *
   * @return the image
   */
  public Image getImage() {
    if (image == null) {
      image = getImageDescriptor().createImage();
    }
    return image;
  }

  /**
   * Disposes the cached images.
   */
  public static void clearCachedImages() {
    for (CheckstyleUIPluginImages value : values()) {
      if (value.image != null) {
        value.image.dispose();
      }
    }
  }
}
