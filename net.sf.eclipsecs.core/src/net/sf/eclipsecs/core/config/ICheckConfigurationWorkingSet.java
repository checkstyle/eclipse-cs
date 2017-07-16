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

import java.util.Collection;

import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;

/**
 * Interface for implementations that provide editing services for a group of check configuration.
 * 
 * @author Lars Ködderitzsch
 */
public interface ICheckConfigurationWorkingSet {

  /**
   * Creates a new working copy for an existing check configuration. The working copy is associated
   * with this working set albeit the new working copy is not added to the working sets list of
   * workin copies.
   * 
   * @param checkConfig
   *          the check configuration to create the working copy for
   * @return the working copy
   */
  CheckConfigurationWorkingCopy newWorkingCopy(ICheckConfiguration checkConfig);

  /**
   * Creates a new working copy with a given configuration type. The working copy is associated with
   * this working set albeit the new working copy is not added to the working sets list of workin
   * copies.
   * 
   * @param configType
   *          the desired configuration type of the new working copy
   * @return the working copy
   */
  CheckConfigurationWorkingCopy newWorkingCopy(IConfigurationType configType);

  /**
   * Returns the working copies that belong to this working set.
   * 
   * @return the working copies
   */
  CheckConfigurationWorkingCopy[] getWorkingCopies();

  /**
   * Checks if the name of a check configuration (in form of a working copy) clashes with an
   * existing configuration. Names of check configurations must be unique within the working copy.
   * 
   * @param configuration
   *          the working copy to check
   * @return <code>true</code> if there is a collision, <code>false</code> otherwise
   */
  boolean isNameCollision(CheckConfigurationWorkingCopy configuration);

  /**
   * Adds a working copy to the working set.
   * 
   * @param checkConfig
   *          the working copy to add
   */
  void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig);

  /**
   * Removes a working copy from the working set. Returns <code>true</code> if the configuration
   * could be removed, <code>false</code> if it could not be removed because it is being used.
   * 
   * @param checkConfig
   *          the working copy to remove
   * @return <code>true</code> if the configuration was removed, <code>false</code> if it is being
   *         used and therefor cannot be removed
   */
  boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig);

  /**
   * Stores the working set (it configurations) to persistence.
   * 
   * @throws CheckstylePluginException
   *           when storing of the configurations failed
   */
  void store() throws CheckstylePluginException;

  /**
   * Determines if the working set changed.
   * 
   * @return <code>true</code> if the working set changed, <code>false</code> otherwise
   */
  boolean isDirty();

  /**
   * Returns a collection of projects affected by the changes of the working set.
   * 
   * @return the collection of affected projects
   * @throws CheckstylePluginException
   *           unexprected error
   */
  Collection<IProject> getAffectedProjects() throws CheckstylePluginException;
}