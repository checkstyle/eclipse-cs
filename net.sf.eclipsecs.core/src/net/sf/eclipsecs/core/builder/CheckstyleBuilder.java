//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.jobs.AuditorJob;
import net.sf.eclipsecs.core.jobs.BuildProjectJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

/**
 * Project builder for Checkstyle plug-in.
 *
 * @author David Schneider
 * @author Lars Ködderitzsch
 */
public class CheckstyleBuilder extends IncrementalProjectBuilder {

  /** Eclipse extension point ID for the builder. */
  public static final String BUILDER_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleBuilder"; //$NON-NLS-1$

  /**
   * Runs the Checkstyle builder on a project.
   *
   * @param project
   *          Project to be built.
   */
  public static void buildProject(final IProject project) {
    // uses the new Jobs API to run the build in the background
    BuildProjectJob buildJob = new BuildProjectJob(project, IncrementalProjectBuilder.FULL_BUILD);
    buildJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
    buildJob.schedule();
  }

  /**
   * Run the Checkstyle builder on all open projects in the workspace.
   *
   * @throws CheckstylePluginException
   *           Error during the build.
   */
  public static void buildAllProjects() throws CheckstylePluginException {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IProject[] projects = workspace.getRoot().getProjects();

    buildProjects(Arrays.asList(projects));
  }

  /**
   * Builds all checkstyle enabled projects that are open from the given collection of projects.
   *
   * @param projects
   *          the projects to build
   * @throws CheckstylePluginException
   *           Error during the build
   */
  public static void buildProjects(final Collection<IProject> projects)
          throws CheckstylePluginException {

    // Build only open projects with Checkstyle enabled
    List<IProject> checkstyleProjects = new ArrayList<>();

    for (IProject project : projects) {

      try {
        if (project.exists() && project.isOpen() && project.hasNature(CheckstyleNature.NATURE_ID)) {
          checkstyleProjects.add(project);
        }
      } catch (CoreException e) {
        CheckstylePluginException.rethrow(e);
      }
    }

    // uses the new Jobs API to run the build in the background
    BuildProjectJob buildJob = new BuildProjectJob(
            checkstyleProjects.toArray(new IProject[checkstyleProjects.size()]),
            IncrementalProjectBuilder.FULL_BUILD);
    buildJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
    buildJob.schedule();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final IProject[] build(final int kind, @SuppressWarnings("rawtypes") final Map args,
          final IProgressMonitor monitor) throws CoreException {

    // get the associated project for this builder
    IProject project = getProject();

    // remove project level error markers
    project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_ZERO);

    if (CheckstyleNature.hasCorrectBuilderOrder(project)) {

      //
      // get the project configuration
      //
      IProjectConfiguration config = null;
      try {
        config = ProjectConfigurationFactory.getConfiguration(project);
      } catch (CheckstylePluginException e) {
        Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
                e.getMessage() != null ? e.getMessage()
                        : Messages.CheckstyleBuilder_msgErrorUnknown,
                e);
        throw new CoreException(status);
      }

      Collection<IResource> resources = null;

      // get the delta of the latest changes
      IResourceDelta resourceDelta = getDelta(project);

      IFilter[] filters = config.getFilters().toArray(new IFilter[config.getFilters().size()]);

      // find the files for the build
      if (resourceDelta != null) {
        resources = getResources(resourceDelta, filters);
      } else {
        resources = getResources(project, filters);
      }

      handleBuildSelection(resources, config, monitor, project, kind);

    } else {

      // the builder order is wrong. Refuse to check and create a error
      // marker.

      // remove all existing Checkstyle markers
      project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_INFINITE);

      Map<String, Object> markerAttributes = new HashMap<>();
      markerAttributes.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
      markerAttributes.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
      markerAttributes.put(IMarker.MESSAGE,
              NLS.bind(Messages.CheckstyleBuilder_msgWrongBuilderOrder, project.getName()));

      // enables own category under Java Problem Type
      // setting for Problems view (RFE 1530366)
      markerAttributes.put("categoryId", Integer.valueOf(999)); //$NON-NLS-1$

