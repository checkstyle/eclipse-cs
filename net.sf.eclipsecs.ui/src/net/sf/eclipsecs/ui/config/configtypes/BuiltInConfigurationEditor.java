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

package net.sf.eclipsecs.ui.config.configtypes;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Implementation of a location editor with only a not editable text field. This
 * is used to just show the location.
 *
 * @author Lars Ködderitzsch
 */
public class BuiltInConfigurationEditor implements ICheckConfigurationEditor {

  //
  // attributes
  //

  /** the working copy this editor edits. */
  private CheckConfigurationWorkingCopy mWorkingCopy;

  /** the text field containing the config name. */
  private Text mConfigName;

  /** text field containing the location. */
  private Text mLocation;

  /** the text containing the description. */
  private Text mDescription;

  //
  // methods
  //

  @Override
  public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
          CheckConfigurationPropertiesDialog dialog) {
    mWorkingCopy = checkConfiguration;
  }

  @Override
  public Control createEditorControl(Composite parent, final Shell shell) {

    Composite contents = new Composite(parent, SWT.NULL);
    contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    contents.setLayout(layout);

    Label lblConfigName = new Label(contents, SWT.NULL);
    lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
    GridData gridData = new GridData();
    lblConfigName.setLayoutData(gridData);

    mConfigName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mConfigName.setEditable(false);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mConfigName.setLayoutData(gridData);
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(contents, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    gridData = new GridData();
    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    lblConfigLocation.setLayoutData(gridData);

    mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mLocation.setLayoutData(gridData);
    mLocation.setEditable(false);

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    lblDescription.setLayoutData(gridData);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    mDescription.setEditable(false);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;
    gridData.widthHint = 300;
    gridData.heightHint = 100;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    mDescription.setLayoutData(gridData);

    if (mWorkingCopy.getName() != null) {
      mConfigName.setText(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      mLocation.setText(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      mDescription.setText(mWorkingCopy.getDescription());
    }

    return contents;
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
    return mWorkingCopy;
  }

}
