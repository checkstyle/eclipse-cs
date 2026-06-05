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

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.ConfiguredModules.ConfiguredModulesCallbacks;
import net.sf.eclipsecs.ui.util.table.TableComparableProvider;
import net.sf.eclipsecs.ui.util.table.TableSettingsProvider;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class ConfiguredModulesTable extends Composite {

  private final CheckboxTableViewer tableViewer;

  public ConfiguredModulesTable(Composite parent, int style, ViewerFilter ruleGroupModuleFilter,
          boolean configurable, ICheckStateProvider checkStateProvider, ConfiguredModulesCallbacks callbacks,
          List<Module> modules) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().applyTo(this);

    this.tableViewer = new CheckboxTableViewer(createTable(this));
    tableViewer.setLabelProvider(ModuleLabelProvider.INSTANCE);
    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    tableViewer.addFilter(ruleGroupModuleFilter);

    tableViewer.addDoubleClickListener(event -> callbacks.openModule()
            .accept((Module) ((IStructuredSelection) event.getSelection()).getFirstElement()));
    tableViewer.addSelectionChangedListener(event -> {
      String description = null;
      if (event.getStructuredSelection().getFirstElement() instanceof Module module) {
        RuleMetadata meta = module.getMetaData();
        if (meta != null) {
          description = meta.identity().description();
        }
      }
      callbacks.updateDescription().accept(CheckConfigurationConfigureDialogView.getDescriptionHtml(description));
    });
    tableViewer.addCheckStateListener(event -> {
      if (configurable) {
        callbacks.checkStateChanged().accept((Module) event.getElement(), event.getChecked());
      } else {
        tableViewer.setChecked(event.getElement(), !event.getChecked());
      }
    });
    if (configurable) {
      tableViewer.getTable().addKeyListener(KeyListener.keyReleasedAdapter(event -> {
        if (event.character == SWT.DEL || event.keyCode == SWT.ARROW_LEFT) {
          @SuppressWarnings("unchecked")
          List<Module> modulesToDelete = tableViewer.getStructuredSelection().toList();
          callbacks.removeModule().accept(modulesToDelete);
        }
      }));
    }
    tableViewer.setCheckStateProvider(checkStateProvider);
    tableViewer.setInput(modules);

    TableViewerEnhancer.enhance(tableViewer, ModuleLabelProvider.INSTANCE);
  }

  public void refresh() {
    tableViewer.refresh();
  }

  @SuppressWarnings("unchecked")
  public List<Module> getSelectedModules() {
    return tableViewer.getStructuredSelection().toList();
  }

  private static Table createTable(Composite parent) {
    Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);

    TableColumn column1 = new TableColumn(table, SWT.NONE);
    column1.setAlignment(SWT.CENTER);
    column1.setText(Messages.CheckConfigurationConfigureDialog_colEnabled);
    tableLayout.addColumnData(new ColumnWeightData(15));

    TableColumn column2 = new TableColumn(table, SWT.NONE);
    column2.setText(Messages.CheckConfigurationConfigureDialog_colModule);
    tableLayout.addColumnData(new ColumnWeightData(30));

    TableColumn column3 = new TableColumn(table, SWT.NONE);
    column3.setText(Messages.CheckConfigurationConfigureDialog_colSeverity);
    tableLayout.addColumnData(new ColumnWeightData(20));

    TableColumn column4 = new TableColumn(table, SWT.NONE);
    column4.setText(Messages.CheckConfigurationConfigureDialog_colComment);
    tableLayout.addColumnData(new ColumnWeightData(35));

    // by default the table viewer sorts on column 0, but we want to sort by the module label
    table.setSortColumn(column2);

    return table;
  }

  /**
   * Label provider for the table showing the configured modules.
   *
   */
  private static final class ModuleLabelProvider extends LabelProvider
          implements ITableLabelProvider, TableComparableProvider, TableSettingsProvider {

    private static final ModuleLabelProvider INSTANCE = new ModuleLabelProvider();

    private ModuleLabelProvider() {

    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String columnText = null;
      if (element instanceof Module module) {
        columnText = switch (columnIndex) {
          case 0 -> "";
          case 1 -> module.getName() != null ? module.getName() : "";
          case 2 -> module.getSeverity() != null ? module.getSeverity().toXmlValue() : "";
          case 3 -> module.getComment() != null ? module.getComment() : "";
          default -> "";
        };
      }
      return columnText;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int col) {
      Comparable<?> comp;
      if (col == 0 && element instanceof Module) {
        comp = Severity.IGNORE.equals(((Module) element).getSeverity()) ? Integer.valueOf(0)
                : Integer.valueOf(1);
      } else {
        comp = getColumnText(element, col);
      }
      return comp;
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = CheckConfigurationConfigureDialog.class.getName();

      IDialogSettings workbenchSettings = CheckstyleUiPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }

}
