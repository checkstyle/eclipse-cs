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

package net.sf.eclipsecs.ui.util.table;

import java.text.Collator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

public class TableViewerEnhancer {

  /** Key for the column index in the TableColumn data. */
  private static final String WIDGET_DATA_COLUMN_INDEX = "index"; //$NON-NLS-1$

  /** Key for the column index in the persistence store. */
  private static final String TAG_COLUMN_INDEX = "sortColumn"; //$NON-NLS-1$

  /** Key for the sort direction in the persistence store. */
  private static final String TAG_SORT_DIRECTION = "sortDirection"; //$NON-NLS-1$

  /** Key for the widths in the persistence store. */
  private static final String TAG_COLUMN_WIDTH = "colWidth"; //$NON-NLS-1$

  /** Key for the selection index in the persistence store. */
  private static final String TAG_CURRENT_SELECTION = "selectedRow"; //$NON-NLS-1$

  /** Integer constant for the forward sort direction value. */
  private static final int DIRECTION_FORWARD = 1;

  /** Integer constant for the reverse sort direction value. */
  private static final int DIRECTION_REVERSE = -1;

  private TableViewerEnhancer() {

  }

  public static <T extends ITableSettingsProvider & ITableComparableProvider> void enhance(
          TableViewer tableViewer, T config) {
    IDialogSettings tableSettings = config.getTableSettings();
    Table table = tableViewer.getTable();
    TableColumn[] columns = table.getColumns();
    int defaultSortColumnIndex = 0;
    for (int i = 0, size = columns.length; i < size; i++) {
      int colIndex = i;
      columns[i].addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        if (columns[colIndex] == table.getSortColumn()) {
          setSortDirection(table, getSortDirection(table) * -1);
        } else {
          table.setSortColumn(columns[colIndex]);
          setSortDirection(table, DIRECTION_FORWARD);
        }
        tableViewer.refresh(false);
        saveState(table, tableSettings);
      }));
      columns[i].addControlListener(
              ControlListener.controlResizedAdapter(event -> saveState(table, tableSettings)));
      columns[i].setData(WIDGET_DATA_COLUMN_INDEX, i);
      if (columns[i] == table.getSortColumn()) {
        defaultSortColumnIndex = i;
      }
    }

    setSortDirection(table, getIntSetting(tableSettings, TAG_SORT_DIRECTION, DIRECTION_FORWARD));
    int sortColumnIndex = getIntSetting(tableSettings, TAG_COLUMN_INDEX, defaultSortColumnIndex);
    if (sortColumnIndex >= 0 && sortColumnIndex < columns.length) {
      table.setSortColumn(table.getColumn(sortColumnIndex));
    }

    tableViewer.setComparator(new TableSorter(config));

    // restore the column widths
    try {
      TableLayout layout = new TableLayout();
      for (int i = 0, size = columns.length; i < size; i++) {
        int width = tableSettings.getInt(TAG_COLUMN_WIDTH + i);
        layout.addColumnData(new ColumnPixelData(width));
      }
      table.setLayout(layout);
    } catch (NumberFormatException ex) {
      // fall back to the default layout
    }

    // restore the selection
    try {
      table.select(tableSettings.getInt(TAG_CURRENT_SELECTION));
    } catch (NumberFormatException ex) {
      // NOOP
    }
  }

  private static int getIntSetting(IDialogSettings tableSettings, String setting, int def) {
    try {
      return tableSettings.getInt(setting);
    } catch (NumberFormatException ex) {
      return def;
    }
  }

  /**
   * Saves the sorting state to the dialog settings.
   */
  private static void saveState(Table table, IDialogSettings tableSettings) {
    tableSettings.put(TAG_COLUMN_INDEX, (int) table.getSortColumn().getData("index"));
    tableSettings.put(TAG_SORT_DIRECTION, getSortDirection(table));

    // store the column widths
    TableColumn[] columns = table.getColumns();
    for (int i = 0, size = columns.length; i < size; i++) {
      int width = columns[i].getWidth();
      if (width > 0) {
        tableSettings.put(TAG_COLUMN_WIDTH + i, width);
      }
    }

    // store the selection
    tableSettings.put(TAG_CURRENT_SELECTION, table.getSelectionIndex());
  }

  private static int getSortDirection(Table table) {
    return table.getSortDirection() == SWT.DOWN ? DIRECTION_REVERSE : DIRECTION_FORWARD;
  }

  private static void setSortDirection(Table table, int sortDirection) {
    table.setSortDirection(sortDirection == DIRECTION_FORWARD ? SWT.UP : SWT.DOWN);
  }

  private static class TableSorter extends ViewerComparator {

    /** Collator to support natural sorting of strings. */
    private static final Collator COLLATOR = Collator
            .getInstance(CheckstyleUIPlugin.getPlatformLocale());

    private final ITableComparableProvider comparableProvider;

    private TableSorter(ITableComparableProvider comparableProvider) {
      this.comparableProvider = comparableProvider;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int compare(Viewer viewer, Object left, Object right) {
      Table table = ((TableViewer) viewer).getTable();
      int sortedColumnIndex = (int) table.getSortColumn().getData(WIDGET_DATA_COLUMN_INDEX);
      Comparable compLeft = comparableProvider.getComparableValue(left, sortedColumnIndex);
      Comparable compRight = comparableProvider.getComparableValue(right, sortedColumnIndex);

      int compareResult = 0;

      // support for string collation
      if (compLeft instanceof String && compRight instanceof String) {
        compareResult = COLLATOR.compare(compLeft, compRight);
      } else {
        compareResult = compLeft.compareTo(compRight);
      }

      // take sort direction into account
      return compareResult * getSortDirection(table);
    }

  }

}
