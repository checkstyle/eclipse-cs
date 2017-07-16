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

package net.sf.eclipsecs.ui.actions;

import java.util.Collection;

import net.sf.eclipsecs.core.jobs.ConfigureDeconfigureNatureJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.ui.Messages;

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

/**
 * Action to diable Checkstyle on one ore more projects.
 * 
 * @author Lars Ködderitzsch
 */
public class DeactivateProjectsAction implements IObjectActionDelegate {

  private Collection<IProject> mSelectedProjects;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public void selectionChanged(IAction action, ISelection selection) {

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection sel = (IStructuredSelection) selection;
      mSelectedProjects = sel.toList();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(IAction action) {
    BulkCheckstyleActivateJob job = new BulkCheckstyleActivateJob(mSelectedProjects);
    job.schedule();
  }

  /**
   * Activates Checkstyle on a collection of projects.
   * 
   * @author Lars Ködderitzsch
   */
  private class BulkCheckstyleActivateJob extends WorkspaceJob {

    private Collection<IProject> mProjectsToDeactivate;

    public BulkCheckstyleActivateJob(Collection<IProject> projectsToDeactivate) {
      super(Messages.DeactivateProjectsPrintAction_msgDeactivateSelectedProjects);
      this.mProjectsToDeactivate = projectsToDeactivate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

      for (IProject configurationTarget : mProjectsToDeactivate) {

        if (configurationTarget.isOpen()
                && configurationTarget.hasNature(CheckstyleNature.NATURE_ID)) {

          ConfigureDeconfigureNatureJob job = new ConfigureDeconfigureNatureJob(configurationTarget,
                  CheckstyleNature.NATURE_ID);
          job.schedule();
        }
      }

      return Status.OK_STATUS;
    }
  }
}
