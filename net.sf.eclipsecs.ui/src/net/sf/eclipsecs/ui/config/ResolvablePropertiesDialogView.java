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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class ResolvablePropertiesDialogView extends Composite {

    /** The table viewer for resolvable properties. */
    private final TableViewer mTableViewer;

    public ResolvablePropertiesDialogView(Composite parent, int style,
        IDialogSettings tableSettings,
        Consumer<ResolvableProperty> openPropertyItemEditor,
        Consumer<List<ResolvableProperty>> removePropertyItems) {
        super(parent, style);

        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

        final Composite tableComposite = new Composite(this, SWT.NONE);
        GridDataFactory.create(GridData.FILL_BOTH).applyTo(tableComposite);
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout(tableColumnLayout);

        mTableViewer = new TableViewer(tableComposite,
            SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        mTableViewer.getTable().setHeaderVisible(true);
        mTableViewer.getTable().setLinesVisible(true);
        mTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        mTableViewer.addDoubleClickListener(
            event -> openPropertyItemEditor.accept(getSelectedProperties().getFirst()));
        mTableViewer.getTable().addKeyListener(KeyListener.keyReleasedAdapter(event -> {
            if (event.character == SWT.DEL) {
                removePropertyItems.accept(getSelectedProperties());
            }
            if (event.character == ' ' && !getSelectedProperties().isEmpty()) {
                openPropertyItemEditor.accept(getSelectedProperties().getFirst());
            }
        }));

        final TableViewerColumn nameCol = new TableViewerColumn(mTableViewer, SWT.NONE);
        nameCol.getColumn().setText(Messages.ResolvablePropertiesDialog_colName);
        nameCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
            element -> ((ResolvableProperty) element).getPropertyName()));
        tableColumnLayout.setColumnData(nameCol.getColumn(), new ColumnWeightData(1));

        final TableViewerColumn valueCol = new TableViewerColumn(mTableViewer, SWT.NONE);
        valueCol.getColumn().setText(Messages.ResolvablePropertiesDialog_colValue);
        valueCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
            element -> ((ResolvableProperty) element).getValue()));
        tableColumnLayout.setColumnData(valueCol.getColumn(), new ColumnWeightData(1));

        TableViewerEnhancer.enhance(mTableViewer, tableSettings, tableColumnLayout);

        final Composite buttonBar = new Composite(this, SWT.NULL);
        GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonBar);
        GridDataFactory.swtDefaults().align(GridData.BEGINNING, GridData.BEGINNING)
            .applyTo(buttonBar);

        final Button btnAdd = createButton(buttonBar, Messages.ResolvablePropertiesDialog_btnAdd);
        btnAdd.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> openPropertyItemEditor.accept(null)));
        final Button btnEdit = createButton(buttonBar, Messages.ResolvablePropertiesDialog_btnEdit);
        btnEdit.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            if (getSelectedProperties().size() > 0) {
                openPropertyItemEditor.accept(getSelectedProperties().getFirst());
            }
        }));
        final Button btnRemove =
            createButton(buttonBar, Messages.ResolvablePropertiesDialog_btnRemove);
        btnRemove.addSelectionListener(SelectionListener
            .widgetSelectedAdapter(event -> removePropertyItems.accept(getSelectedProperties())));
    }

    @SuppressWarnings("unchecked")
    public List<ResolvableProperty> getSelectedProperties() {
        return mTableViewer.getStructuredSelection().toList();
    }

    public void setResolvableProperties(List<ResolvableProperty> resolvableProperties) {
        mTableViewer.setInput(resolvableProperties);
    }

    public void refresh() {
        mTableViewer.refresh();
    }

    private static Button createButton(Composite parent, String text) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(button);
        return button;
    }

}
