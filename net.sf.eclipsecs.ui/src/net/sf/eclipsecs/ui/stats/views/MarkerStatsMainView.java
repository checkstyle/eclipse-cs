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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsView.MarkerStatsViewActions;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsViewDataProviders.MarkerStatsViewDetailDataProviders;
import net.sf.eclipsecs.ui.stats.views.MarkerStatsViewDataProviders.MarkerStatsViewMasterDataProviders;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

public final class MarkerStatsMainView extends Composite {

    /** The stack layout. */
    private final StackLayout mStackLayout;
    /** The master table viewer. */
    private final MainTableViewer mMasterViewer;
    /** The detail table viewer. */
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
        mDetailViewer = new DetailTableViewer(this, SWT.NONE, providers.detail(), site,
            updateActions, actions.mDrillBackAction(), actions.mShowErrorAction());

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
     *            a collection of IAction objets
     * @param viewer
     *            the table viewer
     * @param site
     *            the workbench part site
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

        /** The table viewer. */
        private final TableViewer tableViewer;

        private MainTableViewer(Composite parent, int style,
            MarkerStatsViewMasterDataProviders providers, IWorkbenchPartSite site,
            Runnable updateActions, IAction drillDownAction) {
            super(parent, style, MarkerStat.class);

            final TableColumnLayout tableColumnLayout = new TableColumnLayout();
            setLayout(tableColumnLayout);

            this.tableViewer = new TableViewer(this,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

            Table table = tableViewer.getTable();
            table.setLinesVisible(true);
            table.setHeaderVisible(true);

            createColumns(tableColumnLayout);

            tableViewer.setContentProvider(providers.contentProvider());
            TableViewerEnhancer.enhance(tableViewer, providers.getTableSettings(),
                tableColumnLayout);

            tableViewer.addSelectionChangedListener(event -> updateActions.run());

            tableViewer.addDoubleClickListener(event -> drillDownAction.run());

            final ArrayList<Object> actionList = new ArrayList<>();
            actionList.add(drillDownAction);
            hookContextMenu(actionList, tableViewer, site);
        }

        private void createColumns(TableColumnLayout tableColumnLayout) {
            final TableViewerColumn severityCol = new TableViewerColumn(tableViewer, SWT.CENTER);
            severityCol.setLabelProvider(ColumnLabelProvider.createImageProvider(element -> {
                final ISharedImages imgs = PlatformUI.getWorkbench().getSharedImages();
                return switch (((MarkerStat) element).getMaxSeverity()) {
                    case IMarker.SEVERITY_ERROR -> imgs.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    case IMarker.SEVERITY_WARNING -> imgs.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    case IMarker.SEVERITY_INFO -> imgs.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                    default -> null;
                };
            }));
            severityCol.getColumn().pack();
            severityCol.getColumn().setResizable(false);
            tableColumnLayout.setColumnData(severityCol.getColumn(),
                new ColumnPixelData(severityCol.getColumn().getWidth()));
            TableViewerEnhancer.setColumnComparator(severityCol.getColumn(),
                Comparator.comparingInt(MarkerStat::getMaxSeverity).reversed());

            final TableViewerColumn idCol = new TableViewerColumn(tableViewer, SWT.LEFT);
            idCol.getColumn().setText(Messages.MarkerStatsView_kindOfErrorColumn);
            idCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
                element -> ((MarkerStat) element).getIdentifiant()));
            idCol.getColumn().pack();
            tableColumnLayout.setColumnData(idCol.getColumn(),
                new ColumnPixelData(idCol.getColumn().getWidth()));

