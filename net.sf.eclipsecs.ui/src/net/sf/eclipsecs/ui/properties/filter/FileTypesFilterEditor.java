//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.ui.Messages;

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

/**
 * Editor dialog for the package filter.
 * 
 * @author Lars Ködderitzsch
 */
public class FileTypesFilterEditor implements IFilterEditor {

  /** the dialog for this editor. */
  private FileTypesDialog mDialog;

  /** the filter data. */
  private List<String> mFilterData;

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void setInputProject(IProject input) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFilterData(List<String> filterData) {
    this.mFilterData = new ArrayList<>(filterData);
  }

  /**
   * {@inheritDoc}
   */
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
   * @author Lars Ködderitzsch
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

    /**
     * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite composite = (Composite) super.createDialogArea(parent);

      Composite main = new Composite(composite, SWT.NONE);
      GridLayout layout = new GridLayout(2, false);
      layout.marginHeight = 0;
      layout.marginWidth = 0;
      main.setLayout(layout);
      GridData gd = new GridData(GridData.FILL_BOTH);
      main.setLayoutData(gd);

      mFileTypeText = new Text(main, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      mFileTypeText.setLayoutData(gd);

      mAddButton = new Button(main, SWT.PUSH);
      mAddButton.setText(Messages.FileTypesFilterEditor_btnAdd);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.verticalAlignment = SWT.TOP;
      mAddButton.setLayoutData(gd);
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
      gd = new GridData(GridData.FILL_BOTH);
      gd.heightHint = 100;
      gd.widthHint = 150;
      gd.grabExcessVerticalSpace = true;
      mListViewer.getControl().setLayoutData(gd);

      mRemoveButton = new Button(main, SWT.PUSH);
      mRemoveButton.setText(Messages.FileTypesFilterEditor_btnRemove);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.verticalAlignment = SWT.TOP;
      mRemoveButton.setLayoutData(gd);
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

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
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
