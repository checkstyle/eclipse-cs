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

package com.atlassw.tools.eclipse.checkstyle.config.gui.widgets;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;

/**
 * A string property configuration widget.
 */
public class ConfigPropertyWidgetFile extends ConfigPropertyWidgetAbstractBase
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    private Composite mContents;

    private Text mTextWidget;

    private Button mBtnBrowse;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Creates the widget.
     * 
     * @param parent the parent composite
     * @param prop the property
     */
    public ConfigPropertyWidgetFile(Composite parent, ConfigProperty prop)
    {
        super(parent, prop);
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * @see ConfigPropertyWidgetAbstractBase#getValueWidget(org.eclipse.swt.widgets.Composite)
     */
    protected Control getValueWidget(Composite parent)
    {

        if (mContents == null)
        {

            mContents = new Composite(parent, SWT.NULL);
            mContents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            mContents.setLayout(layout);

            mTextWidget = new Text(mContents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            mTextWidget.setLayoutData(gd);

            mBtnBrowse = new Button(mContents, SWT.PUSH);
            mBtnBrowse.setText(Messages.ConfigPropertyWidgetFile_btnBrowse0);
            mBtnBrowse.setLayoutData(new GridData());

            mBtnBrowse.addSelectionListener(new SelectionListener()
            {

                public void widgetSelected(SelectionEvent e)
                {
                    FileDialog fileDialog = new FileDialog(mTextWidget.getShell());
                    fileDialog.setFileName(mTextWidget.getText());

                    String file = fileDialog.open();
                    if (null != file)
                    {
                        mTextWidget.setText(file);
                    }
                }

                public void widgetDefaultSelected(SelectionEvent e)
                {
                // NOOP
                }
            });

            String initValue = getInitValue();
            if (initValue != null)
            {
                mTextWidget.setText(initValue);
            }
        }

        return mContents;
    }

    /**
     * {@inheritDoc}
     */
    public String getValue()
    {
        String result = mTextWidget.getText();
        if (result == null)
        {
            result = ""; //$NON-NLS-1$
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled)
    {
        mTextWidget.setEnabled(enabled);
        mBtnBrowse.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    public void restorePropertyDefault()
    {
        String defaultValue = getConfigProperty().getMetaData().getDefaultValue();
        mTextWidget.setText(defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
    }
}