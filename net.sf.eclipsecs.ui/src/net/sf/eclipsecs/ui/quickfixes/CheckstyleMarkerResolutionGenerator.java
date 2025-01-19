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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;

/**
 * Provides marker resolutions (quickfixes) for Checkstyle markers.
 *
 */
public class CheckstyleMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    return CheckstyleQuickfixes.getInstance().getQuickfixes().stream()
            .filter(fix -> fix.canFix(marker))
            .toArray(IMarkerResolution[]::new);
  }

  @Override
  public boolean hasResolutions(IMarker marker) {
    if (!isCheckstyleMarker(marker)) {
      return false;
    }
    return CheckstyleQuickfixes.getInstance().getQuickfixes().stream()
            .anyMatch(fix -> fix.canFix(marker));
  }

  /**
   * @return {@code true} if this is a checkstyle marker
   */
  private boolean isCheckstyleMarker(IMarker marker) {
    try {
      return CheckstyleMarker.MARKER_ID.equals(marker.getType());
    } catch (CoreException ex) {
      return false;
    }
  }


}
