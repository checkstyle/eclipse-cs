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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.ui.Messages;

public final class FileMatchPatternTable extends Composite {

  private final CheckboxTableViewer mPatternViewer;

  public FileMatchPatternTable(Composite parent, int style, FileMatchPatternTableCallbacks callbacks) {
    super(parent, style);
    setLayout(new FillLayout());

    Group composite = new Group(this, SWT.NONE);
    composite.setText(Messages.FileSetEditDialog_titlePatternsTable);
    composite.setLayout(new FormLayout());

    final Composite buttons = new Composite(composite, SWT.NULL);
    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 3);
    formData.right = new FormAttachment(100, -3);
    formData.bottom = new FormAttachment(100, -3);

    buttons.setLayoutData(formData);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    buttons.setLayout(layout);

    final Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
    formData = new FormData();
    formData.left = new FormAttachment(0, 3);
    formData.top = new FormAttachment(0, 3);
    formData.right = new FormAttachment(buttons, -3, SWT.LEFT);
    formData.bottom = new FormAttachment(100, -3);
    table.setLayoutData(formData);

    TableLayout tableLayout = new TableLayout();
    table.setLayout(tableLayout);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    TableColumn column1 = new TableColumn(table, SWT.NONE);
    column1.setText(Messages.FileSetEditDialog_colInclude);

    tableLayout.addColumnData(new ColumnWeightData(11));

    TableColumn column2 = new TableColumn(table, SWT.NONE);
    column2.setText(Messages.FileSetEditDialog_colRegex);
    tableLayout.addColumnData(new ColumnWeightData(89));

    this.mPatternViewer = new CheckboxTableViewer(table);

    mPatternViewer.setLabelProvider(new FileMatchPatternLabelProvider());
    mPatternViewer.setContentProvider(new ArrayContentProvider());
    mPatternViewer.addDoubleClickListener(event -> {
      FileMatchPattern pattern = (FileMatchPattern) ((IStructuredSelection) event.getSelection())
              .getFirstElement();
      callbacks.editFileMatchPattern.accept(pattern);
      callbacks.updateMatchView.run();
    });
    mPatternViewer.addCheckStateListener(event -> {
      if (event.getElement() instanceof FileMatchPattern) {
        FileMatchPattern pattern = (FileMatchPattern) event.getElement();
        pattern.setIsIncludePattern(event.getChecked());
        mPatternViewer.refresh();
        callbacks.updateMatchView.run();
      }
    });

    //
    // Build the buttons.
    //

    Button mAddButton = createPushButton(buttons, Messages.FileSetEditDialog_btnAdd);
    mAddButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      callbacks.addFileMatchPattern.run();
      callbacks.updateMatchView.run();
    }));

    Button mEditButton = createPushButton(buttons, Messages.FileSetEditDialog_btnEdit);
    mEditButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      FileMatchPattern pattern = (FileMatchPattern) mPatternViewer.getStructuredSelection()
              .getFirstElement();
      callbacks.editFileMatchPattern.accept(pattern);
      callbacks.updateMatchView.run();
    }));

    Button mRemoveButton = createPushButton(buttons, Messages.FileSetEditDialog_btnRemove);
    mRemoveButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      FileMatchPattern pattern = (FileMatchPattern) mPatternViewer.getStructuredSelection()
              .getFirstElement();
      callbacks.removeFileMatchPattern.accept(pattern);
      callbacks.updateMatchView.run();
    }));

    Button mUpButton = createPushButton(buttons, Messages.FileSetEditDialog_btnUp);
    mUpButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      FileMatchPattern pattern = (FileMatchPattern) mPatternViewer.getStructuredSelection()
              .getFirstElement();
      callbacks.upFileMatchPattern.accept(pattern);
      callbacks.updateMatchView.run();
    }));

    Button mDownButton = createPushButton(buttons, Messages.FileSetEditDialog_btnDown);
    mDownButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      FileMatchPattern pattern = (FileMatchPattern) mPatternViewer.getStructuredSelection()
              .getFirstElement();
      callbacks.downFileMatchPattern.accept(pattern);
      callbacks.updateMatchView.run();
    }));
  }

  public void refresh() {
    mPatternViewer.refresh();
  }

  public void setChecked(FileMatchPattern pattern, boolean includePattern) {
    mPatternViewer.setChecked(pattern, includePattern);
  }

  public void setInput(List<FileMatchPattern> fileMatchPatterns) {
    mPatternViewer.setInput(fileMatchPatterns);
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
  private static Button createPushButton(Composite parent, String label) {
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    button.setLayoutData(data);
    return button;
  }

  public record FileMatchPatternTableCallbacks(Consumer<FileMatchPattern> editFileMatchPattern,
          Runnable updateMatchView, Runnable addFileMatchPattern,
          Consumer<FileMatchPattern> removeFileMatchPattern,
          Consumer<FileMatchPattern> upFileMatchPattern,
          Consumer<FileMatchPattern> downFileMatchPattern) {

  }

  /**
   * Provides the labels for the FileSet list display.
   */
  private static class FileMatchPatternLabelProvider extends LabelProvider
          implements ITableLabelProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
      if (element instanceof FileMatchPattern pattern) {
        return switch (columnIndex) {
          case 0 -> new String();
          case 1 -> pattern.getMatchPattern();
          default -> element.toString();
        };
      }
      return element.toString();
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }
}