            final TableViewerColumn countCol = new TableViewerColumn(tableViewer, SWT.RIGHT);
            countCol.getColumn().setText(Messages.MarkerStatsView_numberOfErrorsColumn);
            countCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
                element -> Integer.toString(((MarkerStat) element).getCount())));
            countCol.getColumn().pack();
            tableColumnLayout.setColumnData(countCol.getColumn(),
                new ColumnPixelData(countCol.getColumn().getWidth()));
            TableViewerEnhancer.setColumnComparator(countCol.getColumn(),
                Comparator.comparingInt(stat -> ((MarkerStat) stat).getCount()));
        }

        @Override
        protected TableViewer getTableViewer() {
            return tableViewer;
        }

    }

    private static final class DetailTableViewer extends AbstractStatTableViewer<IMarker> {

        /** The table viewer. */
        private final TableViewer tableViewer;

        private DetailTableViewer(Composite parent, int style,
            MarkerStatsViewDetailDataProviders providers, IWorkbenchPartSite site,
            Runnable updateActions, IAction drillBackAction, IAction showErrorAction) {
            super(parent, style, IMarker.class);

            final TableColumnLayout tableColumnLayout = new TableColumnLayout();
            setLayout(tableColumnLayout);

            this.tableViewer = new TableViewer(this,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

            Table table = tableViewer.getTable();
            table.setLinesVisible(true);
            table.setHeaderVisible(true);

            createColumns(tableColumnLayout);

            // set the providers
            tableViewer.setContentProvider(providers.contentProvider());
            TableViewerEnhancer.enhance(tableViewer,
                providers.getTableSettings(), tableColumnLayout);

            // add selection listener to maintain action state
            tableViewer.addSelectionChangedListener(event -> updateActions.run());

            // hooks the action to double click
            tableViewer.addDoubleClickListener(event -> showErrorAction.run());

            // and to the context menu too
            hookContextMenu(List.of(drillBackAction, showErrorAction), tableViewer, site);
        }

        private void createColumns(TableColumnLayout tableColumnLayout) {
            final TableViewerColumn severityCol = new TableViewerColumn(tableViewer, SWT.CENTER);
            severityCol.setLabelProvider(ColumnLabelProvider.createImageProvider(element -> {
                final ISharedImages imgs = PlatformUI.getWorkbench().getSharedImages();
                return switch (((IMarker) element).getAttribute(IMarker.SEVERITY, 0)) {
                    case IMarker.SEVERITY_ERROR -> imgs.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    case IMarker.SEVERITY_WARNING -> imgs.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
                    case IMarker.SEVERITY_INFO -> imgs.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
                    default -> null;
                };
            }));
            severityCol.getColumn().pack();
            severityCol.getColumn().setResizable(false);
            tableColumnLayout.setColumnData(severityCol.getColumn(),
                new ColumnPixelData(severityCol.getColumn().getWidth()));
            TableViewerEnhancer.setColumnComparator(severityCol.getColumn(),
                Comparator.comparingInt(marker -> -((IMarker) marker).getAttribute(IMarker.SEVERITY,
                    Integer.MAX_VALUE)));

            final TableViewerColumn fileCol = new TableViewerColumn(tableViewer, SWT.LEFT);
            fileCol.getColumn().setText(Messages.MarkerStatsView_fileColumn);
            fileCol.setLabelProvider(ColumnLabelProvider
                .createTextProvider(element -> ((IMarker) element).getResource().getName()));
            fileCol.getColumn().pack();
            tableColumnLayout.setColumnData(fileCol.getColumn(),
                new ColumnPixelData(fileCol.getColumn().getWidth()));

            final TableViewerColumn folderCol = new TableViewerColumn(tableViewer, SWT.LEFT);
            folderCol.getColumn().setText(Messages.MarkerStatsView_folderColumn);
            folderCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
                marker -> ((IMarker) marker).getResource().getParent().getFullPath().toString()));
            folderCol.getColumn().pack();
            tableColumnLayout.setColumnData(folderCol.getColumn(),
                new ColumnPixelData(folderCol.getColumn().getWidth()));

            final TableViewerColumn lineCol = new TableViewerColumn(tableViewer, SWT.RIGHT);
            lineCol.getColumn().setText(Messages.MarkerStatsView_lineColumn);
            lineCol.setLabelProvider(ColumnLabelProvider.createTextProvider(element -> String
                .valueOf(((IMarker) element).getAttribute(IMarker.LINE_NUMBER, 0))));
            lineCol.getColumn().pack();
            tableColumnLayout.setColumnData(lineCol.getColumn(),
                new ColumnPixelData(lineCol.getColumn().getWidth()));
            TableViewerEnhancer.setColumnComparator(lineCol.getColumn(), Comparator.comparingInt(
                marker -> ((IMarker) marker).getAttribute(IMarker.LINE_NUMBER, Integer.MAX_VALUE)));

            final TableViewerColumn messageCol = new TableViewerColumn(tableViewer, SWT.LEFT);
            messageCol.getColumn().setText(Messages.MarkerStatsView_messageColumn);
            messageCol.setLabelProvider(ColumnLabelProvider.createTextProvider(
                element -> ((IMarker) element).getAttribute(IMarker.MESSAGE, "")));
            messageCol.getColumn().pack();
            tableColumnLayout.setColumnData(messageCol.getColumn(),
                new ColumnPixelData(messageCol.getColumn().getWidth()));
        }

        @Override
        protected TableViewer getTableViewer() {
            return tableViewer;
        }
    }

    private abstract static class AbstractStatTableViewer<T> extends Composite {

        /** The selection class type. */
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
            Optional<T> selection = Optional.empty();
            if (getTableViewer().getSelection() instanceof StructuredSelection structuredSelection
                && selectionClass.isInstance(structuredSelection.getFirstElement())) {
                selection = Optional.of(selectionClass.cast(structuredSelection.getFirstElement()));
            }
            return selection;
        }

        public void refresh() {
            getTableViewer().refresh();
        }

    }

}
