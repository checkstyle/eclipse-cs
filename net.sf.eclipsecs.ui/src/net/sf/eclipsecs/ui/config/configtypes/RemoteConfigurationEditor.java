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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.config.configtypes;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.configtypes.RemoteConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.RemoteConfigurationType.RemoteConfigAuthenticator;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

import org.apache.commons.lang3.StringUtils;
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

/**
 * Implementation of a location editor to input a remote location. Contains just
 * a text field to input the URL.
 *
 * @author Lars Ködderitzsch
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
    gridData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    lblConfigLocation.setLayoutData(gridData);

    mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mLocation.setLayoutData(gridData);

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

    Group credentialsGroup = new Group(contents, SWT.NULL);
    credentialsGroup.setText(Messages.RemoteConfigurationEditor_titleCredentialsGroup);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    credentialsGroup.setLayoutData(gridData);
    credentialsGroup.setLayout(new GridLayout(2, false));

    Label lblUserName = new Label(credentialsGroup, SWT.NULL);
    lblUserName.setText(Messages.RemoteConfigurationEditor_lblUserName);
    gridData = new GridData();
    lblUserName.setLayoutData(gridData);

    mUserName = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 100;
    mUserName.setLayoutData(gridData);

    Label lblPassword = new Label(credentialsGroup, SWT.NULL);
    lblPassword.setText(Messages.RemoteConfigurationEditor_lblPassword);
    gridData = new GridData();
    lblPassword.setLayoutData(gridData);

    mPassword = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
    gridData = new GridData();
    gridData.widthHint = 100;
    mPassword.setLayoutData(gridData);

    Group advancedGroup = new Group(contents, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    advancedGroup.setLayoutData(gridData);
    advancedGroup.setLayout(new GridLayout(2, false));

    mChkCacheConfig = new Button(advancedGroup, SWT.CHECK);
    mChkCacheConfig.setText(Messages.RemoteConfigurationEditor_btnCacheRemoteConfig);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;
    mChkCacheConfig.setLayoutData(gridData);

    if (mWorkingCopy.getName() != null) {
      mConfigName.setText(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      mLocation.setText(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      mDescription.setText(mWorkingCopy.getDescription());
    }

    mChkCacheConfig.setSelection(Boolean.parseBoolean(mWorkingCopy.getAdditionalData().get(RemoteConfigurationType.KEY_CACHE_CONFIG)));

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

    return contents;
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
                new URL(mLocation.getText()), mUserName.getText(), mPassword.getText());
      } else {
        RemoteConfigurationType.RemoteConfigAuthenticator
                .removeCachedAuthInfo(new URL(mLocation.getText()));
      }
    } catch (MalformedURLException ex) {
      CheckstylePluginException.rethrow(ex);
    }

    mWorkingCopy.setName(mConfigName.getText());
    mWorkingCopy.setLocation(mLocation.getText());
    mWorkingCopy.setDescription(mDescription.getText());

    mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_CONFIG,
            "" + mChkCacheConfig.getSelection()); //$NON-NLS-1$

    return mWorkingCopy;
  }
}
