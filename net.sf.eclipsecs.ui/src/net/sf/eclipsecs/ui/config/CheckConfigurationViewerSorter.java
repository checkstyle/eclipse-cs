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

package net.sf.eclipsecs.ui.config;

import net.sf.eclipsecs.core.config.ICheckConfiguration;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Sorts CheckConfiguration objects into their display order.
 */
public class CheckConfigurationViewerSorter extends ViewerComparator {

  /**
   * {@inheritDoc}
   */
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    int result = 0;

    if ((e1 instanceof ICheckConfiguration) && (e2 instanceof ICheckConfiguration)) {
      ICheckConfiguration cfg1 = (ICheckConfiguration) e1;
      ICheckConfiguration cfg2 = (ICheckConfiguration) e2;

      String string1 = cfg1.getName();
      String string2 = cfg2.getName();

      result = string1.compareToIgnoreCase(string2);
    }

    return result;
  }
}
