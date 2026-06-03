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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.ui.stats.Messages;

public final class CheckstyleMarkerFilterResourceFilterGroup extends Composite {

  private final Group mFilterComposite;
  private final Button mRadioOnAnyResource;
  private final Button mRadioAnyResourceInSameProject;
  private final Button mRadioSelectedResource;
  private final Button mRadioSelectedResourceAndChildren;
  private final Button mRadioSelectedWorkingSet;
  private final Label mLblSelectedWorkingSet;
  private final Button mBtnWorkingSet;
  private final Button mChkSeverityEnabled;
  private final Button mChkSeverityError;
  private final Button mChkSeverityWarning;
  private final Button mChkSeverityInfo;

  public CheckstyleMarkerFilterResourceFilterGroup(Composite parent, int style,
          Runnable updateControlState, Runnable selectWorkingSet) {
    super(parent, style);
    setLayout(new FillLayout());

    mFilterComposite = new Group(this, SWT.NULL);
    mFilterComposite.setText(Messages.CheckstyleMarkerFilterDialog_groupResourceSetting);
    mFilterComposite.setLayout(new GridLayout(3, false));
    mFilterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mRadioOnAnyResource = createButton(mFilterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnAnyResource,
            GridDataFactory.swtDefaults().span(3, 1));

    mRadioAnyResourceInSameProject = createButton(mFilterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnAnyResourceInSameProject,
            GridDataFactory.swtDefaults().span(3, 1));

    mRadioSelectedResource = createButton(mFilterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResource,
            GridDataFactory.swtDefaults().span(3, 1));

    mRadioSelectedResourceAndChildren = new Button(mFilterComposite, SWT.RADIO);
    mRadioSelectedResourceAndChildren
            .setText(Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResourceAndChilds);
    GridDataFactory.swtDefaults().span(3, 1).applyTo(mRadioSelectedResourceAndChildren);

    mRadioSelectedWorkingSet = createButton(mFilterComposite, SWT.RADIO,
            Messages.CheckstyleMarkerFilterDialog_btnOnWorkingSet, GridDataFactory.swtDefaults());

    mLblSelectedWorkingSet = new Label(mFilterComposite, SWT.NULL);
    mLblSelectedWorkingSet.setLayoutData(
            new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

    mBtnWorkingSet = createButton(mFilterComposite, SWT.PUSH,
            Messages.CheckstyleMarkerFilterDialog_btnSelect,
            GridDataFactory.swtDefaults().span(1, 2));
    mBtnWorkingSet.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> selectWorkingSet.run()));

    Composite severityGroup = new Composite(mFilterComposite, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(4).margins(0, 5).applyTo(severityGroup);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(3, 1).applyTo(severityGroup);

    mChkSeverityEnabled = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnMarkerSeverity,
            GridDataFactory.create(GridData.FILL_HORIZONTAL));
    mChkSeverityEnabled.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> updateControlState.run()));

    mChkSeverityError = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityError, GridDataFactory.swtDefaults());

    mChkSeverityWarning = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityWarning, GridDataFactory.swtDefaults());

    mChkSeverityInfo = createButton(severityGroup, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_btnSeverityInfo, GridDataFactory.swtDefaults());
  }

  public void setFromFilter(int onResource, boolean selectBySeverity, int severity) {
    mRadioOnAnyResource.setSelection(onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE);
    mRadioAnyResourceInSameProject
            .setSelection(onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
    mRadioSelectedResource
            .setSelection(onResource == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
    mRadioSelectedResourceAndChildren
            .setSelection(onResource == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
    mRadioSelectedWorkingSet.setSelection(onResource == CheckstyleMarkerFilter.ON_WORKING_SET);

    mChkSeverityEnabled.setSelection(selectBySeverity);

    mChkSeverityError.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_ERROR) > 0);
    mChkSeverityWarning.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_WARNING) > 0);
    mChkSeverityInfo.setSelection((severity & CheckstyleMarkerFilter.SEVERITY_INFO) > 0);
  }

  public void propagateEnabled(boolean enabled) {
    mFilterComposite.setEnabled(enabled);
    mRadioOnAnyResource.setEnabled(enabled);
    mRadioAnyResourceInSameProject.setEnabled(enabled);
    mRadioSelectedResource.setEnabled(enabled);
    mRadioSelectedResourceAndChildren.setEnabled(enabled);
    mRadioSelectedWorkingSet.setEnabled(enabled);
    mLblSelectedWorkingSet.setEnabled(enabled);
    mBtnWorkingSet.setEnabled(enabled);
    mChkSeverityEnabled.setEnabled(enabled);

    mChkSeverityError.setEnabled(enabled && mChkSeverityEnabled.getSelection());
    mChkSeverityWarning.setEnabled(enabled && mChkSeverityEnabled.getSelection());
    mChkSeverityInfo.setEnabled(enabled && mChkSeverityEnabled.getSelection());
  }

  public void setWorkingSetLabel(String text) {
    mLblSelectedWorkingSet.setText(text);
  }

  public int getOnResource() {
    if (mRadioSelectedResource.getSelection()) {
      return CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY;
    }
    if (mRadioSelectedResourceAndChildren.getSelection()) {
      return CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN;
    }
    if (mRadioAnyResourceInSameProject.getSelection()) {
      return CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT;
    }
    if (mRadioSelectedWorkingSet.getSelection()) {
      return CheckstyleMarkerFilter.ON_WORKING_SET;
    }
    return CheckstyleMarkerFilter.ON_ANY_RESOURCE;
  }

  public int getSeverity() {
    int severity = 0;
    if (mChkSeverityError.getSelection()) {
      severity = severity | CheckstyleMarkerFilter.SEVERITY_ERROR;
    }
    if (mChkSeverityWarning.getSelection()) {
      severity = severity | CheckstyleMarkerFilter.SEVERITY_WARNING;
    }
    if (mChkSeverityInfo.getSelection()) {
      severity = severity | CheckstyleMarkerFilter.SEVERITY_INFO;
    }
    return severity;
  }

  public boolean getSelectBySeverity() {
    return mChkSeverityEnabled.getSelection();
  }

  private static Button createButton(Composite parent, int style, String text, GridDataFactory gridDataFactory) {
    Button button = new Button(parent, style);
    button.setText(text);
    gridDataFactory.applyTo(button);
    return button;
  }

}
