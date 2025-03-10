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

package net.sf.eclipsecs.ui.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution2;

/**
 * Interface for a quickfix implementation for checkstyle markers.
 *
 */
public interface ICheckstyleMarkerResolution extends IMarkerResolution2 {

  /**
   * Checks if this quickfix can actually fix the marker occurrence.
   *
   * @param marker
   *          the marker to potentially be fixed.
   * @return <code>true</code> if this quickfix can fix the marker,
   *         <code>false</code> otherwise.
   */
  boolean canFix(IMarker marker);

  /**
   * @param module id of the checkstyle module
   */
  void setModule(String module);

}
