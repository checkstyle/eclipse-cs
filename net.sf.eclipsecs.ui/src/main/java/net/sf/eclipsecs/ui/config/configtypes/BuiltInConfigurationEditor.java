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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationPropertiesDialog;

/**
 * Implementation of a location editor with only a not editable text field. This is used to just
 * show the location.
 *
 */
public class BuiltInConfigurationEditor implements ICheckConfigurationEditor {

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

    //
    // methods
    //

    /**
     * Creates a new instance.
     */
    public BuiltInConfigurationEditor() {
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
        configName.setEditable(false);
        configName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        configName.setFocus();

        final Label lblConfigLocation = new Label(contents, SWT.NULL);
        lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING)
            .applyTo(lblConfigLocation);

        location = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        location.setEditable(false);

        final Label lblDescription = new Label(contents, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
        GridDataFactory.swtDefaults().span(2, 1).applyTo(lblDescription);

        description =
            new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.VERTICAL);
        description.setEditable(false);
        GridDataFactory.create(GridData.FILL_BOTH).span(2, 1)
            .hint(ICheckConfigurationEditor.CONFIG_DESCRIPTION_SIZE).grab(true, true)
            .applyTo(description);

        if (workingCopy.getName() != null) {
            configName.setText(workingCopy.getName());
        }
        if (workingCopy.getLocation() != null) {
            location.setText(workingCopy.getLocation());
        }
        if (workingCopy.getDescription() != null) {
            description.setText(workingCopy.getDescription());
        }

        return contents;
    }

    @Override
    public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException {
        return workingCopy;
    }

}
