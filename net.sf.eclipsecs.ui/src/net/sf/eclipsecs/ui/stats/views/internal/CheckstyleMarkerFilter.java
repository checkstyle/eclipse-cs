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

package net.sf.eclipsecs.ui.stats.views.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IWorkingSet;

/**
 * Filter class for Checkstyle markers. This filter is used by the Checkstyle statistics views.
 *
 * @author Lars Ködderitzsch
 */
public class CheckstyleMarkerFilter implements Cloneable {

  //
  // constants
  //

  private static final String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$

  private static final String TAG_ENABLED = "enabled"; //$NON-NLS-1$

  private static final String TAG_ON_RESOURCE = "onResource"; //$NON-NLS-1$

  private static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$

  private static final String TAG_SELECT_BY_SEVERITY = "selectBySeverity"; //$NON-NLS-1$

  private static final String TAG_SEVERITY = "severity"; //$NON-NLS-1$

  private static final String TAG_SELECT_BY_REGEX = "selectByRegex"; //$NON-NLS-1$

  private static final String TAG_REGULAR_EXPRESSIONS = "regularExpressions"; //$NON-NLS-1$

  public static final int ON_ANY_RESOURCE = 0;

  public static final int ON_SELECTED_RESOURCE_ONLY = 1;

  public static final int ON_SELECTED_RESOURCE_AND_CHILDREN = 2;

  public static final int ON_ANY_RESOURCE_OF_SAME_PROJECT = 3;

  public static final int ON_WORKING_SET = 4;

  private static final int DEFAULT_SEVERITY = 0;

  public static final int SEVERITY_ERROR = 1 << 2;

  public static final int SEVERITY_WARNING = 1 << 1;

  public static final int SEVERITY_INFO = 1 << 0;

  private static final int DEFAULT_ON_RESOURCE = ON_ANY_RESOURCE;

  private static final boolean DEFAULT_SELECT_BY_SEVERITY = false;

  private static final boolean DEFAULT_ACTIVATION_STATUS = true;

  //
  // attributes
  //

  /** Determines if this filter is enabled. */
  private boolean mEnabled;

  /** The selection mode. */
  private int mOnResource;

  /** The selected working set. */
  private IWorkingSet mWorkingSet;

  /** Flags if the severity filtering is active. */
  private boolean mSelectBySeverity;

  /** The selected severity. */
  private int mSeverity;

  /** The focused resources within the current workbench page. */
  private IResource[] mFocusResources;

  /** Flags if the regex filter is enabled. */
  private boolean mFilterByRegex;

  /** List of regular expressions used to filter messages. */
  private List<String> mFilterRegex;

  //
  // methods
  //

  /**
   * Searches the workspace for markers that pass this filter.
   *
   * @param mon
   *          the progress monitor
   * @return the array of Checkstyle markers that pass this filter.
   * @throws CoreException
   *           an unexpected error occurred
   */
  public IMarker[] findMarkers(IProgressMonitor mon) throws CoreException {

    List<IMarker> unfiltered = Collections.emptyList();

    if (!isEnabled()) {
      unfiltered = findCheckstyleMarkers(
              new IResource[] { ResourcesPlugin.getWorkspace().getRoot() },
              IResource.DEPTH_INFINITE, mon);
    } else {

      switch (getOnResource()) {
        case ON_ANY_RESOURCE: {
          unfiltered = findCheckstyleMarkers(
                  new IResource[] { ResourcesPlugin.getWorkspace().getRoot() },
                  IResource.DEPTH_INFINITE, mon);
          break;
        }
        case ON_SELECTED_RESOURCE_ONLY: {
          unfiltered = findCheckstyleMarkers(mFocusResources, IResource.DEPTH_ZERO, mon);
          break;
        }
        case ON_SELECTED_RESOURCE_AND_CHILDREN: {
          unfiltered = findCheckstyleMarkers(mFocusResources, IResource.DEPTH_INFINITE, mon);
          break;
        }
        case ON_ANY_RESOURCE_OF_SAME_PROJECT: {
          unfiltered = findCheckstyleMarkers(getProjects(mFocusResources), IResource.DEPTH_INFINITE,
                  mon);
          break;
        }
        case ON_WORKING_SET: {
          unfiltered = findCheckstyleMarkers(getResourcesInWorkingSet(mWorkingSet),
                  IResource.DEPTH_INFINITE, mon);
          break;
        }
        default: {
          break;
        }
      }
    }

    if (unfiltered == null) {
      unfiltered = Collections.emptyList();
    }

    return unfiltered.toArray(new IMarker[unfiltered.size()]);
  }

