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

import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.configtypes.RemoteConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.RemoteConfigurationType.RemoteConfigAuthenticator;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

/**
 * Implementation of a location editor to input a remote location. Contains just
 * a text field to input the URL.
 *
 */
public class RemoteConfigurationEditor implements ICheckConfigurationEditor {

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

  /** check box to set if the configuration should be cached. */
  private Button mChkCacheConfig;

  private Text mUserName;

  private Text mPassword;

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
    mLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    GridDataFactory.create(GridData.FILL_BOTH).span(2, 1).hint(300, 100).grab(true, true).applyTo(mDescription);

    createCredentialsGroup(contents);

    Group advancedGroup = new Group(contents, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
    advancedGroup.setLayout(new GridLayout(2, false));

    mChkCacheConfig = new Button(advancedGroup, SWT.CHECK);
    mChkCacheConfig.setText(Messages.RemoteConfigurationEditor_btnCacheRemoteConfig);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(mChkCacheConfig);

    init(shell);

    return contents;
  }

  private void createCredentialsGroup(Composite parent) {
    Group credentialsGroup = new Group(parent, SWT.NULL);
    credentialsGroup.setText(Messages.RemoteConfigurationEditor_titleCredentialsGroup);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(credentialsGroup);
    credentialsGroup.setLayout(new GridLayout(2, false));

    Label lblUserName = new Label(credentialsGroup, SWT.NULL);
    lblUserName.setText(Messages.RemoteConfigurationEditor_lblUserName);
    lblUserName.setLayoutData(new GridData());

    mUserName = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER);
    GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(mUserName);

    Label lblPassword = new Label(credentialsGroup, SWT.NULL);
    lblPassword.setText(Messages.RemoteConfigurationEditor_lblPassword);
    lblPassword.setLayoutData(new GridData());

    mPassword = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
    GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(mPassword);
  }

  private void init(Shell shell) {
    if (mWorkingCopy.getName() != null) {
      mConfigName.setText(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      mLocation.setText(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      mDescription.setText(mWorkingCopy.getDescription());
    }

    mChkCacheConfig.setSelection(Boolean.parseBoolean(
            mWorkingCopy.getAdditionalData().get(RemoteConfigurationType.KEY_CACHE_CONFIG)));

    if (mWorkingCopy.getLocation() != null) {
      try {

        final RemoteConfigAuthenticator auth = RemoteConfigAuthenticator
                .create(mWorkingCopy.getResolvedConfigurationFileURL());

        if (auth != null) {
          mUserName.setText(auth.getUsername());
          mPassword.setText(new String(auth.getPassword()));
        }
      } catch (CheckstylePluginException ex) {
        CheckstyleUIPlugin.errorDialog(shell, ex, true);
      }
    }
  }

  @Override
  public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {

    // set the cachefile name
    if (mChkCacheConfig.getSelection() && mWorkingCopy.getAdditionalData()
            .get(RemoteConfigurationType.KEY_CACHE_FILE_LOCATION) == null) {

      long currentTime = System.currentTimeMillis();

      mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_FILE_LOCATION,
              mWorkingCopy.getName() + "_" + currentTime + "_cache.xml");
      mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_PROPS_FILE_LOCATION,
              mWorkingCopy.getName() + "_" + currentTime + "_cache.properties");
    }

    // store credentials if necessary
    try {
      if (StringUtils.isNotBlank(mUserName.getText())
              || StringUtils.isNotBlank(mPassword.getText())) {
        RemoteConfigurationType.RemoteConfigAuthenticator.storeCredentials(
                URI.create(mLocation.getText()).toURL(), mUserName.getText(), mPassword.getText());
      } else {
        RemoteConfigurationType.RemoteConfigAuthenticator
                .removeCachedAuthInfo(URI.create(mLocation.getText()).toURL());
      }
    } catch (Exception ex) {
      CheckstylePluginException.rethrow(ex);
    }

    mWorkingCopy.setName(mConfigName.getText());
    mWorkingCopy.setLocation(mLocation.getText());
    mWorkingCopy.setDescription(mDescription.getText());

    mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_CONFIG,
            Boolean.toString(mChkCacheConfig.getSelection()));

    return mWorkingCopy;
  }
}
