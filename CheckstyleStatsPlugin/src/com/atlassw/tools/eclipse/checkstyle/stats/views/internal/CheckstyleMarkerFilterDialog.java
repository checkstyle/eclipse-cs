//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats.views.internal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * Dialog to edit the marker filter.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleMarkerFilterDialog extends TitleAreaDialog
{

    //
    // attributes
    //

    private Button mChkFilterEnabled;

    private Button mRadioOnAnyResource;

    private Button mRadioAnyResourceInSameProject;

    private Button mRadioSelectedResource;

    private Button mRadioSelectedResourceAndChildren;

    private Button mRadioSelectedWorkingSet;

    private Label mLblSelectedWorkingSet;

    private Button mBtnWorkingSet;

    private Button mChkSeverityEnabled;

    private Button mChkSeverityError;

    private Button mChkSeverityWarning;

    private Button mChkSeverityInfo;

    private Composite mFilterComposite;

    private Button mBtnDefault;

    /** The filter to be edited. */
    private CheckstyleMarkerFilter mFilter;

    /** the selected working set. */
    private IWorkingSet mSelectedWorkingSet;

    /** The controller of this dialog. */
    private PageController mController = new PageController();

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
    public CheckstyleMarkerFilterDialog(Shell shell,
        CheckstyleMarkerFilter filter)
    {

        super(shell);
        mFilter = filter;
    }

    //
    // methods
    //

    /**
     * Returns the edited filter.
     * 
     * @return the edited filter
     */
    public CheckstyleMarkerFilter getFilter()
    {
        return mFilter;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent)
    {

        Composite composite = (Composite) super.createDialogArea(parent);

        Composite dialog = new Composite(composite, SWT.NONE);
        dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
        dialog.setLayout(new GridLayout(1, false));

        mChkFilterEnabled = new Button(dialog, SWT.CHECK);
        mChkFilterEnabled.setText("Enabled");
        mChkFilterEnabled.addSelectionListener(mController);

        Group onResourceGroup = new Group(dialog, SWT.NULL);
        onResourceGroup.setText("Show statistics");
        onResourceGroup.setLayout(new GridLayout(3, false));
        onResourceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mFilterComposite = onResourceGroup;

        mRadioOnAnyResource = new Button(onResourceGroup, SWT.RADIO);
        mRadioOnAnyResource.setText("On any resource");
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        mRadioOnAnyResource.setLayoutData(gd);

        mRadioAnyResourceInSameProject = new Button(onResourceGroup, SWT.RADIO);
        mRadioAnyResourceInSameProject
            .setText("On any resource in same project");
        gd = new GridData();
        gd.horizontalSpan = 3;
        mRadioAnyResourceInSameProject.setLayoutData(gd);

        mRadioSelectedResource = new Button(onResourceGroup, SWT.RADIO);
        mRadioSelectedResource.setText("On selected resource only");
        gd = new GridData();
        gd.horizontalSpan = 3;
        mRadioSelectedResource.setLayoutData(gd);

        mRadioSelectedResourceAndChildren = new Button(onResourceGroup,
            SWT.RADIO);
        mRadioSelectedResourceAndChildren
            .setText("On selected resource and its children");
        gd = new GridData();
        gd.horizontalSpan = 3;
        mRadioSelectedResourceAndChildren.setLayoutData(gd);

        mRadioSelectedWorkingSet = new Button(onResourceGroup, SWT.RADIO);
        mRadioSelectedWorkingSet.setText("On working set:");
        mRadioSelectedWorkingSet.setLayoutData(new GridData());

        mLblSelectedWorkingSet = new Label(onResourceGroup, SWT.NULL);
        mLblSelectedWorkingSet.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

        mBtnWorkingSet = new Button(onResourceGroup, SWT.PUSH);
        mBtnWorkingSet.setText("Select...");
        gd = new GridData();
        gd.horizontalSpan = 1;
        gd.verticalSpan = 2;
        mBtnWorkingSet.setLayoutData(gd);
        mBtnWorkingSet.addSelectionListener(mController);

        Composite severityGroup = new Composite(onResourceGroup, SWT.NULL);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = 0;
        severityGroup.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        severityGroup.setLayoutData(gd);

        mChkSeverityEnabled = new Button(severityGroup, SWT.CHECK);
        mChkSeverityEnabled.setText("On Checkstyle warnings with severity:");
        mChkSeverityEnabled
            .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mChkSeverityEnabled.addSelectionListener(mController);

        mChkSeverityError = new Button(severityGroup, SWT.CHECK);
        mChkSeverityError.setText("Error");
        mChkSeverityError.setLayoutData(new GridData());

        mChkSeverityWarning = new Button(severityGroup, SWT.CHECK);
        mChkSeverityWarning.setText("Warning");
        mChkSeverityWarning.setLayoutData(new GridData());

        mChkSeverityInfo = new Button(severityGroup, SWT.CHECK);
        mChkSeverityInfo.setText("Info");
        mChkSeverityInfo.setLayoutData(new GridData());

        // init the controls
        updateUIFromFilter();

        this.setTitle("Checkstyle Statistics filter");
        this
            .setMessage("Use this filter settings to specify on which resources/severities\nthe statistics will be compiled. ");

        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {

        mBtnDefault = createButton(parent, IDialogConstants.BACK_ID, "Default",
            false);
        mBtnDefault.addSelectionListener(mController);

        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
            true);
        createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText("Checkstyle Statistics Filter");
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {
        updateFilterFromUI();
        super.okPressed();
    }

    /**
     * Updates the ui controls from the filter data.
     */
    private void updateUIFromFilter()
    {

        mChkFilterEnabled.setSelection(mFilter.isEnabled());

        mRadioOnAnyResource
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_ANY_RESOURCE);
        mRadioAnyResourceInSameProject
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        mRadioSelectedResource
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        mRadioSelectedResourceAndChildren
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        mRadioSelectedWorkingSet
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_WORKING_SET);

        mSelectedWorkingSet = mFilter.getWorkingSet();
        initWorkingSetLabel();

        mChkSeverityEnabled.setSelection(mFilter.getSelectBySeverity());

        mChkSeverityError
            .setSelection((mFilter.getSeverity() & CheckstyleMarkerFilter.SEVERITY_ERROR) > 0);
        mChkSeverityWarning
            .setSelection((mFilter.getSeverity() & CheckstyleMarkerFilter.SEVERITY_WARNING) > 0);
        mChkSeverityInfo
            .setSelection((mFilter.getSeverity() & CheckstyleMarkerFilter.SEVERITY_INFO) > 0);

        mController.updateControlState();
    }

    /**
     * Updates the filter data from the ui controls.
     */
    private void updateFilterFromUI()
    {

        mFilter.setEnabled(mChkFilterEnabled.getSelection());

        if (mRadioSelectedResource.getSelection())
        {
            mFilter
                .setOnResource(CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        }
        else if (mRadioSelectedResourceAndChildren.getSelection())
        {
            mFilter
                .setOnResource(CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        }
        else if (mRadioAnyResourceInSameProject.getSelection())
        {
            mFilter
                .setOnResource(CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        }
        else if (mRadioSelectedWorkingSet.getSelection())
        {
            mFilter.setOnResource(CheckstyleMarkerFilter.ON_WORKING_SET);
        }
        else
        {
            mFilter.setOnResource(CheckstyleMarkerFilter.ON_ANY_RESOURCE);
        }

        mFilter.setWorkingSet(mSelectedWorkingSet);

        mFilter.setSelectBySeverity(mChkSeverityEnabled.getSelection());
        int severity = 0;
        if (mChkSeverityError.getSelection())
        {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_ERROR;
        }
        if (mChkSeverityWarning.getSelection())
        {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_WARNING;
        }
        if (mChkSeverityInfo.getSelection())
        {
            severity = severity | CheckstyleMarkerFilter.SEVERITY_INFO;
        }
        mFilter.setSeverity(severity);
    }

    /**
     * Initializes the label for the selected working set.
     */
    private void initWorkingSetLabel()
    {

        if (mSelectedWorkingSet == null)
        {
            mLblSelectedWorkingSet.setText("<no woking set selected>");
        }
        else
        {
            mLblSelectedWorkingSet.setText((mSelectedWorkingSet.getName()));
        }
    }

    /**
     * The controller for this dialog.
     * 
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController implements SelectionListener
    {

        /**
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e)
        {
            if (e.widget == mChkFilterEnabled
                || e.widget == mChkSeverityEnabled)
            {
                updateControlState();
            }
            else if (mBtnDefault == e.widget)
            {
                mFilter.resetState();
                updateUIFromFilter();
            }
            else if (mBtnWorkingSet == e.widget)
            {
                IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench()
                    .getWorkingSetManager().createWorkingSetSelectionDialog(
                        getShell(), false);

                if (mSelectedWorkingSet != null)
                {
                    dialog
                        .setSelection(new IWorkingSet[] { mSelectedWorkingSet });
                }
                if (dialog.open() == Window.OK)
                {
                    IWorkingSet[] result = dialog.getSelection();
                    if (result != null && result.length > 0)
                    {
                        mSelectedWorkingSet = result[0];
                    }
                    else
                    {
                        mSelectedWorkingSet = null;
                    }
                    initWorkingSetLabel();
                }
            }
        }

        /**
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetDefaultSelected(SelectionEvent e)
        {
            // NOOP
        }

        /**
         * updates the enablement state of the controls.
         */
        private void updateControlState()
        {

            mFilterComposite.setEnabled(mChkFilterEnabled.getSelection());
            mRadioOnAnyResource.setEnabled(mChkFilterEnabled.getSelection());
            mRadioAnyResourceInSameProject.setEnabled(mChkFilterEnabled
                .getSelection());
            mRadioSelectedResource.setEnabled(mChkFilterEnabled.getSelection());
            mRadioSelectedResourceAndChildren.setEnabled(mChkFilterEnabled
                .getSelection());
            mRadioSelectedWorkingSet.setEnabled(mChkFilterEnabled
                .getSelection());
            mLblSelectedWorkingSet.setEnabled(mChkFilterEnabled.getSelection());
            mBtnWorkingSet.setEnabled(mChkFilterEnabled.getSelection()
                && mChkFilterEnabled.getSelection());
            mChkSeverityEnabled.setEnabled(mChkFilterEnabled.getSelection());

            mChkSeverityError.setEnabled(mChkFilterEnabled.getSelection()
                && mChkSeverityEnabled.getSelection());
            mChkSeverityWarning.setEnabled(mChkFilterEnabled.getSelection()
                && mChkSeverityEnabled.getSelection());
            mChkSeverityInfo.setEnabled(mChkFilterEnabled.getSelection()
                && mChkSeverityEnabled.getSelection());
        }
    }
}
