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

package net.sf.eclipsecs.core.projectconfig.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * Filter that excludes all files that are not opened in an eclipse editor.
 * 
 * @author Lars Ködderitzsch
 */
public class UnOpenedFilesFilter extends AbstractFilter {

  private static List<IFile> sOpenedFiles = new ArrayList<>();

  /**
   * Registers a opened file.
   * 
   * @param file
   *          the file
   */
  public static void addOpenedFile(IFile file) {
    sOpenedFiles.add(file);
  }

  /**
   * Deregisters the opened file.
   * 
   * @param file
   *          the file
   */
  public static void removeOpenedFile(IFile file) {
    sOpenedFiles.remove(file);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(Object element) {

    if (element instanceof IFile) {

      return sOpenedFiles.contains(element);
    }
    return false;
  }
}