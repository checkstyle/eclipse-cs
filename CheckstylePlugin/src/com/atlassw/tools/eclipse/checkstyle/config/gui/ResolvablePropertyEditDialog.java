//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.gui;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.atlassw.tools.eclipse.checkstyle.util.SWTUtil;

/**
 * Property page.
 */
public class ResolvablePropertyEditDialog extends TitleAreaDialog
{

    private Text mTxtName;

    private Text mTxtValue;

    private ResolvableProperty mProperty;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Constructor for SamplePropertyPage.
     * 
     * @param parent Parent shell for the dialog window.
     * @param prop Property to be edited.
     */
    ResolvablePropertyEditDialog(Shell parent, ResolvableProperty prop)
    {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        mProperty = prop;
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent)
    {

        Composite composite = (Composite) super.createDialogArea(parent);
        this.setTitle(Messages.ResolvablePropertyEditDialog_titleMessageArea);
        this.setMessage(Messages.ResolvablePropertyEditDialog_msgEditProperty);

        Composite dialog = new Composite(composite, SWT.NONE);
        dialog.setLayout(new GridLayout(2, false));
        dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label lblName = new Label(dialog, SWT.NULL);
        lblName.setText(Messages.ResolvablePropertyEditDialog_lblName);
        mTxtName = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        mTxtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTxtName.setText(mProperty.getPropertyName() != null ? mProperty.getPropertyName() : ""); //$NON-NLS-1$

        Label lblValue = new Label(dialog, SWT.NULL);
        lblValue.setText(Messages.ResolvablePropertyEditDialog_lblValue);
        mTxtValue = new Text(dialog, SWT.SINGLE | SWT.BORDER);
        mTxtValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTxtValue.setText(mProperty.getValue() != null ? mProperty.getValue() : ""); //$NON-NLS-1$

        // integrate content assist
        ContentAssistHandler.createHandlerForText(mTxtValue, createContentAssistant());

        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#create()
     */
    public void create()
    {
        super.create();
        SWTUtil.addResizeSupport(this, CheckstylePlugin.getDefault().getDialogSettings(),
                ResolvablePropertyEditDialog.class.getName());
    }

    /**
     * {@inheritDoc}
     */
    protected void okPressed()
    {

        if (StringUtils.trimToNull(mTxtName.getText()) == null)
        {
            this.setErrorMessage(Messages.ResolvablePropertyEditDialog_msgMissingName);
            return;
        }
        if (StringUtils.trimToNull(mTxtValue.getText()) == null)
        {
            this.setErrorMessage(Messages.ResolvablePropertyEditDialog_msgMissingValue);
            return;
        }

        //
        // Get the entered value.
        //
        mProperty.setPropertyName(mTxtName.getText());
        mProperty.setValue(mTxtValue.getText());

        super.okPressed();
    }

    /**
     * {@inheritDoc}
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(Messages.ResolvablePropertyEditDialog_titleDialog);
    }

    /**
     * Creates the content assistant.
     * 
     * @return the content assistant
     */
    private SubjectControlContentAssistant createContentAssistant()
    {

        final SubjectControlContentAssistant contentAssistant = new SubjectControlContentAssistant();

        contentAssistant.setRestoreCompletionProposalSize(CheckstylePlugin.getDefault()
                .getDialogSettings());

        IContentAssistProcessor processor = new PropertiesContentAssistProcessor();
        contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        contentAssistant
                .setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        contentAssistant.setInformationControlCreator(new IInformationControlCreator()
        {
            /*
             * @see IInformationControlCreator#createInformationControl(Shell)
             */
            public IInformationControl createInformationControl(Shell parent)
            {
                return new DefaultInformationControl(parent);
            }
        });

        return contentAssistant;
    }

}