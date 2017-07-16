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

import com.puppycrawl.tools.checkstyle.PropertyResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.URIUtil;

/**
 * Adds support for additional checkstyle config files (header, suppressions etc.) to be delivered
 * with a builtin configuration.
 *
 * @author Lars Ködderitzsch
 */
public class BuiltInFilePropertyResolver implements PropertyResolver {

  /** constant for the samedir variable. */
  private static final String SAMEDIR_LOC = "samedir"; //$NON-NLS-1$

  /** constant for the config_loc variable. */
  private static final String CONFIG_LOC = "config_loc"; //$NON-NLS-1$

  private final String mBuiltInConfigLocation;

  /**
   * Creates the resolver.
   *
   * @param builtInConfigLocation
   *          the bundle based url of the builtin configuration file
   */
  public BuiltInFilePropertyResolver(String builtInConfigLocation) {
    mBuiltInConfigLocation = builtInConfigLocation;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String resolve(String property) {

    String value = null;

    if ((SAMEDIR_LOC.equals(property) || CONFIG_LOC.equals(property))
            && mBuiltInConfigLocation != null) {

      int lastSlash = mBuiltInConfigLocation.lastIndexOf("/"); //$NON-NLS-1$
      if (lastSlash > -1) {
        value = mBuiltInConfigLocation.substring(0, lastSlash + 1);
      }
    }

    if (value != null) {
      try {
        URL bundleLocatedURL = new URL(value);
        URL fileURL = FileLocator.toFileURL(bundleLocatedURL);

        value = URIUtil.toFile(fileURL.toURI()).getAbsolutePath();
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    return value;
  }

}
