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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Implementation of a filter that filters all ressources that are not within a
 * source directory.
 * 
 * @author Lars Ködderitzsch
 */
public class NonSrcDirsFilter extends AbstractFilter
{

    //
    // attributes
    //

    /** the current project. */
    private IProject mCurrentProject;

    /** the list of source paths of the current project. */
    private List mCurrentSourcePaths;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object element)
    {

        boolean goesThrough = false;

        if (element instanceof IResource)
        {
            IResource resource = (IResource) element;

            IProject project = resource.getProject();
            if (mCurrentProject != project)
            {
                mCurrentSourcePaths = getSourceDirPaths(project);
                mCurrentProject = project;
            }

            Iterator it = mCurrentSourcePaths.iterator();
            while (it.hasNext())
            {

                IPath sourcePath = (IPath) it.next();
                if (sourcePath.isPrefixOf(resource.getFullPath()))
                {
                    goesThrough = true;
                    break;
                }
            }
        }
        return goesThrough;
    }

    /**
     * Gets all source paths of a project.
     * 
     * @param project the project
     * @return the list of source paths
     */
    private List getSourceDirPaths(IProject project)
    {

        List sourceDirs = new ArrayList();

        try
        {
            if (project.hasNature(JavaCore.NATURE_ID))
            {
                IJavaProject javaProject = JavaCore.create(project);
                IClasspathEntry[] cp = javaProject.getResolvedClasspath(true);
                for (int i = 0; i < cp.length; i++)
                {
                    if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE)
                    {
                        sourceDirs.add(cp[i].getPath());
                    }
                }
            }
        }
        catch (JavaModelException e)
        {
            CheckstyleLog.log(e);
        }
        catch (CoreException e)
        {
            CheckstyleLog.log(e);
        }

        return sourceDirs;
    }
}