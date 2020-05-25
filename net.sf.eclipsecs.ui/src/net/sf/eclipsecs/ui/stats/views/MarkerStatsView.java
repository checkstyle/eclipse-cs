//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================

package net.sf.eclipsecs.ui.stats.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.internal.FiltersAction;
import net.sf.eclipsecs.ui.util.table.EnhancedTableViewer;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * View that displays statistics about checkstyle markers.
 *
 * @author Fabrice BELLINGARD
 * @author Lars Ködderitzsch
 */
public class MarkerStatsView extends AbstractStatsView {

  //
  // constants
  //

  /** The unique view id. */
  public static final String VIEW_ID = MarkerStatsView.class.getName();

  private static final String TAG_SECTION_MASTER = "masterView";

  private static final String TAG_SECTION_DETAIL = "detailView";

  //
  // attributes
  //

  /** The description label. */
  private Label mDescLabel;

  /** The main composite. */
  private Composite mMainSection;

  /** The stack layout of the main composite. */
  private StackLayout mStackLayout;

  /** The master viewer. */
  private EnhancedTableViewer mMasterViewer;

  /** The detail viewer. */
  private EnhancedTableViewer mDetailViewer;

  /** Action to show the charts view. */
  private Action mChartAction;

  /** The action to show the detail view. */
  private Action mDrillDownAction;

  /** The action to go back to the master view. */
  private Action mDrillBackAction;

  /** Opens the editor and shows the error in the code. */
  private Action mShowErrorAction;

  /** The current violation category to show in details view. */
  private String mCurrentDetailCategory;

  /** The state if the view is currently drilled down to details. */
  private boolean mIsDrilledDown;

  //
  // methods
  //

