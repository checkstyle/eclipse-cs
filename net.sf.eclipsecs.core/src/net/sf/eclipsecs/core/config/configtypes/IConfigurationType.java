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

import java.net.URL;

import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Interface for a configuration type.
 *
 * @author Lars Ködderitzsch
 */
public interface IConfigurationType {

  /**
   * Initializes the configuration type.
   *
   * @param name
   *          the displayable name of the configuration type
   * @param internalName
   *          the internal name of the configuration type
   * @param definingPluginId
   *          the plugin id the configuration type was defined in
   * @param isCreatable
   *          <code>true</code> if a configuration of this type can be created by the user.
   * @param isEditable
   *          <code>true</code> if a configuration of this type can be edited by the user.
   * @param isConfigurable
   *          <code>true</code> if a configuration of this type can be configured by the user.
   */
  void initialize(String name, String internalName, String definingPluginId, boolean isCreatable,
          boolean isEditable, boolean isConfigurable);

  /**
   * The displayable name of the configuration type.
   *
   * @return the displayable name
   */
  String getName();

  /**
   * Returns the internal name of the configuration type.
   *
   * @return the internal name
   */
  String getInternalName();

  /**
   * Return if a check configuration of this type can be created by the user.
   *
   * @return <code>true</code> if the check configuration type is creatable, otherwise
   *         <code>false</code>
   */
  boolean isCreatable();

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
   * @param checkConfiguration
   *          the actual check configuration
   * @return <code>true</code> if the checkstyle configuration can be configured.
   */
  boolean isConfigurable(ICheckConfiguration checkConfiguration);

  /**
   * Notifies that a check configuration has been removed.
   *
   * @param checkConfiguration
   *          the check configuration which was removed
   * @throws CheckstylePluginException
   *           error while processing the notification
   */
  void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException;

  /**
   * Returns the resolved location URL of the Checkstyle configuration file configured for the given
   * check configuration. Clients should not try to open an actual stream to the configuration file,
   * since this might not work directly.
   *
   * @param checkConfiguration
   *          the check configuration
   * @return the Checkstyle configuration file location as URL
   * @throws CheckstylePluginException
   *           exception while resolving the URL
   */
  URL getResolvedConfigurationFileURL(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException;

  /**
   * Get all data of the Checkstyle configuration (file data, additional properties...) in one go.
   * This is done to optimize the number of accesses that must be done on the configuration files.
   *
   * @param checkConfiguration
   *          the check configuration to get the data from
   * @return all Checkstyle configuration file data necessary to create a checker
   * @throws CheckstylePluginException
   *           exception while getting the Checkstyle configuration file data
   */
  CheckstyleConfigurationFile getCheckstyleConfiguration(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException;
}
