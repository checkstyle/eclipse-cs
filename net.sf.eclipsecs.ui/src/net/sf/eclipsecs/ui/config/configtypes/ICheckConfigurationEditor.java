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

package net.sf.eclipsecs.ui.config.configtypes;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for the check configuration type specific location editor.
 *
 * @author Lars Ködderitzsch
 */
public interface ICheckConfigurationEditor {

  /**
   * Initializes the configuration editor with its properties.
   *
   * @param checkConfiguration
   *          the working copy
   * @param dialog
   *          the dialog
   */
  void initialize(CheckConfigurationWorkingCopy checkConfiguration,
          CheckConfigurationPropertiesDialog dialog);

  /**
   * Create the editor control. This method is always called after the
   * initialize method.
   *
   * @param parent
   *          the parent composite
   * @param shell
   *          the parent shell
   * @return the location editor control
   */
  Control createEditorControl(Composite parent, Shell shell);

  /**
   * Returns the edited working copy.
   *
   * @return the edited working copy
   * @throws CheckstylePluginException
   *           a validation error occurred when setting the editor data to the
   *           working copy
   */
  CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException;
}
