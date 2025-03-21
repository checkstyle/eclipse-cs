//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.actions;

import java.util.Collection;
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

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.ui.Messages;

/**
 * Action to diable Checkstyle on one ore more projects.
 *
 */
public class ClearSelectedFilesAction implements IObjectActionDelegate {

  private IStructuredSelection mSelection;

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // NOOP
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {

    if (selection instanceof IStructuredSelection) {
      mSelection = (IStructuredSelection) selection;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run(IAction action) {

    List<IResource> resourcesToClear = mSelection.toList();

    ClearMarkersJob job = new ClearMarkersJob(resourcesToClear);
    job.schedule();
  }

  /**
   * Activates Checkstyle on a collection of projects.
   *
   */
  private static class ClearMarkersJob extends WorkspaceJob {

    private Collection<IResource> mResourcesToClear;

    public ClearMarkersJob(Collection<IResource> resourcesToClear) {
      super(Messages.ClearSelectedFilesAction_title);
      this.mResourcesToClear = resourcesToClear;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

      for (IResource resource : mResourcesToClear) {
        if (resource.isAccessible()) {
          resource.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);
        }
      }

      return Status.OK_STATUS;
    }
  }
}
