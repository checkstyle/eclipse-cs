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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.util.regex.RegexCompletionProposalFactory;

/**
 * Dialog to edit the marker filter.
 *
 */
public class CheckstyleMarkerFilterDialog extends TitleAreaDialog {

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

  private Group mGrpRegex;

  private Button mChkSelectByRegex;

  private Label mLblRegexFilter;

  private Button mBtnEditRegex;

  private Button mBtnDefault;

  /** The filter to be edited. */
  private CheckstyleMarkerFilter mFilter;

  /** the selected working set. */
  private IWorkingSet mSelectedWorkingSet;

  /** The controller of this dialog. */
  private PageController mController = new PageController();

  /** The regular expressions to filter by. */
  private List<String> mRegularExpressions;

  //
  // constructors
  //

  /**
   * Creates the filter dialog.
   *
   * @param shell
   *          the parent shell
   * @param filter
   *          the filter instance
   */
  public CheckstyleMarkerFilterDialog(Shell shell, CheckstyleMarkerFilter filter) {

    super(shell);
    setHelpAvailable(false);
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
  public CheckstyleMarkerFilter getFilter() {
    return mFilter;
  }

  @Override
  protected Control createDialogArea(Composite parent) {

    Composite composite = (Composite) super.createDialogArea(parent);

    Composite dialog = new Composite(composite, SWT.NONE);
    dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
    dialog.setLayout(new GridLayout(1, false));

    mChkFilterEnabled = new Button(dialog, SWT.CHECK);
    mChkFilterEnabled.setText(Messages.CheckstyleMarkerFilterDialog_btnEnabled);
    mChkFilterEnabled.addSelectionListener(mController);

    Group onResourceGroup = new Group(dialog, SWT.NULL);
    onResourceGroup.setText(Messages.CheckstyleMarkerFilterDialog_groupResourceSetting);
    onResourceGroup.setLayout(new GridLayout(3, false));
    onResourceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mFilterComposite = onResourceGroup;

    mRadioOnAnyResource = new Button(onResourceGroup, SWT.RADIO);
    mRadioOnAnyResource.setText(Messages.CheckstyleMarkerFilterDialog_btnOnAnyResource);
    GridData gridData = new GridData();
    gridData.horizontalSpan = 3;
    mRadioOnAnyResource.setLayoutData(gridData);

    mRadioAnyResourceInSameProject = new Button(onResourceGroup, SWT.RADIO);
    mRadioAnyResourceInSameProject
            .setText(Messages.CheckstyleMarkerFilterDialog_btnOnAnyResourceInSameProject);
    gridData = new GridData();
    gridData.horizontalSpan = 3;
    mRadioAnyResourceInSameProject.setLayoutData(gridData);

    mRadioSelectedResource = new Button(onResourceGroup, SWT.RADIO);
    mRadioSelectedResource.setText(Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResource);
    gridData = new GridData();
    gridData.horizontalSpan = 3;
    mRadioSelectedResource.setLayoutData(gridData);

    mRadioSelectedResourceAndChildren = new Button(onResourceGroup, SWT.RADIO);
    mRadioSelectedResourceAndChildren
            .setText(Messages.CheckstyleMarkerFilterDialog_btnOnSelectedResourceAndChilds);
    gridData = new GridData();
    gridData.horizontalSpan = 3;
    mRadioSelectedResourceAndChildren.setLayoutData(gridData);

    mRadioSelectedWorkingSet = new Button(onResourceGroup, SWT.RADIO);
    mRadioSelectedWorkingSet.setText(Messages.CheckstyleMarkerFilterDialog_btnOnWorkingSet);
    mRadioSelectedWorkingSet.setLayoutData(new GridData());

    mLblSelectedWorkingSet = new Label(onResourceGroup, SWT.NULL);
    mLblSelectedWorkingSet.setLayoutData(
            new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

    mBtnWorkingSet = new Button(onResourceGroup, SWT.PUSH);
    mBtnWorkingSet.setText(Messages.CheckstyleMarkerFilterDialog_btnSelect);
    gridData = new GridData();
    gridData.horizontalSpan = 1;
    gridData.verticalSpan = 2;
    mBtnWorkingSet.setLayoutData(gridData);
    mBtnWorkingSet.addSelectionListener(mController);

    Composite severityGroup = new Composite(onResourceGroup, SWT.NULL);
    GridLayout layout = new GridLayout(4, false);
    layout.marginWidth = 0;
    severityGroup.setLayout(layout);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 3;
    severityGroup.setLayoutData(gridData);

    mChkSeverityEnabled = new Button(severityGroup, SWT.CHECK);
    mChkSeverityEnabled.setText(Messages.CheckstyleMarkerFilterDialog_btnMarkerSeverity);
    mChkSeverityEnabled.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    mChkSeverityEnabled.addSelectionListener(mController);

    mChkSeverityError = new Button(severityGroup, SWT.CHECK);
    mChkSeverityError.setText(Messages.CheckstyleMarkerFilterDialog_btnSeverityError);
    mChkSeverityError.setLayoutData(new GridData());

    mChkSeverityWarning = new Button(severityGroup, SWT.CHECK);
    mChkSeverityWarning.setText(Messages.CheckstyleMarkerFilterDialog_btnSeverityWarning);
    mChkSeverityWarning.setLayoutData(new GridData());

    mChkSeverityInfo = new Button(severityGroup, SWT.CHECK);
    mChkSeverityInfo.setText(Messages.CheckstyleMarkerFilterDialog_btnSeverityInfo);
    mChkSeverityInfo.setLayoutData(new GridData());

    mGrpRegex = new Group(dialog, SWT.NULL);
    mGrpRegex.setText(Messages.CheckstyleMarkerFilterDialog_lblExcludeMarkers);
    layout = new GridLayout(3, false);
    mGrpRegex.setLayout(layout);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    mGrpRegex.setLayoutData(gridData);

    mChkSelectByRegex = new Button(mGrpRegex, SWT.CHECK);
    mChkSelectByRegex.setText(Messages.CheckstyleMarkerFilterDialog_lblRegex);
    mChkSelectByRegex.setLayoutData(new GridData());

    mLblRegexFilter = new Label(mGrpRegex, SWT.NULL);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 100;
    mLblRegexFilter.setLayoutData(gridData);

    mBtnEditRegex = new Button(mGrpRegex, SWT.PUSH);
    mBtnEditRegex.setText(Messages.CheckstyleMarkerFilterDialog_btnEdit);
    mBtnEditRegex.setLayoutData(new GridData());
    mBtnEditRegex.addSelectionListener(mController);

    // init the controls
    updateUIFromFilter();

    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
    this.setTitle(Messages.CheckstyleMarkerFilterDialog_title);
    this.setMessage(Messages.CheckstyleMarkerFilterDialog_titleMessage);

    return composite;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {

    mBtnDefault = createButton(parent, IDialogConstants.BACK_ID,
            Messages.CheckstyleMarkerFilterDialog_btnRestoreDefault, false);
    mBtnDefault.addSelectionListener(mController);

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

    mChkFilterEnabled.setSelection(mFilter.isEnabled());

    mRadioOnAnyResource
            .setSelection(mFilter.getOnResource() == CheckstyleMarkerFilter.ON_ANY_RESOURCE);
    mRadioAnyResourceInSameProject.setSelection(
            mFilter.getOnResource() == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
    mRadioSelectedResource.setSelection(
            mFilter.getOnResource() == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
    mRadioSelectedResourceAndChildren.setSelection(
            mFilter.getOnResource() == CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
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

    mChkSelectByRegex.setSelection(mFilter.isFilterByRegex());
    mRegularExpressions = mFilter.getFilterRegex();
    initRegexLabel();

    mController.updateControlState();
  }

  /**
   * Updates the filter data from the ui controls.
   */
  private void updateFilterFromUI() {

    mFilter.setEnabled(mChkFilterEnabled.getSelection());

    if (mRadioSelectedResource.getSelection()) {
      mFilter.setOnResource(CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
    } else if (mRadioSelectedResourceAndChildren.getSelection()) {
      mFilter.setOnResource(CheckstyleMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
    } else if (mRadioAnyResourceInSameProject.getSelection()) {
      mFilter.setOnResource(CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
    } else if (mRadioSelectedWorkingSet.getSelection()) {
      mFilter.setOnResource(CheckstyleMarkerFilter.ON_WORKING_SET);
    } else {
      mFilter.setOnResource(CheckstyleMarkerFilter.ON_ANY_RESOURCE);
    }

    mFilter.setWorkingSet(mSelectedWorkingSet);

    mFilter.setSelectBySeverity(mChkSeverityEnabled.getSelection());
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
    mFilter.setSeverity(severity);

    mFilter.setFilterByRegex(mChkSelectByRegex.getSelection());
    mFilter.setFilterRegex(mRegularExpressions);
  }

  /**
   * Initializes the label for the selected working set.
   */
  private void initWorkingSetLabel() {

    if (mSelectedWorkingSet == null) {
      mLblSelectedWorkingSet.setText(Messages.CheckstyleMarkerFilterDialog_msgNoWorkingSetSelected);
    } else {
      mLblSelectedWorkingSet.setText(mSelectedWorkingSet.getName());
    }
  }

  /**
   * Initializes the label for the regular expressions.
   */
  private void initRegexLabel() {

    StringBuilder buf = new StringBuilder();

    int size = mRegularExpressions != null ? mRegularExpressions.size() : 0;
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", "); //$NON-NLS-1$
      }
      buf.append(mRegularExpressions.get(i));
    }

    if (size == 0) {
      buf.append(Messages.CheckstyleMarkerFilterDialog_msgNoRegexDefined);
    }

    mLblRegexFilter.setText(buf.toString());
  }

  /**
   * The controller for this dialog.
   *
   */
  private class PageController implements SelectionListener {

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (e.widget == mChkFilterEnabled || e.widget == mChkSeverityEnabled) {
        updateControlState();
      } else if (mBtnDefault == e.widget) {
        mFilter.resetState();
        updateUIFromFilter();
      } else if (mBtnWorkingSet == e.widget) {
        IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench().getWorkingSetManager()
                .createWorkingSetSelectionDialog(getShell(), false);

        if (mSelectedWorkingSet != null) {
          dialog.setSelection(new IWorkingSet[] { mSelectedWorkingSet });
        }
        if (dialog.open() == Window.OK) {
          IWorkingSet[] result = dialog.getSelection();
          if (result != null && result.length > 0) {
            mSelectedWorkingSet = result[0];
          } else {
            mSelectedWorkingSet = null;
          }
          initWorkingSetLabel();
        }
      } else if (mBtnEditRegex == e.widget) {
        List<String> regex = new ArrayList<>(mRegularExpressions);
        RegexDialog dialog = new RegexDialog(getShell(), regex);
        if (Window.OK == dialog.open()) {
          mRegularExpressions = regex;
          initRegexLabel();
        }
      }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      // NOOP
    }

    /**
     * updates the enablement state of the controls.
     */
    private void updateControlState() {

      mFilterComposite.setEnabled(mChkFilterEnabled.getSelection());
      mRadioOnAnyResource.setEnabled(mChkFilterEnabled.getSelection());
      mRadioAnyResourceInSameProject.setEnabled(mChkFilterEnabled.getSelection());
      mRadioSelectedResource.setEnabled(mChkFilterEnabled.getSelection());
      mRadioSelectedResourceAndChildren.setEnabled(mChkFilterEnabled.getSelection());
      mRadioSelectedWorkingSet.setEnabled(mChkFilterEnabled.getSelection());
      mLblSelectedWorkingSet.setEnabled(mChkFilterEnabled.getSelection());
      mBtnWorkingSet
              .setEnabled(mChkFilterEnabled.getSelection() && mChkFilterEnabled.getSelection());
      mChkSeverityEnabled.setEnabled(mChkFilterEnabled.getSelection());

      mChkSeverityError
              .setEnabled(mChkFilterEnabled.getSelection() && mChkSeverityEnabled.getSelection());
      mChkSeverityWarning
              .setEnabled(mChkFilterEnabled.getSelection() && mChkSeverityEnabled.getSelection());
      mChkSeverityInfo
              .setEnabled(mChkFilterEnabled.getSelection() && mChkSeverityEnabled.getSelection());

      mGrpRegex.setEnabled(mChkFilterEnabled.getSelection());
      mChkSelectByRegex.setEnabled(mChkFilterEnabled.getSelection());
      mLblRegexFilter.setEnabled(mChkFilterEnabled.getSelection());
      mBtnEditRegex.setEnabled(mChkFilterEnabled.getSelection());
    }
  }

  /**
   * Dialog to edit regular expressions to filter by.
   *
   */
  private class RegexDialog extends TitleAreaDialog {

    private ListViewer mListViewer;

    private Button mAddButton;

    private Button mRemoveButton;

    private Text mRegexText;

    private List<String> mFileTypesList;

    /**
     * Creates a file matching pattern editor dialog.
     *
     * @param parentShell
     *          the parent shell
     * @param pattern
     *          the pattern
     */
    public RegexDialog(Shell parentShell, List<String> fileTypes) {
      super(parentShell);
      mFileTypesList = fileTypes;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite composite = (Composite) super.createDialogArea(parent);

      Composite main = new Composite(composite, SWT.NONE);
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

      final Composite buttons = new Composite(main, SWT.NONE);
      layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      buttons.setLayout(layout);
      buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

      mRegexText = new Text(controls, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.grabExcessHorizontalSpace = true;
      mRegexText.setLayoutData(gridData);

      mAddButton = new Button(buttons, SWT.PUSH);
      mAddButton.setText(Messages.CheckstyleMarkerFilterDialog_btnAdd);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.verticalAlignment = SWT.TOP;
      mAddButton.setLayoutData(gridData);
      mAddButton.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          String text = mRegexText.getText();
          if (text.trim().length() > 0) {

            try {
              // check for the patterns validity
              Pattern.compile(text);

              mFileTypesList.add(text);
              mListViewer.refresh();
              mRegexText.setText(""); //$NON-NLS-1$

            } catch (PatternSyntaxException ex) {
              RegexDialog.this.setErrorMessage(
                      NLS.bind(Messages.CheckstyleMarkerFilterDialog_msgInvalidRegex,
                              ex.getLocalizedMessage()));
            }
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
          // NOOP
        }
      });

      mListViewer = new ListViewer(controls, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      mListViewer.setLabelProvider(new LabelProvider());
      mListViewer.setContentProvider(new ArrayContentProvider());
      mListViewer.setInput(mFileTypesList);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      gridData.widthHint = 150;
      gridData.grabExcessHorizontalSpace = true;
      mListViewer.getControl().setLayoutData(gridData);

      mRemoveButton = new Button(buttons, SWT.PUSH);
      mRemoveButton.setText(Messages.CheckstyleMarkerFilterDialog_btnRemove);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.verticalAlignment = SWT.TOP;
      mRemoveButton.setLayoutData(gridData);
      mRemoveButton.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          IStructuredSelection selection = (IStructuredSelection) mListViewer.getSelection();
          mFileTypesList.remove(selection.getFirstElement());
          mListViewer.refresh();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
          // NOOP
        }
      });

      // integrate content assist
      RegexCompletionProposalFactory.createForText(mRegexText);

      this.setTitle(Messages.CheckstyleMarkerFilterDialog_titleRegexEditor);
      this.setMessage(Messages.CheckstyleMarkerFilterDialog_msgEditRegex);

      return main;
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
  }
}