  /**
   * {@inheritDoc}
   */
  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);

    // set up the main layout
    GridLayout layout = new GridLayout(1, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    parent.setLayout(layout);

    // the label
    mDescLabel = new Label(parent, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    mDescLabel.setLayoutData(gridData);

    // the main section
    mMainSection = new Composite(parent, SWT.NONE);
    mStackLayout = new StackLayout();
    mStackLayout.marginHeight = 0;
    mStackLayout.marginWidth = 0;
    mMainSection.setLayout(mStackLayout);
    mMainSection.setLayoutData(new GridData(GridData.FILL_BOTH));

    // create the master viewer
    mMasterViewer = createMasterView(mMainSection);

    // create the detail viewer
    mDetailViewer = createDetailView(mMainSection);

    mStackLayout.topControl = mMasterViewer.getTable();

    updateActions();

    // initialize the view data
    refresh();

    // initFromSettings();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFocus() {
    super.setFocus();
    mStackLayout.topControl.setFocus();
  }

  /**
   * Creates the table viewer for the master view.
   *
   * @param parent
   *          the parent composite
   * @return the master table viewer
   */
  private EnhancedTableViewer createMasterView(Composite parent) {
    EnhancedTableViewer masterViewer = new EnhancedTableViewer(parent,
            SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    masterViewer.getControl().setLayoutData(gridData);

    // setup the table columns
    Table table = masterViewer.getTable();
    table.setLinesVisible(true);
    table.setHeaderVisible(true);

    TableColumn severityCol = new TableColumn(table, SWT.CENTER, 0);
    severityCol.setWidth(20);
    severityCol.setResizable(false);

    TableColumn idCol = new TableColumn(table, SWT.LEFT, 1);
    idCol.setText(Messages.MarkerStatsView_kindOfErrorColumn);
    idCol.setWidth(400);

    TableColumn countCol = new TableColumn(table, SWT.CENTER, 2);
    countCol.setText(Messages.MarkerStatsView_numberOfErrorsColumn);
    countCol.pack();

    // set the providers
    masterViewer.setContentProvider(new MasterContentProvider());
    MasterViewMultiProvider multiProvider = new MasterViewMultiProvider();
    masterViewer.setLabelProvider(multiProvider);
    masterViewer.setTableComparableProvider(multiProvider);
    masterViewer.setTableSettingsProvider(multiProvider);
    masterViewer.installEnhancements();

    // add selection listener to maintain action state
    masterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        updateActions();
      }
    });

    // hooks the action to double click
    hookDoubleClickAction(mDrillDownAction, masterViewer);

    // and to the context menu too
    ArrayList<Object> actionList = new ArrayList<>(3);
    actionList.add(mDrillDownAction);
    actionList.add(new Separator());
    actionList.add(mChartAction);
    hookContextMenu(actionList, masterViewer);

    return masterViewer;
  }

  /**
   * Creates the table viewer for the detail view.
   *
   * @param parent
   *          the parent composite
   * @return the detail table viewer
   */
  private EnhancedTableViewer createDetailView(Composite parent) {
    // le tableau
    EnhancedTableViewer detailViewer = new EnhancedTableViewer(parent,
            SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    detailViewer.getControl().setLayoutData(gridData);

    // setup the table columns
    Table table = detailViewer.getTable();
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

    TableColumn countCol = new TableColumn(table, SWT.CENTER, 3);
    countCol.setText(Messages.MarkerStatsView_lineColumn);
    countCol.pack();

    TableColumn messageCol = new TableColumn(table, SWT.LEFT, 4);
    messageCol.setText(Messages.MarkerStatsView_messageColumn);
    messageCol.setWidth(300);

    // set the providers
    detailViewer.setContentProvider(new DetailContentProvider());
    DetailViewMultiProvider multiProvider = new DetailViewMultiProvider();
    detailViewer.setLabelProvider(multiProvider);
    detailViewer.setTableComparableProvider(multiProvider);
    detailViewer.setTableSettingsProvider(multiProvider);
    detailViewer.installEnhancements();

    // add selection listener to maintain action state
    detailViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        updateActions();
      }
    });

    // hooks the action to double click
    hookDoubleClickAction(mShowErrorAction, detailViewer);

    // and to the context menu too
    ArrayList<Object> actionList = new ArrayList<>(1);
    actionList.add(mDrillBackAction);
    actionList.add(mShowErrorAction);
    actionList.add(new Separator());
    actionList.add(mChartAction);
    hookContextMenu(actionList, detailViewer);

    return detailViewer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initMenu(IMenuManager menu) {
    menu.add(new FiltersAction(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initToolBar(IToolBarManager tbm) {
    tbm.add(mChartAction);
    tbm.add(new Separator());
    tbm.add(mDrillBackAction);
    tbm.add(mDrillDownAction);
    tbm.add(new FiltersAction(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getViewId() {
    return VIEW_ID;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void handleStatsRebuilt() {

    if (mMasterViewer != null && !mMasterViewer.getTable().isDisposed()) {

      mMasterViewer.setInput(getStats());
      mDetailViewer.setInput(getStats());

      if (mIsDrilledDown && mDetailViewer.getTable().getItemCount() == 0) {
        drillBack();
      } else {
        // update the actions and the label
        updateActions();
        updateLabel();
      }
    }
  }

  @Override
  protected void makeActions() {
    // Action used to display the pie chart
    mChartAction = new Action() {
      @Override
      public void run() {
        try {
          getSite().getWorkbenchWindow().getActivePage().showView(GraphStatsView.VIEW_ID);
        } catch (PartInitException e) {
          CheckstyleLog.log(e,
                  NLS.bind(Messages.MarkerStatsView_unableToOpenGraph, GraphStatsView.VIEW_ID));
          // TODO : Open information dialog to notify the user
        }
      }
    };
    mChartAction.setText(Messages.MarkerStatsView_displayChart);
    mChartAction.setToolTipText(Messages.MarkerStatsView_displayChartTooltip);
    mChartAction.setImageDescriptor(CheckstyleUIPluginImages.GRAPH_VIEW_ICON);

    // action used to display the detail of a specific error type
    mDrillDownAction = new Action() {
      @Override
      public void run() {
        IStructuredSelection selection = (IStructuredSelection) mMasterViewer.getSelection();
        if (selection.getFirstElement() instanceof MarkerStat) {
          MarkerStat markerStat = (MarkerStat) selection.getFirstElement();

          mIsDrilledDown = true;
          mCurrentDetailCategory = markerStat.getIdentifiant();
          mStackLayout.topControl = mDetailViewer.getTable();
          mMainSection.layout();
          mDetailViewer.setInput(mDetailViewer.getInput());

          updateActions();
          updateLabel();
        }
      }
    };
    mDrillDownAction.setText(Messages.MarkerStatsView_showDetails);
    mDrillDownAction.setToolTipText(Messages.MarkerStatsView_showDetailsTooltip);
    mDrillDownAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
    mDrillDownAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));

    // action used to go back to the master view
    mDrillBackAction = new Action() {
      @Override
      public void run() {
        drillBack();
      }

    };
    mDrillBackAction.setText(Messages.MarkerStatsView_actionBack);
    mDrillBackAction.setToolTipText(Messages.MarkerStatsView_actionBackTooltip);
    mDrillBackAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
    mDrillBackAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));

    // action used to show a specific error in the editor
    mShowErrorAction = new Action() {
      @Override
      public void run() {
        IStructuredSelection selection = (IStructuredSelection) mDetailViewer.getSelection();
        if (selection.getFirstElement() instanceof IMarker) {
          IMarker marker = (IMarker) selection.getFirstElement();
          try {
            IDE.openEditor(getSite().getPage(), marker);
          } catch (PartInitException e) {
            CheckstyleLog.log(e, Messages.MarkerStatsView_unableToShowMarker);
            // TODO : Open information dialog to notify the user
          }
        }
      }
    };
    mShowErrorAction.setText(Messages.MarkerStatsView_displayError);
    mShowErrorAction.setToolTipText(Messages.MarkerStatsView_displayErrorTooltip);
    mShowErrorAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(IDE.SharedImages.IMG_OPEN_MARKER));

  }

  /**
   * Helper method to manage the state of the view's actions.
   */
  private void updateActions() {
    mDrillBackAction.setEnabled(mIsDrilledDown);
    mDrillDownAction.setEnabled(!mIsDrilledDown && !mMasterViewer.getSelection().isEmpty());
    mShowErrorAction.setEnabled(mIsDrilledDown && !mDetailViewer.getSelection().isEmpty());
  }

  private void drillBack() {
    mIsDrilledDown = false;
    mCurrentDetailCategory = null;
    mStackLayout.topControl = mMasterViewer.getTable();
    mMainSection.layout();
    mMasterViewer.refresh();

    updateActions();
    updateLabel();
  }

  /**
   * Helper method to update the label of the view.
   */
  private void updateLabel() {
    if (!mIsDrilledDown) {

      Stats stats = getStats();
      if (stats != null) {
        String text = NLS.bind(Messages.MarkerStatsView_lblOverviewMessage,
                new Object[] { Integer.valueOf(stats.getMarkerCount()),
                    Integer.valueOf(stats.getMarkerStats().size()),
                    Integer.valueOf(stats.getMarkerCountAll()) });
        mDescLabel.setText(text);
      } else {
        mDescLabel.setText("");
      }
    } else {

      String text = NLS.bind(Messages.MarkerStatsView_lblDetailMessage, new Object[] {
          mCurrentDetailCategory, Integer.valueOf(mDetailViewer.getTable().getItemCount()) });
      mDescLabel.setText(text);
    }
  }

  /**
   * Adds the actions to the tableviewer context menu.
   *
   * @param actions
   *          a collection of IAction objets
   */
  private void hookContextMenu(final Collection<Object> actions, StructuredViewer viewer) {
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager manager) {
        for (Iterator<Object> iter = actions.iterator(); iter.hasNext();) {
          Object item = iter.next();
          if (item instanceof IContributionItem) {
            manager.add((IContributionItem) item);
          } else if (item instanceof IAction) {
            manager.add((IAction) item);
          }
        }
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      }
    });
    Menu menu = menuMgr.createContextMenu(viewer.getControl());
    viewer.getControl().setMenu(menu);

    getSite().registerContextMenu(menuMgr, viewer);
  }

  /**
   * Specifies which action will be run when double clicking on the viewer.
   *
   * @param action
   *          the IAction to add
   */
  private void hookDoubleClickAction(final IAction action, StructuredViewer viewer) {
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent event) {
        action.run();
      }
    });
  }

  /**
   * Content provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private class MasterContentProvider implements IStructuredContentProvider {
    private Object[] mCurrentMarkerStats;

    @Override
    public Object[] getElements(Object inputElement) {
      if (mCurrentMarkerStats == null) {
        // find the marker statistics for the current category
        Stats currentStats = (Stats) inputElement;
        mCurrentMarkerStats = currentStats.getMarkerStats().toArray();
      }

      return mCurrentMarkerStats;
    }

    @Override
    public void dispose() {
      mCurrentMarkerStats = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      mCurrentMarkerStats = null;
    }
  }

  /**
   * Content provider for the detail table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private class DetailContentProvider implements IStructuredContentProvider {
    private Object[] mCurrentDetails;

    @Override
    public Object[] getElements(Object inputElement) {
      if (mCurrentDetails == null) {
        // find the marker statistics for the current category
        Stats currentStats = (Stats) inputElement;
        Collection<MarkerStat> markerStats = currentStats.getMarkerStats();
        Iterator<MarkerStat> it = markerStats.iterator();
        while (it.hasNext()) {
          MarkerStat markerStat = it.next();
          if (markerStat.getIdentifiant().equals(mCurrentDetailCategory)) {
            mCurrentDetails = markerStat.getMarkers().toArray();
            break;
          }
        }
      }

      return mCurrentDetails != null ? mCurrentDetails : new Object[0];
    }

    @Override
    public void dispose() {
      mCurrentDetails = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      mCurrentDetails = null;
    }

  }

  /**
   * Label provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private class MasterViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    @Override
    public String getColumnText(Object obj, int index) {
      MarkerStat stat = (MarkerStat) obj;
      String text = null;

      switch (index) {
        case 1:
          text = stat.getIdentifiant();
          break;
        case 2:
          text = stat.getCount() + ""; //$NON-NLS-1$
          break;

        default:
          text = ""; //$NON-NLS-1$
          break;
      }

      return text;
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
      Image image = null;
      MarkerStat stat = (MarkerStat) obj;

      if (index == 0) {
        int severity = stat.getMaxSeverity();
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        if (IMarker.SEVERITY_ERROR == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (IMarker.SEVERITY_WARNING == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (IMarker.SEVERITY_INFO == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      }
      return image;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int colIndex) {
      MarkerStat stat = (MarkerStat) element;
      Comparable<?> comparable = null;

      switch (colIndex) {
        case 0:
          comparable = Integer.valueOf(stat.getMaxSeverity() * -1);
          break;
        case 1:
          comparable = stat.getIdentifiant();
          break;
        case 2:
          comparable = Integer.valueOf(stat.getCount());
          break;

        default:
          comparable = ""; //$NON-NLS-1$
          break;
      }

      return comparable;
    }

    @Override
    public IDialogSettings getTableSettings() {

      IDialogSettings mainSettings = getDialogSettings();

      IDialogSettings settings = mainSettings.getSection(TAG_SECTION_MASTER);

      if (settings == null) {
        settings = mainSettings.addNewSection(TAG_SECTION_MASTER);
      }

      return settings;
    }
  }

  /**
   * Label provider for the detail table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private class DetailViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    @Override
    public String getColumnText(Object obj, int index) {
      IMarker marker = (IMarker) obj;
      String text = null;

      try {
        switch (index) {
          case 1:
            text = marker.getResource().getName();
            break;
          case 2:
            text = marker.getResource().getParent().getFullPath().toString();
            break;
          case 3:
            text = Objects.toString(marker.getAttribute(IMarker.LINE_NUMBER), "");
            break;
          case 4:
            text = Objects.toString(marker.getAttribute(IMarker.MESSAGE), "");
            break;
          default:
            text = ""; //$NON-NLS-1$
            break;
        }
      } catch (Exception e) {
        // Can't do anything: let's put a default value
        text = Messages.MarkerStatsView_unknownProblem;
        CheckstyleLog.log(e);
      }

      return text;
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
      Image image = null;
      IMarker marker = (IMarker) obj;

      if (index == 0) {
        int severity = MarkerUtilities.getSeverity(marker);
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        if (IMarker.SEVERITY_ERROR == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (IMarker.SEVERITY_WARNING == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (IMarker.SEVERITY_INFO == severity) {
          image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      }
      return image;
    }

    @Override
    public Comparable<?> getComparableValue(Object element, int colIndex) {
      IMarker marker = (IMarker) element;
      Comparable<?> comparable = null;

      switch (colIndex) {
        case 0:
          comparable = Integer.valueOf(marker.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE) * -1);
          break;
        case 1:
          comparable = marker.getResource().getName();
          break;
        case 2:
          comparable = marker.getResource().getParent().getFullPath().toString();
          break;
        case 3:
          comparable = Integer.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, Integer.MAX_VALUE));
          break;
        case 4:
          comparable = marker.getAttribute(IMarker.MESSAGE, "");
          break;
        default:
          comparable = ""; //$NON-NLS-1$
          break;
      }

      return comparable;
    }

    @Override
    public IDialogSettings getTableSettings() {

      IDialogSettings mainSettings = getDialogSettings();

      IDialogSettings settings = mainSettings.getSection(TAG_SECTION_DETAIL);

      if (settings == null) {
        settings = mainSettings.addNewSection(TAG_SECTION_DETAIL);
      }

      return settings;
    }
  }
}
