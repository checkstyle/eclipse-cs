///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.ui.config.configtypes;

import java.net.MalformedURLException;
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
 * Implementation of a location editor to input a remote location. Contains just a text field to
 * input the URL.
 *
 */
public class RemoteConfigurationEditor implements ICheckConfigurationEditor {

    //
    // attributes
    //

    /** The working copy this editor edits. */
    private CheckConfigurationWorkingCopy workingCopy;

    /** The text field containing the config name. */
    private Text configName;

    /** Text field containing the location. */
    private Text location;

    /** The text containing the description. */
    private Text description;

    /** Check box to set if the configuration should be cached. */
    private Button chkCacheConfig;

    /** The user name text field. */
    private Text userName;

    /** The password text field. */
    private Text password;

    //
    // methods
    //

    /**
     * Creates a new instance.
     */
    public RemoteConfigurationEditor() {
    }

    @Override
    public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
        CheckConfigurationPropertiesDialog dialog) {
        workingCopy = checkConfiguration;
    }

    @Override
    public Control createEditorControl(Composite parent, final Shell shell) {
        final Composite contents = new Composite(parent, SWT.NULL);
        contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(contents);

        final Label lblConfigName = new Label(contents, SWT.NULL);
        lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
        lblConfigName.setLayoutData(new GridData());

        configName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        configName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        configName.setFocus();

        final Label lblConfigLocation = new Label(contents, SWT.NULL);
        lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING)
            .applyTo(lblConfigLocation);

        location = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Label lblDescription = new Label(contents, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
        GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

        description =
            new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
        GridDataFactory.create(GridData.FILL_BOTH).span(2, 1)
            .hint(ICheckConfigurationEditor.CONFIG_DESCRIPTION_SIZE)
            .grab(true, true)
            .applyTo(description);

        createCredentialsGroup(contents);

        final Group advancedGroup = new Group(contents, SWT.NULL);
        advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
        advancedGroup.setLayout(new GridLayout(2, false));

        chkCacheConfig = new Button(advancedGroup, SWT.CHECK);
        chkCacheConfig.setText(Messages.RemoteConfigurationEditor_btnCacheRemoteConfig);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(chkCacheConfig);

        init(shell);

        return contents;
    }

    /**
     * Creates the credentials group of the editor.
     *
     * @param parent
     *            the parent composite
     */
    private void createCredentialsGroup(Composite parent) {
        final Group credentialsGroup = new Group(parent, SWT.NULL);
        credentialsGroup.setText(Messages.RemoteConfigurationEditor_titleCredentialsGroup);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(credentialsGroup);
        credentialsGroup.setLayout(new GridLayout(2, false));

        final Label lblUserName = new Label(credentialsGroup, SWT.NULL);
        lblUserName.setText(Messages.RemoteConfigurationEditor_lblUserName);
        lblUserName.setLayoutData(new GridData());

        userName = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(userName);

        final Label lblPassword = new Label(credentialsGroup, SWT.NULL);
        lblPassword.setText(Messages.RemoteConfigurationEditor_lblPassword);
        lblPassword.setLayoutData(new GridData());

        password = new Text(credentialsGroup, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(password);
    }

    /**
     * Initializes the editor contents from the working copy.
     *
     * @param shell
     *            the parent shell
     */
    private void init(Shell shell) {
        if (workingCopy.getName() != null) {
            configName.setText(workingCopy.getName());
        }
        if (workingCopy.getLocation() != null) {
            location.setText(workingCopy.getLocation());
        }
        if (workingCopy.getDescription() != null) {
            description.setText(workingCopy.getDescription());
        }

        chkCacheConfig.setSelection(Boolean.parseBoolean(
            workingCopy.getAdditionalData().get(RemoteConfigurationType.KEY_CACHE_CONFIG)));

        if (workingCopy.getLocation() != null) {
            try {

                final RemoteConfigAuthenticator auth = RemoteConfigAuthenticator
                    .create(workingCopy.getResolvedConfigurationFileURL());

                if (auth != null) {
                    userName.setText(auth.getUsername());
                    password.setText(new String(auth.getPassword()));
                }
            }
            catch (CheckstylePluginException exception) {
                CheckstyleUIPlugin.errorDialog(shell, exception, true);
            }
        }
    }

    @Override
    public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {

        // set the cachefile name
        if (chkCacheConfig.getSelection() && workingCopy.getAdditionalData()
            .get(RemoteConfigurationType.KEY_CACHE_FILE_LOCATION) == null) {

            final long currentTime = System.currentTimeMillis();

            workingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_FILE_LOCATION,
                makeCacheFileName(workingCopy.getName(), currentTime, "xml"));
            workingCopy.getAdditionalData().put(
                RemoteConfigurationType.KEY_CACHE_PROPS_FILE_LOCATION,
                makeCacheFileName(workingCopy.getName(), currentTime, "properties"));
        }

        // store credentials if necessary
        try {
            if (StringUtils.isNotBlank(userName.getText())
                || StringUtils.isNotBlank(password.getText())) {
                RemoteConfigurationType.RemoteConfigAuthenticator.storeCredentials(
                    URI.create(location.getText()).toURL(), userName.getText(),
                    password.getText());
            }
            else {
                RemoteConfigurationType.RemoteConfigAuthenticator
                    .removeCachedAuthInfo(URI.create(location.getText()).toURL());
            }
        }
        catch (MalformedURLException exception) {
            CheckstylePluginException.rethrow(exception);
        }

        workingCopy.setName(configName.getText());
        workingCopy.setLocation(location.getText());
        workingCopy.setDescription(description.getText());

        workingCopy.getAdditionalData().put(RemoteConfigurationType.KEY_CACHE_CONFIG,
            Boolean.toString(chkCacheConfig.getSelection()));

        return workingCopy;
    }

    /**
     * Creates a cache file name for the given configuration name, time and extension.
     *
     * @param configName
     *            the configuration name
     * @param time
     *            the current time in milliseconds
     * @param extension
     *            the file extension
     * @return the cache file name
     */
    private static String makeCacheFileName(String configName, long time, String extension) {
        return String.join("_", configName, String.valueOf(time), "cache.") + extension;
    }

}
