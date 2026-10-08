//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.stats.Messages;

/**
 * Dialog to edit the marker filter.
 *
 */
public class CheckstyleMarkerFilterDialog extends TitleAreaDialog {

    //
    // attributes
    //

    /** The dialog view. */
    private CheckstyleMarkerFilterDialogView dialogView;

    /** The default button. */
    private Button btnDefault;

    /** The filter to be edited. */
    private CheckstyleMarkerFilter filter;

    /** The selected working set. */
    private IWorkingSet selectedWorkingSet;

    /** The regular expressions to filter by. */
    private List<String> regularExpressions;

    //
    // constructors
    //

    /**
     * Creates the filter dialog.
     *
     * @param shell
     *            the parent shell
     * @param filter
     *            the filter instance
     */
    public CheckstyleMarkerFilterDialog(Shell shell, CheckstyleMarkerFilter filter) {

        super(shell);
        setHelpAvailable(false);
        this.filter = filter;
    }

    //
    // methods
    //

    /**
     * Returns the edited filter.
     *
     * @return the edited filter
     */
    public CheckstyleMarkerFilter getFilter() {
        return filter;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);

        this.dialogView = new CheckstyleMarkerFilterDialogView(composite, SWT.NONE,
            this::selectWorkingSet, this::editRegularExpressions);
        GridDataFactory.create(GridData.FILL_BOTH).applyTo(dialogView);

        // init the controls
        updateUIFromFilter();

        setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
        setTitle(Messages.CheckstyleMarkerFilterDialog_title);
        setMessage(Messages.CheckstyleMarkerFilterDialog_titleMessage);

        return composite;
    }

    /**
     * Opens the dialog to edit the regular expressions used for filtering.
     */
    private void editRegularExpressions() {
        final List<String> regex = new ArrayList<>(regularExpressions);
        final CheckstyleMarkerFilterRegexDialog regexDialog =
            new CheckstyleMarkerFilterRegexDialog(getShell(), regex);
        if (Window.OK == regexDialog.open()) {
            regularExpressions = regex;
            initRegexLabel();
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        btnDefault = createButton(parent, IDialogConstants.BACK_ID,
            Messages.CheckstyleMarkerFilterDialog_btnRestoreDefault, false);
        btnDefault.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            filter = CheckstyleMarkerFilter.resetState(filter.focusResources());
            updateUIFromFilter();
        }));

        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.CheckstyleMarkerFilterDialog_btnShellTitle);
    }

    @Override
    protected void okPressed() {
        updateFilterFromUI();
        super.okPressed();
    }

    /**
     * Updates the ui controls from the filter data.
     */
    private void updateUIFromFilter() {
        selectedWorkingSet = filter.workingSet();
        initWorkingSetLabel();

        regularExpressions = filter.filterRegex();
        initRegexLabel();

        dialogView.set(filter.enabled(), filter.onResource(), filter.selectBySeverity(),
            filter.severity(), filter.filterByRegex());
    }

    /**
     * Updates the filter data from the ui controls.
     */
    private void updateFilterFromUI() {
        filter =
            new CheckstyleMarkerFilter(dialogView.getFilterEnabled(), dialogView.getOnResource(),
                selectedWorkingSet, dialogView.getSelectBySeverity(), dialogView.getSeverity(),
                dialogView.getSelectByRegex(), regularExpressions, filter.focusResources());
    }

    /**
     * Initializes the label for the selected working set.
     */
    private void initWorkingSetLabel() {
        if (selectedWorkingSet == null) {
            dialogView
                .setWorkingSetLabel(Messages.CheckstyleMarkerFilterDialog_msgNoWorkingSetSelected);
        }
        else {
            dialogView.setWorkingSetLabel(selectedWorkingSet.getName());
        }
    }

    /**
     * Initializes the label for the regular expressions.
     */
    private void initRegexLabel() {

        final StringBuilder buf = new StringBuilder();

        final int size;
        if (regularExpressions != null) {
            size = regularExpressions.size();
        }
        else {
            size = 0;
        }
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                buf.append(", ");
            }
            buf.append(regularExpressions.get(index));
        }

        if (size == 0) {
            buf.append(Messages.CheckstyleMarkerFilterDialog_msgNoRegexDefined);
        }

        dialogView.setRegexLabel(buf.toString());
    }

    /**
     * Opens the working set selection dialog and updates the selected working set.
     */
    private void selectWorkingSet() {
        final IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench().getWorkingSetManager()
            .createWorkingSetSelectionDialog(getShell(), false);

        if (selectedWorkingSet != null) {
            dialog.setSelection(new IWorkingSet[] {
                selectedWorkingSet,
            });
        }
        if (dialog.open() == Window.OK) {
            final IWorkingSet[] result = dialog.getSelection();
            if (result != null && result.length > 0) {
                selectedWorkingSet = result[0];
            }
            else {
                selectedWorkingSet = null;
            }
            initWorkingSetLabel();
        }
    }

}
