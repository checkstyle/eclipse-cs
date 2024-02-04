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

package net.sf.eclipsecs.core.config.configtypes;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;

/**
 * Implementation of the configuration type for a built in check configuration, that is located
 * inside the plugin.
 *
 * @author Lars Ködderitzsch
 */
public class BuiltInConfigurationType extends ConfigurationType {

  /**
   * constant for the contributor key. It stores the id of the plugin which contributes the built in
   * configuration, so that the file can be retrieved properly.
   */
  public static final String CONTRIBUTOR_KEY = "contributor";

  @Override
  protected URL resolveLocation(ICheckConfiguration checkConfiguration) {

    String contributorName = checkConfiguration.getAdditionalData().get(CONTRIBUTOR_KEY);

    Bundle contributor = Platform.getBundle(contributorName);
    URL locationUrl = FileLocator.find(contributor, new Path(checkConfiguration.getLocation()),
            null);

    // suggested by https://sourceforge.net/p/eclipse-cs/bugs/410/
    if (locationUrl == null) {
      locationUrl = contributor.getResource(checkConfiguration.getLocation());
    }

    return locationUrl;
  }

  @Override
  protected byte[] getAdditionPropertiesBundleBytes(URL checkConfigURL) {
    // just returns null since additional property file is not needed nor
    // supported
    return null;
  }

  @Override
  protected PropertyResolver getPropertyResolver(ICheckConfiguration config,
          CheckstyleConfigurationFile configFile) {
    MultiPropertyResolver resolver = new MultiPropertyResolver();
    resolver.addPropertyResolver(new ResolvablePropertyResolver(config));
    resolver.addPropertyResolver(
            new BuiltInFilePropertyResolver(resolveLocation(config).toString()));

    return resolver;
  }
}
