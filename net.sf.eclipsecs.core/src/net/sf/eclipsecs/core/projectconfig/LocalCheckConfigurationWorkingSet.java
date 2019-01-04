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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;

/**
 * Working set implementation that manages local configurations configured for the project.
 *
 * @author Lars Ködderitzsch
 */
public class LocalCheckConfigurationWorkingSet implements ICheckConfigurationWorkingSet {

  /** The project configuration. */
  private final IProjectConfiguration mProjectConfig;

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
  LocalCheckConfigurationWorkingSet(IProjectConfiguration projectConfig,
          List<ICheckConfiguration> checkConfigs) {

    mProjectConfig = projectConfig;
    mWorkingCopies = new ArrayList<>();
    mDeletedConfigurations = new ArrayList<>();

    for (ICheckConfiguration cfg : checkConfigs) {
      CheckConfigurationWorkingCopy workingCopy = new CheckConfigurationWorkingCopy(cfg, this);
      mWorkingCopies.add(workingCopy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(ICheckConfiguration checkConfig) {
    return new CheckConfigurationWorkingCopy(checkConfig, this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(IConfigurationType configType) {
    return new CheckConfigurationWorkingCopy(configType, this, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckConfigurationWorkingCopy[] getWorkingCopies() {
    return mWorkingCopies.toArray(new CheckConfigurationWorkingCopy[mWorkingCopies.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
    mWorkingCopies.add(checkConfig);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {

    boolean inUse = mProjectConfig.isConfigInUse(checkConfig);

    if (!inUse) {
      mWorkingCopies.remove(checkConfig);
      mDeletedConfigurations.add(checkConfig);
    }

    return !inUse;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void store() throws CheckstylePluginException {
    notifyDeletedCheckConfigs();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDirty() {
    if (mDeletedConfigurations.size() > 0) {
      return true;
    }

    boolean dirty = false;
    for (CheckConfigurationWorkingCopy workingCopy : mWorkingCopies) {
      dirty = workingCopy.isDirty();

      if (dirty) {
        break;
      }
    }
    return dirty;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
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
    for (ICheckConfiguration checkConfig : mDeletedConfigurations) {
      checkConfig.getType().notifyCheckConfigRemoved(checkConfig);
    }
  }

}
