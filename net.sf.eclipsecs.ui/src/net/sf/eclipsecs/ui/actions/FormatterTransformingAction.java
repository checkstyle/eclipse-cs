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

import net.sf.eclipsecs.core.jobs.TransformFormatterRulesJob;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to start transforming checkstyle-rules to formatter-rules.
 *
 * @author lakiluk
 */
public class FormatterTransformingAction implements IObjectActionDelegate {

  @Override
  public void setActivePart(final IAction arg0, final IWorkbenchPart arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public void run(final IAction arg0) {
    final TransformFormatterRulesJob job = new TransformFormatterRulesJob();
    job.schedule();
  }

  @Override
  public void selectionChanged(final IAction arg0, final ISelection arg1) {
    // TODO Auto-generated method stub

  }

}
