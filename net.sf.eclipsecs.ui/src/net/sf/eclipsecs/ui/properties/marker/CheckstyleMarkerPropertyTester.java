//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.properties.marker;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Test whether a given {@link IMarker} shows a Checkstyle issue.
 */
public class CheckstyleMarkerPropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (!(receiver instanceof IMarker)) {
      return false;
    }
    IMarker marker = (IMarker) receiver;
    try {
      return CheckstyleMarker.MARKER_ID.equals(marker.getType());
    } catch (CoreException ex) {
      CheckstyleLog.log(ex);
    }

    return false;
  }

}