  /**
   * @return
   *         <ul>
   *         <li><code>MarkerFilter.ON_ANY_RESOURCE</code> if showing items associated with any
   *         resource.</li>
   *         <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code> if showing items associated
   *         with the selected resource within the workbench.</li>
   *         <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code> if showing items
   *         associated with the selected resource within the workbench and its children.</li>
   *         <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code> if showing items in the
   *         same project as the selected resource within the workbench.</li>
   *         <li><code>MarkerFilter.ON_WORKING_SET</code> if showing items in some working set.</li>
   *         </ul>
   */
  public int getOnResource() {
    return mOnResource;
  }

  /**
   * Sets the type of filtering by selection.
   *
   * @param onResource
   *          must be one of:
   *          <ul>
   *          <li><code>MarkerFilter.ON_ANY_RESOURCE</code></li>
   *          <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code></li>
   *          <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code></li>
   *          <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code></li>
   *          <li><code>MarkerFilter.ON_WORKING_SET</code></li>
   *          </ul>
   */
  public void setOnResource(int onResource) {
    if (onResource >= ON_ANY_RESOURCE && onResource <= ON_WORKING_SET) {
      this.mOnResource = onResource;
    }
  }

  /**
   * Returns the selected resource.
   *
   * @return the selected resource(s) within the workbench.
   */
  public IResource[] getFocusResource() {
    return mFocusResources;
  }

  /**
   * Sets the focused resources.
   *
   * @param resources
   *          the focused resources
   */
  public void setFocusResource(IResource[] resources) {
    mFocusResources = resources;
  }

  /**
   * @return
   *         <ul>
   *         <li><code>true</code> if the filter is enabled.</li>
   *         <li><code>false</code> if the filter is not enabled.</li>
   *         </ul>
   */
  public boolean isEnabled() {
    return mEnabled;
  }

  /**
   * Sets the enablement state of the filter.
   *
   * @param enabled
   *          the enablement
   */
  public void setEnabled(boolean enabled) {
    this.mEnabled = enabled;
  }

  /**
   * Returns the current working set.
   *
   * @return the current working set or <code>null</code> if no working set is defined.
   */
  public IWorkingSet getWorkingSet() {
    return mWorkingSet;
  }

  /**
   * Sets the current working set.
   *
   * @param workingSet
   *          the working set
   */
  public void setWorkingSet(IWorkingSet workingSet) {
    this.mWorkingSet = workingSet;
  }

  /**
   * Returns if the markers will be selected by severity.
   *
   * @return <code>true</code> if markers will be selected by severity
   */
  public boolean getSelectBySeverity() {
    return mSelectBySeverity;
  }

  /**
   * Sets if the markers will be selected by severity.
   *
   * @param selectBySeverity
   *          <code>true</code> if markers will be selected by severity
   */
  public void setSelectBySeverity(boolean selectBySeverity) {
    this.mSelectBySeverity = selectBySeverity;
  }

  /**
   * Returns the severity.
   *
   * @return the severity
   */
  public int getSeverity() {
    return mSeverity;
  }

  /**
   * Sets the severity.
   *
   * @param severity
   *          the severity
   */
  public void setSeverity(int severity) {
    this.mSeverity = severity;
  }

  /**
   * Returns if the regex filter is enabled.
   *
   * @return <code>true</code> if the regex filter is enabled
   */
  public boolean isFilterByRegex() {
    return mFilterByRegex;
  }

