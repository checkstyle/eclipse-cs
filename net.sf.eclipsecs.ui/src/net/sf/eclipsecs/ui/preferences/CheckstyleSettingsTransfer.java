//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.preferences;

import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.preferences.SettingsTransfer;

/**
 * Support for transferring internal eclipse-cs workspace settings to another
 * workspace.
 *
 * @author Lars Ködderitzsch
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
    } catch (CheckstylePluginException e) {
      return new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID,
              "Checkstyle settings transfer failed", e);
    }

    return Status.OK_STATUS;
  }

}
