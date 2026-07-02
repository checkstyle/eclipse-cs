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

package net.sf.eclipsecs.ui.stats.views.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;

/**
 * Filter class for Checkstyle markers. This filter is used by the Checkstyle statistics views.
 *
 * @param enabled
 *            Determines if this filter is enabled.
 * @param onResource
 *            The selection mode (e.g., ON_ANY_RESOURCE, ON_SELECTED_RESOURCE, etc.).
 * @param workingSet
 *            The selected working set to filter by, if the mode is ON_WORKING_SET.
 * @param selectBySeverity
 *            Flags if the severity-based filtering is active.
 * @param severity
 *            The bitmask of selected severities (Error, Warning, Info).
 * @param filterByRegex
 *            Flags if the regular expression-based filtering is enabled.
 * @param filterRegex
 *            The list of regular expressions used to exclude markers by their message.
 * @param focusResources
 *            The transient resources currently focused in the workbench.
 */
public record CheckstyleMarkerFilter(boolean enabled, int onResource, IWorkingSet workingSet,
    boolean selectBySeverity, int severity, boolean filterByRegex, List<String> filterRegex,
    IResource[] focusResources) {

    //
    // constants
    //

    /** Apply filter to all resources. */
    public static final int ON_ANY_RESOURCE = 0;

    /** Apply filter to the selected resource only. */
    public static final int ON_SELECTED_RESOURCE_ONLY = 1;

    /** Apply filter to the selected resource and its children. */
    public static final int ON_SELECTED_RESOURCE_AND_CHILDREN = 2;

    /** Apply filter to any resource in the same project. */
    public static final int ON_ANY_RESOURCE_OF_SAME_PROJECT = 3;

    /** Apply filter to a working set. */
    public static final int ON_WORKING_SET = 4;

    /** Severity bit flag for error markers. */
    public static final int SEVERITY_ERROR = 1 << 2;

    /** Severity bit flag for warning markers. */
    public static final int SEVERITY_WARNING = 1 << 1;

    /** Severity bit flag for info markers. */
    public static final int SEVERITY_INFO = 1 << 0;

    /** Dialog settings section name. */
    private static final String TAG_DIALOG_SECTION = "filter";

    /** Dialog settings key for the enabled flag. */
    private static final String TAG_ENABLED = "enabled";

    /** Dialog settings key for the resource scope. */
    private static final String TAG_ON_RESOURCE = "onResource";

    /** Dialog settings key for the working set. */
    private static final String TAG_WORKING_SET = "workingSet";

    /** Dialog settings key for the severity selection flag. */
    private static final String TAG_SELECT_BY_SEVERITY = "selectBySeverity";

    /** Dialog settings key for the severity value. */
    private static final String TAG_SEVERITY = "severity";

    /** Dialog settings key for the regex selection flag. */
    private static final String TAG_SELECT_BY_REGEX = "selectByRegex";

    /** Dialog settings key for the regular expressions list. */
    private static final String TAG_REGULAR_EXPRESSIONS = "regularExpressions";

    /** Default severity value. */
    private static final int DEFAULT_SEVERITY = 0;

    /** Default resource scope. */
    private static final int DEFAULT_ON_RESOURCE = ON_ANY_RESOURCE;

    /** Default value for the select-by-severity flag. */
    private static final boolean DEFAULT_SELECT_BY_SEVERITY = false;

    /** Default activation status. */
    private static final boolean DEFAULT_ACTIVATION_STATUS = true;

    //
    // methods
    //

    public CheckstyleMarkerFilter withFocusResources(IResource[] resources) {
        return new CheckstyleMarkerFilter(enabled, onResource, workingSet, selectBySeverity,
            severity, filterByRegex, filterRegex, resources);
    }

    /**
     * Searches the workspace for markers that pass this filter.
     *
     * @param mon
     *            the progress monitor
     * @return the array of Checkstyle markers that pass this filter.
     * @throws CoreException
     *             an unexpected error occurred
     */
    public IMarker[] findMarkers(IProgressMonitor mon) throws CoreException {

        final List<IMarker> unfiltered;

        if (enabled) {
            unfiltered = switch (onResource) {
                case ON_ANY_RESOURCE -> findCheckstyleMarkers(new IResource[] {
                    ResourcesPlugin.getWorkspace().getRoot(),
                    }, IResource.DEPTH_INFINITE, mon);
                case ON_SELECTED_RESOURCE_ONLY -> findCheckstyleMarkers(focusResources,
                    IResource.DEPTH_ZERO, mon);
                case ON_SELECTED_RESOURCE_AND_CHILDREN -> findCheckstyleMarkers(focusResources,
                    IResource.DEPTH_INFINITE, mon);
                case ON_ANY_RESOURCE_OF_SAME_PROJECT -> findCheckstyleMarkers(
                    getProjects(focusResources).toArray(new IResource[0]), IResource.DEPTH_INFINITE,
                    mon);
                case ON_WORKING_SET -> findCheckstyleMarkers(getResourcesInWorkingSet(workingSet),
                    IResource.DEPTH_INFINITE, mon);
                default -> Collections.emptyList();
            };
        }
        else {
            unfiltered = findCheckstyleMarkers(new IResource[] {
                ResourcesPlugin.getWorkspace().getRoot(),
            }, IResource.DEPTH_INFINITE, mon);
        }

        return unfiltered.toArray(new IMarker[unfiltered.size()]);
    }

    /**
     * Restores the state of the filter from the given dialog settings.
     *
     * @param dialogSettings
     *            the dialog settings
     * @param focusResource
     *            the resource to focus on
     */
    public static CheckstyleMarkerFilter restoreState(IDialogSettings dialogSettings,
        IResource[] focusResource) {
        final IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);

        final boolean enabled = findSetting(settings, TAG_ENABLED).map(Boolean::parseBoolean)
            .orElse(DEFAULT_ACTIVATION_STATUS);
        final boolean selectBySeverity = findSetting(settings, TAG_SELECT_BY_SEVERITY)
            .map(Boolean::parseBoolean).orElse(DEFAULT_SELECT_BY_SEVERITY);
        final boolean filterByRegex =
            findSetting(settings, TAG_SELECT_BY_REGEX).map(Boolean::parseBoolean).orElse(false);

        final int mOnResource = findSetting(settings, TAG_ON_RESOURCE)
            .flatMap(CheckstyleMarkerFilter::tryParseInt).orElse(DEFAULT_ON_RESOURCE);

        final IWorkingSet mWorkingSet = findSetting(settings, TAG_WORKING_SET)
            .map(PlatformUI.getWorkbench().getWorkingSetManager()::getWorkingSet).orElse(null);

        final int mSeverity = findSetting(settings, TAG_SEVERITY)
            .flatMap(CheckstyleMarkerFilter::tryParseInt).orElse(DEFAULT_SEVERITY);

        List<String> mFilterRegex = new ArrayList<>();
        if (settings != null) {
            final String[] regex = settings.getArray(TAG_REGULAR_EXPRESSIONS);
            if (regex != null) {
                mFilterRegex = Arrays.asList(regex);
            }
        }

        return new CheckstyleMarkerFilter(enabled, mOnResource, mWorkingSet, selectBySeverity,
            mSeverity, filterByRegex, mFilterRegex, focusResource);
    }

    private static Optional<Integer> tryParseInt(String setting) {
        Optional<Integer> parsed;
        try {
            parsed = Optional.of(Integer.parseInt(setting));
        }
        catch (NumberFormatException ex) {
            parsed = Optional.empty();
        }
        return parsed;
    }

    private static Optional<String> findSetting(IDialogSettings dialogSettings, String key) {
        return Optional.ofNullable(dialogSettings)
            .flatMap(settings -> Optional.ofNullable(settings.get(key)));
    }

    /**
     * Saves the state of the filter into the given dialog settings.
     *
     * @param dialogSettings
     *            the dialog settings
     */
    public void saveState(IDialogSettings dialogSettings) {
        if (dialogSettings != null) {
            IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);

            if (settings == null) {
                settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
            }

            settings.put(TAG_ENABLED, enabled);
            settings.put(TAG_ON_RESOURCE, onResource);

            if (workingSet != null) {
                settings.put(TAG_WORKING_SET, workingSet.getName());
            }

            settings.put(TAG_SELECT_BY_SEVERITY, selectBySeverity);
            settings.put(TAG_SEVERITY, severity);

            settings.put(TAG_SELECT_BY_REGEX, filterByRegex);

            if (filterRegex != null) {
                settings.put(TAG_REGULAR_EXPRESSIONS,
                    filterRegex.toArray(new String[filterRegex.size()]));
            }
        }
    }

    /**
     * Restores the default state of the filter.
     *
     * @param focusResources
     *            the resources to focus on
     */
    public static CheckstyleMarkerFilter resetState(IResource[] focusResources) {
        return new CheckstyleMarkerFilter(DEFAULT_ACTIVATION_STATUS, DEFAULT_ON_RESOURCE, null,
            DEFAULT_SELECT_BY_SEVERITY, DEFAULT_SEVERITY, false, new ArrayList<>(), focusResources);
    }

    /**
     * Returns a list of all markers in the given set of resources.
     *
     * @param resources
     *            the resources
     * @param depth
     *            the depth with which the markers are searched
     * @param mon
     *            the progress monitor
     * @throws CoreException
     *             if the resource does not exist or the project is not open
     */
    private List<IMarker> findCheckstyleMarkers(IResource[] resources, int depth,
        IProgressMonitor mon) throws CoreException {
        final List<IMarker> markers = new ArrayList<>();
        for (IResource resource : resources) {
            if (resource.isAccessible()) {
                markers.addAll(
                    Arrays.asList(resource.findMarkers(CheckstyleMarker.MARKER_ID, true, depth)));
            }
        }
        Stream<IMarker> filteredMarkers = markers.stream();
        if (enabled) {
            if (selectBySeverity) {
                filteredMarkers = filteredMarkers.filter(this::doSelectBySeverity);
            }
            if (filterByRegex) {
                filteredMarkers = filteredMarkers.filter(this::selectByRegex);
            }
        }
        return filteredMarkers.toList();
    }

    /**
     * Selects markers by its severity.
     *
     * @param item
     *            the marker
     * @return <code>true</code> if the marker is selected
     */
    private boolean doSelectBySeverity(IMarker item) {
        boolean select = true;
        if (selectBySeverity) {
            final int markerSeverity = item.getAttribute(IMarker.SEVERITY, -1);
            if (markerSeverity == IMarker.SEVERITY_ERROR) {
                final int flag = severity & SEVERITY_ERROR;
                select = flag > 0;
            }
            else if (markerSeverity == IMarker.SEVERITY_WARNING) {
                final int flag = severity & SEVERITY_WARNING;
                select = flag > 0;
            }
            else if (markerSeverity == IMarker.SEVERITY_INFO) {
                final int flag = severity & SEVERITY_INFO;
                select = flag > 0;
            }
        }

        return select;
    }

    /**
     * Selects marker by matching the message against regular expressions.
     *
     * @param item
     *            the marker
     * @return <code>true</code> if the marker is selected
     */
    private boolean selectByRegex(IMarker item) {
        boolean select = true;
        if (filterByRegex) {
            final int size = filterRegex != null ? filterRegex.size() : 0;
            for (int i = 0; i < size; i++) {

                final String regex = filterRegex.get(i);

                final String message = item.getAttribute(IMarker.MESSAGE, null);

                if (message != null && message.matches(regex)) {
                    select = false;
                    break;
                }
            }
        }
        return select;
    }

    /**
     * Returns the set of projects that contain the given set of resources.
     *
     * @param resources
     *            the resources
     * @return the collection of projects for the given resources
     */
    public static Set<IProject> getProjects(IResource[] resources) {
        return Arrays.stream(resources).map(IResource::getProject).collect(Collectors.toSet());
    }

    /**
     * Returns all resources within the working set.
     *
     * @param workingSet
     *            the working set
     * @return the array of resources from the given working set
     */
    private static IResource[] getResourcesInWorkingSet(IWorkingSet workingSet) {
        final IResource[] resources;
        if (workingSet == null) {
            resources = new IResource[0];
        }
        else {
            final IAdaptable[] elements = workingSet.getElements();
            final List<IResource> result = new ArrayList<>(elements.length);

            for (int idx = 0; idx < elements.length; idx++) {
                final IResource next = elements[idx].getAdapter(IResource.class);

                if (next != null) {
                    result.add(next);
                }
            }

            resources = result.toArray(new IResource[result.size()]);
        }
        return resources;
    }

}
