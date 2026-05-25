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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
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

  private ResourceFilterGroup resourceFilterGroup;

  private Button mChkFilterEnabled;

  private Group mGrpRegex;

  private Button mChkSelectByRegex;

  private Label mLblRegexFilter;

  private Button mBtnEditRegex;

  private Button mBtnDefault;

  /** The filter to be edited. */
  private CheckstyleMarkerFilter mFilter;

  /** the selected working set. */
  private IWorkingSet mSelectedWorkingSet;

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
    mChkFilterEnabled.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> updateControlState()));

    resourceFilterGroup = new ResourceFilterGroup(dialog, SWT.NONE, this::updateControlState,
            this::selectWorkingSet);
    GridDataFactory.fillDefaults().applyTo(resourceFilterGroup);

    mGrpRegex = new Group(dialog, SWT.NULL);
    mGrpRegex.setText(Messages.CheckstyleMarkerFilterDialog_lblExcludeMarkers);
    mGrpRegex.setLayout(new GridLayout(3, false));
    mGrpRegex.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mChkSelectByRegex = createButton(mGrpRegex, SWT.CHECK,
            Messages.CheckstyleMarkerFilterDialog_lblRegex, GridDataFactory.swtDefaults());

    mLblRegexFilter = new Label(mGrpRegex, SWT.NULL);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).hint(100, SWT.DEFAULT).applyTo(mLblRegexFilter);

    mBtnEditRegex = createButton(mGrpRegex, SWT.PUSH, Messages.CheckstyleMarkerFilterDialog_btnEdit,
            GridDataFactory.swtDefaults());
    mBtnEditRegex.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      List<String> regex = new ArrayList<>(mRegularExpressions);
      RegexDialog regexDialog = new RegexDialog(getShell(), regex);
      if (Window.OK == regexDialog.open()) {
        mRegularExpressions = regex;
        initRegexLabel();
      }
    }));

    // init the controls
    updateUIFromFilter();

    setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
    setTitle(Messages.CheckstyleMarkerFilterDialog_title);
    setMessage(Messages.CheckstyleMarkerFilterDialog_titleMessage);

    return composite;
  }

  private static Button createButton(Composite parent, int style, String text, GridDataFactory gridDataFactory) {
    Button button = new Button(parent, style);
    button.setText(text);
    gridDataFactory.applyTo(button);
    return button;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    mBtnDefault = createButton(parent, IDialogConstants.BACK_ID,
            Messages.CheckstyleMarkerFilterDialog_btnRestoreDefault, false);
    mBtnDefault.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      mFilter = CheckstyleMarkerFilter.resetState(mFilter.focusResources());
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
    mChkFilterEnabled.setSelection(mFilter.enabled());

    resourceFilterGroup.setFromFilter(mFilter.onResource(), mFilter.selectBySeverity(),
            mFilter.severity());

    mSelectedWorkingSet = mFilter.workingSet();
    initWorkingSetLabel();

    mChkSelectByRegex.setSelection(mFilter.filterByRegex());
    mRegularExpressions = mFilter.filterRegex();
    initRegexLabel();

    updateControlState();
  }

  /**
   * Updates the filter data from the ui controls.
   */
  private void updateFilterFromUI() {
    mFilter = new CheckstyleMarkerFilter(mChkFilterEnabled.getSelection(),
            resourceFilterGroup.getOnResource(), mSelectedWorkingSet,
            resourceFilterGroup.getSelectBySeverity(), resourceFilterGroup.getSeverity(),
            mChkSelectByRegex.getSelection(), mRegularExpressions, mFilter.focusResources());
  }

  /**
   * Initializes the label for the selected working set.
   */
  private void initWorkingSetLabel() {
    if (mSelectedWorkingSet == null) {
      resourceFilterGroup
              .setWorkingSetLabel(Messages.CheckstyleMarkerFilterDialog_msgNoWorkingSetSelected);
    } else {
      resourceFilterGroup.setWorkingSetLabel(mSelectedWorkingSet.getName());
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
   * Updates the enablement state of the controls.
   */
  private void updateControlState() {
    resourceFilterGroup.propagateEnabled(mChkFilterEnabled.getSelection());
    mGrpRegex.setEnabled(mChkFilterEnabled.getSelection());
    mChkSelectByRegex.setEnabled(mChkFilterEnabled.getSelection());
    mLblRegexFilter.setEnabled(mChkFilterEnabled.getSelection());
    mBtnEditRegex.setEnabled(mChkFilterEnabled.getSelection());
  }

  private void selectWorkingSet() {
    IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench().getWorkingSetManager()
            .createWorkingSetSelectionDialog(getShell(), false);

    if (mSelectedWorkingSet != null) {
      dialog.setSelection(new IWorkingSet[] {
          mSelectedWorkingSet,
      });
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
  }

  private static class ResourceFilterGroup extends Composite {

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

    public ResourceFilterGroup(Composite parent, int style, Runnable updateControlState, Runnable selectWorkingSet) {
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

  }

  /**
   * Dialog to edit regular expressions to filter by.
   *
   */
  private static class RegexDialog extends TitleAreaDialog {

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

      createButtons(main);

      mRegexText = new Text(controls, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.grabExcessHorizontalSpace = true;
      mRegexText.setLayoutData(gridData);

      mListViewer = new ListViewer(controls, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      mListViewer.setLabelProvider(new LabelProvider());
      mListViewer.setContentProvider(new ArrayContentProvider());
      mListViewer.setInput(mFileTypesList);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      gridData.widthHint = 150;
      gridData.grabExcessHorizontalSpace = true;
      mListViewer.getControl().setLayoutData(gridData);

      // integrate content assist
      RegexCompletionProposalFactory.createForText(mRegexText);

      this.setTitle(Messages.CheckstyleMarkerFilterDialog_titleRegexEditor);
      this.setMessage(Messages.CheckstyleMarkerFilterDialog_msgEditRegex);

      return main;
    }

    private Composite createButtons(Composite parent) {
      final Composite buttons = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(1, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      buttons.setLayout(layout);
      buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

      mAddButton = new Button(buttons, SWT.PUSH);
      mAddButton.setText(Messages.CheckstyleMarkerFilterDialog_btnAdd);
      GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.verticalAlignment = SWT.TOP;
      mAddButton.setLayoutData(gridData);
      mAddButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        String text = mRegexText.getText();
        if (text.trim().length() > 0 && checkPatternValidity(text)) {
          mFileTypesList.add(text);
          mListViewer.refresh();
          mRegexText.setText("");
        }
      }));

      mRemoveButton = new Button(buttons, SWT.PUSH);
      mRemoveButton.setText(Messages.CheckstyleMarkerFilterDialog_btnRemove);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.verticalAlignment = SWT.TOP;
      mRemoveButton.setLayoutData(gridData);
      mRemoveButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        mFileTypesList.remove(mListViewer.getStructuredSelection().getFirstElement());
        mListViewer.refresh();
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

    private boolean checkPatternValidity(String text) {
      try {
        Pattern.compile(text);
        return true;
      } catch (PatternSyntaxException ex) {
        RegexDialog.this.setErrorMessage(NLS.bind(
                Messages.CheckstyleMarkerFilterDialog_msgInvalidRegex, ex.getLocalizedMessage()));
        return false;
      }
    }
  }
}
