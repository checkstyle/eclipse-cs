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
import java.util.HashSet;
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
 *  Project builder for Checkstyle plug-in.
 */
public class CheckstyleBuilder extends IncrementalProjectBuilder
{
    //=================================================
    // Public static final variables.
    //=================================================

    /** Eclipse extension point ID for the builder. */
    public static final String BUILDER_ID =
        "com.atlassw.tools.eclipse.checkstyle.CheckstyleBuilder";

    //=================================================
    // Static class variables.
    //=================================================

    /** Java file suffix. */
    private static final String JAVA_SUFFIX = ".java";

    //=================================================
    // Instance member variables.
    //=================================================

    private boolean mEclipse_2_1_Safe = false;

    private boolean mEclipseVersionDetermined = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    /**
     *  Runs Checkstyle on the project's Java source.  For incremental builds
     *  only the modified source files are checked.
     * 
     *  @param  kind     The kind of build to perform.
     * 
     *  @param  args     Arguments pass to the build.
     * 
     *  @param  monitor  The build progress monitor.
     * 
     *  @return List of projects or <code>null</code>
     * 
     *  @throws CoreException  An error occured during the build.
     */
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {
        switch (kind)
        {
            case AUTO_BUILD :
                doAutoBuild(args, monitor);
                break;

            case FULL_BUILD :
                doFullBuild(args, monitor);
                break;

            case INCREMENTAL_BUILD :
                doIncrementalBuild(args, monitor);
                break;

            default :
                doFullBuild(args, monitor);
                break;
        }

        return null;
    }

    /**
     *  Initializes data used to run the Checkstyle plug-in.
     * 
     *  @param  config    The configuration element used to trigger this 
     *                     execution. It can be queried by the executable
     *                     extension for specific configuration properties.
     * 
     *  @param propertyName   The name of an attribute of the configuration 
     *                        element used on the 
     *                        createExecutableExtension(String) call. This
     *                        argument can be used in the cases where a single
     *                        configuration element is used to define multiple
     *                        executable extensions.
     * 
     *  @param data       Adapter data in the form of a String, a Hashtable, 
     *                    or null.
     * 
     *  @throws CoreException  An error occured during the build.
     */
    public void setInitializationData(
        IConfigurationElement config,
        String propertyName,
        Object data)
        throws CoreException
    {
        super.setInitializationData(config, propertyName, data);
    }

    private void doAutoBuild(Map args, IProgressMonitor monitor) throws CoreException
    {
        doBuild(args, monitor);
    }

    private void doFullBuild(Map args, IProgressMonitor monitor) throws CoreException
    {
        doBuild(args, monitor);
    }

    private void doIncrementalBuild(Map args, IProgressMonitor monitor) throws CoreException
    {
        doBuild(args, monitor);
    }
    private void doBuild(Map args, IProgressMonitor monitor) throws CoreException
    {
        IProject project = getProject();
        if (project == null)
        {
            CheckstyleLog.warning("project is null");
            return;
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
            Status status =
                new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR, msg, e);
            throw new CoreException(status);
        }

