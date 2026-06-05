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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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

  private Shell shell;

  private ExternalFileConfigurationEditorView editorView;

  //
  // methods
  //

  @Override
  public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
          CheckConfigurationPropertiesDialog dialog) {
    mWorkingCopy = checkConfiguration;
  }

  @Override
  public Control createEditorControl(Composite parent, final Shell parentShell) {
    this.shell = parentShell;
    this.editorView = new ExternalFileConfigurationEditorView(parent, SWT.NULL);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(editorView);

    if (mWorkingCopy.getName() != null) {
      editorView.setConfigName(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      editorView.setConfigLocation(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      editorView.setDescription(mWorkingCopy.getDescription());
    }

    editorView.setProtectConfig(Boolean.parseBoolean(mWorkingCopy.getAdditionalData()
            .get(ExternalFileConfigurationType.KEY_PROTECT_CONFIG)));

    return editorView;
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
    mWorkingCopy.setName(editorView.getConfigName());
    mWorkingCopy.setDescription(editorView.getDescription());
    mWorkingCopy.getAdditionalData().put(ExternalFileConfigurationType.KEY_PROTECT_CONFIG,
            Boolean.toString(editorView.getProtectConfig()));

    try {
      mWorkingCopy.setLocation(editorView.getConfigLocation());
    } catch (CheckstylePluginException ex) {
      String locationText = editorView.getConfigLocation();

      if (StringUtils.isNotBlank(locationText) && ensureFileExists(locationText)) {
        mWorkingCopy.setLocation(locationText);
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
   * @param locationText
   *          the configuration file location
   * @throws CheckstylePluginException
   *           error when trying to ensure the location file existance
   */
  private boolean ensureFileExists(String locationText) throws CheckstylePluginException {
    // support dynamic location strings
    String resolvedLocation = ExternalFileConfigurationType.resolveDynamicLocation(locationText);

    boolean exists;
    File file = new File(resolvedLocation);
    if (!file.exists()) {
      boolean confirm = MessageDialog.openQuestion(shell,
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
        exists = true;
      } else {
        exists = false;
      }
    } else {
      exists = true;
    }
    return exists;
  }
}
