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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Filter that excludes all files that are not opened in an eclipse editor.
 * 
 * @author Lars Ködderitzsch
 */
public class UnOpenedFilesFilter extends AbstractFilter
{

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object element)
    {

        if (element instanceof IFile)
        {

            //TODO refactor!
            IWorkbench workBench = PlatformUI.getWorkbench();

            IWorkbenchWindow[] windows = workBench.getWorkbenchWindows();
            for (int i = 0; i < windows.length; i++)
            {

                IWorkbenchPage[] pages = windows[i].getPages();

                for (int j = 0; j < pages.length; j++)
                {

                    IEditorReference[] editorRefs = pages[j].getEditorReferences();

                    for (int k = 0; k < editorRefs.length; k++)
                    {

                        IEditorPart editor = editorRefs[k] != null ? editorRefs[k].getEditor(false)
                                : null;

                        IEditorInput input = editor != null ? editor.getEditorInput() : null;

                        if (input != null && input instanceof IFileEditorInput)
                        {

                            IFileEditorInput fileInput = (IFileEditorInput) input;
                            boolean accept = fileInput.getFile().equals(element);
                            if (accept)
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}