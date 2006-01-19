//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Implementation of a file based location editor. Contains a text field with
 * the config file path and a 'Browse...' button opening a file dialog.
 * 
 * @author Lars Ködderitzsch
 */
public class ExternalFileConfigurationEditor implements ICheckConfigurationEditor
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

    /** browse button. */
    private Button mBtnBrowse;

    /** the text containing the description. */
    private Text mDescription;

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
        lblConfigLocation.setLayoutData(gd);

        Composite locationComposite = new Composite(contents, SWT.NULL);
        locationComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        locationComposite.setLayout(layout);

        mLocation = new Text(locationComposite, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mLocation.setLayoutData(gd);

        mBtnBrowse = new Button(locationComposite, SWT.PUSH);
        mBtnBrowse.setText(Messages.FileConfigurationLocationEditor_btnBrowse);
        gd = new GridData();
        mBtnBrowse.setLayoutData(gd);

        mBtnBrowse.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                FileDialog fileDialog = new FileDialog(shell);
                fileDialog.setFileName(mLocation.getText());

                String file = fileDialog.open();
                if (null != file)
                {
                    mLocation.setText(file);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            // NOOP
            }
        });

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
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy getEditedWorkingCopy() throws CheckstylePluginException
    {
        mWorkingCopy.setName(mConfigName.getText());
        mWorkingCopy.setLocation(mLocation.getText());
        mWorkingCopy.setDescription(mDescription.getText());

        return mWorkingCopy;
    }

}