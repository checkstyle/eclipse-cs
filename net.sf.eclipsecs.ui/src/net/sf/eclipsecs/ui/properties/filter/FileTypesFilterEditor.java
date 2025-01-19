//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.ui.Messages;

/**
 * Editor dialog for the package filter.
 *
 */
public class FileTypesFilterEditor implements IFilterEditor {

  /** the dialog for this editor. */
  private FileTypesDialog mDialog;

  /** the filter data. */
  private List<String> mFilterData;

  @Override
  public int openEditor(Shell parent) {
    this.mDialog = new FileTypesDialog(parent, mFilterData);

    // open the dialog
    int retCode = this.mDialog.open();

    // actualize the filter data
    if (Window.OK == retCode) {
      this.mFilterData = this.getFilterDataFromDialog();
    }

    return retCode;
  }

  @Override
  public void setInputProject(IProject input) {
    // NOOP
  }

  @Override
  public void setFilterData(List<String> filterData) {
    this.mFilterData = new ArrayList<>(filterData);
  }

  @Override
  public List<String> getFilterData() {
    return this.mFilterData;
  }

  /**
   * Helper method to extract the edited data from the dialog.
   *
   * @return the filter data
   */
  private List<String> getFilterDataFromDialog() {
    return mFilterData;
  }

  /**
   * Dialog to edit file types to check.
   *
   */
  private class FileTypesDialog extends Dialog {

    private ListViewer mListViewer;

    private Button mAddButton;

    private Button mRemoveButton;

    private Text mFileTypeText;

    private List<String> mFileTypesList;

    /**
     * Creates a file matching pattern editor dialog.
     *
     * @param parentShell
     *          the parent shell
     * @param pattern
     *          the pattern
     */
    public FileTypesDialog(Shell parentShell, List<String> fileTypes) {
      super(parentShell);
      mFileTypesList = fileTypes;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite composite = (Composite) super.createDialogArea(parent);

      Composite main = new Composite(composite, SWT.NONE);
      GridLayout layout = new GridLayout(2, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      main.setLayout(layout);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      main.setLayoutData(gridData);

      mFileTypeText = new Text(main, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      mFileTypeText.setLayoutData(gridData);

      mAddButton = new Button(main, SWT.PUSH);
      mAddButton.setText(Messages.FileTypesFilterEditor_btnAdd);
      gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.verticalAlignment = SWT.TOP;
      mAddButton.setLayoutData(gridData);
      mAddButton.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          String text = mFileTypeText.getText();
          if (text.trim().length() > 0) {
            mFileTypesList.add(mFileTypeText.getText());
            mListViewer.refresh();
            mFileTypeText.setText(""); //$NON-NLS-1$
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
          // NOOP
        }
      });

      mListViewer = new ListViewer(main, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      mListViewer.setLabelProvider(new LabelProvider());
      mListViewer.setContentProvider(new ArrayContentProvider());
      mListViewer.setInput(mFileTypesList);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.heightHint = 100;
      gridData.widthHint = 150;
      gridData.grabExcessVerticalSpace = true;
      mListViewer.getControl().setLayoutData(gridData);

      mRemoveButton = new Button(main, SWT.PUSH);
      mRemoveButton.setText(Messages.FileTypesFilterEditor_btnRemove);
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
      return main;
    }

    @Override
    protected void okPressed() {
      super.okPressed();
    }

    /**
     * Over-rides method from Window to configure the shell (e.g. the enclosing
     * window).
     */
    @Override
    protected void configureShell(Shell shell) {
      super.configureShell(shell);
      shell.setText(Messages.FileTypesFilterEditor_title);
    }
  }
}
