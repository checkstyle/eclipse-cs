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

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor.CheckConfigurationWorkingSetEditorModel;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class CheckConfigurationWorkingSetEditorConfigTable extends Composite {

    /** The table viewer for check configurations. */
    private final TableViewer tableViewer;

    public CheckConfigurationWorkingSetEditorConfigTable(Composite parent, int style,
        CheckConfigurationWorkingSetEditorModel model,
        Runnable configureCheckConfig,
        Consumer<CheckConfigurationWorkingCopy> handleSelectionChanged) {
        super(parent, style);
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        setLayout(tableColumnLayout);
        Table table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        tableViewer = new TableViewer(table);

        final TableViewerColumn col1 = new TableViewerColumn(tableViewer, SWT.NULL);
        col1.getColumn().setText(Messages.CheckstylePreferencePage_colCheckConfig);
        col1.setLabelProvider(ColumnLabelProvider.createTextImageProvider(
            element -> ((CheckConfiguration) element).getName(),
            element -> ConfigurationTypesUI.getConfigurationTypeImage(
                ((CheckConfiguration) element).getType())));
        tableColumnLayout.setColumnData(col1.getColumn(), new ColumnWeightData(1));

        final TableViewerColumn col2 = new TableViewerColumn(tableViewer, SWT.NULL);
        col2.getColumn().setText(Messages.CheckstylePreferencePage_colLocation);
        col2.setLabelProvider(ColumnLabelProvider.createTextProvider(
            element -> ((CheckConfiguration) element).getLocation()));
        tableColumnLayout.setColumnData(col2.getColumn(), new ColumnWeightData(1));

        final TableViewerColumn col3 = new TableViewerColumn(tableViewer, SWT.NULL);
        col3.getColumn().setText(Messages.CheckstylePreferencePage_colType);
        col3.setLabelProvider(ColumnLabelProvider.createTextProvider(
            element -> ((CheckConfiguration) element).getType().getName()));
        tableColumnLayout.setColumnData(col3.getColumn(), new ColumnWeightData(1));

        if (model.global()) {
            final TableViewerColumn col4 = new TableViewerColumn(tableViewer, SWT.NULL);
            col4.getColumn().setText(Messages.CheckstylePreferencePage_colDefault);
            col4.setLabelProvider(
                ColumnLabelProvider.createTextImageProvider(element -> "", element -> {
                    final CheckConfigurationWorkingCopy cfg =
                        (CheckConfigurationWorkingCopy) element;
                    return model.isDefaultConfig().test(cfg)
                        ? CheckstyleUIPluginImages.TICK_ICON.getImage()
                        : null;
                }));
            col4.getColumn().pack();
            tableColumnLayout.setColumnData(col4.getColumn(),
                new ColumnPixelData(col4.getColumn().getWidth()));
        }

        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.setInput(model.configs());
        tableViewer.addDoubleClickListener(event -> configureCheckConfig.run());
        tableViewer.addSelectionChangedListener(event -> {
            CheckConfigurationWorkingCopy checkConfig = (CheckConfigurationWorkingCopy) tableViewer
                .getStructuredSelection().getFirstElement();
            handleSelectionChanged.accept(checkConfig);
        });

        TableViewerEnhancer.enhance(tableViewer, model.tableSettings(), tableColumnLayout);
    }

    public void refresh() {
        tableViewer.refresh(true);
    }

    public CheckConfigurationWorkingCopy getSelection() {
        return (CheckConfigurationWorkingCopy) tableViewer.getStructuredSelection()
            .getFirstElement();
    }

    public void setConfigs(CheckConfigurationWorkingCopy[] configs) {
        this.tableViewer.setInput(configs);
        this.tableViewer.refresh();
    }

    public void setSelection(CheckConfigurationWorkingCopy config) {
        this.tableViewer.setSelection(new StructuredSelection(config));
    }

}
