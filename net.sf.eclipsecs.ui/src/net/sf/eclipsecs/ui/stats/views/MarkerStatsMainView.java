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

package net.sf.eclipsecs.ui.stats.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;

import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsView.MarkerStatsViewActions;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsViewDataProviders.MarkerStatsViewDetailDataProviders;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsViewDataProviders.MarkerStatsViewMasterDataProviders;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class MarkerStatsMainView extends Composite {

  private final StackLayout mStackLayout;
  private final MainTableViewer mMasterViewer;
  private final DetailTableViewer mDetailViewer;

  public MarkerStatsMainView(Composite parent, int style, MarkerStatsViewDataProviders providers,
          IWorkbenchPartSite site, Runnable updateActions, MarkerStatsViewActions actions) {
    super(parent, style);

    mStackLayout = new StackLayout();
    mStackLayout.marginHeight = 0;
    mStackLayout.marginWidth = 0;
    setLayout(mStackLayout);

    // create the master viewer
    mMasterViewer = new MainTableViewer(this, SWT.NONE, providers.master(), site, updateActions,
            actions.mDrillDownAction());

    // create the detail viewer
    mDetailViewer = new DetailTableViewer(this, SWT.NONE, providers.detail(), site, updateActions,
            actions.mDrillBackAction(), actions.mShowErrorAction());

    mStackLayout.topControl = mMasterViewer;
  }

  public void setStats(Stats stats) {
    mMasterViewer.setStats(stats);
    mDetailViewer.setStats(stats);
  }

  public void setMasterAsTopControl() {
    toggleTopControl(mMasterViewer);
  }

  public void setDetailAsTopControl() {
    toggleTopControl(mDetailViewer);
  }

  private void toggleTopControl(AbstractStatTableViewer<?> control) {
    mStackLayout.topControl = control;
    layout();
    control.refresh();
  }

  public void focusTopControl() {
    mStackLayout.topControl.setFocus();
  }

  public Optional<IMarker> getSelectedMarker() {
    return mDetailViewer.getSelection();
  }

  public Optional<MarkerStat> getSelectedMarkerCategory() {
    return mMasterViewer.getSelection();
  }

  /**
   * Adds the actions to the tableviewer context menu.
   *
   * @param actions
   *          a collection of IAction objets
   * @param site
   */
  private static void hookContextMenu(final Collection<Object> actions, TableViewer viewer,
          IWorkbenchPartSite site) {
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(manager -> {
      for (Object item : actions) {
        if (item instanceof IContributionItem) {
          manager.add((IContributionItem) item);
        } else if (item instanceof IAction) {
          manager.add((IAction) item);
        }
      }
      manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    });
    viewer.getControl().setMenu(menuMgr.createContextMenu(viewer.getControl()));
    site.registerContextMenu(menuMgr, viewer);
  }

  private static final class MainTableViewer extends AbstractStatTableViewer<MarkerStat> {

    private final TableViewer tableViewer;

    private MainTableViewer(Composite parent, int style,
            MarkerStatsViewMasterDataProviders providers, IWorkbenchPartSite site,
            Runnable updateActions, IAction drillDownAction) {
      super(parent, style, MarkerStat.class);
      tableViewer = new TableViewer(
              this,
              SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      tableViewer.getControl().setLayoutData(gridData);

      // setup the table columns
      Table table = tableViewer.getTable();
      table.setLinesVisible(true);
      table.setHeaderVisible(true);

      TableColumn severityCol = new TableColumn(table, SWT.CENTER, 0);
      severityCol.setWidth(20);
      severityCol.setResizable(false);

      TableColumn idCol = new TableColumn(table, SWT.LEFT, 1);
      idCol.setText(Messages.MarkerStatsView_kindOfErrorColumn);
      idCol.setWidth(400);

      TableColumn countCol = new TableColumn(table, SWT.RIGHT, 2);
      countCol.setText(Messages.MarkerStatsView_numberOfErrorsColumn);
      countCol.pack();

      // set the providers
      tableViewer.setContentProvider(providers.contentProvider());
      tableViewer.setLabelProvider(providers.multiProvider());
      TableViewerEnhancer.enhance(tableViewer, providers.multiProvider());

      // add selection listener to maintain action state
      tableViewer.addSelectionChangedListener(event -> updateActions.run());

      // hooks the action to double click
      tableViewer.addDoubleClickListener(event -> drillDownAction.run());

      // and to the context menu too
      ArrayList<Object> actionList = new ArrayList<>(3);
      actionList.add(drillDownAction);
      hookContextMenu(actionList, tableViewer, site);
    }

    @Override
    protected TableViewer getTableViewer() {
      return tableViewer;
    }

  }

  private static final class DetailTableViewer extends AbstractStatTableViewer<IMarker> {

    private final TableViewer tableViewer;

    private DetailTableViewer(Composite parent, int style,
            MarkerStatsViewDetailDataProviders providers, IWorkbenchPartSite site,
            Runnable updateActions, IAction drillBackAction, IAction showErrorAction) {
      super(parent, style, IMarker.class);
      this.tableViewer = new TableViewer(this,
              SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
      GridData gridData = new GridData(GridData.FILL_BOTH);
      tableViewer.getControl().setLayoutData(gridData);

      Table table = tableViewer.getTable();
      table.setLinesVisible(true);
      table.setHeaderVisible(true);

      TableColumn severityCol = new TableColumn(table, SWT.CENTER, 0);
      severityCol.setWidth(20);
      severityCol.setResizable(false);

      TableColumn idCol = new TableColumn(table, SWT.LEFT, 1);
      idCol.setText(Messages.MarkerStatsView_fileColumn);
      idCol.setWidth(150);

      TableColumn folderCol = new TableColumn(table, SWT.LEFT, 2);
      folderCol.setText(Messages.MarkerStatsView_folderColumn);
      folderCol.setWidth(300);

      TableColumn countCol = new TableColumn(table, SWT.RIGHT, 3);
      countCol.setText(Messages.MarkerStatsView_lineColumn);
      countCol.pack();

      TableColumn messageCol = new TableColumn(table, SWT.LEFT, 4);
      messageCol.setText(Messages.MarkerStatsView_messageColumn);
      messageCol.setWidth(300);

      // set the providers
      tableViewer.setContentProvider(providers.contentProvider());
      tableViewer.setLabelProvider(providers.multiProvider());
      TableViewerEnhancer.enhance(tableViewer, providers.multiProvider());

      // add selection listener to maintain action state
      tableViewer.addSelectionChangedListener(event -> updateActions.run());

      // hooks the action to double click
      tableViewer.addDoubleClickListener(event -> showErrorAction.run());

      // and to the context menu too
      hookContextMenu(List.of(drillBackAction, showErrorAction), tableViewer, site);
    }

    @Override
    protected TableViewer getTableViewer() {
      return tableViewer;
    }
  }

  private abstract static class AbstractStatTableViewer<T> extends Composite {

    private final Class<T> selectionClass;

    private AbstractStatTableViewer(Composite parent, int style, Class<T> selectionClass) {
      super(parent, style);
      this.selectionClass = selectionClass;

      setLayout(new FillLayout());
    }

    protected abstract TableViewer getTableViewer();

    public void setStats(Stats stats) {
      getTableViewer().setInput(stats);
    }

    public Optional<T> getSelection() {
      if (getTableViewer().getSelection() instanceof StructuredSelection selection
              && selectionClass.isInstance(selection.getFirstElement())) {
        return Optional.of(selectionClass.cast(selection.getFirstElement()));
      }
      return Optional.empty();
    }

    public void refresh() {
      getTableViewer().refresh();
    }

  }

}
