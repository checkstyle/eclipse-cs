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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sf.eclipsecs.core.jobs.RunCheckstyleOnFilesJob;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;

/**
 * Action to run Checkstyle on one ore more projects.
 *
 * @author Lars Ködderitzsch
 */
public class CheckSelectedFilesAction extends AbstractHandler implements IObjectActionDelegate {

  private IWorkbenchPart mPart;

  private IStructuredSelection mSelection;

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    mPart = targetPart;
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {

    if (selection instanceof IStructuredSelection) {
      mSelection = (IStructuredSelection) selection;
    }
  }

  @Override
  public void run(IAction action) {
    checkSelection(mSelection);
  }

  private void checkSelection(IStructuredSelection selection) {
    Set<IResource> resources = new HashSet<>();
    for (Object object : selection.toList()) {
      if (object instanceof IAdaptable adaptable) {
        var resource = adaptable.getAdapter(IResource.class);
        if (resource != null) {
          resources.add(resource);
        }
      }
    }
    List<IFile> filesToCheck = new ArrayList<>();
    try {
      addFileResources(List.copyOf(resources), filesToCheck);
      if (filesToCheck.isEmpty()) {
        return;
      }

      RunCheckstyleOnFilesJob job = new RunCheckstyleOnFilesJob(filesToCheck);
      job.setRule(job);
      job.schedule();
    } catch (CoreException ex) {
      CheckstyleUIPlugin.errorDialog(mPart.getSite().getShell(), ex, true);
    }
  }

  /**
   * Recursively add all files contained in the given resource collection to the second list.
   *
   * @param resources
   *          list of resource
   * @param files
   *          the list of files
   * @throws CoreException
   *           an unexpected exception
   */
  private void addFileResources(List<IResource> resources, List<IFile> files) throws CoreException {
    for (IResource resource : resources) {

      if (!resource.isAccessible()) {
        continue;
      }

      if (resource instanceof IFile) {
        files.add((IFile) resource);
      } else if (resource instanceof IContainer) {
        addFileResources(Arrays.asList(((IContainer) resource).members()), files);
      }
    }
  }

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    var selection = HandlerUtil.getCurrentSelection(event);
    if (selection instanceof IStructuredSelection structuredSelection) {
      checkSelection(structuredSelection);
    }
    return null;
  }
}
