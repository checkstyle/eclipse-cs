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

    public void set(boolean enabled, int onResource, boolean selectBySeverity, int severity,
        boolean filterByRegex) {
        mChkFilterEnabled.setSelection(enabled);
        resourceFilterGroup.setFromFilter(onResource, selectBySeverity, severity);
        mChkSelectByRegex.setSelection(filterByRegex);
        updateControlState();
    }

    public void setWorkingSetLabel(String label) {
        resourceFilterGroup.setWorkingSetLabel(label);
    }

    public void setRegexLabel(String label) {
        mLblRegexFilter.setText(label);
    }

    public boolean getFilterEnabled() {
        return mChkFilterEnabled.getSelection();
    }

    public int getOnResource() {
        return resourceFilterGroup.getOnResource();
    }

    public int getSeverity() {
        return resourceFilterGroup.getSeverity();
    }

    public boolean getSelectBySeverity() {
        return resourceFilterGroup.getSelectBySeverity();
    }

    public boolean getSelectByRegex() {
        return mChkSelectByRegex.getSelection();
    }

}
