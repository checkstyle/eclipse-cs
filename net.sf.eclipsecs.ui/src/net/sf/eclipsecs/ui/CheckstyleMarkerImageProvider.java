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

package net.sf.eclipsecs.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;

/**
 * Image provider for Checkstyle markers.
 *
 */
public class CheckstyleMarkerImageProvider implements IAnnotationImageProvider {

  @Override
  public Image getManagedImage(Annotation annotation) {
    String type = annotation.getType();
    if (CheckstyleMarker.ERROR_TYPE.equals(type)) {
      return CheckstyleUIPluginImages.MARKER_ERROR.getImage();
    } else if (CheckstyleMarker.WARNING_TYPE.equals(type)) {
      return CheckstyleUIPluginImages.MARKER_WARNING.getImage();
    } else if (CheckstyleMarker.INFO_TYPE.equals(type)) {
      return CheckstyleUIPluginImages.MARKER_INFO.getImage();
    }

    return null;
  }

  @Override
  public String getImageDescriptorId(Annotation annotation) {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor(String imageDescritporId) {
    return null;
  }
}
