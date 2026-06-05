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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationLabelProvider;

/**
 * Property page.
 */
public class ComplexFileSetsEditor implements FileSetsEditor {

  private final PropertyPageContext propertyPageContext;
  private ComplexFileSetsEditorView editorView;
  private Shell shell;
  private List<FileSet> mFileSets;

  /**
   * Creates the ComplexFileSetsEditor.
   *
   * @param propsPage
   *          the property page
   */
  public ComplexFileSetsEditor(PropertyPageContext propertyPageContext) {
    this.propertyPageContext = propertyPageContext;
  }

  @Override
  public void setFileSets(List<FileSet> fileSets) {
    mFileSets = fileSets;

  }

  @Override
  public List<FileSet> getFileSets() {
    return mFileSets;
  }

  @Override
  public Control createContents(Composite parent) {
    this.shell = parent.getShell();

    editorView = new ComplexFileSetsEditorView(parent, SWT.NONE, this::changeEnabledState,
            this::editFileSet, this::addFileSet, this::removeFileSet, mFileSets);

    return editorView;
  }

  @Override
  public void refresh() {
    // NOOP
  }

  private void addFileSet() {
    try {
      FileSetEditDialog dialog = new FileSetEditDialog(shell, null, propertyPageContext);
      if (Window.OK == dialog.open()) {
        FileSet fileSet = dialog.getFileSet();
        mFileSets.add(fileSet);
        editorView.refresh();
        editorView.setChecked(fileSet, fileSet.isEnabled());

        propertyPageContext.updateButtons();
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUiPlugin.errorDialog(shell,
              NLS.bind(Messages.errorFailedAddFileset, ex.getMessage()), ex, true);
    }
  }

  private void editFileSet(FileSet fileSet) {
    if (fileSet != null) {
      try {
        FileSetEditDialog dialog = new FileSetEditDialog(shell, fileSet.clone(),
                propertyPageContext);
        if (Window.OK == dialog.open()) {
          FileSet newFileSet = dialog.getFileSet();
          mFileSets.remove(fileSet);
          mFileSets.add(newFileSet);
          editorView.refresh();
          editorView.setChecked(newFileSet, newFileSet.isEnabled());

          propertyPageContext.updateButtons();
        }
      } catch (CheckstylePluginException ex) {
        CheckstyleUiPlugin.errorDialog(shell,
                NLS.bind(Messages.errorFailedEditFileset, ex.getMessage()), ex, true);
      }
    }
  }

  private void removeFileSet(FileSet fileSet) {
    if (fileSet != null) {
      mFileSets.remove(fileSet);
      editorView.refresh();
      propertyPageContext.updateButtons();
    }
  }

  private void changeEnabledState(CheckStateChangedEvent event) {
    if (event.getElement() instanceof FileSet) {
      FileSet fileSet = (FileSet) event.getElement();
      fileSet.setEnabled(event.getChecked());
      editorView.refresh();
    }
  }

  /**
   * Provides the labels for the FileSet list display.
   */
  public static final class FileSetLabelProvider extends LabelProvider implements ITableLabelProvider {

    public static final FileSetLabelProvider INSTANCE = new FileSetLabelProvider();

    private FileSetLabelProvider() {

    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String columnText;
      if (element instanceof FileSet fileSet) {
        columnText = switch (columnIndex) {
          case 0 -> new String();
          case 1 -> fileSet.getName();
          case 2 -> fileSet.getCheckConfig() != null
                  ? CheckConfigurationLabelProvider.INSTANCE.getText(fileSet.getCheckConfig())
                  : "";
          default -> element.toString();
        };
      } else {
        columnText = element.toString();
      }
      return columnText;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }

  /**
   * Sorts CheckConfiguration objects into their display order.
   */
  public static final class FileSetViewerSorter extends ViewerComparator {

    public static final FileSetViewerSorter INSTANCE = new FileSetViewerSorter();

    private FileSetViewerSorter() {

    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      int result = 0;

      if (e1 instanceof FileSet && e2 instanceof FileSet) {
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
