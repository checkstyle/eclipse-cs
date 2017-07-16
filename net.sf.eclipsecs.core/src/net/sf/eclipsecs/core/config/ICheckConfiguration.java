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

package net.sf.eclipsecs.core.config;

import java.net.URL;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Interface for a check configuration object.
 * 
 * @author Lars Ködderitzsch
 */
public interface ICheckConfiguration {

  /**
   * Returns the displayable name of the configuration.
   * 
   * @return the displayable name of the configuration
   */
  String getName();

  /**
   * Return a description of the check configuration.
   * 
   * @return a description.
   */
  String getDescription();

  /**
   * Returns the location of the checkstyle configuration file.
   * 
   * @return the location of the configuration file
   */
  String getLocation();

  /**
   * Return the type of the configuration.
   * 
   * @return the configuration type
   */
  IConfigurationType getType();

  /**
   * Gets additional data for this configuration.
   * 
   * @return the additional data in form of a Map
   */
  Map<String, String> getAdditionalData();

  /**
   * Returns the list of properties added to the configuration.
   * 
   * @return the list of configured properties
   */
  List<ResolvableProperty> getResolvableProperties();

  /**
   * Determines if the configuration properties are editable by the user.
   * 
   * @return <code>true</code>, if the configuration is editable
   */
  boolean isEditable();

  /**
   * Determines if the checkstyle configuration associates with this check configuration can be
   * configured.
   * 
   * @return <code>true</code> if the checkstyle configuration can be configured.
   */
  boolean isConfigurable();

  /**
   * Returns if the check configuration is a global configuration, configured for the workspace, or
   * a local configuration for a single project.
   * 
   * @return <code>true</code> if the check configuration is configured globally
   */
  boolean isGlobal();

  /**
   * Returns the resolved location URL of the Checkstyle configuration file configured for this
   * check configuration. Clients should not try to open an actual stream to the configuration file,
   * since this is not guaranteed to work.
   * 
   * @return the Checkstyle configuration file location as URL
   * @throws CheckstylePluginException
   *           exception while resolving the URL
   */
  URL getResolvedConfigurationFileURL() throws CheckstylePluginException;

  /**
   * Get all data of the Checkstyle configuration (file data, additional properties...) in one go.
   * This is done to optimize the number of accesses that must be done on the configuration files.
   * 
   * @return all Checkstyle configuration file data necessary to create a checker
   * @throws CheckstylePluginException
   *           exception while getting the Checkstyle configuration file data
   */
  CheckstyleConfigurationFile getCheckstyleConfiguration() throws CheckstylePluginException;
}