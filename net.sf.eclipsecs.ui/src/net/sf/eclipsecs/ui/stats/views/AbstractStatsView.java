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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.stats.Messages;
import net.sf.eclipsecs.ui.stats.data.CreateStatsJob;
import net.sf.eclipsecs.ui.stats.data.Stats;
import net.sf.eclipsecs.ui.stats.views.internal.CheckstyleMarkerFilter;
import net.sf.eclipsecs.ui.stats.views.internal.CheckstyleMarkerFilterDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Abstract view that gathers common behaviour for the stats views.
 *
 * @author Fabrice BELLINGARD
 * @author Lars Ködderitzsch
 */
public abstract class AbstractStatsView extends ViewPart {

  //
  // attributes
  //

  /** the main composite. */
  private Composite mMainComposite;

  /** The filter for this stats view. */
  private CheckstyleMarkerFilter mFilter;

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

  /**
   * {@inheritDoc}
   *
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {

    mMainComposite = parent;

    // create and register the workspace focus listener
    mFocusListener = new ISelectionListener() {
      @Override
      public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        AbstractStatsView.this.focusSelectionChanged(part, selection);
      }
    };

    getSite().getPage().addSelectionListener(mFocusListener);
    focusSelectionChanged(getSite().getPage().getActivePart(), getSite().getPage().getSelection());

    // create and register the listener for resource changes
    mResourceListener = new IResourceChangeListener() {
      @Override
      public void resourceChanged(IResourceChangeEvent event) {

        IMarkerDelta[] markerDeltas = event.findMarkerDeltas(CheckstyleMarker.MARKER_ID, true);

        if (markerDeltas.length > 0) {
          refresh();
        }
      }
    };

    ResourcesPlugin.getWorkspace().addResourceChangeListener(mResourceListener);

    makeActions();
    initActionBars(getViewSite().getActionBars());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFocus() {

  }

  /**
   * {@inheritDoc}
   */
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
            mMainComposite.getShell(), (CheckstyleMarkerFilter) getFilter().clone());

    if (dialog.open() == Window.OK) {
      CheckstyleMarkerFilter filter = dialog.getFilter();
      filter.saveState(getDialogSettings());

      mFilter = filter;
      refresh();
    }
  }

  /**
   * Initializes the action bars of this view.
   *
   * @param actionBars
   *          the action bars
   */
  protected void initActionBars(IActionBars actionBars) {
    initMenu(actionBars.getMenuManager());
    initToolBar(actionBars.getToolBarManager());
  }

  protected abstract void initMenu(IMenuManager menu);

  protected abstract void initToolBar(IToolBarManager tbm);

  /**
   * Returns the filter of this view.
   *
   * @return the filter
   */
  protected final CheckstyleMarkerFilter getFilter() {
    if (mFilter == null) {
      mFilter = new CheckstyleMarkerFilter();
      mFilter.restoreState(getDialogSettings());
    }

    return mFilter;
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

    @SuppressWarnings("cast")
    final IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite()
            .getAdapter(IWorkbenchSiteProgressService.class);

    // rebuild statistics data
    CreateStatsJob job = new CreateStatsJob(getFilter(), getViewId());
    job.setPriority(Job.DECORATE);
    job.setRule(ResourcesPlugin.getWorkspace().getRoot());
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        mStats = ((CreateStatsJob) event.getJob()).getStats();
        Job uiJob = new WorkbenchJob(Messages.AbstractStatsView_msgRefreshStats) {

          @Override
          public IStatus runInUIThread(IProgressMonitor monitor) {
            handleStatsRebuilt();
            return Status.OK_STATUS;
          }
        };
        uiJob.setPriority(Job.DECORATE);
        uiJob.setSystem(true);
        uiJob.schedule();
      }
    });
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

  /**
   * Invoked on selection changes within the workspace.
   *
   * @param part
   *          the workbench part the selection occurred
   * @param selection
   *          the selection
   */
  private void focusSelectionChanged(IWorkbenchPart part, ISelection selection) {

    List<IResource> resources = new ArrayList<>();
    if (part instanceof IEditorPart) {
      IEditorPart editor = (IEditorPart) part;
      IFile file = getFile(editor.getEditorInput());
      if (file != null) {
        resources.add(file);
      }
    } else {
      if (selection instanceof IStructuredSelection) {
        for (Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator
                .hasNext();) {
          Object object = iterator.next();
          if (object instanceof IWorkingSet) {

            IWorkingSet workingSet = (IWorkingSet) object;
            IAdaptable[] elements = workingSet.getElements();
            for (int i = 0; i < elements.length; i++) {
              considerAdaptable(elements[i], resources);
            }
          } else if (object instanceof IAdaptable) {
            considerAdaptable((IAdaptable) object, resources);
          }
        }
      }
    }

    IResource[] focusedResources = new IResource[resources.size()];
    resources.toArray(focusedResources);

    // check if update necessary -> if so then update
    boolean updateNeeded = updateNeeded(mFocusedResources, focusedResources);
    if (updateNeeded) {
      mFocusedResources = focusedResources;
      getFilter().setFocusResource(focusedResources);
      refresh();
    }
  }

  @SuppressWarnings("cast")
  private void considerAdaptable(IAdaptable adaptable, Collection<IResource> resources) {

    IResource resource = (IResource) adaptable.getAdapter(IResource.class);

    if (resource == null) {
      resource = (IResource) adaptable.getAdapter(IFile.class);
    }

    if (resource != null) {
      resources.add(resource);
    }
  }

  /**
   * *** * Copied from ResourceUtil.getFile() since ResourceUtil is only available since Eclipse 3.1
   * *** Returns the file corresponding to the given editor input, or <code>null</code> if there is
   * no applicable file. Returns <code>null</code> if the given editor input is <code>null</code>.
   *
   * @param editorInput
   *          the editor input, or <code>null</code>
   * @return the file corresponding to the editor input, or <code>null</code>
   */
  public static IFile getFile(IEditorInput editorInput) {
    if (editorInput == null) {
      return null;
    }
    // Note: do not treat IFileEditorInput as a special case. Use the
    // adapter mechanism instead.
    // See Bug 87288 [IDE] [EditorMgmt] Should avoid explicit checks for
    // [I]FileEditorInput
    Object o = editorInput.getAdapter(IFile.class);
    if (o instanceof IFile) {
      return (IFile) o;
    }
    return null;
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
    CheckstyleMarkerFilter filter = getFilter();
    if (!filter.isEnabled()) {
      return false;
    }

    int onResource = filter.getOnResource();
    if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE
            || onResource == CheckstyleMarkerFilter.ON_WORKING_SET) {
      return false;
    }
    if (newResources == null || newResources.length < 1) {
      return false;
    }
    if (oldResources == null || oldResources.length < 1) {
      return true;
    }
    if (Arrays.equals(oldResources, newResources)) {
      return false;
    }
    if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT) {
      Collection<IProject> oldProjects = CheckstyleMarkerFilter.getProjectsAsCollection(oldResources);
      Collection<IProject> newProjects = CheckstyleMarkerFilter.getProjectsAsCollection(newResources);

      if (oldProjects.size() == newProjects.size()) {
        return !newProjects.containsAll(oldProjects);
      }
      return true;
    }

    return true;
  }

}
