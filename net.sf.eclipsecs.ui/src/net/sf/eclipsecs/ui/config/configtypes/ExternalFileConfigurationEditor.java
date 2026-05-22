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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ConfigurationWriter;
import net.sf.eclipsecs.core.config.configtypes.ExternalFileConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

/**
 * Implementation of a file based location editor. Contains a text field with
 * the config file path and a 'Browse...' button opening a file dialog.
 *
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
    GridLayoutFactory gridLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0);
    gridLayoutFactory.applyTo(contents);

    Label lblConfigName = new Label(contents, SWT.NULL);
    lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
    lblConfigName.setLayoutData(new GridData());

    mConfigName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mConfigName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(contents, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    lblConfigLocation.setLayoutData(new GridData());

    Composite locationComposite = new Composite(contents, SWT.NULL);
    locationComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    gridLayoutFactory.applyTo(locationComposite);

    mLocation = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBtnBrowse = new Button(locationComposite, SWT.PUSH);
    mBtnBrowse.setText(Messages.FileConfigurationLocationEditor_btnBrowse);
    mBtnBrowse.setLayoutData(new GridData());

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
    GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).hint(300, 100).grab(true, true).applyTo(mDescription);

    Group advancedGroup = new Group(contents, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
    advancedGroup.setLayout(new GridLayout(2, false));

    mChkProtectConfig = new Button(advancedGroup, SWT.CHECK);
    mChkProtectConfig.setText(Messages.ExternalFileConfigurationEditor_btnProtectConfigFile);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(mChkProtectConfig);

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
            Boolean.toString(mChkProtectConfig.getSelection()));

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
