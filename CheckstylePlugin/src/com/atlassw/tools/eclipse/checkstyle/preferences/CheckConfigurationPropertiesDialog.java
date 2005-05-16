//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.preferences;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationLocationEditor;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Dialog to show/edit the properties (name, location, description) of a check
 * configuration. Also used to create new check configurations.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationPropertiesDialog extends TitleAreaDialog
{

    //
    // attributes
    //

    /** the check configuration. */
    private ICheckConfiguration mCheckConfig;

    /** the combo box containing the config type. */
    private ComboViewer mConfigType;

    /** the text field containing the config name. */
    private Text mConfigName;

    /** place holder for the location editor. */
    private Composite mEditorPlaceHolder;

    /** the editor for the configuration location. */
    private IConfigurationLocationEditor mLocationEditor;

    /** the text containing the description. */
    private Text mDescription;

    //
    // constructor
    //

    /**
     * Creates the properties dialog for check configurations.
     * 
     * @param parent the parent shell
     * @param checkConfig the check configuration or <code>null</code> if a
     *            new check config should be created
     */
    public CheckConfigurationPropertiesDialog(Shell parent, ICheckConfiguration checkConfig)
    {
        super(parent);
        mCheckConfig = checkConfig;
    }

    //
    // methods
    //

    /**
     * Get the check configuration from the editor.
     * 
     * @return the check configuration
     * @throws CheckstylePluginException if the data is not valid
     */
    public ICheckConfiguration getCheckConfiguration() throws CheckstylePluginException
    {
        ICheckConfiguration result = null;
        if (mCheckConfig != null && !mCheckConfig.isEditable() || mConfigName.isDisposed())
        {
            result = mCheckConfig;
        }
        else if (mCheckConfig != null)
        {
            result = mCheckConfig;

            result.setName(mConfigName.getText());
            result.setLocation(mLocationEditor.getLocation());
            result.setDescription(mDescription.getText());
        }
        else if (mCheckConfig == null)
        {
            try
            {
                if (mConfigType.getSelection() instanceof IStructuredSelection)
                {
                    IConfigurationType type = (IConfigurationType) ((IStructuredSelection) mConfigType
                            .getSelection()).getFirstElement();

                    result = (ICheckConfiguration) type.getImplementationClass().newInstance();
                    result.initialize(mConfigName.getText(), mLocationEditor.getLocation(), type,
                            mDescription.getText());
                }
            }
            catch (Exception e)
            {
                CheckstylePluginException.rethrow(e);
            }
        }

        return result;
    }

    /**
     * Creates the dialogs main contents.
     * 
     * @param parent the parent composite
     */
    protected Control createDialogArea(Composite parent)
    {

        Composite composite = (Composite) super.createDialogArea(parent);

        Composite contents = new Composite(composite, SWT.NULL);
        contents.setLayout(new GridLayout(2, false));
        GridData fd = new GridData(GridData.FILL_BOTH);
        contents.setLayoutData(fd);

        Label lblConfigType = new Label(contents, SWT.NULL);
        lblConfigType.setText(Messages.CheckConfigurationPropertiesDialog_lblConfigType);
        fd = new GridData();
        lblConfigType.setLayoutData(fd);

        mConfigType = new ComboViewer(contents);
        fd = new GridData();
        mConfigType.getCombo().setLayoutData(fd);
        mConfigType.setContentProvider(new ArrayContentProvider());
        mConfigType.setLabelProvider(new LabelProvider()
        {
            /**
             * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
             */
            public String getText(Object element)
            {
                return ((IConfigurationType) element).getName();
            }

            /**
             * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
             */
            public Image getImage(Object element)
            {
                return ((IConfigurationType) element).getTypeImage();
            }
        });
        mConfigType.addSelectionChangedListener(new ISelectionChangedListener()
        {
            /**
             * @see ISelectionChangedListener#selectionChanged(
             *      org.eclipse.jface.viewers.SelectionChangedEvent)
             */
            public void selectionChanged(SelectionChangedEvent event)
            {
                if (event.getSelection() instanceof IStructuredSelection)
                {
                    IConfigurationType type = (IConfigurationType) ((IStructuredSelection) event
                            .getSelection()).getFirstElement();
                    createLocationEditor(type);
                }
            }
        });

        Label lblConfigName = new Label(contents, SWT.NULL);
        lblConfigName.setText(Messages.CheckConfigurationPropertiesDialog_lblName);
        fd = new GridData();
        lblConfigName.setLayoutData(fd);

        mConfigName = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        fd = new GridData(GridData.FILL_HORIZONTAL);
        mConfigName.setLayoutData(fd);

        Label lblConfigLocation = new Label(contents, SWT.NULL);
        lblConfigLocation.setText(Messages.CheckConfigurationPropertiesDialog_lblLocation);
        fd = new GridData();
        fd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
        lblConfigLocation.setLayoutData(fd);

        mEditorPlaceHolder = new Composite(contents, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        mEditorPlaceHolder.setLayout(layout);
        fd = new GridData(GridData.FILL_HORIZONTAL);
        fd.heightHint = 23;
        mEditorPlaceHolder.setLayoutData(fd);

        Label lblDescription = new Label(contents, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationPropertiesDialog_lblDescription);
        fd = new GridData();
        fd.horizontalSpan = 2;
        lblDescription.setLayoutData(fd);

        mDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.BORDER
                | SWT.VERTICAL);
        fd = new GridData(GridData.FILL_BOTH);
        fd.horizontalSpan = 2;
        fd.widthHint = 300;
        fd.heightHint = 100;
        fd.grabExcessHorizontalSpace = true;
        fd.grabExcessVerticalSpace = true;
        mDescription.setLayoutData(fd);

        contents.layout();
        initialize();

        return composite;
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.CheckConfigurationPropertiesDialog_titleCheckProperties);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        try
        {
            //Check if the configuration is valid
            mCheckConfig = getCheckConfiguration();
            super.okPressed();
        }
        catch (CheckstylePluginException e)
        {
            this.setErrorMessage(e.getLocalizedMessage());
        }
    }

    /**
     * Creates the configuration type specific location editor.
     * 
     * @param configType the configuration type
     */
    private void createLocationEditor(IConfigurationType configType)
    {

        Class editorClass = configType.getLocationEditorClass();

        try
        {
            mLocationEditor = (IConfigurationLocationEditor) editorClass.newInstance();

            //remove old editor
            Control[] controls = mEditorPlaceHolder.getChildren();
            for (int i = 0; i < controls.length; i++)
            {
                controls[i].dispose();
            }

            mLocationEditor.createEditorControl(mEditorPlaceHolder, getShell());

            if (mCheckConfig != null)
            {
                mLocationEditor.setLocation(mCheckConfig.getLocation());
                mLocationEditor.setEditable(mCheckConfig.isEditable());
            }

            mEditorPlaceHolder.redraw();
            mEditorPlaceHolder.update();
            mEditorPlaceHolder.layout();

        }
        catch (Exception ex)
        {
            CheckstyleLog.errorDialog(getShell(), ex, true);
        }
    }

    /**
     * Initialize the dialogs controls with the data.
     */
    private void initialize()
    {

        if (mCheckConfig == null)
        {
            this.setTitle(Messages.CheckConfigurationPropertiesDialog_titleCheckConfig);
            this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgCreateNewCheckConfig);

            IConfigurationType[] types = ConfigurationTypes.getCreatableConfigTypes();
            mConfigType.setInput(types);
            mConfigType.setSelection(new StructuredSelection(types[0]), true);

            createLocationEditor(types[0]);

        }
        else
        {
            this.setTitle(Messages.CheckConfigurationPropertiesDialog_titleCheckConfig);
            this.setMessage(Messages.CheckConfigurationPropertiesDialog_msgEditCheckConfig);

            mConfigType.setInput(new IConfigurationType[] { mCheckConfig.getType() });
            //type of existing configs cannot be changed
            mConfigType.getCombo().setEnabled(false);
            mConfigType.setSelection(new StructuredSelection(mCheckConfig.getType()), true);
            createLocationEditor(mCheckConfig.getType());

            mConfigName.setText(mCheckConfig.getName());
            mConfigName.setEditable(mCheckConfig.isEditable());

            if (mCheckConfig.getDescription() != null)
            {
                mDescription.setText(mCheckConfig.getDescription());
            }
            mDescription.setEditable(mCheckConfig.isEditable());
        }

        //set the logo
        this.setTitleImage(CheckstylePlugin.getLogo());
    }
}