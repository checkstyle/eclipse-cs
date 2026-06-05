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

package net.sf.eclipsecs.core.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.configtypes.BuiltInConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationType;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.ProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XmlUtil;

/**
 * Working set implementation that manages global configurations configured for the Eclipse
 * workspace.
 *
 */
public class GlobalCheckConfigurationWorkingSet implements CheckConfigurationWorkingSet {

  /** The internal list of working copies belonging to this working set. */
  private final List<CheckConfigurationWorkingCopy> mWorkingCopies;

  /** List of working copies that were deleted from the working set. */
  private final List<CheckConfigurationWorkingCopy> mDeletedConfigurations;

  /** The default check configuration to be used for unconfigured projects. */
  private CheckConfigurationWorkingCopy mDefaultCheckConfig;

  /**
   * The default built-in check configuration to be used for unconfigured projects.
   */
  private CheckConfiguration mDefaultBuiltInCheckConfig;

  /**
   * Creates a working set to manage global configurations.
   *
   * @param checkConfigs
   *          the list of global check configurations
   * @param defaultConfig
   *          the defaul check configuration
   */
  GlobalCheckConfigurationWorkingSet(List<CheckConfiguration> checkConfigs,
          CheckConfiguration defaultConfig, CheckConfiguration defaultBuiltInCheckConfiguration) {

    mWorkingCopies = new ArrayList<>();
    mDeletedConfigurations = new ArrayList<>();
    mDefaultBuiltInCheckConfig = defaultBuiltInCheckConfiguration;

    for (CheckConfiguration cfg : checkConfigs) {

      CheckConfigurationWorkingCopy workingCopy = new CheckConfigurationWorkingCopy(cfg, this);
      mWorkingCopies.add(workingCopy);

      if (cfg == defaultConfig) {
        mDefaultCheckConfig = workingCopy;
      }
    }
  }

  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(CheckConfiguration checkConfig) {
    return new CheckConfigurationWorkingCopy(checkConfig, this);
  }

  @Override
  public CheckConfigurationWorkingCopy newWorkingCopy(ConfigurationType configType) {
    return new CheckConfigurationWorkingCopy(configType, this, true);
  }

  @Override
  public CheckConfigurationWorkingCopy[] getWorkingCopies() {
    return mWorkingCopies.toArray(new CheckConfigurationWorkingCopy[mWorkingCopies.size()]);
  }

