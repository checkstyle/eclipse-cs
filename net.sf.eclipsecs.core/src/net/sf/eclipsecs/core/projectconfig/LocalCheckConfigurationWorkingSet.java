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

package net.sf.eclipsecs.core.projectconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Working set implementation that manages local configurations configured for the project.
 *
 */
public class LocalCheckConfigurationWorkingSet implements CheckConfigurationWorkingSet {

  /** The project configuration. */
  private final ProjectConfiguration mProjectConfig;

  /** The internal list of working copies belonging to this working set. */
  private final List<CheckConfigurationWorkingCopy> mWorkingCopies;

  /** List of working copies that were deleted from the working set. */
  private final List<CheckConfigurationWorkingCopy> mDeletedConfigurations;

  /**
   * Creates a working set to manage local configurations.
   *
   * @param projectConfig
   *          the project configuration
   * @param checkConfigs
   *          the list of local check configurations
   */
  LocalCheckConfigurationWorkingSet(ProjectConfiguration projectConfig,
          List<CheckConfiguration> checkConfigs) {

    mProjectConfig = projectConfig;
    mWorkingCopies = new ArrayList<>();
    mDeletedConfigurations = new ArrayList<>();

    for (CheckConfiguration cfg : checkConfigs) {
      CheckConfigurationWorkingCopy workingCopy = new CheckConfigurationWorkingCopy(cfg, this);
      mWorkingCopies.add(workingCopy);
    }
  }

  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(CheckConfiguration checkConfig) {
    return new CheckConfigurationWorkingCopy(checkConfig, this);
  }

  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(ConfigurationType configType) {
    return new CheckConfigurationWorkingCopy(configType, this, false);
  }

  @Override
  public CheckConfigurationWorkingCopy[] getWorkingCopies() {
    return mWorkingCopies.toArray(new CheckConfigurationWorkingCopy[mWorkingCopies.size()]);
  }

  @Override
  public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
    mWorkingCopies.add(checkConfig);
  }

  @Override
  public boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {

    boolean inUse = mProjectConfig.isConfigInUse(checkConfig);

    if (!inUse) {
      mWorkingCopies.remove(checkConfig);
      mDeletedConfigurations.add(checkConfig);
    }

    return !inUse;
  }

  @Override
  public void store() throws CheckstylePluginException {
    notifyDeletedCheckConfigs();
  }

  @Override
  public boolean isDirty() {
    boolean dirty = false;
    if (mDeletedConfigurations.isEmpty()) {
      for (CheckConfigurationWorkingCopy workingCopy : mWorkingCopies) {
        dirty = workingCopy.isDirty();
        if (dirty) {
          break;
        }
      }
    } else {
      dirty = true;
    }
    return dirty;
  }

  @Override
  public boolean isNameCollision(CheckConfigurationWorkingCopy configuration) {

    boolean result = false;
    for (CheckConfigurationWorkingCopy tmp : mWorkingCopies) {
      if (tmp != configuration && tmp.getName().equals(configuration.getName())) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Returns the project of the local check configuration working set.
   *
   * @return the project
   */
  public IProject getProject() {
    return mProjectConfig.getProject();
  }

  @Override
  public Collection<IProject> getAffectedProjects() {
    Set<IProject> projects = new HashSet<>();

    CheckConfigurationWorkingCopy[] workingCopies = this.getWorkingCopies();
    for (int i = 0; i < workingCopies.length; i++) {

      // skip non dirty configurations
      if (workingCopies[i].hasConfigurationChanged()
              && mProjectConfig.isConfigInUse(workingCopies[i])) {
        projects.add(mProjectConfig.getProject());
        break;
      }
    }

    return projects;
  }

  /**
   * Notifies the check configurations that have been deleted.
   *
   * @throws CheckstylePluginException
   *           an exception while notifiing for deletion
   */
  private void notifyDeletedCheckConfigs() throws CheckstylePluginException {
    for (CheckConfiguration checkConfig : mDeletedConfigurations) {
      checkConfig.getType().notifyCheckConfigRemoved(checkConfig);
    }
  }

}
