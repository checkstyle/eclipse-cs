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

package com.atlassw.tools.eclipse.checkstyle.actions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleMarker;

/**
 * Action to diable Checkstyle on one ore more projects.
 * 
 * @author Lars Koedderitzsch
 */
public class ClearSelectedFilesAction implements IObjectActionDelegate
{

    private IStructuredSelection mSelection;

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection)
    {

        if (selection instanceof IStructuredSelection)
        {
            mSelection = (IStructuredSelection) selection;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action)
    {

        List resourcesToClear = mSelection.toList();

        ClearMarkersJob job = new ClearMarkersJob(resourcesToClear);
        job.schedule();
    }

    /**
     * Activates Checkstyle on a collection of projects.
     * 
     * @author Lars Koedderitzsch
     */
    private class ClearMarkersJob extends WorkspaceJob
    {

        private Collection mResourcesToClear;

        public ClearMarkersJob(Collection resourcesToClear)
        {
            super(Messages.ClearSelectedFilesAction_title);
            this.mResourcesToClear = resourcesToClear;
        }

        /**
         * {@inheritDoc}
         */
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
        {

            Iterator it = mResourcesToClear.iterator();
            while (it.hasNext())
            {

                IResource resource = (IResource) it.next();
                if (resource.isAccessible())
                {
                    resource.deleteMarkers(CheckstyleMarker.MARKER_ID, true,
                            IResource.DEPTH_INFINITE);
                }
            }

            return Status.OK_STATUS;
        }
    }
}
