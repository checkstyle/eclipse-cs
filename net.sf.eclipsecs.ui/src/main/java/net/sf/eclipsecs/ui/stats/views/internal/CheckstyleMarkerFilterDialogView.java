///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.ui.stats.views.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.ui.stats.Messages;

/**
 * Composite implementing the user interface of the Checkstyle marker filter dialog, combining the
 * filter enablement checkbox, the resource and severity filter group and the regular expression
 * based exclusion settings.
 */
public final class CheckstyleMarkerFilterDialogView extends Composite {

    /** Number of columns for the regex group. */
    private static final int REGEX_GROUP_NUM_COLUMNS = 3;

    /** The filter enabled checkbox. */
    private final Button chkFilterEnabled;
    /** The resource filter group. */
    private final CheckstyleMarkerFilterResourceFilterGroup resourceFilterGroup;
    /** The regex group. */
    private final Group grpRegex;
    /** The select by regex checkbox. */
    private final Button chkSelectByRegex;
    /** The regex filter label. */
    private final Label lblRegexFilter;
    /** The edit regex button. */
    private final Button btnEditRegex;

    /**
     * Creates the checkstyle marker filter dialog view.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the widget style
     * @param selectWorkingSet
     *            callback to select a working set
     * @param editRegularExpressions
     *            callback to edit the regular expressions
     */
    public CheckstyleMarkerFilterDialogView(Composite parent, int style, Runnable selectWorkingSet,
        Runnable editRegularExpressions) {
        super(parent, style);

        GridLayoutFactory.swtDefaults().applyTo(this);

        chkFilterEnabled = new Button(this, SWT.CHECK);
        chkFilterEnabled.setText(Messages.CheckstyleMarkerFilterDialog_btnEnabled);
        chkFilterEnabled.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> updateControlState()));

        resourceFilterGroup = new CheckstyleMarkerFilterResourceFilterGroup(this, SWT.NONE,
            this::updateControlState, selectWorkingSet);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(resourceFilterGroup);

        grpRegex = new Group(this, SWT.NULL);
        grpRegex.setText(Messages.CheckstyleMarkerFilterDialog_lblExcludeMarkers);
        GridLayoutFactory.swtDefaults().numColumns(REGEX_GROUP_NUM_COLUMNS).applyTo(grpRegex);
        GridDataFactory.fillDefaults().applyTo(grpRegex);

        chkSelectByRegex = createButton(grpRegex, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_lblRegex, GridDataFactory.swtDefaults());

        lblRegexFilter = new Label(grpRegex, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
            .applyTo(lblRegexFilter);

        btnEditRegex = createButton(grpRegex, SWT.PUSH,
            Messages.CheckstyleMarkerFilterDialog_btnEdit, GridDataFactory.swtDefaults());
        btnEditRegex.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> editRegularExpressions.run()));
    }

    /**
     * Creates a button applying the given grid data factory.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the button style
     * @param text
     *            the button text
     * @param gridDataFactory
     *            the grid data factory to apply
     * @return the created button
     */
    private static Button createButton(Composite parent, int style, String text,
        GridDataFactory gridDataFactory) {
        final Button button = new Button(parent, style);
        button.setText(text);
        gridDataFactory.applyTo(button);
        return button;
    }

    /**
     * Updates the enablement state of the controls.
     */
    private void updateControlState() {
        resourceFilterGroup.propagateEnabled(chkFilterEnabled.getSelection());
        grpRegex.setEnabled(chkFilterEnabled.getSelection());
        chkSelectByRegex.setEnabled(chkFilterEnabled.getSelection());
        lblRegexFilter.setEnabled(chkFilterEnabled.getSelection());
        btnEditRegex.setEnabled(chkFilterEnabled.getSelection());
    }

    /**
     * Sets the filter state of the view.
     *
     * @param enabled
     *            whether the filter is enabled
     * @param onResource
     *            the resource filter type
     * @param selectBySeverity
     *            whether to filter by severity
     * @param severity
     *            the severity
     * @param filterByRegex
     *            whether to filter by regular expression
     */
    public void set(boolean enabled, int onResource, boolean selectBySeverity, int severity,
        boolean filterByRegex) {
        chkFilterEnabled.setSelection(enabled);
        resourceFilterGroup.setFromFilter(onResource, selectBySeverity, severity);
        chkSelectByRegex.setSelection(filterByRegex);
        updateControlState();
    }

    /**
     * Sets the working set label.
     *
     * @param label
     *            the working set label
     */
    public void setWorkingSetLabel(String label) {
        resourceFilterGroup.setWorkingSetLabel(label);
    }

    /**
     * Sets the regular expression label.
     *
     * @param label
     *            the regular expression label
     */
    public void setRegexLabel(String label) {
        lblRegexFilter.setText(label);
    }

    /**
     * Returns whether the filter is enabled.
     *
     * @return whether the filter is enabled
     */
    public boolean getFilterEnabled() {
        return chkFilterEnabled.getSelection();
    }

    /**
     * Returns the resource filter type.
     *
     * @return the resource filter type
     */
    public int getOnResource() {
        return resourceFilterGroup.getOnResource();
    }

    /**
     * Returns the selected severity.
     *
     * @return the selected severity
     */
    public int getSeverity() {
        return resourceFilterGroup.getSeverity();
    }

    /**
     * Returns whether to filter by severity.
     *
     * @return whether to filter by severity
     */
    public boolean getSelectBySeverity() {
        return resourceFilterGroup.getSelectBySeverity();
    }

    /**
     * Returns whether to filter by regular expression.
     *
     * @return whether to filter by regular expression
     */
    public boolean getSelectByRegex() {
        return chkSelectByRegex.getSelection();
    }

}
