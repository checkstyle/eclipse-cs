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

package net.sf.eclipsecs.core.config.configtypes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.URIUtil;

/**
 * Implementation of the configuration type for a internal check configuration, that is located
 * inside the plugin.
 *
 * @author Lars Ködderitzsch
 */
public class InternalConfigurationType extends ConfigurationType {

  /**
   * Resolves the location inside the plugins workspace state location.
   *
   * @param location
   *          the location
   * @return the resolved location in the workspace
   */
  public static String resolveLocationInWorkspace(String location) {

    IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
    configPath = configPath.append(location);
    return configPath.toString();
  }

  @Override
  protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException {
    String location = checkConfiguration.getLocation();

    // resolve the location in the workspace
    location = resolveLocationInWorkspace(location);

    return new File(location).toURI().toURL();
  }

  @Override
  public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException {
    super.notifyCheckConfigRemoved(checkConfiguration);

    // remove the configuration file from the workspace metadata
    URL configFileURL = checkConfiguration.getResolvedConfigurationFileURL();
    if (configFileURL != null) {

      try {
        File configFile = URIUtil.toFile(configFileURL.toURI());

        if (configFile != null) {
          configFile.delete();
        }
      } catch (URISyntaxException e) {
        CheckstylePluginException.rethrow(e);
      }
    }
  }
}
