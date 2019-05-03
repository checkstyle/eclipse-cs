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

import com.google.common.base.Strings;

import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.configtypes.RemoteConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

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

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
          CheckConfigurationPropertiesDialog dialog) {
    mWorkingCopy = checkConfiguration;
  }

  /**
   * {@inheritDoc}
   */
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
    GridData gd = new GridData();
    lblConfigName.setLayoutData(gd);

    mConfigName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    mConfigName.setLayoutData(gd);
    mConfigName.setFocus();

    Label lblConfigLocation = new Label(contents, SWT.NULL);
    lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
    gd = new GridData();
    gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
    lblConfigLocation.setLayoutData(gd);

    mLocation = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    mLocation.setLayoutData(gd);

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
    gd = new GridData();
    gd.horizontalSpan = 2;
    lblDescription.setLayoutData(gd);

    mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
    gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 2;
    gd.widthHint = 300;
    gd.heightHint = 100;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    mDescription.setLayoutData(gd);

    Group credentialsGroup = new Group(contents, SWT.NULL);
    credentialsGroup.setText(Messages.RemoteConfigurationEditor_titleCredentialsGroup);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    credentialsGroup.setLayoutData(gd);
    credentialsGroup.setLayout(new GridLayout(2, false));

    Label lblUserName = new Label(credentialsGroup, SWT.NULL);
    lblUserName.setText(Messages.RemoteConfigurationEditor_lblUserName);
    gd = new GridData();
    lblUserName.setLayoutData(gd);

    mUserName = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER);
    gd = new GridData();
    gd.widthHint = 100;
    mUserName.setLayoutData(gd);

    Label lblPassword = new Label(credentialsGroup, SWT.NULL);
    lblPassword.setText(Messages.RemoteConfigurationEditor_lblPassword);
    gd = new GridData();
    lblPassword.setLayoutData(gd);

    mPassword = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
    gd = new GridData();
    gd.widthHint = 100;
    mPassword.setLayoutData(gd);

    Group advancedGroup = new Group(contents, SWT.NULL);
    advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    advancedGroup.setLayoutData(gd);
    advancedGroup.setLayout(new GridLayout(2, false));

    mChkCacheConfig = new Button(advancedGroup, SWT.CHECK);
    mChkCacheConfig.setText(Messages.RemoteConfigurationEditor_btnCacheRemoteConfig);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    mChkCacheConfig.setLayoutData(gd);

    if (mWorkingCopy.getName() != null) {
      mConfigName.setText(mWorkingCopy.getName());
    }
    if (mWorkingCopy.getLocation() != null) {
      mLocation.setText(mWorkingCopy.getLocation());
    }
    if (mWorkingCopy.getDescription() != null) {
      mDescription.setText(mWorkingCopy.getDescription());
    }

    mChkCacheConfig.setSelection(Boolean
            .valueOf(mWorkingCopy.getAdditionalData().get(RemoteConfigurationType.KEY_CACHE_CONFIG))
            .booleanValue());

    if (mWorkingCopy.getLocation() != null) {
      try {

        PasswordAuthentication auth = RemoteConfigurationType.RemoteConfigAuthenticator
                .getPasswordAuthentication(mWorkingCopy.getResolvedConfigurationFileURL());

        if (auth != null) {
          mUserName.setText(auth.getUserName());
          mPassword.setText(new String(auth.getPassword()));
        }
      } catch (CheckstylePluginException e) {
        CheckstyleUIPlugin.errorDialog(shell, e, true);
      }
    }

    return contents;
  }

  /**
   * {@inheritDoc}
   */
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
      if (Strings.emptyToNull(mUserName.getText()) != null
              || Strings.emptyToNull(mPassword.getText()) != null) {
        RemoteConfigurationType.RemoteConfigAuthenticator.storeCredentials(
                new URL(mLocation.getText()), mUserName.getText(), mPassword.getText());
      } else {
        RemoteConfigurationType.RemoteConfigAuthenticator
                .removeCachedAuthInfo(new URL(mLocation.getText()));
      }
    } catch (MalformedURLException e) {
      CheckstylePluginException.rethrow(e);
    }

    mWorkingCopy.setName(mConfigName.getText());
    mWorkingCopy.setLocation(mLocation.getText());
    mWorkingCopy.setDescription(mDescription.getText());

    mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_CONFIG,
            "" + mChkCacheConfig.getSelection()); //$NON-NLS-1$

    return mWorkingCopy;
  }
}
