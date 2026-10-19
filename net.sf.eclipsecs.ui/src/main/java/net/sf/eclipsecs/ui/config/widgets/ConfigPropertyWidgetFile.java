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

package net.sf.eclipsecs.ui.config.widgets;

import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.PropertiesContentAssistProcessor;

/**
 * A string property configuration widget.
 */
public final class ConfigPropertyWidgetFile extends AbstractConfigPropertyWidget {

    /** The contents composite. */
    private Composite contents;

    /** The text widget. */
    private Text textWidget;

    /** The browse button. */
    private Button btnBrowse;

    /**
     * Creates the widget.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the property
     */
    private ConfigPropertyWidgetFile(Composite parent, ConfigProperty prop) {
        super(parent, prop);
    }

    /**
     * Creates a file property widget.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the property
     * @return the created widget
     */
    public static ConfigPropertyWidgetFile create(Composite parent, ConfigProperty prop) {
        return new ConfigPropertyWidgetFile(parent, prop);
    }

    @Override
    protected Control getValueWidget(Composite parent) {
        if (contents == null) {
            contents = new Composite(parent, SWT.NULL);
            GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(contents);
            GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(contents);

            textWidget = new Text(contents, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
            GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(textWidget);

            // integrate content assist
            ContentAssistHandler.createHandlerForText(textWidget, createContentAssistant());

            btnBrowse = new Button(contents, SWT.PUSH);
            btnBrowse.setText(Messages.ConfigPropertyWidgetFile_btnBrowse0);
            GridDataFactory.swtDefaults().applyTo(btnBrowse);

            btnBrowse.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
                final FileDialog fileDialog = new FileDialog(textWidget.getShell());
                fileDialog.setFileName(textWidget.getText());
                final String file = fileDialog.open();
                if (file != null) {
                    textWidget.setText(file);
                }
            }));

            final String initValue = getInitValue();
            if (initValue != null) {
                textWidget.setText(initValue);
            }
        }

        return contents;
    }

    @Override
    public String getValue() {
        String result = textWidget.getText();
        if (result == null) {
            result = "";
        }
        return result;
    }

    @Override
    public void setEnabled(boolean enabled) {
        textWidget.setEnabled(enabled);
        btnBrowse.setEnabled(enabled);
    }

    @Override
    public void restorePropertyDefault() {
        final ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
        String defaultValue = metadata.getDefaultValue();
        if (metadata.getOverrideDefault() != null) {
            defaultValue = metadata.getOverrideDefault();
        }
        if (defaultValue == null) {
            defaultValue = "";
        }
        textWidget.setText(defaultValue);
    }

    /**
     * Creates the content assistant.
     *
     * @return the content assistant
     */
    private SubjectControlContentAssistant createContentAssistant() {

        final SubjectControlContentAssistant contentAssistant =
            new SubjectControlContentAssistant();

        contentAssistant
            .setRestoreCompletionProposalSize(CheckstyleUIPlugin.getDefault().getDialogSettings());

        final IContentAssistProcessor processor = new PropertiesContentAssistProcessor();
        contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        contentAssistant
            .setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        contentAssistant.setInformationControlCreator(DefaultInformationControl::new);

        return contentAssistant;
    }

}
