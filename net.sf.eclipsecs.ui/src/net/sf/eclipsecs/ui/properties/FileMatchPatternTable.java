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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.properties.FileMatchPatternControl.FileMatchPatternControlCallbacks;

public final class FileMatchPatternTable extends Composite {

    /** The checkbox table viewer for patterns. */
    private final CheckboxTableViewer mPatternViewer;

    public FileMatchPatternTable(Composite parent, int style,
        FileMatchPatternControlCallbacks callbacks) {
        super(parent, style);
        GridLayoutFactory.fillDefaults().applyTo(this);

        final Table table = createTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

        this.mPatternViewer = new CheckboxTableViewer(table);

        mPatternViewer.setLabelProvider(new FileMatchPatternLabelProvider());
        mPatternViewer.setContentProvider(ArrayContentProvider.getInstance());
        mPatternViewer.addDoubleClickListener(event -> {
            final FileMatchPattern pattern =
                (FileMatchPattern) ((IStructuredSelection) event.getSelection()).getFirstElement();
            callbacks.editFileMatchPattern().accept(pattern);
            callbacks.updateMatchView().run();
        });
        mPatternViewer.addCheckStateListener(event -> {
            if (event.getElement() instanceof FileMatchPattern pattern) {
                pattern.setIsIncludePattern(event.getChecked());
                mPatternViewer.refresh();
                callbacks.updateMatchView().run();
            }
        });
        mPatternViewer.setCheckStateProvider(FileMatchPatternTableCheckStateProvider.INSTANCE);
    }

    public FileMatchPattern getSelectedPattern() {
        return (FileMatchPattern) mPatternViewer.getStructuredSelection().getFirstElement();
    }

    public void refresh() {
        mPatternViewer.refresh();
    }

    public void setInput(List<FileMatchPattern> fileMatchPatterns) {
        mPatternViewer.setInput(fileMatchPatterns);
    }

    private static Table createTable(Composite parent) {
        final Table table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText(Messages.FileSetEditDialog_colInclude);
        column1.pack();

        final TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText(Messages.FileSetEditDialog_colRegex);
        table.addControlListener(ControlListener.controlResizedAdapter(event -> {
            column2.setWidth(Math.max(table.getClientArea().width - column1.getWidth(), 0));
        }));

        return table;
    }

    /**
     * Provides the labels for the FileSet list display.
     */
    private static final class FileMatchPatternLabelProvider extends LabelProvider
        implements ITableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            final String columnText;
            if (element instanceof FileMatchPattern pattern) {
                columnText = switch (columnIndex) {
                    case 0 -> new String();
                    case 1 -> pattern.getMatchPattern();
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

    private static final class FileMatchPatternTableCheckStateProvider
        implements ICheckStateProvider {

        /** The singleton instance. */
        private static final FileMatchPatternTableCheckStateProvider INSTANCE =
            new FileMatchPatternTableCheckStateProvider();

        private FileMatchPatternTableCheckStateProvider() {

        }

        @Override
        public boolean isChecked(Object element) {
            return ((FileMatchPattern) element).isIncludePattern();
        }

        @Override
        public boolean isGrayed(Object element) {
            return false;
        }

    }

}
