//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Implementation of a location editor to input a remote location. Contains just
 * a text field to input the URL.
 * 
 * @author Lars Ködderitzsch
 */
public class RemoteConfigurationEditor implements ICheckConfigurationEditor
{

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

    /** check box to set if http basic authentication will be used. */
    private Button mChkUseBasicAuthentication;

    /** the user name for http basic authentication support. */
    private Text mUserName;

    /** the password for http basic authentication support. */
    private Text mPassword;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public Control createEditorControl(Composite parent, final Shell shell)
    {

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

        mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER
                | SWT.VERTICAL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.widthHint = 300;
        gd.heightHint = 100;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        mDescription.setLayoutData(gd);

        Group advancedGroup = new Group(contents, SWT.NULL);
        advancedGroup.setText("Advanced options");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        advancedGroup.setLayoutData(gd);
        advancedGroup.setLayout(new GridLayout(2, false));

        mChkCacheConfig = new Button(advancedGroup, SWT.CHECK);
        mChkCacheConfig.setText("Cache configuration file");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mChkCacheConfig.setLayoutData(gd);

        mChkUseBasicAuthentication = new Button(advancedGroup, SWT.CHECK);
        mChkUseBasicAuthentication.setText("Use HTTP-Basic authentication");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mChkUseBasicAuthentication.setLayoutData(gd);
        mChkUseBasicAuthentication.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                mUserName.setEnabled(mChkUseBasicAuthentication.getSelection());
                mPassword.setEnabled(mChkUseBasicAuthentication.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            // NOOP
            }
        });

        Label lblUserName = new Label(advancedGroup, SWT.NULL);
        lblUserName.setText("Username:");
        gd = new GridData();
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
        lblUserName.setLayoutData(gd);

        mUserName = new Text(advancedGroup, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mUserName.setLayoutData(gd);

        Label lblPassword = new Label(advancedGroup, SWT.NULL);
        lblPassword.setText("Password:");
        gd = new GridData();
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
        lblPassword.setLayoutData(gd);

        mPassword = new Text(advancedGroup, SWT.LEFT | SWT.PASSWORD | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mPassword.setLayoutData(gd);

        return contents;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(CheckConfigurationWorkingCopy checkConfiguration)
    {
        mWorkingCopy = checkConfiguration;

        if (mWorkingCopy.getName() != null)
        {
            mConfigName.setText(mWorkingCopy.getName());
        }
        if (mWorkingCopy.getLocation() != null)
        {
            mLocation.setText(mWorkingCopy.getLocation());
        }
        if (mWorkingCopy.getDescription() != null)
        {
            mDescription.setText(mWorkingCopy.getDescription());
        }

        mChkCacheConfig.setSelection(Boolean.valueOf(
                (String) mWorkingCopy.getAdditionalData().get(
                        RemoteConfigurationType.KEY_CACHE_CONFIG)).booleanValue());

        mChkUseBasicAuthentication.setSelection(Boolean.valueOf(
                (String) mWorkingCopy.getAdditionalData().get(
                        RemoteConfigurationType.KEY_USE_BASIC_AUTH)).booleanValue());

        mUserName.setEnabled(mChkUseBasicAuthentication.getSelection());
        mPassword.setEnabled(mChkUseBasicAuthentication.getSelection());

        String userName = (String) mWorkingCopy.getAdditionalData().get(
                RemoteConfigurationType.KEY_USERNAME);
        if (userName != null)
        {
            mUserName.setText(userName);
        }

        String password = (String) mWorkingCopy.getAdditionalData().get(
                RemoteConfigurationType.KEY_PASSWORD);
        if (password != null)
        {
            mPassword.setText(password);
        }
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException
    {

        mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_CONFIG,
                "" + mChkCacheConfig.getSelection());

        // set the cachefile name
        if (mChkCacheConfig.getSelection()
                && mWorkingCopy.getAdditionalData().get(
                        RemoteConfigurationType.KEY_CACHE_FILE_LOCATION) == null)
        {
            mWorkingCopy.getAdditionalData().put(
                    RemoteConfigurationType.KEY_CACHE_FILE_LOCATION,
                    mWorkingCopy.getName() + "_" + (mWorkingCopy.isGlobal() ? "global" : "local")
                            + "_cache.xml");
        }

        mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_USE_BASIC_AUTH,
                "" + mChkUseBasicAuthentication.getSelection());

        if (mUserName.getText() != null && mUserName.getText().trim().length() != 0)
        {
            mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_USERNAME,
                    mUserName.getText());
        }

        if (mPassword.getText() != null && mPassword.getText().trim().length() != 0)
        {
            mWorkingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_PASSWORD,
                    mPassword.getText());
        }

        mWorkingCopy.setName(mConfigName.getText());
        mWorkingCopy.setLocation(mLocation.getText());
        mWorkingCopy.setDescription(mDescription.getText());

        return mWorkingCopy;
    }
}