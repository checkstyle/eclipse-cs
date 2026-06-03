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

package net.sf.eclipsecs.ui.properties;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.properties.ComplexFileSetsEditor.FileSetLabelProvider;
import net.sf.eclipsecs.ui.properties.ComplexFileSetsEditor.FileSetViewerSorter;

public final class ComplexFileSetsEditorTableView extends Composite {

  private final CheckboxTableViewer mViewer;

  public ComplexFileSetsEditorTableView(Composite parent, int style,
          ICheckStateListener changeEnabledState, Consumer<FileSet> editFileSet,
          List<FileSet> mFileSets) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().applyTo(this);

    mViewer = new CheckboxTableViewer(createTable(this));
    mViewer.setLabelProvider(FileSetLabelProvider.INSTANCE);
    mViewer.setContentProvider(ArrayContentProvider.getInstance());
    mViewer.setComparator(FileSetViewerSorter.INSTANCE);
    mViewer.setInput(mFileSets);

    for (FileSet fileSet : mFileSets) {
      mViewer.setChecked(fileSet, fileSet.isEnabled());
    }

    mViewer.addDoubleClickListener(event -> editFileSet.accept(getSelectedFileSet()));
    mViewer.addCheckStateListener(changeEnabledState);
  }

  public void refresh() {
    mViewer.refresh();
  }

  public void setChecked(FileSet fileSet, boolean enabled) {
    mViewer.setChecked(fileSet, enabled);
  }

  public FileSet getSelectedFileSet() {
    return (FileSet) mViewer.getStructuredSelection().getFirstElement();
  }

  private static Table createTable(Composite parent) {
    Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(table);

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

    return table;
  }

}
