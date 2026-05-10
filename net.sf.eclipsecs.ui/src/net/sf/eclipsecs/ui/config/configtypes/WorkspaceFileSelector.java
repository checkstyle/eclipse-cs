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

package net.sf.eclipsecs.ui.config.configtypes;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.ui.Messages;

public class WorkspaceFileSelector {

  private WorkspaceFileSelector() {

  }

  public static Optional<String> select(Shell shell) {
    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
            new WorkbenchLabelProvider(), new WorkbenchContentProvider());
    dialog.setHelpAvailable(false);
    dialog.setTitle(Messages.ProjectConfigurationLocationEditor_titleSelectConfigFile);
    dialog.setMessage(Messages.ProjectConfigurationLocationEditor_msgSelectConfigFile);
    dialog.setBlockOnOpen(true);
    dialog.setAllowMultiple(false);
    dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
    dialog.setValidator(selection -> {
      if (selection.length == 1 && selection[0] instanceof IFile) {
        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.ERROR, new String(), null);
      }
      return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, new String(), null);
    });
    if (Window.OK == dialog.open()) {
      Object[] result = dialog.getResult();
      IFile checkFile = (IFile) result[0];
      return Optional.of(checkFile.getFullPath().toString());
    }
    return Optional.empty();
  }

}
