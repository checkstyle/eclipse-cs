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

package net.sf.eclipsecs.ui.config;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor.ConfigurationLabelProvider;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class CheckConfigurationWorkingSetEditorConfigTable extends Composite {

  private final TableViewer tableViewer;

  public CheckConfigurationWorkingSetEditorConfigTable(Composite parent, int style, boolean useDefaultColumn,
          CheckConfigurationWorkingCopy[] configs, ConfigurationLabelProvider multiProvider,
          Runnable configureCheckConfig,
          Consumer<CheckConfigurationWorkingCopy> handleSelectionChanged) {
    super(parent, style);
    setLayout(new FillLayout());
    Table table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);

    TableColumn column1 = new TableColumn(table, SWT.NULL);
    column1.setText(Messages.CheckstylePreferencePage_colCheckConfig);
    tableLayout.addColumnData(new ColumnWeightData(40));

    TableColumn column2 = new TableColumn(table, SWT.NULL);
    column2.setText(Messages.CheckstylePreferencePage_colLocation);
    tableLayout.addColumnData(new ColumnWeightData(30));

    TableColumn column3 = new TableColumn(table, SWT.NULL);
    column3.setText(Messages.CheckstylePreferencePage_colType);
    tableLayout.addColumnData(new ColumnWeightData(30));

    if (useDefaultColumn) {
      TableColumn column4 = new TableColumn(table, SWT.NULL);
      column4.setText(Messages.CheckstylePreferencePage_colDefault);
      tableLayout.addColumnData(new ColumnWeightData(12));
    }

    tableViewer = new TableViewer(table);
    tableViewer.setLabelProvider(multiProvider);
    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    tableViewer.setInput(configs);
    tableViewer.addDoubleClickListener(event -> configureCheckConfig.run());
    tableViewer.addSelectionChangedListener(event -> {
      CheckConfigurationWorkingCopy checkConfig = (CheckConfigurationWorkingCopy) tableViewer
              .getStructuredSelection().getFirstElement();
      handleSelectionChanged.accept(checkConfig);
    });
    TableViewerEnhancer.enhance(tableViewer, multiProvider);
  }

  public void refresh() {
    tableViewer.refresh(true);
  }

  public CheckConfigurationWorkingCopy getSelection() {
    return (CheckConfigurationWorkingCopy) tableViewer.getStructuredSelection().getFirstElement();
  }

  public void setConfigs(CheckConfigurationWorkingCopy[] configs) {
    this.tableViewer.setInput(configs);
    this.tableViewer.refresh();
  }

  public void setSelection(CheckConfigurationWorkingCopy config) {
    this.tableViewer.setSelection(new StructuredSelection(config));
  }

}
