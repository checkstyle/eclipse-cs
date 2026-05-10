//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.stats.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ide.ResourceUtil;

public class SelectionTool {

  private SelectionTool() {

  }

  public static List<IResource> resolveSelection(IWorkbenchPart part, ISelection selection) {
    if (part instanceof IEditorPart editor) {
      IFile file = ResourceUtil.getFile(editor.getEditorInput());
      if (file != null) {
        return List.of(file);
      }
    } else if (selection instanceof IStructuredSelection structuredSelection) {
      List<IResource> resources = new ArrayList<>();
      for (Object object : structuredSelection) {
        if (object instanceof IWorkingSet workingSet) {
          IAdaptable[] elements = workingSet.getElements();
          for (int i = 0; i < elements.length; i++) {
            considerAdaptable(elements[i]).ifPresent(resources::add);
          }
        } else if (object instanceof IAdaptable adaptable) {
          considerAdaptable(adaptable).ifPresent(resources::add);
        }
      }
      return resources;
    }
    return Collections.emptyList();
  }

  private static Optional<IResource> considerAdaptable(IAdaptable adaptable) {
    IResource resource = adaptable.getAdapter(IResource.class);
    if (resource == null) {
      resource = adaptable.getAdapter(IFile.class);
    }
    return Optional.ofNullable(resource);
  }

}
