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
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Implementation of a check configuration that uses an exteral checkstyle configuration file.
 *
 * @author Lars Ködderitzsch
 */
public class ExternalFileConfigurationType extends ConfigurationType {

  /** Key to access the information if the configuration is protected. */
  public static final String KEY_PROTECT_CONFIG = "protect-config-file"; //$NON-NLS-1$

  /** Property resolver used to add dynamic location support. */
  private static final PropertyResolver DYNAMIC_LOC_RESOLVER;

  static {
    MultiPropertyResolver resolver = new MultiPropertyResolver();
    resolver.addPropertyResolver(new ClasspathVariableResolver());
    resolver.addPropertyResolver(new SystemPropertyResolver());
    DYNAMIC_LOC_RESOLVER = resolver;
  }

  /**
   * Tries to resolve a dynamic location into the real file path.
   *
   * @param location
   *          the probably unresolved location string
   * @return the resolved location
   * @throws CheckstylePluginException
   *           unexpected error while resolving the dynamic properties
   */
  public static String resolveDynamicLocation(String location) throws CheckstylePluginException {

    String newLocation = location;

    try {
      // support dynamic locations for external configurations
      while (PropertyUtil.hasUnresolvedProperties(newLocation)) {
        newLocation = PropertyUtil.replaceProperties(newLocation, DYNAMIC_LOC_RESOLVER);
      }
    } catch (CheckstyleException e) {
      CheckstylePluginException.rethrow(e);
    }
    return newLocation;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException {

    String location = checkConfiguration.getLocation();

    // support dynamic locations for external configurations
    try {
      location = resolveDynamicLocation(location);
    } catch (CheckstylePluginException e) {
      CheckstyleLog.log(e);
      throw new IOException(e.getMessage());
    }

    return new File(location).toURI().toURL();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConfigurable(ICheckConfiguration checkConfiguration) {

    boolean isConfigurable = true;

    boolean isProtected = Boolean
            .valueOf(checkConfiguration.getAdditionalData().get(KEY_PROTECT_CONFIG)).booleanValue();
    isConfigurable = !isProtected;

    if (!isProtected) {

      String location = checkConfiguration.getLocation();

      try {

        // support dynamic locations for external configurations
        location = resolveDynamicLocation(location);
      } catch (CheckstylePluginException e) {
        CheckstyleLog.log(e);
        isConfigurable = false;
      }

      // The configuration can be changed when the external configuration
      // file
      // can is writable
      isConfigurable = new File(location).canWrite();
    }
    return isConfigurable;
  }

}
