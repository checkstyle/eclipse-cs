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

package net.sf.eclipsecs.core.projectconfig;

import java.util.List;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;

import org.eclipse.core.resources.IProject;

/**
 * The public interface of a project configuration.
 * 
 * @author Lars Ködderitzsch
 */
public interface IProjectConfiguration {

  /**
   * Returns the project which this configuration belongs to.
   * 
   * @return the project
   */
  IProject getProject();

  /**
   * Returns the list of check configurations locally configured for this project.
   * 
   * @param <T>
   *          the type of check configurations
   * @return the list of local check configurations
   */
  <T extends ICheckConfiguration> List<T> getLocalCheckConfigurations();

  /**
   * Returns the file sets configured for the project.
   * 
   * @return the file sets
   */
  List<FileSet> getFileSets();

  /**
   * Gets the filters of this file set.
   * 
   * @return the filters
   */
  List<IFilter> getFilters();

  /**
   * Returns if the simple configuration should be used.
   * 
   * @return <code>true</code>, if this project uses the simple configuration, <code>false</code>
   *         otherwise
   */
  boolean isUseSimpleConfig();

  /**
   * Checks if this project configuration uses the given checkstyle configuration.
   * 
   * @param configuration
   *          the check configuration
   * @return <code>true</code>, if the project config uses the checkstyle config, <code>false</code>
   *         otherwise
   */
  boolean isConfigInUse(ICheckConfiguration configuration);

  /**
   * Returns if the formatter synching feature is enabled.
   * 
   * @return <code>true</code> if checkstyle settings are synched into formatter settings.
   */
  boolean isSyncFormatter();
}
