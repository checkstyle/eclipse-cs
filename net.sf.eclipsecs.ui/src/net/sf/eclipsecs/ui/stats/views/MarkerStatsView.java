//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.MarkerUtilities;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.MarkerStat;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.internal.FiltersAction;
import net.sf.eclipsecs.ui.util.table.ITableComparableProvider;
import net.sf.eclipsecs.ui.util.table.ITableSettingsProvider;
import net.sf.eclipsecs.ui.util.table.TableViewerEnhancer;

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
  private MainTableViewer mMasterViewer;

  /** The detail viewer. */
  private DetailTableViewer mDetailViewer;

  /** The action to show the detail view. */
  private Action mDrillDownAction;

  /** The action to go back to the master view. */
  private Action mDrillBackAction;

  /** Opens the editor and shows the error in the code. */
  private Action mShowErrorAction;

  /** The state if the view is currently drilled down to details. */
  private boolean mIsDrilledDown;

  //
  // methods
  //

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
    mMasterViewer = new MainTableViewer(mMainSection, SWT.NONE, getSite(), getDialogSettings(),
            this::updateActions, mDrillDownAction);

    // create the detail viewer
    mDetailViewer = new DetailTableViewer(mMainSection, SWT.NONE, getSite(), getDialogSettings(),
            this::updateActions, mDrillBackAction, mShowErrorAction);

    mStackLayout.topControl = mMasterViewer;

    updateActions();

    // initialize the view data
    refresh();

    // initFromSettings();
  }

  @Override
  public void setFocus() {
    super.setFocus();
    mStackLayout.topControl.setFocus();
  }

  @Override
  protected void initMenu(IMenuManager menu) {
    menu.add(new FiltersAction(this));
  }

  @Override
  protected void initToolBar(IToolBarManager tbm) {
    tbm.add(mDrillBackAction);
    tbm.add(mDrillDownAction);
    tbm.add(new FiltersAction(this));
  }

  @Override
  protected String getViewId() {
    return VIEW_ID;
  }

  @Override
  protected void handleStatsRebuilt() {

    if (mMasterViewer != null && !mMasterViewer.getTable().isDisposed()) {

      mMasterViewer.setStats(getStats());
      mDetailViewer.setStats(getStats());

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
    // action used to display the detail of a specific error type
    mDrillDownAction = new Action() {
      @Override
      public void run() {
        mMasterViewer.getSelection().ifPresent(markerStat -> {
          mIsDrilledDown = true;
          mDetailViewer.setCurrentDetailCategory(markerStat.getIdentifiant());
          mStackLayout.topControl = mDetailViewer;
          mMainSection.layout();
          mDetailViewer.refresh();

          updateActions();
          updateLabel();
        });
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
        mDetailViewer.getSelection().ifPresent(marker -> {
          try {
            IDE.openEditor(getSite().getPage(), marker);
          } catch (PartInitException ex) {
            CheckstyleLog.log(ex, Messages.MarkerStatsView_unableToShowMarker);
            // TODO : Open information dialog to notify the user
          }
        });
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
    mDetailViewer.setCurrentDetailCategory(null);
    mStackLayout.topControl = mMasterViewer;
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

      String text = NLS.bind(Messages.MarkerStatsView_lblDetailMessage,
              new Object[] { mDetailViewer.getCurrentDetailCategory(),
                  Integer.valueOf(mDetailViewer.getTable().getItemCount()) });
      mDescLabel.setText(text);
    }
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

  private abstract static class AbstractStatTableViewer<T> extends Composite {

    private final Class<T> selectionClass;

    private AbstractStatTableViewer(Composite parent, int style, Class<T> selectionClass) {
      super(parent, style);
      this.selectionClass = selectionClass;

      setLayout(new FillLayout());
    }

    protected abstract TableViewer getTableViewer();

    public Table getTable() {
      return getTableViewer().getTable();
    }

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

  private static class MainTableViewer extends AbstractStatTableViewer<MarkerStat> {

    private final TableViewer tableViewer;

    private MainTableViewer(Composite parent, int style, IWorkbenchPartSite site,
            IDialogSettings mainSettings, Runnable updateActions, IAction drillDownAction) {
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
      tableViewer.setContentProvider(new MasterContentProvider());
      MasterViewMultiProvider multiProvider = new MasterViewMultiProvider(mainSettings);
      tableViewer.setLabelProvider(multiProvider);
      TableViewerEnhancer.enhance(tableViewer, multiProvider);

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

  private static class DetailTableViewer extends AbstractStatTableViewer<IMarker> {

    private final TableViewer tableViewer;
    private final DetailContentProvider contentProvider;

    private DetailTableViewer(Composite parent, int style, IWorkbenchPartSite site,
            IDialogSettings mainSettings, Runnable updateActions, IAction drillBackAction,
            IAction showErrorAction) {
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

      this.contentProvider = new DetailContentProvider();

      // set the providers
      tableViewer.setContentProvider(contentProvider);
      DetailViewMultiProvider multiProvider = new DetailViewMultiProvider(mainSettings);
      tableViewer.setLabelProvider(multiProvider);
      TableViewerEnhancer.enhance(tableViewer, multiProvider);

      // add selection listener to maintain action state
      tableViewer.addSelectionChangedListener(event -> updateActions.run());

      // hooks the action to double click
      tableViewer.addDoubleClickListener(event -> showErrorAction.run());

      // and to the context menu too
      ArrayList<Object> actionList = new ArrayList<>(1);
      actionList.add(drillBackAction);
      actionList.add(showErrorAction);
      hookContextMenu(actionList, tableViewer, site);
    }

    public String getCurrentDetailCategory() {
      return this.contentProvider.getCurrentDetailCategory();
    }

    private void setCurrentDetailCategory(String currentDetailCategory) {
      this.contentProvider.setCurrentDetailCategory(currentDetailCategory);
    }

    @Override
    protected TableViewer getTableViewer() {
      return tableViewer;
    }
  }

  /**
   * Content provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private static class MasterContentProvider implements IStructuredContentProvider {
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
  private static class DetailContentProvider implements IStructuredContentProvider {

    private Object[] mCurrentDetails;
    private String currentDetailCategory;

    private DetailContentProvider() {

    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (mCurrentDetails == null) {
        // find the marker statistics for the current category
        Stats currentStats = (Stats) inputElement;
        Collection<MarkerStat> markerStats = currentStats.getMarkerStats();
        Iterator<MarkerStat> iter = markerStats.iterator();
        while (iter.hasNext()) {
          MarkerStat markerStat = iter.next();
          if (markerStat.getIdentifiant().equals(currentDetailCategory)) {
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

    public String getCurrentDetailCategory() {
      return currentDetailCategory;
    }

    public void setCurrentDetailCategory(String currentDetailCategory) {
      this.currentDetailCategory = currentDetailCategory;
    }

  }

  /**
   * Label provider for the master table viewer.
   *
   * @author Lars Ködderitzsch
   */
  private static class MasterViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    private final IDialogSettings mainSettings;

    private MasterViewMultiProvider(IDialogSettings mainSettings) {
      this.mainSettings = mainSettings;
    }

    @Override
    public String getColumnText(Object obj, int index) {
      MarkerStat stat = (MarkerStat) obj;
      return switch (index) {
        case 1 -> stat.getIdentifiant();
        case 2 -> Integer.toString(stat.getCount());
        default -> "";
      };
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
      return switch (colIndex) {
        case 0 -> Integer.valueOf(stat.getMaxSeverity() * -1);
        case 1 -> stat.getIdentifiant();
        case 2 -> Integer.valueOf(stat.getCount());
        default -> "";
      };
    }

    @Override
    public IDialogSettings getTableSettings() {
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
  private static class DetailViewMultiProvider extends LabelProvider
          implements ITableLabelProvider, ITableComparableProvider, ITableSettingsProvider {

    private final IDialogSettings mainSettings;

    private DetailViewMultiProvider(IDialogSettings mainSettings) {
      this.mainSettings = mainSettings;
    }

    @Override
    public String getColumnText(Object obj, int index) {
      IMarker marker = (IMarker) obj;
      try {
        return switch (index) {
          case 1 -> marker.getResource().getName();
          case 2 -> marker.getResource().getParent().getFullPath().toString();
          case 3 -> Objects.toString(marker.getAttribute(IMarker.LINE_NUMBER), "");
          case 4 -> Objects.toString(marker.getAttribute(IMarker.MESSAGE), "");
          default -> "";
        };
      } catch (Exception ex) {
        // Can't do anything: let's put a default value
        CheckstyleLog.log(ex);
        return Messages.MarkerStatsView_unknownProblem;
      }
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
      return switch (colIndex) {
        case 0 -> Integer.valueOf(marker.getAttribute(IMarker.SEVERITY, Integer.MAX_VALUE) * -1);
        case 1 -> marker.getResource().getName();
        case 2 -> marker.getResource().getParent().getFullPath().toString();
        case 3 -> Integer.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, Integer.MAX_VALUE));
        case 4 -> marker.getAttribute(IMarker.MESSAGE, "");
        default -> "";
      };
    }

    @Override
    public IDialogSettings getTableSettings() {
      IDialogSettings settings = mainSettings.getSection(TAG_SECTION_DETAIL);

      if (settings == null) {
        settings = mainSettings.addNewSection(TAG_SECTION_DETAIL);
      }

      return settings;
    }
  }
}
