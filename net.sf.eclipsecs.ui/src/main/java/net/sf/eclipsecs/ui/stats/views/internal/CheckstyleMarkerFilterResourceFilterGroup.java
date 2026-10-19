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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.ui.stats.Messages;

/**
 * Composite of the marker filter dialog offering radio buttons to select the resources the filter
 * applies to, together with checkboxes to restrict filtering by marker severities.
 */
public final class CheckstyleMarkerFilterResourceFilterGroup extends Composite {

    /** Number of columns of the radio button group. */
    private static final int NUM_COLUMNS = 3;
    /** Number of columns of the severity group. */
    private static final int SEVERITY_GROUP_NUM_COLUMS = 4;

    /** The filter composite group. */
    private final Group filterComposite;
    /** The "on any resource" radio button. */
    private final Button radioOnAnyResource;
    /** The "any resource in same project" radio button. */
    private final Button radioAnyResourceInSameProject;
    /** The "selected resource" radio button. */
    private final Button radioSelectedResource;
    /** The "selected resource and children" radio button. */
    private final Button radioSelectedResourceAndChildren;
    /** The "selected working set" radio button. */
    private final Button radioSelectedWorkingSet;
    /** The selected working set label. */
    private final Label lblSelectedWorkingSet;
    /** The working set button. */
    private final Button btnWorkingSet;
    /** The severity enabled checkbox. */
    private final Button chkSeverityEnabled;
    /** The severity error checkbox. */
    private final Button chkSeverityError;
    /** The severity warning checkbox. */
    private final Button chkSeverityWarning;
    /** The severity info checkbox. */
    private final Button chkSeverityInfo;

    /**
     * Creates the marker filter resource filter group.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the composite style bits
     * @param updateControlState
     *            runnable invoked when the control state should be updated
     * @param selectWorkingSet
     *            runnable invoked to select a working set
     */
    public CheckstyleMarkerFilterResourceFilterGroup(Composite parent, int style,
        Runnable updateControlState, Runnable selectWorkingSet) {
        super(parent, style);
        setLayout(new FillLayout());

        filterComposite = new Group(this, SWT.NULL);
        filterComposite.setText(Messages.CheckstyleMarkerFilterDialog_groupResourceSetting);
        filterComposite.setLayout(new GridLayout(NUM_COLUMNS, false));
        filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        radioOnAnyResource = createButton(filterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnAnyResource,
            GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1));

