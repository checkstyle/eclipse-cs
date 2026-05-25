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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ConfigurationWriter;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.configtypes.ExternalFileConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.InternalConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

/**
 * Implementation of a location editor to input a remote location. Contains just
 * a text field to input the URL.
 *
 */
public class InternalConfigurationEditor implements ICheckConfigurationEditor {

  //
  // attributes
  //

  /** The properties dialog. */
  private CheckConfigurationPropertiesDialog mDialog;

  /** the working copy this editor edits. */
  private CheckConfigurationWorkingCopy mWorkingCopy;

  /** the text field containing the config name. */
  private Text mConfigName;

  /** text field containing the location. */
  private Text mLocation;

  /** the text containing the description. */
  private Text mDescription;

  /** button to import an existing configuration. */
  private Button mBtnImport;

  //
  // methods
  //

  @Override
  public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
          CheckConfigurationPropertiesDialog dialog) {
    mWorkingCopy = checkConfiguration;
    mDialog = dialog;
  }

  @Override
  public Control createEditorControl(Composite parent, final Shell shell) {
    Composite contents = new Composite(parent, SWT.NULL);
    contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(contents);

    Label lblConfigName = new Label(contents, SWT.NULL);
    lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
    lblConfigName.setLayoutData(new GridData());

    mConfigName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mConfigName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(contents, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(lblConfigLocation);

    mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    mLocation.setEditable(false);
    mLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).hint(300, 100).grab(true, true).applyTo(mDescription);

    mBtnImport = new Button(contents, SWT.PUSH);
    mBtnImport.setText(Messages.InternalConfigurationEditor_btnImport);
    GridDataFactory.swtDefaults().span(2, 1).align(GridData.END, GridData.CENTER).applyTo(mBtnImport);

    mBtnImport.addSelectionListener(SelectionListener.widgetSelectedAdapter(
            event -> promptImportConfigFile(mConfigName.getShell()).ifPresent(configFileString -> {
              ICheckConfiguration tmpSourceConfig = new CheckConfiguration("dummy",
                      configFileString, null, new ExternalFileConfigurationType(), true, null,
                      null);
              try {
                tmpSourceConfig.copyConfiguration(getEditedWorkingCopy());
              } catch (CheckstylePluginException ex) {
                mDialog.setErrorMessage(ex.getLocalizedMessage());
              }
            })));

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

  private static Optional<String> promptImportConfigFile(Shell shell) {
    FileDialog fileDialog = new FileDialog(shell);
    fileDialog.setText(Messages.InternalConfigurationEditor_titleImportDialog);
    fileDialog.setFilterExtensions(new String[] {
        "*.xml",
        "*.*",
    });
    String configFileString = fileDialog.open();
    if (configFileString != null && new File(configFileString).exists()) {
      return Optional.of(configFileString);
    }
    return Optional.empty();
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
    mWorkingCopy.setName(mConfigName.getText());

    if (mWorkingCopy.getLocation() == null) {

      String location = "internal_config_" + System.currentTimeMillis() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
      try {
        mWorkingCopy.setLocation(location);
      } catch (CheckstylePluginException ex) {
        if (StringUtils.isNotBlank(location) && ensureFileExists(location)) {
          mWorkingCopy.setLocation(location);
        } else {
          throw ex;
        }
      }
    }
    mWorkingCopy.setDescription(mDescription.getText());

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

    String resolvedLocation = InternalConfigurationType.resolveLocationInWorkspace(location);

    File file = new File(resolvedLocation);
    if (!file.exists()) {

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

    return true;
  }
}
