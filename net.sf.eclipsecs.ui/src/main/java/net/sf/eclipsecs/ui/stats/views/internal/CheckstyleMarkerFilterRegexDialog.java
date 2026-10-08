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

package net.sf.eclipsecs.ui.stats.views.internal;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.util.regex.RegexCompletionProposalFactory;

/**
 * Dialog to edit regular expressions to filter by.
 *
 */
public class CheckstyleMarkerFilterRegexDialog extends TitleAreaDialog {

    /** The list viewer. */
    private ListViewer listViewer;

    /** The add button. */
    private Button addButton;

    /** The remove button. */
    private Button removeButton;

    /** The regex text input. */
    private Text regexText;

    /** The file types list. */
    private List<String> fileTypesList;

    /**
     * Creates a file matching pattern editor dialog.
     *
     * @param parentShell
     *            the parent shell
     * @param fileTypes
     *            the file types
     */
    public CheckstyleMarkerFilterRegexDialog(Shell parentShell, List<String> fileTypes) {
        super(parentShell);
        fileTypesList = fileTypes;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);

        final Composite main = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        main.setLayout(layout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        main.setLayoutData(gridData);

        final Composite controls = new Composite(main, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        controls.setLayout(layout);
        controls.setLayoutData(new GridData(GridData.FILL_BOTH));

        createButtons(main);

        regexText = new Text(controls, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        regexText.setLayoutData(gridData);

        listViewer =
            new ListViewer(controls, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        listViewer.setLabelProvider(new LabelProvider());
        listViewer.setContentProvider(ArrayContentProvider.getInstance());
        listViewer.setInput(fileTypesList);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        listViewer.getControl().setLayoutData(gridData);

        // integrate content assist
        RegexCompletionProposalFactory.createForText(regexText);

        this.setTitle(Messages.CheckstyleMarkerFilterDialog_titleRegexEditor);
        this.setMessage(Messages.CheckstyleMarkerFilterDialog_msgEditRegex);

        return main;
    }

    /**
     * Creates the add and remove buttons for the dialog.
     *
     * @param parent
     *            the parent composite
     * @return the composite holding the buttons
     */
    private Composite createButtons(Composite parent) {
        final Composite buttons = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText(Messages.CheckstyleMarkerFilterDialog_btnAdd);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = SWT.TOP;
        addButton.setLayoutData(gridData);
        addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            final String text = regexText.getText();
            if (text.trim().length() > 0 && checkPatternValidity(text)) {
                fileTypesList.add(text);
                listViewer.refresh();
                regexText.setText("");
            }
        }));

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText(Messages.CheckstyleMarkerFilterDialog_btnRemove);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = SWT.TOP;
        removeButton.setLayoutData(gridData);
        removeButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            fileTypesList.remove(listViewer.getStructuredSelection().getFirstElement());
            listViewer.refresh();
        }));

        return buttons;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    /**
     * Over-rides method from Window to configure the shell (e.g. the enclosing window).
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.CheckstyleMarkerFilterDialog_titleRegexEditor);
    }

    /**
     * Checks whether the given text is a valid regular expression, showing an error message when it
     * is not.
     *
     * @param text
     *            the text to validate
     * @return true if the text is a valid pattern, false otherwise
     */
    private boolean checkPatternValidity(String text) {
        boolean valid;
        try {
            Pattern.compile(text);
            valid = true;
        }
        catch (PatternSyntaxException exception) {
            CheckstyleMarkerFilterRegexDialog.this.setErrorMessage(NLS.bind(
                Messages.CheckstyleMarkerFilterDialog_msgInvalidRegex,
                exception.getLocalizedMessage()));
            valid = false;
        }
        return valid;
    }

}
