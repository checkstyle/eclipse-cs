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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for a filter editor.
 * 
 * @author Lars Ködderitzsch
 */
public interface IFilterEditor {

  /**
   * Opens the filter editor dialog (blocking).
   * 
   * @param parent
   *          the parent shell
   * @return the returncode
   */
  int openEditor(Shell parent);

  /**
   * Sets the input for this filter editor.
   * 
   * @param input
   *          the input
   */
  void setInputProject(IProject input);

  /**
   * Sets the actual filter data for the editor.
   * 
   * @param filterData
   *          the actual filter data
   */
  void setFilterData(List<String> filterData);

  /**
   * Gets the filter data from the editor.
   * 
   * @return the edited filter data
   */
  List<String> getFilterData();

}
