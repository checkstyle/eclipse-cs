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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of a check configuration that uses an exteral checkstyle configuration file.
 *
 * @author Lars Ködderitzsch
 */
public class ProjectConfigurationType extends ConfigurationType {

  /** Key to access the information if the configuration is protected. */
  public static final String KEY_PROTECT_CONFIG = "protect-config-file"; //$NON-NLS-1$

  @Override
  protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException {
    IResource configFileResource = ResourcesPlugin.getWorkspace().getRoot()
            .findMember(checkConfiguration.getLocation());

    if (configFileResource != null) {
      return configFileResource.getLocation().toFile().toURI().toURL();
    } else {
      throw new FileNotFoundException(NLS.bind(Messages.ProjectConfigurationType_msgFileNotFound,
              checkConfiguration.getLocation()));
    }
  }

  @Override
  public boolean isConfigurable(ICheckConfiguration checkConfiguration) {
    boolean isConfigurable = true;

    boolean isProtected = Boolean
            .valueOf(checkConfiguration.getAdditionalData().get(KEY_PROTECT_CONFIG)).booleanValue();
    isConfigurable = !isProtected;

    if (!isProtected) {

      // The configuration can be changed when the external configuration
      // file can is writable
      try {

        File file = URIUtil.toFile(checkConfiguration.getResolvedConfigurationFileURL().toURI());
        isConfigurable = file != null && file.canWrite();
      } catch (CheckstylePluginException | URISyntaxException e) {
        CheckstyleLog.log(e);
        isConfigurable = false;
      }
    }
    return isConfigurable;
  }
}
