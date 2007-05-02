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

import org.eclipse.core.resources.IProject;
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
import com.atlassw.tools.eclipse.checkstyle.nature.CheckstyleNature;
import com.atlassw.tools.eclipse.checkstyle.nature.ConfigureDeconfigureNatureJob;

/**
 * Action to diable Checkstyle on one ore more projects.
 * 
 * @author Lars Koedderitzsch
 */
public class DeactivateProjectsAction implements IObjectActionDelegate
{

    private IWorkbenchPart mPart;

    private Collection mSelectedProjects;

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        mPart = targetPart;
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection)
    {

        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection sel = (IStructuredSelection) selection;
            mSelectedProjects = sel.toList();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action)
    {
        BulkCheckstyleActivateJob job = new BulkCheckstyleActivateJob(mSelectedProjects);
        job.schedule();
    }

    /**
     * Activates Checkstyle on a collection of projects.
     * 
     * @author Lars Koedderitzsch
     */
    private class BulkCheckstyleActivateJob extends WorkspaceJob
    {

        private Collection mProjectsToDeactivate;

        public BulkCheckstyleActivateJob(Collection projectsToDeactivate)
        {
            super(Messages.DeactivateProjectsPrintAction_msgDeactivateSelectedProjects);
            this.mProjectsToDeactivate = projectsToDeactivate;
        }

        /**
         * {@inheritDoc}
         */
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
        {

            Iterator it = mProjectsToDeactivate.iterator();
            while (it.hasNext())
            {

                IProject configurationTarget = (IProject) it.next();

                if (configurationTarget.isOpen()
                        && configurationTarget.hasNature(CheckstyleNature.NATURE_ID))
                {

                    ConfigureDeconfigureNatureJob job = new ConfigureDeconfigureNatureJob(
                            configurationTarget, CheckstyleNature.NATURE_ID);
                    job.schedule();
                }
            }

            return Status.OK_STATUS;
        }
    }
}
