//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Project builder for Checkstyle plug-in.
 * 
 * @author David Schneider
 * @author Lars Ködderitzsch
 */
public class CheckstyleBuilder extends IncrementalProjectBuilder
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Eclipse extension point ID for the builder. */
    public static final String BUILDER_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleBuilder";

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     * Runs the Checkstyle builder on a project.
     * 
     * @param project Project to be built.
     * 
     * @param shell Shell to display progress and messages on.
     * 
     * @throws CheckstylePluginException Error during the build.
     */
    public static void buildProject(IProject project, Shell shell) throws CheckstylePluginException
    {
        //uses the new Jobs API to run the build in the background
        BuildProjectJob buildJob = new BuildProjectJob(project,
                IncrementalProjectBuilder.FULL_BUILD);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
        buildJob.schedule();
    }

    /**
     * Run the Checkstyle builder on all open projects in the workspace.
     * 
     * @param shell Shell to display progress and messages on.
     * 
     * @throws CheckstylePluginException Error during the build.
     */
    public static void buildAllProjects(Shell shell) throws CheckstylePluginException
    {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();

        //uses the new Jobs API to run the build in the background
        BuildProjectJob buildJob = new BuildProjectJob(projects,
                IncrementalProjectBuilder.FULL_BUILD);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
        buildJob.schedule();
    }

    /**
     * @see org.eclipse.core.internal.events.InternalBuilder #build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected final IProject[] build(int kind, Map args, IProgressMonitor monitor)
        throws CoreException
    {

        //get the associated project for this builder
        IProject project = getProject();

        if (project != null)
        {

            //
            //  get the project configuration
            //
            ProjectConfiguration config = null;
            try
            {
                config = ProjectConfigurationFactory.getConfiguration(project);
            }
            catch (CheckstylePluginException e)
            {
                Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID,
                        IStatus.ERROR, e.getMessage(), e);
                throw new CoreException(status);
            }

            Collection files = null;

            //get the delta of the latest changes
            IResourceDelta resourceDelta = getDelta(project);

            //find the files for the build
            if (resourceDelta != null)
            {
                files = getFiles(resourceDelta, config.getEnabledFilters());
            }
            else
            {
                files = getFiles(project, config.getEnabledFilters());
            }

            System.out.println("Passing through filters: " + files.size());

            handleBuildSelection(files, config, monitor, project, kind);
        }

        return new IProject[] { project };
    }

    /**
     * Builds the selected resources.
     * 
     * @param resources the resourcesto build
     * @param monitor the progress monitor
     * @param project the built project
     * @param kind the kind of build
     * @throws CoreException if the build fails
     */
    protected void handleBuildSelection(Collection resources, ProjectConfiguration configuration,
            IProgressMonitor monitor, IProject project, int kind) throws CoreException
    {

        //on full build remove all previous checkstyle markers
        if (kind == IncrementalProjectBuilder.FULL_BUILD)
        {
            project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_INFINITE);
        }

        try
        {

            //
            // Build a set of auditors from the file sets of this project
            // configuration.
            // File sets that share the same check configuration merge into
            // one Auditor.
            //
            List fileSets = configuration.getEnabledFileSets();

            Map audits = new HashMap();

            int size = fileSets != null ? fileSets.size() : 0;
            for (int i = 0; i < size; i++)
            {

                FileSet fileSet = (FileSet) fileSets.get(i);

                //get an already created audit from the map
                Auditor audit = (Auditor) audits.get(fileSet.getCheckConfigName());

                //create the audit with the file sets check configuration
                if (audit == null)
                {
                    ICheckConfiguration checkConfig = fileSet.getCheckConfig();
                    if (checkConfig == null)
                    {
                        throw new CheckstylePluginException("Fileset from project '"
                                + project.getName() + "' has no valid check configuration.");
                    }

                    audit = new Auditor(checkConfig);
                    audits.put(fileSet.getCheckConfigName(), audit);
                }

                //check which files belong to the file set
                Iterator it = resources.iterator();
                while (it.hasNext())
                {

                    IFile file = (IFile) it.next();

                    //if file set includes file add to the audit
                    if (fileSet.includesFile(file))
                    {
                        audit.addFile(file);
                    }
                }
            }

            //run all auditors
            Iterator it = audits.values().iterator();
            while (it.hasNext())
            {
                if (monitor.isCanceled())
                {
                    break;
                }
                ((Auditor) it.next()).runAudit(project, monitor);
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error(e.getLocalizedMessage(), e);
            Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR, e
                    .getLocalizedMessage(), e);
            throw new CoreException(status);
        }
    }

    /**
     * Get the files for the build by analyzing the resource delta.
     * 
     * @param delta the delta of changes
     * @param filters filters to exclude elements from the check
     * @return collection of files to build
     * @throws CoreException an unexpected error occurred
     */
    private Collection getFiles(IResourceDelta delta, IFilter[] filters) throws CoreException
    {

        List resources = new ArrayList(0);

        IResourceDelta[] affectedChildren = delta.getAffectedChildren();

        for (int i = 0; i < affectedChildren.length; i++)
        {

            IResourceDelta childDelta = affectedChildren[i];

            //check if a resource has changed
            int deltaKind = childDelta.getKind();
            if ((deltaKind == IResourceDelta.ADDED) || (deltaKind == IResourceDelta.CHANGED))
            {

                IResource child = childDelta.getResource();

                //filter resources
                boolean goesThrough = true;
                for (int j = 0; j < filters.length; j++)
                {

                    if (!filters[j].accept(child))
                    {
                        goesThrough = false;
                        break;
                    }
                }

                //the child has made it through the filters
                if (goesThrough)
                {

                    //add to the resources to check
                    resources.add(child);
                }

                //recurse over containers
                if (child instanceof IContainer)
                {
                    resources.addAll(getFiles(childDelta, filters));
                }
            }
        }
        return resources;
    }

    /**
     * Get all files to build from a given container.
     * 
     * @param container the container
     * @param filters filters to exclude elements from the check
     * @return collection of files to build
     * @throws CoreException an unexpected error occurred
     */
    private Collection getFiles(IContainer container, IFilter[] filters) throws CoreException
    {

        List resources = new ArrayList();

        IResource[] children = container.members();

        //loop over children resources
        for (int i = 0; i < children.length; i++)
        {

            IResource child = children[i];

            //filter resources
            boolean goesThrough = true;
            for (int j = 0; j < filters.length; j++)
            {

                if (!filters[j].accept(child))
                {
                    goesThrough = false;
                    break;
                }
            }

            //the child has made it through the filters
            if (goesThrough)
            {

                //add to the resources to check
                resources.add(child);
            }

            //recurse over containers
            if (child instanceof IContainer)
            {
                resources.addAll(getFiles((IContainer) child, filters));
            }
        }
        return resources;
    }

}