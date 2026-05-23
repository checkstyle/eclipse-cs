//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import net.sf.eclipsecs.core.jobs.TransformFormatterRulesJob;

/**
 * Action to start transforming checkstyle-rules to formatter-rules.
 *
 */
public class FormatterTransformingAction implements IActionDelegate {

  private IProject project;

  @Override
  public void run(final IAction arg0) {
    final TransformFormatterRulesJob job = new TransformFormatterRulesJob(project);
    job.schedule();
  }

  @Override
  public void selectionChanged(final IAction action, final ISelection selection) {
    if (selection instanceof IStructuredSelection structuredSelection
            && structuredSelection.getFirstElement() instanceof IProject selectedProject) {
      this.project = selectedProject;
    }
  }

}
