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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.regex.RegExContentAssistProcessor;

/**
 * A string property configuration widget.
 */
public class ConfigPropertyWidgetRegex extends ConfigPropertyWidgetAbstractBase
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

    private Text mTextWidget;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Creates the widget.
     * 
     * @param parent the parent composite
     * @param prop the property
     */
    public ConfigPropertyWidgetRegex(Composite parent, ConfigProperty prop)
    {
        super(parent, prop);
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * {@inheritDoc}
     */
    protected Control getValueWidget(Composite parent)
    {

        if (mTextWidget == null)
        {

            //
            // Create a text entry field.
            //
            mTextWidget = new Text(parent, SWT.SINGLE | SWT.BORDER);
            mTextWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            // integrate content assist
            ContentAssistHandler.createHandlerForText(mTextWidget, createContentAssistant());

            String initValue = getInitValue();
            if (initValue != null)
            {
                mTextWidget.setText(initValue);
            }
        }

        return mTextWidget;
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
    public void restorePropertyDefault()
    {
        String defaultValue = getConfigProperty().getMetaData().getDefaultValue();
        mTextWidget.setText(defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void validate() throws CheckstylePluginException
    {
        try
        {
            //
            // Compile the text to a regex pattern
            //
            Pattern.compile(mTextWidget.getText());
        }
        catch (PatternSyntaxException e)
        {
            CheckstylePluginException.rethrow(e, e.getLocalizedMessage());
        }
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

        IContentAssistProcessor processor = new RegExContentAssistProcessor(true);
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