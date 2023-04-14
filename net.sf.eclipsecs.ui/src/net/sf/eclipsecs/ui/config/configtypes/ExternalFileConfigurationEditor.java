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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ConfigurationWriter;
import net.sf.eclipsecs.core.config.configtypes.ExternalFileConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Implementation of a file based location editor. Contains a text field with
 * the config file path and a 'Browse...' button opening a file dialog.
 *
 * @author Lars Ködderitzsch
 */
public class ExternalFileConfigurationEditor implements ICheckConfigurationEditor {

  //
  // attributes
  //

  /** the working copy this editor edits. */
  private CheckConfigurationWorkingCopy mWorkingCopy;

  /** the text field containing the config name. */
  private Text mConfigName;

  /** text field containing the location. */
  private Text mLocation;

  /** browse button. */
  private Button mBtnBrowse;

  /** the text containing the description. */
  private Text mDescription;

  /**
   * check box to set if the configuration file is not editable by the
   * configuration editor.
   */
  private Button mChkProtectConfig;

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
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mConfigName.setLayoutData(gridData);
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(contents, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    gridData = new GridData();
    lblConfigLocation.setLayoutData(gridData);

    Composite locationComposite = new Composite(contents, SWT.NULL);
    locationComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    locationComposite.setLayout(layout);

    mLocation = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mLocation.setLayoutData(gridData);

    mBtnBrowse = new Button(locationComposite, SWT.PUSH);
    mBtnBrowse.setText(Messages.FileConfigurationLocationEditor_btnBrowse);
    gridData = new GridData();
    mBtnBrowse.setLayoutData(gridData);

    mBtnBrowse.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        FileDialog fileDialog = new FileDialog(shell);
        fileDialog.setFileName(mLocation.getText());

        String file = fileDialog.open();
        if (file != null) {
          mLocation.setText(file);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        // NOOP
      }
    });

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    lblDescription.setLayoutData(gridData);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;
    gridData.widthHint = 300;
    gridData.heightHint = 100;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    mDescription.setLayoutData(gridData);

    Group advancedGroup = new Group(contents, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    advancedGroup.setLayoutData(gridData);
    advancedGroup.setLayout(new GridLayout(2, false));

    mChkProtectConfig = new Button(advancedGroup, SWT.CHECK);
    mChkProtectConfig.setText(Messages.ExternalFileConfigurationEditor_btnProtectConfigFile);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    mChkProtectConfig.setLayoutData(gridData);

    if (mWorkingCopy.getName() != null) {
      mConfigName.setText(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      mLocation.setText(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      mDescription.setText(mWorkingCopy.getDescription());
    }

    mChkProtectConfig.setSelection(Boolean.parseBoolean(mWorkingCopy.getAdditionalData().get(ExternalFileConfigurationType.KEY_PROTECT_CONFIG)));

    return contents;
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {

    mWorkingCopy.setName(mConfigName.getText());
    mWorkingCopy.setDescription(mDescription.getText());
    mWorkingCopy.getAdditionalData().put(ExternalFileConfigurationType.KEY_PROTECT_CONFIG,
            "" + mChkProtectConfig.getSelection()); //$NON-NLS-1$

    try {
      mWorkingCopy.setLocation(mLocation.getText());
    } catch (CheckstylePluginException ex) {
      String location = mLocation.getText();

      if (StringUtils.isNotBlank(location) && ensureFileExists(location)) {
        mWorkingCopy.setLocation(mLocation.getText());
      } else {
        throw ex;
      }
    }

    return mWorkingCopy;
  }

  /**
   * Helper method trying to ensure that the file location provided by the user
   * exists. If that is not the case it prompts the user if an empty
   * configuration file should be created.
   *
   * @param location
   *          the configuration file location
   * @throws CheckstylePluginException
   *           error when trying to ensure the location file existance
   */
  private boolean ensureFileExists(String location) throws CheckstylePluginException {

    // support dynamic location strings
    String resolvedLocation = ExternalFileConfigurationType.resolveDynamicLocation(location);

    File file = new File(resolvedLocation);
    if (!file.exists()) {
      boolean confirm = MessageDialog.openQuestion(mBtnBrowse.getShell(),
              Messages.ExternalFileConfigurationEditor_titleFileDoesNotExist,
              Messages.ExternalFileConfigurationEditor_msgFileDoesNotExist);
      if (confirm) {

        if (file.getParentFile() != null) {
          file.getParentFile().mkdirs();
        }

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
          ConfigurationWriter.writeNewConfiguration(out, mWorkingCopy);
        } catch (IOException ioe) {
          CheckstylePluginException.rethrow(ioe);
        }
        return true;
      }
      return false;
    }

    return true;
  }
}
