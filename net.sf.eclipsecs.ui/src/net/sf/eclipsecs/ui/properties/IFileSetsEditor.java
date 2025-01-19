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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Interface for the part of the checkstyle plugin properties page that
 * configures file sets for the project.
 *
 */
public interface IFileSetsEditor {
  /**
   * Creates the contents of the file set editor.
   *
   * @param parent
   *          the parent component
   * @return the control
   * @throws CheckstylePluginException
   *           error while creating and initializing the control
   */
  Control createContents(Composite parent) throws CheckstylePluginException;

  /**
   * Set the file sets for the editor.
   *
   * @param fileSets
   *          the list of file sets
   * @throws CheckstylePluginException
   *           an unexpected exception occurred
   */
  void setFileSets(List<FileSet> fileSets) throws CheckstylePluginException;

  /**
   * Returns the file sets.
   *
   * @return the list of file sets
   */
  List<FileSet> getFileSets();

  /**
   * Refreshes the view.
   */
  void refresh();
}
