//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

import net.sf.eclipsecs.core.jobs.TransformCheckstyleRulesJob;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to start transforming checkstyle-rules to formatter-rules.
 *
 * @author lakiluk
 */
public class CheckstyleTransformingAction implements IObjectActionDelegate {

  /** Selection in workspace. */
  private ISelection mSelection;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setActivePart(final IAction arg0, final IWorkbenchPart arg1) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run(final IAction arg0) {
    final TransformCheckstyleRulesJob job = new TransformCheckstyleRulesJob(
            ((IProject) ((IStructuredSelection) mSelection).getFirstElement()));
    job.schedule();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void selectionChanged(final IAction arg0, final ISelection arg1) {
    mSelection = arg1;
  }

}