        radioAnyResourceInSameProject = createButton(filterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnAnyResourceInSameProject,
            GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1));

        radioSelectedResource = createButton(filterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResource,
            GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1));

        radioSelectedResourceAndChildren = new Button(filterComposite, SWT.RADIO);
        radioSelectedResourceAndChildren
            .setText(Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResourceAndChilds);
        GridDataFactory.swtDefaults().span(NUM_COLUMNS, 1)
            .applyTo(radioSelectedResourceAndChildren);

        radioSelectedWorkingSet = createButton(filterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnWorkingSet, GridDataFactory.swtDefaults());

        lblSelectedWorkingSet = new Label(filterComposite, SWT.NULL);
        lblSelectedWorkingSet.setLayoutData(
            new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

        btnWorkingSet = createButton(filterComposite, SWT.PUSH,
            Messages.CheckstyleMarkerFilterDialog_btnSelect, GridDataFactory.swtDefaults());
        btnWorkingSet.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> selectWorkingSet.run()));

        final Composite severityGroup = new Composite(filterComposite, SWT.NULL);
        GridLayoutFactory.fillDefaults().numColumns(SEVERITY_GROUP_NUM_COLUMS)
            .applyTo(severityGroup);
        GridDataFactory.fillDefaults().span(NUM_COLUMNS, 1)
            .applyTo(severityGroup);

        chkSeverityEnabled = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnMarkerSeverity,
            GridDataFactory.create(GridData.FILL_HORIZONTAL));
        chkSeverityEnabled.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> updateControlState.run()));

        chkSeverityError = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityError, GridDataFactory.swtDefaults());

        chkSeverityWarning = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityWarning,
            GridDataFactory.swtDefaults());

        chkSeverityInfo = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityInfo, GridDataFactory.swtDefaults());
    }

    /**
     * Sets the filter fields to reflect the given filter settings.
     *
     * @param onResource
     *            the resource restriction flag
     * @param selectBySeverity
     *            whether to restrict filter by severity
     * @param severity
     *            the severity bit mask
     */
    public void setFromFilter(int onResource, boolean selectBySeverity, int severity) {
        radioOnAnyResource.setSelection(onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE);
        radioAnyResourceInSameProject
            .setSelection(onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        radioSelectedResource
            .setSelection(onResource == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        radioSelectedResourceAndChildren
            .setSelection(onResource == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        radioSelectedWorkingSet.setSelection(onResource == CheckstyleMarkerFilter.ON_WORKING_SET);

        chkSeverityEnabled.setSelection(selectBySeverity);

        chkSeverityError.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_ERROR) > 0);
        chkSeverityWarning.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_WARNING) > 0);
        chkSeverityInfo.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_INFO) > 0);
    }

    /**
     * Enables or disables the filter widgets.
     *
     * @param enabled
     *            true to enable, false to disable
     */
    public void propagateEnabled(boolean enabled) {
        filterComposite.setEnabled(enabled);
        radioOnAnyResource.setEnabled(enabled);
        radioAnyResourceInSameProject.setEnabled(enabled);
        radioSelectedResource.setEnabled(enabled);
        radioSelectedResourceAndChildren.setEnabled(enabled);
        radioSelectedWorkingSet.setEnabled(enabled);
        lblSelectedWorkingSet.setEnabled(enabled);
        btnWorkingSet.setEnabled(enabled);
        chkSeverityEnabled.setEnabled(enabled);

        chkSeverityError.setEnabled(enabled && chkSeverityEnabled.getSelection());
        chkSeverityWarning.setEnabled(enabled && chkSeverityEnabled.getSelection());
        chkSeverityInfo.setEnabled(enabled && chkSeverityEnabled.getSelection());
    }

    /**
     * Sets the text of the selected working set label.
     *
     * @param text
     *            the text to display
     */
    public void setWorkingSetLabel(String text) {
        lblSelectedWorkingSet.setText(text);
    }

    /**
     * Returns the selected resource restriction.
     *
     * @return the selected resource restriction
     */
    public int getOnResource() {
        final int onResource;
        if (radioSelectedResource.getSelection()) {
            onResource = CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY;
        }
        else if (radioSelectedResourceAndChildren.getSelection()) {
            onResource = CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN;
        }
        else if (radioAnyResourceInSameProject.getSelection()) {
            onResource = CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
        }
        else if (radioSelectedWorkingSet.getSelection()) {
            onResource = CheckstyleMarkerFilter.ON_WORKING_SET;
        }
        else {
            onResource = CheckstyleMarkerFilter.ON_ANY_RESOURCE;
        }
        return onResource;
    }

    /**
     * Returns the selected marker severities as a bit mask.
     *
     * @return the selected marker severities bit mask
     */
    public int getSeverity() {
        int severity = 0;
        if (chkSeverityError.getSelection()) {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_ERROR;
        }
        if (chkSeverityWarning.getSelection()) {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_WARNING;
        }
        if (chkSeverityInfo.getSelection()) {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_INFO;
        }
        return severity;
    }

    /**
     * Returns whether filtering is restricted by severity.
     *
     * @return true if filtering is restricted by severity
     */
    public boolean getSelectBySeverity() {
        return chkSeverityEnabled.getSelection();
    }

    /**
     * Creates a button with the given text and applies the layout data.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the button style bits
     * @param text
     *            the button text
     * @param gridDataFactory
     *            the layout data factory to apply
     * @return the created button
     */
    private static Button createButton(Composite parent, int style, String text,
        GridDataFactory gridDataFactory) {
        final Button button = new Button(parent, style);
        button.setText(text);
        gridDataFactory.applyTo(button);
        return button;
    }

}
