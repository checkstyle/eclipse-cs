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

//=================================================
// Imports from java namespace
//=================================================
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.filters.FilterSet;
import com.atlassw.tools.eclipse.checkstyle.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.nature.CheckstyleNature;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * Project builder for Checkstyle plug-in.
 */
public class CheckstyleBuilder extends IncrementalProjectBuilder
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Eclipse extension point ID for the builder. */
    public static final String        BUILDER_ID   = "com.atlassw.tools.eclipse.checkstyle.CheckstyleBuilder";

    //=================================================
    // Static class variables.
    //=================================================

    /** Java file suffix. */
    private static final String       JAVA_SUFFIX  = ".java";

    /** The ClassLoader to use for the checkstyle process. */
    private static ProjectClassLoader sClassLoader = new ProjectClassLoader();

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

            Collection files = null;

            //get the delta of the latest changes
            IResourceDelta resourceDelta = getDelta(project);

            //find the files for the build
            if (resourceDelta != null)
            {
                files = getFiles(resourceDelta);
            }
            else
            {
                files = getFiles(project);
            }

            handleBuildSelection(files, monitor, project, kind);
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
    protected void handleBuildSelection(Collection resources, IProgressMonitor monitor,
            IProject project, int kind) throws CoreException
    {

        //on full build remove all previous checkstyle markers
        if (kind == IncrementalProjectBuilder.FULL_BUILD)
        {
            project.deleteMarkers(CheckstyleMarker.MARKER_ID, false, IResource.DEPTH_INFINITE);
        }

        //
        //  Get the list of enabled file sets for the project.
        //
        List fileSets = null;
        try
        {
            fileSets = FileSetFactory.getEnabledFileSets(project);
        }
        catch (CheckstylePluginException e)
        {
            String msg = e.getMessage();
            CheckstyleLog.error(msg, e);
            Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
                    msg, e);
            throw new CoreException(status);
        }

        int size = resources.size();

        //begin checkstyle task
        monitor.beginTask(CheckstylePlugin.getResourceString("taskCheckstyleRun"), size);

        //
        //  Build a classloader with which to resolve Exception
        //  classes for JavadocMethodCheck.
        //
        sClassLoader.intializeWithProject(project);

        //
        //  Audit the files that need to be audited.
        //
        Auditor auditor = new Auditor(getProject(), fileSets);

        try
        {
            auditor.checkFiles(resources, sClassLoader, monitor);
        }
        catch (CheckstylePluginException e)
        {
            String msg = "Error occured while checking file: " + e.getMessage();
            CheckstyleLog.error(msg, e);
            Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
                    msg, e);
            throw new CoreException(status);
        }
    }

 
    private Collection getFiles(IResourceDelta delta) throws CoreException
    {
        ArrayList files = new ArrayList(0);
        ArrayList folders = new ArrayList(0);

        IResourceDelta[] affectedChildren = delta.getAffectedChildren();
        for (int i = 0; i < affectedChildren.length; i++)
        {
            IResourceDelta childDelta = affectedChildren[i];
            IResource child = childDelta.getResource();
            int childType = child.getType();
            if (childType == IResource.FILE)
            {
                int deltaKind = childDelta.getKind();
                if ((deltaKind == IResourceDelta.ADDED) || (deltaKind == IResourceDelta.CHANGED))
                {
                    if (child.getName().endsWith(JAVA_SUFFIX))
                    {
                        files.add(child);
                    }
                }
            }
            else if (childType == IResource.FOLDER)
            {
                folders.add(childDelta);
            }
        }

        //
        //  Get the files from the sub-folders.
        //
        Iterator iter = folders.iterator();
        while (iter.hasNext())
        {
            files.addAll(getFiles((IResourceDelta) iter.next()));
        }

        return files;
    }

    private Collection getFiles(IContainer container) throws CoreException
    {
        ArrayList files = new ArrayList(0);
        ArrayList folders = new ArrayList(0);

        IResource[] children = container.members();
        for (int i = 0; i < children.length; i++)
        {
            IResource child = children[i];
            int childType = child.getType();
            if (childType == IResource.FILE)
            {
                if (child.getName().endsWith(JAVA_SUFFIX))
                {
                    files.add(child);
                }
            }
            else if (childType == IResource.FOLDER)
            {
                folders.add(child);
            }
        }

        //
        //  Get the files from the sub-folders.
        //
        Iterator iter = folders.iterator();
        while (iter.hasNext())
        {
            files.addAll(getFiles((IContainer) iter.next()));
        }

        return files;
    }

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
}