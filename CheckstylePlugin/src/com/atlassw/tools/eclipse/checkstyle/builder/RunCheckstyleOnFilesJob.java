//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.IProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Job that invokes Checkstyle on a list of workspace files.
 * 
 * @author Lars Ködderitzsch
 */
public class RunCheckstyleOnFilesJob extends WorkspaceJob
{

    private List mFilesToCheck;

    /**
     * Creates the job for a list of <code>IFile</code> objects.
     * 
     * @param files the files to check
     */
    public RunCheckstyleOnFilesJob(List files)
    {
        super(Messages.RunCheckstyleOnFilesJob_title);
        mFilesToCheck = files;
    }

    /**
     * Creates the job for a single file.
     * 
     * @param file the file to check
     */
    public RunCheckstyleOnFilesJob(IFile file)
    {
        super(Messages.RunCheckstyleOnFilesJob_title);
        mFilesToCheck = new ArrayList();
        mFilesToCheck.add(file);
    }

    /**
     * {@inheritDoc}
     */
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
    {

        try
        {

            Map projectFilesMap = getFilesSortedToProject(mFilesToCheck);

            Iterator it = projectFilesMap.keySet().iterator();
            while (it.hasNext())
            {

                IProject project = (IProject) it.next();
                List files = (List) projectFilesMap.get(project);

                IProjectConfiguration checkConfig = ProjectConfigurationFactory
                        .getConfiguration(project);

                filter(files, checkConfig);

                CheckstyleBuilder builder = new CheckstyleBuilder();
                builder.handleBuildSelection(files, checkConfig, monitor, project,
                        IncrementalProjectBuilder.INCREMENTAL_BUILD);
            }
        }
        catch (CheckstylePluginException e)
        {
            Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR, e
                    .getLocalizedMessage(), e);
            throw new CoreException(status);
        }
        return Status.OK_STATUS;
    }

    private Map getFilesSortedToProject(List filesToCheck)
    {

        Map projectFilesMap = new HashMap();

        for (int i = 0, size = filesToCheck.size(); i < size; i++)
        {

            IFile file = (IFile) filesToCheck.get(i);
            IProject project = file.getProject();

            List projectFiles = (List) projectFilesMap.get(project);
            if (projectFiles == null)
            {

                projectFiles = new ArrayList();
                projectFilesMap.put(project, projectFiles);
            }
            projectFiles.add(file);
        }

        return projectFilesMap;
    }

    private void filter(List files, IProjectConfiguration projectConfig)
    {

        List filters = projectConfig.getFilters();
        Iterator it = filters.iterator();
        while (it.hasNext())
        {
            IFilter filter = (IFilter) it.next();

            Iterator filesIt = files.iterator();
            while (filesIt.hasNext())
            {

                IResource file = (IResource) filesIt.next();

                if (filter.isEnabled() && !filter.accept(file))
                {
                    filesIt.remove();
                }
            }
        }
    }
}