  /**
   * Sets if the regex filter is enabled.
   *
   * @param filterByRegex
   *          <code>true</code> if messages are filtered by the regular expressions
   */
  public void setFilterByRegex(boolean filterByRegex) {
    mFilterByRegex = filterByRegex;
  }

  /**
   * Returns the regular expressions.
   *
   * @return the regular expressions
   */
  public List<String> getFilterRegex() {
    return mFilterRegex;
  }

  /**
   * Sets the list of regular expressions.
   *
   * @param filterRegex
   *          the list of regular expression to filter by
   */
  public void setFilterRegex(List<String> filterRegex) {
    mFilterRegex = filterRegex;
  }

  /**
   * Restores the state of the filter from the given dialog settings.
   *
   * @param dialogSettings
   *          the dialog settings
   */
  public void restoreState(IDialogSettings dialogSettings) {
    resetState();
    IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);

    if (settings != null) {

      String setting = settings.get(TAG_ENABLED);
      if (setting != null) {
        mEnabled = Boolean.valueOf(setting).booleanValue();
      }

      setting = settings.get(TAG_ON_RESOURCE);
      if (setting != null) {
        try {
          mOnResource = Integer.parseInt(setting);
        } catch (NumberFormatException e) {
          // ignore and use default value
        }
      }

      setting = settings.get(TAG_WORKING_SET);
      if (setting != null) {
        setWorkingSet(CheckstyleUIPlugin.getDefault().getWorkbench().getWorkingSetManager()
                .getWorkingSet(setting));
      }

      setting = settings.get(TAG_SELECT_BY_SEVERITY);
      if (setting != null) {
        mSelectBySeverity = Boolean.valueOf(setting).booleanValue();
      }

      setting = settings.get(TAG_SEVERITY);
      if (setting != null) {
        try {
          mSeverity = Integer.parseInt(setting);
        } catch (NumberFormatException e) {
          // ignore and use default value
        }
      }

      setting = settings.get(TAG_SELECT_BY_REGEX);
      if (setting != null) {
        mFilterByRegex = Boolean.valueOf(setting).booleanValue();
      }

      String[] regex = settings.getArray(TAG_REGULAR_EXPRESSIONS);
      if (regex != null) {
        mFilterRegex = Arrays.asList(regex);
      }
    }
  }

  /**
   * Saves the state of the filter into the given dialog settings.
   *
   * @param dialogSettings
   *          the dialog settings
   */
  public void saveState(IDialogSettings dialogSettings) {
    if (dialogSettings != null) {
      IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);

      if (settings == null) {
        settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
      }

      settings.put(TAG_ENABLED, mEnabled);
      settings.put(TAG_ON_RESOURCE, mOnResource);

      if (mWorkingSet != null) {
        settings.put(TAG_WORKING_SET, mWorkingSet.getName());
      }

      settings.put(TAG_SELECT_BY_SEVERITY, mSelectBySeverity);
      settings.put(TAG_SEVERITY, mSeverity);

      settings.put(TAG_SELECT_BY_REGEX, mFilterByRegex);

      if (mFilterRegex != null) {
        settings.put(TAG_REGULAR_EXPRESSIONS,
                mFilterRegex.toArray(new String[mFilterRegex.size()]));
      }
    }
  }

  /**
   * Restores the default state of the filter.
   */
  public void resetState() {
    mEnabled = DEFAULT_ACTIVATION_STATUS;
    mOnResource = DEFAULT_ON_RESOURCE;
    setWorkingSet(null);
    mSelectBySeverity = DEFAULT_SELECT_BY_SEVERITY;
    mSeverity = DEFAULT_SEVERITY;
    mFilterByRegex = false;
    mFilterRegex = new ArrayList<>();
  }

  /**
   * Returns a list of all markers in the given set of resources.
   *
   * @param resources
   *          the resources
   * @param depth
   *          the depth with which the markers are searched
   * @param mon
   *          the progress monitor
   * @throws CoreException
   *           if the resource does not exist or the project is not open
   */
  private List<IMarker> findCheckstyleMarkers(IResource[] resources, int depth, IProgressMonitor mon)
          throws CoreException {
    if (resources == null) {
      return Collections.emptyList();
    }

    List<IMarker> resultList = new ArrayList<>(resources.length * 2);

    for (int i = 0, size = resources.length; i < size; i++) {
      if (resources[i].isAccessible()) {
        Collection<IMarker> markers = Arrays
                .asList(resources[i].findMarkers(CheckstyleMarker.MARKER_ID, true, depth));

        resultList.addAll(markers);
      }
    }
    
    if (!mEnabled) {
      return resultList;
    }

    if (mSelectBySeverity) {
      // further filter the markers by severity
      int size = resultList.size();
      for (int i = size - 1; i >= 0; i--) {
        IMarker marker = resultList.get(i);
        if (!selectBySeverity(marker)) {
          resultList.remove(i);
        }
      }
    }

    if (mFilterByRegex) {
      // further filter the markers by regular expressions
      int size = resultList.size();
      for (int i = size - 1; i >= 0; i--) {
        IMarker marker = resultList.get(i);
        if (!selectByRegex(marker)) {
          resultList.remove(i);
        }
      }
    }

    return resultList;
  }

  /**
   * Selects markers by its severity.
   *
   * @param item
   *          the marker
   * @return <code>true</code> if the marker is selected
   */
  private boolean selectBySeverity(IMarker item) {
    if (mSelectBySeverity) {
      int markerSeverity = item.getAttribute(IMarker.SEVERITY, -1);
      if (markerSeverity == IMarker.SEVERITY_ERROR) {
        return (mSeverity & SEVERITY_ERROR) > 0;
      } else if (markerSeverity == IMarker.SEVERITY_WARNING) {
        return (mSeverity & SEVERITY_WARNING) > 0;
      } else if (markerSeverity == IMarker.SEVERITY_INFO) {
        return (mSeverity & SEVERITY_INFO) > 0;
      }
    }

    return true;
  }

  /**
   * Selects marker by matching the message against regular expressions.
   *
   * @param item
   *          the marker
   * @return <code>true</code> if the marker is selected
   */
  private boolean selectByRegex(IMarker item) {

    if (mFilterByRegex) {

      int size = mFilterRegex != null ? mFilterRegex.size() : 0;
      for (int i = 0; i < size; i++) {

        String regex = mFilterRegex.get(i);

        String message = item.getAttribute(IMarker.MESSAGE, null);

        if (message != null && message.matches(regex)) {
          return false;
        }
      }
    }
    return true;

  }

  /**
   * Returns the set of projects that contain the given set of resources.
   *
   * @param resources
   *          the resources
   * @return the array of projects for the given resources
   */
  private static IProject[] getProjects(IResource[] resources) {
    Collection<IProject> projects = getProjectsAsCollection(resources);
    return projects.toArray(new IProject[projects.size()]);
  }

  /**
   * Returns the set of projects that contain the given set of resources.
   *
   * @param resources
   *          the resources
   * @return the collection of projects for the given resources
   */
  public static Collection<IProject> getProjectsAsCollection(IResource[] resources) {
    HashSet<IProject> projects = new HashSet<>();

    for (int idx = 0, size = resources != null ? resources.length : 0; idx < size; idx++) {
      projects.add(resources[idx].getProject());
    }
    return projects;
  }

  /**
   * Returns all resources within the working set.
   *
   * @param workingSet
   *          the working set
   * @return the array of resources from the given working set
   */
  private static IResource[] getResourcesInWorkingSet(IWorkingSet workingSet) {
    if (workingSet == null) {
      return new IResource[0];
    }

    IAdaptable[] elements = workingSet.getElements();
    List<IResource> result = new ArrayList<>(elements.length);

    for (int idx = 0; idx < elements.length; idx++) {
      @SuppressWarnings("cast")
      IResource next = (IResource) elements[idx].getAdapter(IResource.class);

      if (next != null) {
        result.add(next);
      }
    }

    return result.toArray(new IResource[result.size()]);
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(); // this should never happen
    }
  }
}
