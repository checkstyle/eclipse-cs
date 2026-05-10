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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.CreateStatsJob;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.internal.CheckstyleMarkerFilter;
import net.sf.eclipsecs.ui.stats.views.internal.CheckstyleMarkerFilterDialog;

/**
 * Abstract view that gathers common behaviour for the stats views.
 *
 */
public abstract class AbstractStatsView extends ViewPart {

  //
  // attributes
  //

  /** The filter for this stats view. */
  private CheckstyleMarkerFilter filter;

  /** The focused resources. */
  private IResource[] mFocusedResources;

  /** The views private set of statistics. */
  private Stats mStats;

  /** The listener reacting to selection changes in the workspace. */
  private ISelectionListener mFocusListener;

  /** The listener reacting on resource changes. */
  private IResourceChangeListener mResourceListener;

  //
  // methods
  //

  protected abstract void initMenu(IMenuManager menu);

  protected abstract void initToolBar(IToolBarManager tbm);

  /**
   * Returns the view id of the concrete view. This is used to make separate filter settings (stored
   * in dialog settings) for different concrete views possible.
   *
   * @return the view id
   */
  protected abstract String getViewId();

  /**
   * Callback for subclasses to refresh the content of their controls, since the statistics data has
   * been updated. <br/>
   * Note that the subclass should check if their controls have been disposed, since this method is
   * called by a job that might run even if the view has been closed.
   */
  protected abstract void handleStatsRebuilt();

  /**
   * Create the viewer actions.
   */
  protected abstract void makeActions();

  @Override
  public void createPartControl(Composite parent) {
    filter = CheckstyleMarkerFilter.restoreState(getDialogSettings(), new IResource[0]);

    // create and register the workspace focus listener
    mFocusListener = AbstractStatsView.this::focusSelectionChanged;

    getSite().getPage().addSelectionListener(mFocusListener);
    ISelection selection = getSite().getPage().getSelection();
    if (selection == null || selection instanceof TextSelection) {
      focusSelectionChanged(getSite().getPage().getActiveEditor(), null);
    } else {
      focusSelectionChanged(null, selection);
    }

    // create and register the listener for resource changes
    mResourceListener = event -> {
      int numMarkerDeltas = event.findMarkerDeltas(CheckstyleMarker.MARKER_ID, true).length;
      if (numMarkerDeltas > 0) {
        refresh();
      }
    };

    ResourcesPlugin.getWorkspace().addResourceChangeListener(mResourceListener);

    makeActions();
    initActionBars(getViewSite().getActionBars());
  }

  @Override
  public void setFocus() {

  }

  @Override
  public void dispose() {
    // IMPORTANT: Deregister listeners
    getSite().getPage().removeSelectionListener(mFocusListener);
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(mResourceListener);

    super.dispose();
  }

  /**
   * Opens the filters dialog for the specific stats view.
   */
  public final void openFiltersDialog() {
    CheckstyleMarkerFilterDialog dialog = new CheckstyleMarkerFilterDialog(
            getSite().getShell(), filter);

    if (dialog.open() == Window.OK) {
      filter = dialog.getFilter();
      filter.saveState(getDialogSettings());

      refresh();
    }
  }

  /**
   * Initializes the action bars of this view.
   *
   * @param actionBars
   *          the action bars
   */
  private void initActionBars(IActionBars actionBars) {
    initMenu(actionBars.getMenuManager());
    initToolBar(actionBars.getToolBarManager());
  }

  /**
   * Returns the statistics data.
   *
   * @return the data of this view
   */
  protected final Stats getStats() {
    return mStats;
  }

  /**
   * Causes the view to re-sync its contents with the workspace. Note that changes will be scheduled
   * in a background job, and may not take effect immediately.
   */
  protected final void refresh() {
    final IWorkbenchSiteProgressService service = getSite()
            .getAdapter(IWorkbenchSiteProgressService.class);

    WorkbenchJob uiJob = new WorkbenchJob(Messages.AbstractStatsView_msgRefreshStats) {
      {
        setPriority(Job.DECORATE);
        setSystem(true);
      }

      @Override
      public IStatus runInUIThread(IProgressMonitor monitor) {
        handleStatsRebuilt();
        return Status.OK_STATUS;
      }
    };

    // rebuild statistics data
    CreateStatsJob job = new CreateStatsJob(filter, getViewId());
    job.setPriority(Job.DECORATE);
    job.setRule(ResourcesPlugin.getWorkspace().getRoot());
    job.addJobChangeListener(IJobChangeListener.onDone(event -> {
      mStats = ((CreateStatsJob) event.getJob()).getStats();
      uiJob.schedule();
    }));
    service.schedule(job, 0, true);
  }

  /**
   * Returns the dialog settings for this view.
   *
   * @return the dialog settings
   */
  protected final IDialogSettings getDialogSettings() {

    String concreteViewId = getViewId();

    IDialogSettings workbenchSettings = CheckstyleUIPlugin.getDefault().getDialogSettings();
    IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

    if (settings == null) {
      settings = workbenchSettings.addNewSection(concreteViewId);
    }

    return settings;
  }

  /**
   * Invoked on selection changes within the workspace.
   *
   * @param part
   *          the workbench part the selection occurred
   * @param selection
   *          the selection
   */
  private void focusSelectionChanged(IWorkbenchPart part, ISelection selection) {
    List<IResource> resources = SelectionTool.resolveSelection(part, selection);

    IResource[] focusedResources = new IResource[resources.size()];
    resources.toArray(focusedResources);

    // check if update necessary -> if so then update
    boolean updateNeeded = updateNeeded(mFocusedResources, focusedResources);
    if (updateNeeded) {
      mFocusedResources = focusedResources;
      filter = filter.withFocusResources(focusedResources);
      refresh();
    }
  }

  /**
   * Checks if an update of the statistics data is needed, based on the current and previously
   * selected resources. The current filter setting is also taken into consideration.
   *
   * @param oldResources
   *          the previously selected resources.
   * @param newResources
   *          the currently selected resources
   * @return <code>true</code> if an update of the statistics data is needed
   */
  private boolean updateNeeded(IResource[] oldResources, IResource[] newResources) {
    // determine if an update if refiltering is required
    if (!filter.enabled()) {
      return false;
    }

    int onResource = filter.onResource();
    if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE
            || onResource == CheckstyleMarkerFilter.ON_WORKING_SET) {
      return false;
    }
    if (newResources.length == 0) {
      return false;
    }
    if (oldResources == null || oldResources.length == 0) {
      return true;
    }
    if (Arrays.equals(oldResources, newResources)) {
      return false;
    }
    if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT) {
      return CheckstyleMarkerFilter.getProjects(oldResources)
              .equals(CheckstyleMarkerFilter.getProjects(newResources));
    }

    return true;
  }

}
