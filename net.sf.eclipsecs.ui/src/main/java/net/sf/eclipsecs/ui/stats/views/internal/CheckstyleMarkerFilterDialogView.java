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
    private final Button mChkFilterEnabled;
    /** The resource filter group. */
    private final CheckstyleMarkerFilterResourceFilterGroup resourceFilterGroup;
    /** The regex group. */
    private final Group mGrpRegex;
    /** The select by regex checkbox. */
    private final Button mChkSelectByRegex;
    /** The regex filter label. */
    private final Label mLblRegexFilter;
    /** The edit regex button. */
    private final Button mBtnEditRegex;

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

        mChkFilterEnabled = new Button(this, SWT.CHECK);
        mChkFilterEnabled.setText(Messages.CheckstyleMarkerFilterDialog_btnEnabled);
        mChkFilterEnabled.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> updateControlState()));

        resourceFilterGroup = new CheckstyleMarkerFilterResourceFilterGroup(this, SWT.NONE,
            this::updateControlState, selectWorkingSet);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(resourceFilterGroup);

        mGrpRegex = new Group(this, SWT.NULL);
        mGrpRegex.setText(Messages.CheckstyleMarkerFilterDialog_lblExcludeMarkers);
        GridLayoutFactory.swtDefaults().numColumns(REGEX_GROUP_NUM_COLUMNS).applyTo(mGrpRegex);
        GridDataFactory.fillDefaults().applyTo(mGrpRegex);

        mChkSelectByRegex = createButton(mGrpRegex, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_lblRegex, GridDataFactory.swtDefaults());

        mLblRegexFilter = new Label(mGrpRegex, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
            .applyTo(mLblRegexFilter);

        mBtnEditRegex = createButton(mGrpRegex, SWT.PUSH,
            Messages.CheckstyleMarkerFilterDialog_btnEdit, GridDataFactory.swtDefaults());
        mBtnEditRegex.addSelectionListener(
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
        resourceFilterGroup.propagateEnabled(mChkFilterEnabled.getSelection());
        mGrpRegex.setEnabled(mChkFilterEnabled.getSelection());
        mChkSelectByRegex.setEnabled(mChkFilterEnabled.getSelection());
        mLblRegexFilter.setEnabled(mChkFilterEnabled.getSelection());
        mBtnEditRegex.setEnabled(mChkFilterEnabled.getSelection());
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
        mChkFilterEnabled.setSelection(enabled);
        resourceFilterGroup.setFromFilter(onResource, selectBySeverity, severity);
        mChkSelectByRegex.setSelection(filterByRegex);
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
        mLblRegexFilter.setText(label);
    }

    /**
     * Returns whether the filter is enabled.
     *
     * @return whether the filter is enabled
     */
    public boolean getFilterEnabled() {
        return mChkFilterEnabled.getSelection();
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
        return mChkSelectByRegex.getSelection();
    }

}
