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

import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.ConfiguredModules.ConfiguredModulesCallbacks;
import net.sf.eclipsecs.ui.util.HtmlUtil;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

/**
 * Composite providing a checkbox table of the configured modules with columns for module name,
 * severity and comment, wiring selection, check-state and keyboard events to callbacks.
 *
 */
public final class ConfiguredModulesTable extends Composite {

    /** The checkbox table viewer for configured modules. */
    private final CheckboxTableViewer tableViewer;

    /**
     * Creates the table of configured modules and wires it to the given callbacks.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the style bits
     * @param ruleGroupModuleFilter
     *            the viewer filter applied to the module table
     * @param configurable
     *            whether the modules can be removed and their check state changed
     * @param checkStateProvider
     *            the check state provider for the table
     * @param callbacks
     *            the callbacks notified on user interaction with the table
     * @param modules
     *            the modules to display in the table
     */
    public ConfiguredModulesTable(Composite parent, int style, ViewerFilter ruleGroupModuleFilter,
        boolean configurable, ICheckStateProvider checkStateProvider,
        ConfiguredModulesCallbacks callbacks, List<Module> modules) {
        super(parent, style);

        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        setLayout(tableColumnLayout);

        final Table table =
            new Table(this, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        this.tableViewer = new CheckboxTableViewer(table);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.addFilter(ruleGroupModuleFilter);

        createColumns(tableColumnLayout, table);

        tableViewer.addDoubleClickListener(event -> callbacks.openModule()
            .accept((Module) ((IStructuredSelection) event.getSelection()).getFirstElement()));
        tableViewer.addSelectionChangedListener(event -> {
            String description = null;
            if (event.getStructuredSelection().getFirstElement() instanceof Module module) {
                final RuleMetadata meta = module.getMetaData();
                if (meta != null) {
                    description = meta.identity().description();
                }
            }
            callbacks.updateDescription().accept(HtmlUtil.getDescriptionHtml(description));
        });
        tableViewer.addCheckStateListener(event -> {
            if (configurable) {
                callbacks.checkStateChanged().accept((Module) event.getElement(),
                    event.getChecked());
            }
            else {
                tableViewer.setChecked(event.getElement(), !event.getChecked());
            }
        });
        if (configurable) {
            tableViewer.getTable().addKeyListener(KeyListener.keyReleasedAdapter(event -> {
                if (event.character == SWT.DEL || event.keyCode == SWT.ARROW_LEFT) {
                    @SuppressWarnings("unchecked")
                    final List<Module> modulesToDelete =
                        tableViewer.getStructuredSelection().toList();
                    callbacks.removeModule().accept(modulesToDelete);
                }
            }));
        }
        tableViewer.setCheckStateProvider(checkStateProvider);
        tableViewer.setInput(modules);

        TableViewerEnhancer.enhance(tableViewer, getTableSettings(), tableColumnLayout);
    }

    /**
     * Creates the table columns for module name, severity and comment.
     *
     * @param layout
     *            the table column layout to which the column data is added
     * @param table
     *            the table whose columns are created
     */
    private void createColumns(TableColumnLayout layout, Table table) {
        final TableViewerColumn col1 = new TableViewerColumn(tableViewer, SWT.NONE);
        col1.getColumn().setAlignment(SWT.CENTER);
        col1.getColumn().setText(Messages.CheckConfigurationConfigureDialog_colEnabled);
        col1.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> ""));
        col1.getColumn().pack();
        layout.setColumnData(col1.getColumn(),
            new ColumnPixelData(col1.getColumn().getWidth()));
        TableViewerEnhancer.setColumnComparator(col1.getColumn(), Comparator
            .comparing((Module module) -> Severity.IGNORE.equals(module.getSeverity())).reversed());

        final TableViewerColumn col2 = new TableViewerColumn(tableViewer, SWT.NONE);
        col2.getColumn().setText(Messages.CheckConfigurationConfigureDialog_colModule);
        col2.setLabelProvider(ColumnLabelProvider
            .createTextProvider(element -> nullSafeText(((Module) element).getName())));
        layout.setColumnData(col2.getColumn(), new ColumnWeightData(2));

        final TableViewerColumn col3 = new TableViewerColumn(tableViewer, SWT.NONE);
        col3.getColumn().setText(Messages.CheckConfigurationConfigureDialog_colSeverity);
        col3.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> {
            final Module module = (Module) element;
            String text = "";
            if (module.getSeverity() != null) {
                text = module.getSeverity().getXmlValue();
            }
            return text;
        }));
        col3.getColumn().pack();
        layout.setColumnData(col3.getColumn(),
            new ColumnPixelData(col3.getColumn().getWidth()));

        final TableViewerColumn col4 = new TableViewerColumn(tableViewer, SWT.NONE);
        col4.getColumn().setText(Messages.CheckConfigurationConfigureDialog_colComment);
        col4.setLabelProvider(ColumnLabelProvider
            .createTextProvider(element -> nullSafeText(((Module) element).getComment())));
        layout.setColumnData(col4.getColumn(), new ColumnWeightData(1));

        table.setSortColumn(col2.getColumn());
    }

    /**
     * Refreshes the table viewer to reflect the current module data.
     */
    public void refresh() {
        tableViewer.refresh();
    }

    /**
     * Returns the modules currently selected in the table.
     *
     * @return the list of selected modules
     */
    @SuppressWarnings("unchecked")
    public List<Module> getSelectedModules() {
        return tableViewer.getStructuredSelection().toList();
    }

    /**
     * Returns the dialog settings section for the configure dialog, creating it if it does not yet
     * exist.
     *
     * @return the dialog settings section for the table
     */
    private static IDialogSettings getTableSettings() {
        final String concreteViewId = CheckConfigurationConfigureDialog.class.getName();

        final IDialogSettings workbenchSettings =
            CheckstyleUIPlugin.getDefault().getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

        if (settings == null) {
            settings = workbenchSettings.addNewSection(concreteViewId);
        }

        return settings;
    }

    /**
     * Returns the given value or an empty string if it is null.
     *
     * @param value
     *            the value to convert, may be null
     * @return the value, or an empty string if it was null
     */
    private static String nullSafeText(String value) {
        final String text;
        if (value != null) {
            text = value;
        }
        else {
            text = "";
        }
        return text;
    }
}
