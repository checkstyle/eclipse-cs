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

package net.sf.eclipsecs.core.projectconfig.filters;

import org.eclipse.core.resources.IFile;

public class FilesOlderThanOneDayFilter extends AbstractFilter {

  private static final long MILLIS_IN_24_HOURS = 1000 * 60 * 60 * 24;

  @Override
  public boolean accept(Object o) {
    boolean goesThrough = true;

    if (o instanceof IFile) {
      IFile file = (IFile) o;
      if ((System.currentTimeMillis() - file.getLocalTimeStamp()) < MILLIS_IN_24_HOURS) {
        goesThrough = true;
      } else {
        goesThrough = false;
      }
    }

    return goesThrough;
  }

}
