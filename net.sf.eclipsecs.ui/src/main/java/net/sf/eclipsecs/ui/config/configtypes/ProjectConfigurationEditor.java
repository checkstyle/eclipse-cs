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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ConfigurationWriter;
import net.sf.eclipsecs.core.config.GlobalCheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.configtypes.ExternalFileConfigurationType;
import net.sf.eclipsecs.core.projectconfig.LocalCheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

/**
 * Implementation of a file based location editor. Contains a text field with the config file path
 * and a 'Browse...' button opening a file dialog.
 *
 */
public class ProjectConfigurationEditor implements ICheckConfigurationEditor {

    //
    // attributes
    //

    /** The working copy this editor edits. */
    private CheckConfigurationWorkingCopy workingCopy;

    /** The shell. */
    private Shell shell;

    /** The parent dialog element. */
    private CheckConfigurationPropertiesDialog checkConfigDialog;

    /** The text field containing the config name. */
    private Text configName;

    /** Text field containing the location. */
    private Text locationControl;

    /** The text containing the description. */
    private Text description;

    /**
     * Check box to set if the configuration file is not editable by the configuration editor.
     */
    private Button chkProtectConfig;

    //
    // methods
    //

    /**
     * Creates a new instance.
     */
    public ProjectConfigurationEditor() {
    }

    @Override
    public void initialize(CheckConfigurationWorkingCopy checkConfiguration,
        CheckConfigurationPropertiesDialog dialog) {
        workingCopy = checkConfiguration;
        checkConfigDialog = dialog;
    }

    @Override
    public Control createEditorControl(Composite parent, final Shell parentShell) {
        this.shell = parentShell;
        final Composite contents = new Composite(parent, SWT.NULL);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(contents);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(contents);

        final Label lblConfigName = new Label(contents, SWT.NULL);
        lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
        GridDataFactory.swtDefaults().applyTo(lblConfigName);

        configName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(configName);
        configName.setFocus();

        locationControl = createLocationSection(contents, parentShell);

        final Label lblDescription = new Label(contents, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
        GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

        description =
            new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
        GridDataFactory.create(GridData.FILL_BOTH).span(2, 1)
            .hint(ICheckConfigurationEditor.CONFIG_DESCRIPTION_SIZE)
            .grab(true, true)
            .applyTo(description);

        final Group advancedGroup = new Group(contents, SWT.NULL);
        advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(advancedGroup);

        chkProtectConfig = new Button(advancedGroup, SWT.CHECK);
        chkProtectConfig.setText(Messages.ProjectConfigurationEditor_chkProtectConfigFile);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(chkProtectConfig);

        if (workingCopy.getName() != null) {
            configName.setText(workingCopy.getName());
        }
        if (workingCopy.getLocation() != null) {
            locationControl.setText(workingCopy.getLocation());
        }
        if (workingCopy.getDescription() != null) {
            description.setText(workingCopy.getDescription());
        }

        chkProtectConfig.setSelection(Boolean.parseBoolean(workingCopy.getAdditionalData()
            .get(ExternalFileConfigurationType.KEY_PROTECT_CONFIG)));

        return contents;
    }

    /**
     * Creates the location section with a text field and a browse button.
     *
     * @param parent
     *            the parent composite
     * @param shell
     *            the shell used for the file selection dialog
     * @return the text field displaying the selected location
     */
    private static Text createLocationSection(Composite parent, Shell shell) {
        final Label lblConfigLocation = new Label(parent, SWT.NULL);
        lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
        GridDataFactory.swtDefaults().applyTo(lblConfigLocation);

        final Composite locationComposite = new Composite(parent, SWT.NULL);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(locationComposite);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(locationComposite);

        final Text location = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(location);

        final Button mBtnBrowse = new Button(locationComposite, SWT.PUSH);
        mBtnBrowse.setText(Messages.ProjectConfigurationLocationEditor_btnBrowse);
        GridDataFactory.swtDefaults().applyTo(mBtnBrowse);

        mBtnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(
            event -> WorkspaceFileSelector.select(shell).ifPresent(location::setText)));

        return location;
    }

    @Override
    public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
        workingCopy.setName(configName.getText());
        workingCopy.setDescription(description.getText());

        workingCopy.getAdditionalData().put(ExternalFileConfigurationType.KEY_PROTECT_CONFIG,
            Boolean.toString(chkProtectConfig.getSelection()));

        try {
            workingCopy.setLocation(locationControl.getText());
        }
        catch (CheckstylePluginException exception) {
            String location = locationControl.getText();

            if (StringUtils.isBlank(location)) {
                throw exception;
            }

            final ICheckConfigurationWorkingSet workingSet =
                checkConfigDialog.getCheckConfigurationWorkingSet();
            final IPath tmp = IPath.fromOSString(location);
            final boolean isFirstPartProject =
                ResourcesPlugin.getWorkspace().getRoot().getProject(tmp.segment(0)).exists();

            if (!isFirstPartProject && workingSet instanceof LocalCheckConfigurationWorkingSet) {
                location = ((LocalCheckConfigurationWorkingSet) workingSet).getProject()
                    .getFullPath().append(location).toString();
                locationControl.setText(location);
            }
            else if (!isFirstPartProject
                && workingSet instanceof GlobalCheckConfigurationWorkingSet) {
                throw new CheckstylePluginException(
                    NLS.bind(Messages.ProjectConfigurationEditor_msgNoProjectInWorkspace,
                        tmp.segment(0)),
                    exception);
            }

            if (ensureFileExists(location)) {
                workingCopy.setLocation(locationControl.getText());
            }
            else {
                throw exception;
            }
        }

        return workingCopy;
    }

    /**
     * Helper method trying to ensure that the file location provided by the user exists. If that is
     * not the case it prompts the user if an empty configuration file should be created.
     *
     * @param location
     *            the configuration file location
     * @return whether the file exists or was created
     * @throws CheckstylePluginException
     *             error when trying to ensure the location file existance
     */
    private boolean ensureFileExists(String location) throws CheckstylePluginException {
        IFile file = null;
        try {
            file = ResourcesPlugin.getWorkspace().getRoot().getFile(IPath.fromOSString(location));
        }
        catch (IllegalArgumentException exception) {
            CheckstylePluginException.rethrow(exception);
        }

        final boolean exists;
        if (!file.exists() && file.getLocation() != null) {
            final boolean confirm = MessageDialog.openQuestion(shell,
                Messages.ExternalFileConfigurationEditor_titleFileDoesNotExist,
                Messages.ExternalFileConfigurationEditor_msgFileDoesNotExist);
            if (confirm) {
                final Path trueFile = file.getLocation().toPath();
                try {
                    if (trueFile.getParent() != null) {
                        Files.createDirectories(trueFile.getParent());
                    }
                    try (OutputStream out =
                        new BufferedOutputStream(Files.newOutputStream(trueFile))) {
                        ConfigurationWriter.writeNewConfiguration(out, workingCopy);
                        file.refreshLocal(IResource.DEPTH_INFINITE, null);
                    }
                }
                catch (CoreException | IOException exception) {
                    CheckstylePluginException.rethrow(exception);
                }

                exists = true;
            }
            else {
                exists = false;
            }
        }
        else {
            exists = true;
        }
        return exists;
    }

}
