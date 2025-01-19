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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import net.sf.eclipsecs.core.builder.CheckerFactory;

/**
 * Simple handle which clears the CheckerFactory caches in order to force reload of supplementary
 * Checkstyle configuration files (suppressions, import control files etc.).
 *
 */
public class PurgeCachesAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

  @Override
  public void run(IAction action) {
    CheckerFactory.cleanup();
  }

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    CheckerFactory.cleanup();
    return null;
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void init(IWorkbenchWindow window) {
  }

}
