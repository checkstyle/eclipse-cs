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

package net.sf.eclipsecs.ui.preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.preferences.SettingsTransfer;

import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

/**
 * Support for transferring internal eclipse-cs workspace settings to another
 * workspace.
 *
 */
public class CheckstyleSettingsTransfer extends SettingsTransfer {

  @Override
  public String getName() {
    return Messages.CheckstylePreferenceTransfer_name;
  }

  @Override
  public IStatus transferSettings(IPath newWorkspaceRoot) {

    try {
      CheckConfigurationFactory.transferInternalConfiguration(newWorkspaceRoot);
    } catch (CheckstylePluginException ex) {
      return new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID,
              "Checkstyle settings transfer failed", ex);
    }

    return Status.OK_STATUS;
  }

}
