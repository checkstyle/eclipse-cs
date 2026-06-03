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

  private CheckstyleMarkerFilterDialogView dialogView;

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

  private void editRegularExpressions() {
    List<String> regex = new ArrayList<>(mRegularExpressions);
    CheckstyleMarkerFilterRegexDialog regexDialog = new CheckstyleMarkerFilterRegexDialog(
            getShell(), regex);
    if (Window.OK == regexDialog.open()) {
      mRegularExpressions = regex;
      initRegexLabel();
    }
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
    mSelectedWorkingSet = mFilter.workingSet();
    initWorkingSetLabel();

    mRegularExpressions = mFilter.filterRegex();
    initRegexLabel();

    dialogView.set(mFilter.enabled(), mFilter.onResource(), mFilter.selectBySeverity(),
            mFilter.severity(), mFilter.filterByRegex());
  }

  /**
   * Updates the filter data from the ui controls.
   */
  private void updateFilterFromUI() {
    mFilter = new CheckstyleMarkerFilter(dialogView.getFilterEnabled(),
            dialogView.getOnResource(), mSelectedWorkingSet,
            dialogView.getSelectBySeverity(), dialogView.getSeverity(),
            dialogView.getSelectByRegex(), mRegularExpressions, mFilter.focusResources());
  }

  /**
   * Initializes the label for the selected working set.
   */
  private void initWorkingSetLabel() {
    if (mSelectedWorkingSet == null) {
      dialogView
              .setWorkingSetLabel(Messages.CheckstyleMarkerFilterDialog_msgNoWorkingSetSelected);
    } else {
      dialogView.setWorkingSetLabel(mSelectedWorkingSet.getName());
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

    dialogView.setRegexLabel(buf.toString());
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
}
