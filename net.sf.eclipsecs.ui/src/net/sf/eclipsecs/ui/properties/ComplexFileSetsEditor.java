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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Property page.
 */
public class ComplexFileSetsEditor implements IFileSetsEditor {

  private final IProject mProject;

  private Composite mComposite;

  private CheckboxTableViewer mViewer;

  private Button mAddButton;

  private Button mEditButton;

  private Button mRemoveButton;

  private List<FileSet> mFileSets;

  private final CheckstylePropertyPage mPropertyPage;

  /**
   * Creates the ComplexFileSetsEditor.
   *
   * @param propsPage
   *          the property page
   */
  public ComplexFileSetsEditor(CheckstylePropertyPage propsPage) {
    mPropertyPage = propsPage;
    mProject = (IProject) propsPage.getElement();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFileSets(List<FileSet> fileSets) {
    mFileSets = fileSets;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<FileSet> getFileSets() {
    return mFileSets;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Control createContents(Composite parent) throws CheckstylePluginException {

    mComposite = parent;

    Group composite = new Group(parent, SWT.NONE);
    composite.setText(Messages.ComplexFileSetsEditor_titleAdvancedFilesetEditor);

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    composite.setLayout(layout);

    //
    // Create the table of file sets.
    //
    Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);

    GridData data = new GridData(GridData.FILL_BOTH);
    table.setLayoutData(data);

    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);

    TableColumn column1 = new TableColumn(table, SWT.NONE);
    column1.setText(Messages.ComplexFileSetsEditor_colEnabled);
    column1.setResizable(false);

    TableColumn column2 = new TableColumn(table, SWT.NONE);
    column2.setText(Messages.ComplexFileSetsEditor_colFilesetName);

    TableColumn column3 = new TableColumn(table, SWT.NONE);
    column3.setText(Messages.ComplexFileSetsEditor_colConfiguration);

    tableLayout.addColumnData(new ColumnWeightData(20));
    tableLayout.addColumnData(new ColumnWeightData(40));
    tableLayout.addColumnData(new ColumnWeightData(40));

    mViewer = new CheckboxTableViewer(table);
    mViewer.setLabelProvider(new FileSetLabelProvider());
    mViewer.setContentProvider(new ArrayContentProvider());
    mViewer.setComparator(new FileSetViewerSorter());
    mViewer.setInput(mFileSets);

    //
    // Set checked state
    //
    for (FileSet fileSet : mFileSets) {
      mViewer.setChecked(fileSet, fileSet.isEnabled());
    }

    mViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent e) {
        editFileSet();
      }
    });

    mViewer.addCheckStateListener(new ICheckStateListener() {
      @Override
      public void checkStateChanged(CheckStateChangedEvent event) {
        changeEnabledState(event);
      }
    });

    //
    // Build the buttons.
    //
    Composite buttons = new Composite(composite, SWT.NULL);
    buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    buttons.setLayout(layout);

    mAddButton = createPushButton(buttons, Messages.ComplexFileSetsEditor_btnAdd);
    mAddButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event evt) {
        addFileSet();
      }
    });

    mEditButton = createPushButton(buttons, Messages.ComplexFileSetsEditor_btnEdit);
    mEditButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event evt) {
        editFileSet();
      }
    });

    mRemoveButton = createPushButton(buttons, Messages.ComplexFileSetsEditor_btnRemove);
    mRemoveButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event evt) {
        removeFileSet();
      }
    });

    return composite;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void refresh() {
    // NOOP
  }

  /**
   * Utility method that creates a push button instance and sets the default layout data.
   *
   * @param parent
   *          the parent for the new button
   * @param label
   *          the label for the new button
   * @return the newly-created button
   */
  private Button createPushButton(Composite parent, String label) {
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    button.setLayoutData(data);
    return button;
  }

  private void addFileSet() {
    try {
      FileSetEditDialog dialog = new FileSetEditDialog(mComposite.getShell(), null, mProject,
              mPropertyPage);
      if (Window.OK == dialog.open()) {
        FileSet fileSet = dialog.getFileSet();
        mFileSets.add(fileSet);
        mViewer.refresh();
        mViewer.setChecked(fileSet, fileSet.isEnabled());

        mPropertyPage.getContainer().updateButtons();
      }
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(mComposite.getShell(),
              NLS.bind(Messages.errorFailedAddFileset, e.getMessage()), e, true);
    }
  }

  private void editFileSet() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    FileSet fileSet = (FileSet) selection.getFirstElement();
    if (fileSet == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    try {

      FileSetEditDialog dialog = new FileSetEditDialog(mComposite.getShell(), fileSet.clone(),
              mProject, mPropertyPage);
      if (Window.OK == dialog.open()) {
        FileSet newFileSet = dialog.getFileSet();
        mFileSets.remove(fileSet);
        mFileSets.add(newFileSet);
        mViewer.refresh();
        mViewer.setChecked(newFileSet, newFileSet.isEnabled());

        mPropertyPage.getContainer().updateButtons();
      }
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(mComposite.getShell(),
              NLS.bind(Messages.errorFailedEditFileset, e.getMessage()), e, true);
    }
  }

  private void removeFileSet() {
    IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
    FileSet fileSet = (FileSet) selection.getFirstElement();
    if (fileSet == null) {
      //
      // Nothing is selected.
      //
      return;
    }

    mFileSets.remove(fileSet);
    mViewer.refresh();
    mPropertyPage.getContainer().updateButtons();
  }

  private void changeEnabledState(CheckStateChangedEvent event) {
    if (event.getElement() instanceof FileSet) {
      FileSet fileSet = (FileSet) event.getElement();
      fileSet.setEnabled(event.getChecked());
      mViewer.refresh();
    }
  }

  /**
   * Provides the labels for the FileSet list display.
   */
  class FileSetLabelProvider extends LabelProvider implements ITableLabelProvider {

    private final CheckConfigurationLabelProvider mCheckConfigLabelProvider = new CheckConfigurationLabelProvider();

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof FileSet) {
        FileSet fileSet = (FileSet) element;
        switch (columnIndex) {
          case 0:
            result = new String();
            break;

          case 1:
            result = fileSet.getName();
            break;

          case 2:
            result = fileSet.getCheckConfig() != null
                    ? mCheckConfigLabelProvider.getText(fileSet.getCheckConfig())
                    : ""; //$NON-NLS-1$
            break;

          default:
            break;
        }

      }
      return result;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }

  /**
   * Sorts CheckConfiguration objects into their display order.
   */
  public class FileSetViewerSorter extends ViewerComparator {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      int result = 0;

      if ((e1 instanceof FileSet) && (e2 instanceof FileSet)) {
        FileSet fileSet1 = (FileSet) e1;
        FileSet fileSet2 = (FileSet) e2;

        String name1 = fileSet1.getName();
        String name2 = fileSet2.getName();

        result = name1.compareTo(name2);
      }

      return result;
    }
  }
}
