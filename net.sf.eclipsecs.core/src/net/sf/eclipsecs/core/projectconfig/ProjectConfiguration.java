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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IProject;

import com.google.common.base.MoreObjects;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;

/**
 * Represents the configuration for a project. Contains the file sets configured for the project
 * plus the additional filters.
 *
 * @author Lars Ködderitzsch
 */
public class ProjectConfiguration implements Cloneable, IProjectConfiguration {

  /** The project. */
  private IProject project;

  /** The local check configurations. */
  private List<ICheckConfiguration> localCheckConfigs;

  /** the file sets. */
  private List<FileSet> fileSets;

  /** the filters. */
  private List<IFilter> filters;

  /** Flags if the simple file set editor should be used. */
  private boolean useSimpleConfig = true;

  /** if formatter synching is enabled. */
  private boolean syncFormatter;

  /**
   * Default constructor.
   *
   * @param project
   *          the project
   * @param localConfigs
   *          the list of local check configurations
   * @param fileSets
   *          the list of configured file sets
   * @param filters
   *          the filters
   * @param useSimpleConfig
   *          <code>true</code> if simple configuration is used
   * @param synchFormatter
   *          <code>true</code> if the formatter configuration should be synced to the Checkstyle
   *          settings
   */
  public ProjectConfiguration(IProject project, List<ICheckConfiguration> localConfigs,
          List<FileSet> fileSets, List<IFilter> filters, boolean useSimpleConfig,
          boolean synchFormatter) {
    this.project = project;
    localCheckConfigs = localConfigs != null ? Collections.unmodifiableList(localConfigs)
            : Collections.unmodifiableList(new ArrayList<ICheckConfiguration>());
    this.fileSets = fileSets != null ? Collections.unmodifiableList(fileSets)
            : Collections.unmodifiableList(new ArrayList<FileSet>());

    // build list of filters
    List<IFilter> standardFilters = Arrays.asList(PluginFilters.getConfiguredFilters());
    this.filters = new ArrayList<>(standardFilters);

    if (filters != null) {
      // merge with filters configured for the project
      for (int i = 0, size = this.filters.size(); i < size; i++) {

        IFilter standardFilter = this.filters.get(i);

        for (int j = 0, size2 = filters.size(); j < size2; j++) {
          IFilter configuredFilter = filters.get(j);

          if (standardFilter.getInternalName().equals(configuredFilter.getInternalName())) {
            this.filters.set(i, configuredFilter);
          }
        }
      }
    }

    this.filters = Collections.unmodifiableList(this.filters);

    this.useSimpleConfig = useSimpleConfig;
    this.syncFormatter = synchFormatter;
  }

  @Override
  public IProject getProject() {
    return project;
  }

  @Override
  public List<ICheckConfiguration> getLocalCheckConfigurations() {
    return localCheckConfigs;
  }

  @Override
  public List<FileSet> getFileSets() {
    return fileSets;
  }

  @Override
  public List<IFilter> getFilters() {
    return filters;
  }

  @Override
  public boolean isUseSimpleConfig() {
    return useSimpleConfig;
  }

  @Override
  public boolean isSyncFormatter() {
    return syncFormatter;
  }

  /**
   * Checks if this project configuration uses the given checkstyle configuration.
   *
   * @param configuration
   *          the check configuration
   * @return <code>true</code>, if the project config uses the checkstyle config, <code>false</code>
   *         otherwise
   */
  @Override
  public boolean isConfigInUse(ICheckConfiguration configuration) {

    boolean result = false;

    for (FileSet fileSet : getFileSets()) {
      ICheckConfiguration checkConfig = fileSet.getCheckConfig();
      if (configuration.equals(checkConfig) || (checkConfig instanceof CheckConfigurationWorkingCopy
              && configuration.equals(((CheckConfigurationWorkingCopy) checkConfig)
                      .getSourceCheckConfiguration()))) {
        result = true;
        break;
      }
    }
    return result;
  }

  @Override
  public ProjectConfiguration clone() {
    ProjectConfiguration clone = null;
    try {
      clone = (ProjectConfiguration) super.clone();
      clone.fileSets = new LinkedList<>();
      clone.useSimpleConfig = useSimpleConfig;
      clone.syncFormatter = syncFormatter;

      // clone file sets
      List<FileSet> clonedFileSets = new ArrayList<>();
      for (FileSet fileSet : fileSets) {
        clonedFileSets.add(fileSet.clone());
      }
      clone.fileSets = clonedFileSets;

      // clone filters
      List<IFilter> clonedFilters = new ArrayList<>();
      for (IFilter filter : filters) {
        clonedFilters.add(filter.clone());
      }
      clone.filters = clonedFilters;
    } catch (CloneNotSupportedException ex) {
      // should never happen
      throw new InternalError(ex);
    }

    return clone;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null || !(obj instanceof ProjectConfiguration)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    ProjectConfiguration rhs = (ProjectConfiguration) obj;

    return Objects.equals(project, rhs.project)
            && Objects.equals(localCheckConfigs, rhs.localCheckConfigs)
            && Objects.equals(useSimpleConfig, rhs.useSimpleConfig)
            && Objects.equals(syncFormatter, rhs.syncFormatter)
            && Objects.equals(fileSets, rhs.fileSets) && Objects.equals(filters, rhs.filters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, localCheckConfigs, useSimpleConfig, syncFormatter, fileSets,
            filters);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("project", project)
            .add("localCheckConfigs", localCheckConfigs).add("useSimpleConfig", useSimpleConfig)
            .add("syncFormatter", syncFormatter).add("fileSets", fileSets)
            .add("filters", filters).toString();
  }
}
