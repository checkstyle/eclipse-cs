//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.ui.Messages;

/**
 * Composite building the editor form for external file check configurations, containing the
 * configuration name, file location with browse button, description and protect-config option.
 *
 */
public final class ExternalFileConfigurationEditorView extends Composite {

    /** The config name text field. */
    private final Text mConfigName;
    /** The location text field. */
    private final Text location;
    /** The description text field. */
    private final Text mDescription;
    /** The protect config check box. */
    private final Button mChkProtectConfig;

    /**
     * Constructor building the editor form for external file check configurations.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the SWT style bits to apply to this composite
     */
    public ExternalFileConfigurationEditorView(Composite parent, int style) {
        super(parent, style);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0).applyTo(this);

        final Label lblConfigName = new Label(this, SWT.NULL);
        lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
        GridDataFactory.swtDefaults().applyTo(lblConfigName);

        mConfigName = new Text(this, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(mConfigName);
        mConfigName.setFocus();

        location = createLocationSection(this, parent.getShell());

        final Label lblDescription = new Label(this, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
        GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

        mDescription = new Text(this, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
        GridDataFactory.create(GridData.FILL_BOTH).span(2, 1)
            .hint(ICheckConfigurationEditor.CONFIG_DESCRIPTION_SIZE)
            .grab(true, true)
            .applyTo(mDescription);

        final Group advancedGroup = new Group(this, SWT.NULL);
        advancedGroup.setText(Messages.RemoteConfigurationEditor_titleAdvancedOptions);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(advancedGroup);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(advancedGroup);

        mChkProtectConfig = new Button(advancedGroup, SWT.CHECK);
        mChkProtectConfig.setText(Messages.ExternalFileConfigurationEditor_btnProtectConfigFile);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(mChkProtectConfig);
    }

    /**
     * Returns the configuration name entered in the name text field.
     *
     * @return the configuration name
     */
    public String getConfigName() {
        return mConfigName.getText();
    }

    /**
     * Returns the description entered in the description text field.
     *
     * @return the description
     */
    public String getDescription() {
        return mDescription.getText();
    }

    /**
     * Returns the config file location entered in the location text field.
     *
     * @return the config file location
     */
    public String getConfigLocation() {
        return location.getText();
    }

    /**
     * Returns whether the protect-config option is selected.
     *
     * @return true if the protect-config check box is selected, false otherwise
     */
    public boolean getProtectConfig() {
        return mChkProtectConfig.getSelection();
    }

    /**
     * Sets the configuration name in the name text field.
     *
     * @param configName
     *            the configuration name to set
     */
    public void setConfigName(String configName) {
        mConfigName.setText(configName);
    }

    /**
     * Sets the description in the description text field.
     *
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        mDescription.setText(description);
    }

    /**
     * Sets the config file location in the location text field.
     *
     * @param strLocation
     *            the config file location to set
     */
    public void setConfigLocation(String strLocation) {
        location.setText(strLocation);
    }

    /**
     * Sets the protect-config option in the check box.
     *
     * @param protectConfig
     *            true to select the protect-config check box, false otherwise
     */
    public void setProtectConfig(boolean protectConfig) {
        mChkProtectConfig.setSelection(protectConfig);
    }

    /**
     * Creates the location composite with a text field and a browse button that opens a file
     * dialog.
     *
     * @param parent
     *            the parent composite
     * @param shell
     *            the shell used as the parent of the file dialog
     * @return the location text field
     */
    private static Text createLocationSection(Composite parent, Shell shell) {
        final Label lblConfigLocation = new Label(parent, SWT.NULL);
        lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
        GridDataFactory.swtDefaults().applyTo(lblConfigLocation);

        final Composite locationComposite = new Composite(parent, SWT.NULL);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(locationComposite);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(0, 0)
            .applyTo(locationComposite);

        final Text location = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(location);

        final Button btnBrowse = new Button(locationComposite, SWT.PUSH);
        btnBrowse.setText(Messages.FileConfigurationLocationEditor_btnBrowse);
        GridDataFactory.swtDefaults().applyTo(btnBrowse);

        btnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            final FileDialog fileDialog = new FileDialog(shell);
            fileDialog.setFileName(location.getText());

            final String file = fileDialog.open();
            if (file != null) {
                location.setText(file);
            }
        }));

        return location;
    }

}
