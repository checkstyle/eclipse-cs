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

package net.sf.eclipsecs.ui.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This action tries to run all quickfixes for markers on a selected compilation
 * unit.
 *
 */
public class FixCheckstyleMarkersAction implements IObjectActionDelegate {

  /** the selection that occured in the workspace. */
  private ISelection mSelection;

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    mSelection = selection;
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // empty
  }

  @Override
  public void run(IAction action) {

    IStructuredSelection selection = null;
    if (mSelection instanceof IStructuredSelection) {
      selection = (IStructuredSelection) mSelection;
    }

    // no valid selection
    if (selection == null || selection.size() != 1) {
      return;
    }

    Object element = selection.getFirstElement();

    IFile file = ((IAdaptable) element).getAdapter(IFile.class);
    if (file != null) {

      // call the fixing job
      Job job = new FixCheckstyleMarkersJob(file);
      job.setUser(true);
      job.schedule();
    }
  }
}
