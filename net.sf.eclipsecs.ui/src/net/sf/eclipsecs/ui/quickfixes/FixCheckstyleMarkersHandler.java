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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Command handler to enable key-binding support for the "Apply Checkstyle
 * fixes" action.
 *
 */
public class FixCheckstyleMarkersHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent arg0) throws ExecutionException {

    ITextEditor editor = getActiveEditor();
    IEditorInput input = editor.getEditorInput();

    if (input instanceof FileEditorInput) {

      IFile file = ((FileEditorInput) input).getFile();

      // call the fixing job
      Job job = new FixCheckstyleMarkersJob(file);
      job.setUser(true);
      job.schedule();
    }
    return null;
  }

  private ITextEditor getActiveEditor() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        IEditorPart editor = page.getActiveEditor();
        if (editor instanceof ITextEditor) {
          return (ITextEditor) editor;
        }
      }
    }
    return null;
  }

}