        //
        //  If there are no enabled file sets then remove any Checkstyle markers on the
        //  project and return.
        //
        if (fileSets.size() <= 0)
        {
            //
            //  Remove any markers that may be present in the project.
            //
            project.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);
            return;
        }

        //
        //  Get the collection of files that need to be audited.
        //
        Collection files = null;
        IResourceDelta resourceDelta = getDelta(project);
        if (resourceDelta != null)
        {
            //
            //  Get the files that have been modified.
            //
            files = getFiles(resourceDelta);
        }
        else
        {
            //
            //  Get all the files in the project.
            //
            files = getFiles(project);
        }

        //
        //  If there are no files to audit then return.
        //
        if (files.size() <= 0)
        {
            return;
        }

        //
        //  Build a classloader with which to resolve Exception
        //  classes for JavadocMethodCheck.
        //
        ClassLoader classLoader = null;
        IJavaProject javaProject = (IJavaProject)project.getAdapter(IJavaElement.class);
        try
        {
            List urls = getClasspathURLs(javaProject, false);
            URL[] urlArray = (URL[])urls.toArray(new URL[urls.size()]);
            classLoader = new URLClassLoader(urlArray);
        }
        catch (CoreException e)
        {
            //
            // Unexpected classpath entry type - don't want to ignore.
            //
            throw e;
        }
        catch (Throwable e)
        {
            //
            // Eat this exception - not having a classloader will not prevent
            // checkstyle from working.
            //
            CheckstyleLog.error("Unable to create a classloader for the project", e);
            classLoader = null;
        }
        
        //
        //  Audit the files that need to be audited.
        //
        monitor.beginTask("Checkstyle", files.size());
        Auditor auditor = new Auditor(getProject(), fileSets);
        try
        {
            auditor.checkFiles(files, classLoader, monitor);
        }
        catch (CheckstylePluginException e)
        {
            String msg = "Error occured while checking file: " + e.getMessage();
            CheckstyleLog.error(msg, e);
            Status status =
                new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR, msg, e);
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
            files.addAll(getFiles((IResourceDelta)iter.next()));
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
            files.addAll(getFiles((IContainer)iter.next()));
        }

        return files;
    }

    /**
     * @author Daniel Berg jdt-dev@eclipse.org.
     * @param javaProject
     * @return
     */
    private List getClasspathURLs(IJavaProject javaProject, boolean exportedOnly)
        throws JavaModelException, MalformedURLException, CoreException
    {
        LinkedList urls = new LinkedList();

        IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
        boolean defaultOutputAdded = false;

        for (int i = 0; i < entries.length; i++)
        {
            //
            //  Source entries are apparently always assumed to be exported - but don't
            //  report themselves as such.
            //
            if (!exportedOnly
                || entries[i].isExported()
                || entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
            {
                switch (entries[i].getEntryKind())
                {
                    case IClasspathEntry.CPE_SOURCE :
                        {
                            IPath outputLocation = null;

                            if (isEclipse_2_1_Safe())
                            {
                                outputLocation = entries[i].getOutputLocation();
                            }
                            if (outputLocation == null)
                            {
                                //
                                //  If the output location is null then the project's
                                //  default output location is being used.
                                //
                                if (!defaultOutputAdded)
                                {
                                    defaultOutputAdded = true;
                                    outputLocation = javaProject.getOutputLocation();
                                }
                            }

                            if (outputLocation != null)
                            {
                                //
                                //  When the output location is the project itself, the project
                                //  can't resolve the file - therefore just get the project's
                                //  location. 
                                //
                                if (outputLocation.segmentCount() == 1)
                                {
                                    outputLocation = javaProject.getProject().getLocation();
                                }
                                else
                                {
                                    //
                                    //  Output locations are always workspace relative. Do this mess
                                    //  to get a fully qualified location.
                                    //
                                    outputLocation =
                                        javaProject
                                            .getProject()
                                            .getParent()
                                            .getFile(outputLocation)
                                            .getLocation();
                                }

                                urls.add(outputLocation.addTrailingSeparator().toFile().toURL());
                            }

                            break;
                        }
                    case IClasspathEntry.CPE_LIBRARY :
                        {
                            //
                            //  External jars have fully specified paths, but we've no easy way to tell 
                            //  them apart from project relative paths. So assume it's relative, and try
                            //  and get a path.
                            //
                            IContainer parent = javaProject.getProject().getParent();
                            IPath libPath = parent.getFile(entries[i].getPath()).getLocation();
                            if (libPath == null)
                            {
                                //
                                //  It must be fully specified.
                                //
                                libPath = entries[i].getPath();
                            }
                            urls.add(new URL("file:/" + libPath.toOSString()));

                            break;
                        }
                    case IClasspathEntry.CPE_PROJECT :
                        {
                            IJavaProject dependentProject =
                                (IJavaProject)
                                    (
                                        ResourcesPlugin.getWorkspace().getRoot().getProject(
                                            entries[i].getPath().segment(0))).getAdapter(
                                    IJavaElement.class);

                            urls.addAll(getClasspathURLs(dependentProject, true));

                            break;
                        }
                    default :
                        {
                            String msg =
                                "Encountered unexpected classpath entry : "
                                    + entries[i].getEntryKind();
                            CheckstyleLog.error(msg);
                            Status status =
                                new Status(
                                    IStatus.ERROR,
                                    CheckstylePlugin.PLUGIN_ID,
                                    IStatus.ERROR,
                                    msg,
                                    null);
                            throw new CoreException(status);
                        }
                }
            }
        }

        return urls;
    }

    /**
     *  Handy debug method that prints out the classpath entries.
     * 
     *  @param  urls  List of classpath URLs.
     */
    private void printUrls(List urls)
    {
        for (Iterator iter = urls.iterator(); iter.hasNext();)
        {
            URL url = (URL)iter.next();
            System.out.println(url.toString());
        }
    }
    /**
     *  Runs the Checkstyle builder on a project.
     * 
     *  @param project   Project to be built.
     * 
     *  @param shell     Shell to display progress and messages on.
     * 
     *  @throws CheckstylePluginException  Error during the build.
     */
    public static void buildProject(IProject project, Shell shell) throws CheckstylePluginException
    {
        IProject[] projects = new IProject[1];
        projects[0] = project;
        buildProjects(projects, shell);
    }

    /**
     *  Run the Checkstyle builder on all open projects in the workspace.
     * 
     *  @param shell     Shell to display progress and messages on.
     * 
     *  @throws CheckstylePluginException  Error during the build.
     */
    public static void buildAllProjects(Shell shell) throws CheckstylePluginException
    {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        buildProjects(projects, shell);
    }

    /**
     *  Run Checkstyle on the specified projects.
     */
    private static void buildProjects(IProject[] projects, Shell shell)
        throws CheckstylePluginException
    {
        BuildRunnable buildRunnable = new BuildRunnable();
        for (int i = 0; i < projects.length; i++)
        {
            IProject project = projects[i];

            //
            //  Make sure the project is open.
            //
            if (project.isOpen())
            {
                //
                //  See if the project has the Checkstyle nature.
                //
                try
                {
                    if (project.hasNature(CheckstyleNature.NATURE_ID))
                    {
                        //
                        //  Add the project to the collection that needs
                        //  to bed re-checked.
                        //
                        buildRunnable.addProject(project);
                    }
                }
                catch (CoreException e)
                {
                    String msg = "Error while building projects: " + e.getMessage();
                    CheckstyleLog.error(msg, e);
                    throw new CheckstylePluginException(msg);
                }
            }
        }

        //
        //  Run the build runnable to check all of the projects.
        //
        try
        {
            ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
            monitor.run(true, true, buildRunnable);
        }
        catch (InvocationTargetException e)
        {
            String msg = "Error while building projects: " + e.getMessage();
            CheckstyleLog.error(msg, e);
            throw new CheckstylePluginException(msg);
        }
        catch (InterruptedException e)
        {
            String msg = "Error while building projects: " + e.getMessage();
            CheckstyleLog.error(msg, e);
            throw new CheckstylePluginException(msg);
        }
    }

    /**
     *  Rebuilds projects.
     */
    private static class BuildRunnable implements IRunnableWithProgress
    {
        private List mProjects = new LinkedList();

        void addProject(IProject project)
        {
            mProjects.add(project);
        }

        public void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException
        {
            Iterator iter = mProjects.iterator();
            while (iter.hasNext())
            {
                IProject project = (IProject)iter.next();

                try
                {
                    project.build(
                        IncrementalProjectBuilder.FULL_BUILD,
                        CheckstyleBuilder.BUILDER_ID,
                        null,
                        monitor);
                }
                catch (CoreException e)
                {
                    throw new InvocationTargetException(e);
                }
            }
        }
    }

    /**
     *  Determine if running eclipse 2.1 or newer.
     *
     *  The ability for source folders to have different output folders
     *  was introduced in Eclipse 2.1.  Check the version of the
     *  org.eclipse.jdt.core plug-in to determine if it's safe to
     *  call the getOutputLocation() method, which was introduced
     *  in Eclipse v2.1.
     */
    private boolean isEclipse_2_1_Safe()
    {
        if (!mEclipseVersionDetermined)
        {
            Plugin jdtCorePlugin = Platform.getPlugin("org.eclipse.jdt.core");
            if (jdtCorePlugin != null)
            {
                PluginVersionIdentifier version =
                    jdtCorePlugin.getDescriptor().getVersionIdentifier();
                mEclipse_2_1_Safe = version.isGreaterOrEqualTo(new PluginVersionIdentifier("2.1"));
            }
            mEclipseVersionDetermined = true;
            //CheckstyleLog.info("Eclipse 2.1 Safe = " + mEclipse_2_1_Safe);
        }

        return mEclipse_2_1_Safe;
    }
}
