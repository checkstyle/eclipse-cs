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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

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

  private InternalConfigurationEditorView editorView;

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
    this.editorView = new InternalConfigurationEditorView(parent, SWT.NULL,
            () -> importConfig(shell));
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(this.editorView);

    if (mWorkingCopy.getName() != null) {
      editorView.setConfigName(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      editorView.setConfigLocation(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      editorView.setDescription(mWorkingCopy.getDescription());
    }

    return editorView;
  }

  private void importConfig(Shell shell) {
    promptImportConfigFile(shell).ifPresent(configFileString -> {
      ICheckConfiguration tmpSourceConfig = new CheckConfiguration("dummy",
              configFileString, null, new ExternalFileConfigurationType(), true, null,
              null);
      try {
        tmpSourceConfig.copyConfiguration(getEditedWorkingCopy());
      } catch (CheckstylePluginException ex) {
        mDialog.setErrorMessage(ex.getLocalizedMessage());
      }
    });
  }

  private static Optional<String> promptImportConfigFile(Shell shell) {
    FileDialog fileDialog = new FileDialog(shell);
    fileDialog.setText(Messages.InternalConfigurationEditor_titleImportDialog);
    fileDialog.setFilterExtensions(new String[] {
        "*.xml",
        "*.*",
    });
    Optional<String> configFile = Optional.empty();
    String configFileString = fileDialog.open();
    if (configFileString != null && new File(configFileString).exists()) {
      configFile = Optional.of(configFileString);
    }
    return configFile;
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
    mWorkingCopy.setName(editorView.getConfigName());

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
    mWorkingCopy.setDescription(editorView.getDescription());

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
    Path resolvedLocation = InternalConfigurationType.resolveLocationInWorkspace(location);

    if (!Files.exists(resolvedLocation)) {
      if (resolvedLocation.getParent() != null) {
        try {
          Files.createDirectories(resolvedLocation.getParent());
          try (OutputStream out = new BufferedOutputStream(
                  Files.newOutputStream(resolvedLocation))) {
            ConfigurationWriter.writeNewConfiguration(out, mWorkingCopy);
          }
        } catch (IOException ex) {
          CheckstylePluginException.rethrow(ex);
        }
      }
    }

    return true;
  }
}
