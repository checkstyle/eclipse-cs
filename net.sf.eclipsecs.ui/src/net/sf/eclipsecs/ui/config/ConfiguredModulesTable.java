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
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class ConfiguredModulesTable extends Composite {

  private final Group configuredModulesGroup;
  private final CheckboxTableViewer tableViewer;

  private RuleGroupMetadata currentGroup;

  public ConfiguredModulesTable(Composite parent, int style, boolean configurable,
          List<Module> modules, ConfiguredModulesTableCallbacks callbacks) {
    super(parent, style);

    setLayout(new FillLayout());

    this.configuredModulesGroup = new Group(this, SWT.NONE);
    configuredModulesGroup.setLayout(new GridLayout());
    configuredModulesGroup.setText("\0");

    Table table = new Table(configuredModulesGroup,
            SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
    table.setLayoutData(new GridData(GridData.FILL_BOTH));
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

    this.tableViewer = new CheckboxTableViewer(table);
    ModuleLabelProvider multiProvider = new ModuleLabelProvider();
    tableViewer.setLabelProvider(multiProvider);
    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    tableViewer.addFilter(new RuleGroupModuleFilter());

    tableViewer.addDoubleClickListener(event -> callbacks.openModule
            .accept((Module) ((IStructuredSelection) event.getSelection()).getFirstElement()));
    tableViewer.addSelectionChangedListener(event -> {
      String description = null;
      if (event.getStructuredSelection().getFirstElement() instanceof Module module) {
        RuleMetadata meta = module.getMetaData();
        if (meta != null) {
          description = meta.identity().description();
        }
      }
      callbacks.updateDescription.accept(CheckConfigurationConfigureDialog.getDescriptionHtml(description));
    });
    tableViewer.addCheckStateListener(callbacks.checkStateChanged);
    if (configurable) {
      tableViewer.getTable().addKeyListener(KeyListener.keyReleasedAdapter(event -> {
        if (event.character == SWT.DEL || event.keyCode == SWT.ARROW_LEFT) {
          @SuppressWarnings("unchecked")
          List<Module> modulesToDelete = tableViewer.getStructuredSelection().toList();
          callbacks.removeModule.accept(modulesToDelete);
        }
      }));
    }

    tableViewer.setCheckStateProvider(new ICheckStateProvider() {

      @Override
      public boolean isGrayed(Object element) {
        return !configurable;
      }

      @Override
      public boolean isChecked(Object element) {
        Module module = (Module) element;
        return !Severity.IGNORE.equals(module.getSeverity()) || !module.getMetaData().hasSeverity();
      }
    });

    tableViewer.setInput(modules);

    TableViewerEnhancer.enhance(tableViewer, multiProvider);

    Composite buttons = new Composite(configuredModulesGroup, SWT.NONE);
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    buttons.setLayout(layout);
    buttons.setLayoutData(new GridData());

    Button mRemoveButton = new Button(buttons, SWT.PUSH);
    mRemoveButton.setText(Messages.CheckConfigurationConfigureDialog_btnRemove);
    mRemoveButton.setLayoutData(new GridData());
    if (configurable) {
      mRemoveButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        @SuppressWarnings("unchecked")
        List<Module> modulesToDelete = tableViewer.getStructuredSelection().toList();
        callbacks.removeModule.accept(modulesToDelete);
      }));
    }
    mRemoveButton.setEnabled(configurable);

    Button mEditButton = new Button(buttons, SWT.PUSH);
    mEditButton.setText(Messages.CheckConfigurationConfigureDialog_btnOpen);
    mEditButton.setLayoutData(new GridData());
    mEditButton.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> callbacks.openModule
                    .accept((Module) tableViewer.getStructuredSelection().getFirstElement())));
  }

  public void refresh() {
    tableViewer.refresh();
  }

  public final void setTextHeader(String text) {
    configuredModulesGroup.setText(text);
  }

  public void setChecked(Module module, boolean state) {
    tableViewer.setChecked(module, state);
  }

  public void setCurrentGroup(RuleGroupMetadata currentGroup) {
    this.currentGroup = currentGroup;
  }

  public static record ConfiguredModulesTableCallbacks(Consumer<Module> openModule,
          Consumer<List<Module>> removeModule, Consumer<String> updateDescription,
          ICheckStateListener checkStateChanged) {

  }

  /**
   * Viewer filter that includes all modules that belong to the currently selected group.
   *
   */
  private class RuleGroupModuleFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      RuleMetadata rule = ((Module) element).getMetaData();
      return rule == null
              || currentGroup != null
                      && !rule.hidden()
                      && currentGroup.getGroupName().equals(rule.identity().group().getGroupName());

    }
  }

  /**
   * Label provider for the table showing the configured modules.
   *
   */
  private static class ModuleLabelProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (element instanceof Module module) {
        return switch (columnIndex) {
          case 0 -> "";
          case 1 -> module.getName() != null ? module.getName() : "";
          case 2 -> module.getSeverity() != null ? module.getSeverity().toXmlValue() : "";
          case 3 -> module.getComment() != null ? module.getComment() : "";
          default -> "";
        };
      }
      return null;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int col) {
      if (col == 0 && element instanceof Module) {
        return Severity.IGNORE.equals(((Module) element).getSeverity()) ? Integer.valueOf(0)
                : Integer.valueOf(1);
      }

      return getColumnText(element, col);
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = CheckConfigurationConfigureDialog.class.getName();

      IDialogSettings workbenchSettings = CheckstyleUIPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }
}