  @Override
  public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
    mWorkingCopies.add(checkConfig);
  }

  /**
   * Returns the default check configuration or <code>null</code> if none is set.
   *
   * @return the default check configuration
   */
  public CheckConfigurationWorkingCopy getDefaultCheckConfig() {
    return mDefaultCheckConfig;
  }

  /**
   * Sets the default check configuration.
   *
   * @param defaultCheckConfig
   *          the default check configuration
   */
  public void setDefaultCheckConfig(CheckConfigurationWorkingCopy defaultCheckConfig) {
    this.mDefaultCheckConfig = defaultCheckConfig;
  }

  @Override
  public boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
    boolean used = true;
    try {
      used = ProjectConfigurationFactory
              .isCheckConfigInUse(checkConfig.getSourceCheckConfiguration());

      if (!used) {
        mWorkingCopies.remove(checkConfig);

        // reset default check config
        if (mDefaultCheckConfig == checkConfig) {
          mDefaultCheckConfig = null;
        }

        mDeletedConfigurations.add(checkConfig);
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleLog.log(ex);
    }
    return !used;
  }

  @Override
  public void store() throws CheckstylePluginException {
    updateProjectConfigurations();
    storeToPersistence();
    notifyDeletedCheckConfigs();
    CheckConfigurationFactory.refresh();
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
  public Collection<IProject> getAffectedProjects() throws CheckstylePluginException {

    Set<IProject> projects = new HashSet<>();

    CheckConfigurationWorkingCopy[] workingCopies = this.getWorkingCopies();
    for (int i = 0; i < workingCopies.length; i++) {

      // skip non dirty configurations
      if (!workingCopies[i].hasConfigurationChanged()) {
        continue;
      }

      List<IProject> usingProjects = ProjectConfigurationFactory
              .getProjectsUsingConfig(workingCopies[i]);

      projects.addAll(usingProjects);
    }

    return projects;
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
   * Updates the project configurations that use the changed check configurations.
   *
   * @param configurations
   *          the check configurations
   * @throws CheckstylePluginException
   *           an unexpected exception occurred
   */
  private void updateProjectConfigurations() throws CheckstylePluginException {

    for (CheckConfigurationWorkingCopy checkConfig : mWorkingCopies) {

      CheckConfiguration original = checkConfig.getSourceCheckConfiguration();

      // only if the name of the check config differs from the original
      if (original != null && original.getName() != null
              && !checkConfig.getName().equals(original.getName())) {

        List<IProject> projects = ProjectConfigurationFactory.getProjectsUsingConfig(checkConfig);
        for (IProject project : projects) {

          ProjectConfiguration projectConfig = ProjectConfigurationFactory
                  .getConfiguration(project);

          ProjectConfigurationWorkingCopy workingCopy = new ProjectConfigurationWorkingCopy(
                  projectConfig);

          List<FileSet> fileSets = workingCopy.getFileSets();
          for (FileSet fileSet : fileSets) {

            // Check if the fileset uses the check config
            if (original.equals(fileSet.getCheckConfig())) {
              // set the new check configuration
              fileSet.setCheckConfig(checkConfig);
            }
          }

          // store the project configuration
          if (workingCopy.isDirty()) {
            workingCopy.store();
          }
        }
      }
    }
  }

  /**
   * Store the check configurations to the persistent state storage.
   */
  private void storeToPersistence() throws CheckstylePluginException {

    try {

      IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
      configPath = configPath.append(CheckConfigurationFactory.CHECKSTYLE_CONFIG_FILE);
      File configFile = configPath.toFile();

      CheckConfiguration defaultConfig = mDefaultCheckConfig;

      // don't store as default when it's already the built-in default
      if (defaultConfig != null
              && defaultConfig.getName().equals(mDefaultBuiltInCheckConfig.getName())) {
        defaultConfig = null;
      }

      Document doc = createCheckConfigurationsDocument(mWorkingCopies, defaultConfig);

      // write to the file after the document creation was successful
      // prevents corrupted files in case of error
      byte[] data = XmlUtil.toByteArray(doc);
      Files.write(configFile.toPath(), data);
    } catch (IOException ex) {
      CheckstylePluginException.rethrow(ex, Messages.errorWritingConfigFile);
    }
  }

  /**
   * Notifies the check configurations that have been deleted.
   *
   * @throws CheckstylePluginException
   *           an exception while notifying for deletion
   */
  private void notifyDeletedCheckConfigs() throws CheckstylePluginException {

    for (CheckConfiguration checkConfig : mDeletedConfigurations) {
      checkConfig.getType().notifyCheckConfigRemoved(checkConfig);
    }
  }

  /**
   * Transforms the check configurations to a document.
   */
  private static Document createCheckConfigurationsDocument(
          List<CheckConfigurationWorkingCopy> configurations, CheckConfiguration defaultConfig) {

    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(XmlTags.CHECKSTYLE_ROOT_TAG);
    root.addAttribute(XmlTags.VERSION_TAG,
            CheckConfigurationFactory.CURRENT_CONFIG_FILE_FORMAT_VERSION);

    if (defaultConfig != null) {
      root.addAttribute(XmlTags.DEFAULT_CHECK_CONFIG_TAG, defaultConfig.getName());
    }

    for (CheckConfiguration config : configurations) {

      // don't store built-in configurations to persistence or local
      // configurations
      if (config.getType() instanceof BuiltInConfigurationType || !config.isGlobal()) {
        continue;
      }

      CheckConfigurationXmlWriter.writeCheckConfiguration(root, config, config.getLocation(),
              XmlTags.CHECK_CONFIG_TAG);
    }
    return doc;
  }
}