      // create a marker for the actual resource
      IMarker marker = project.createMarker(CheckstyleMarker.MARKER_ID);
      marker.setAttributes(markerAttributes);
    }

    return new IProject[] { project };
  }

  @Override
  protected void clean(IProgressMonitor monitor) throws CoreException {
    getProject().deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_INFINITE);
  }

  /**
   * Builds the selected resources.
   *
   * @param resources
   *          the resources to build
   * @param configuration
   *          the project configuration
   * @param monitor
   *          the progress monitor
   * @param project
   *          the built project
   * @param kind
   *          the kind of build
   * @param <T>
   *          the resource type parameter
   * @throws CoreException
   *           if the build fails
   */
  public final <T extends IResource> void handleBuildSelection(final Collection<T> resources,
          final IProjectConfiguration configuration, final IProgressMonitor monitor,
          final IProject project, final int kind) throws CoreException {

    // System.out.println(new java.util.Date() + " kind: " + kind + " files:
    // " + resources.size());

    // on full build remove all previous checkstyle markers
    if (kind == IncrementalProjectBuilder.FULL_BUILD) {
      project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_INFINITE);
    }

    boolean backgroundFullBuild = CheckstylePluginPrefs
            .getBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD);

    try {

      //
      // Build a set of auditors from the file sets of this project
      // configuration.
      // File sets that share the same check configuration merge into
      // one Auditor.
      //
      List<FileSet> fileSets = configuration.getFileSets();

      Map<ICheckConfiguration, Auditor> audits = new HashMap<>();

      for (FileSet fileSet : fileSets) {

        // skip not enabled filesets
        if (!fileSet.isEnabled()) {
          continue;
        }

        ICheckConfiguration checkConfig = fileSet.getCheckConfig();
        if (checkConfig == null) {
          throw new CheckstylePluginException(
                  NLS.bind(Messages.errorNoCheckConfig, project.getName()));
        }

        // get an already created audit from the map
        Auditor audit = audits.get(checkConfig);

        // create the audit with the file sets check configuration
        if (audit == null) {

          audit = new Auditor(checkConfig);
          audits.put(checkConfig, audit);
        }

        // check which files belong to the file set
        for (IResource resource : resources) {

          if (resource instanceof IFile) {
            IFile file = (IFile) resource;

            // if file set includes file add to the audit
            if (fileSet.includesFile(file)) {
              audit.addFile(file);

              // remove markers on this file
              file.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_ZERO);

              // remove markers from package to prevent
              // packagehtml messages from accumulatin
              file.getParent().deleteMarkers(CheckstyleMarker.MARKER_ID, false,
                      IResource.DEPTH_ZERO);
            }
          }
        }
      }

      // run all auditors
      for (Auditor audit : audits.values()) {
        if (monitor.isCanceled()) {
          throw new OperationCanceledException();
        }

        if (backgroundFullBuild && kind == FULL_BUILD) {

          AuditorJob j = new AuditorJob(project, audit);
          j.schedule();
        } else {
          audit.runAudit(project, monitor);
        }
      }
    } catch (CheckstylePluginException e) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              e.getLocalizedMessage(), e);
      throw new CoreException(status);
    }
  }

  /**
   * Get the files for the build by analyzing the resource delta.
   *
   * @param delta
   *          the delta of changes
   * @param filters
   *          filters to exclude elements from the check
   * @return collection of files to build
   * @throws CoreException
   *           an unexpected error occurred
   */
  private Collection<IResource> getResources(final IResourceDelta delta, final IFilter[] filters)
          throws CoreException {

    List<IResource> resources = new ArrayList<>();

    IResourceDelta[] affectedChildren = delta.getAffectedChildren();

    for (int i = 0; i < affectedChildren.length; i++) {

      IResourceDelta childDelta = affectedChildren[i];

      // check if a resource has changed
      int deltaKind = childDelta.getKind();
      if ((deltaKind == IResourceDelta.ADDED) || (deltaKind == IResourceDelta.CHANGED)) {

        IResource child = childDelta.getResource();

        // filter resources
        boolean goesThrough = true;
        for (int j = 0; j < filters.length; j++) {

          if (filters[j].isEnabled() && !filters[j].accept(child)) {
            goesThrough = false;
            break;
          }
        }

        // the child has made it through the filters
        if (goesThrough) {

          // add to the resources to check
          resources.add(child);
        }

        // recurse over containers
        if (child instanceof IContainer) {
          resources.addAll(getResources(childDelta, filters));
        }
      }
    }
    return resources;
  }

  /**
   * Get all files to build from a given container.
   *
   * @param container
   *          the container
   * @param filters
   *          filters to exclude elements from the check
   * @return collection of files to build
   * @throws CoreException
   *           an unexpected error occurred
   */
  private Collection<IResource> getResources(final IContainer container, final IFilter[] filters)
          throws CoreException {

    List<IResource> resources = new ArrayList<>();

    IResource[] children = container.members();

    // loop over children resources
    for (int i = 0; i < children.length; i++) {

      IResource child = children[i];

      // filter resources
      boolean goesThrough = true;
      for (int j = 0; j < filters.length; j++) {

        if (filters[j].isEnabled() && !filters[j].accept(child)) {
          goesThrough = false;
          break;
        }
      }

      // the child has made it through the filters
      if (goesThrough) {

        // add to the resources to check
        resources.add(child);
      }

      // recurse over containers
      if (child instanceof IContainer) {
        resources.addAll(getResources((IContainer) child, filters));
      }
    }
    return resources;
  }

  @Override
  public ISchedulingRule getRule(int kind, Map<String, String> args) {
    return getProject();
  }
}
