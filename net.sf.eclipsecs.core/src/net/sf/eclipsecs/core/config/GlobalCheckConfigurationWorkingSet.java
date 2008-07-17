//============================================================================
//
// Copyright (C) 2002-2008  David Schneider, Lars Ködderitzsch
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.configtypes.BuiltInConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Working set implementation that manages global configurations configured for
 * the Eclipse workspace.
 * 
 * @author Lars Ködderitzsch
 */
public class GlobalCheckConfigurationWorkingSet implements ICheckConfigurationWorkingSet {

    /** The internal list of working copies belonging to this working set. */
    private final List<CheckConfigurationWorkingCopy> mWorkingCopies;

    /** List of working copies that were deleted from the working set. */
    private final List<CheckConfigurationWorkingCopy> mDeletedConfigurations;

    /** The default check configuration to be used for unconfigured projects. */
    private CheckConfigurationWorkingCopy mDefaultCheckConfig;

    /**
     * Creates a working set to manage global configurations.
     * 
     * @param checkConfigs the list of global check configurations
     * @param defaultConfig the defaul check configuration
     */
    GlobalCheckConfigurationWorkingSet(List<ICheckConfiguration> checkConfigs,
            ICheckConfiguration defaultConfig) {

        mWorkingCopies = new ArrayList<CheckConfigurationWorkingCopy>();
        mDeletedConfigurations = new ArrayList<CheckConfigurationWorkingCopy>();

        for (ICheckConfiguration cfg : checkConfigs) {

            CheckConfigurationWorkingCopy workingCopy = new CheckConfigurationWorkingCopy(cfg, this);
            mWorkingCopies.add(workingCopy);

            if (cfg == defaultConfig) {
                mDefaultCheckConfig = workingCopy;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy newWorkingCopy(ICheckConfiguration checkConfig) {
        return new CheckConfigurationWorkingCopy(checkConfig, this);
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy newWorkingCopy(IConfigurationType configType) {
        return new CheckConfigurationWorkingCopy(configType, this, true);
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy[] getWorkingCopies() {
        return mWorkingCopies.toArray(new CheckConfigurationWorkingCopy[mWorkingCopies.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
        mWorkingCopies.add(checkConfig);
    }

    /**
     * Returns the default check configuration or <code>null</code> if none is
     * set.
     * 
     * @return the default check configuration
     */
    public CheckConfigurationWorkingCopy getDefaultCheckConfig() {
        return mDefaultCheckConfig;
    }

    /**
     * Sets the default check configuration.
     * 
     * @param defaultCheckConfig the default check configuration
     */
    public void setDefaultCheckConfig(CheckConfigurationWorkingCopy defaultCheckConfig) {
        this.mDefaultCheckConfig = defaultCheckConfig;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig) {
        boolean used = true;
        try {
            used = ProjectConfigurationFactory.isCheckConfigInUse(checkConfig
                    .getSourceCheckConfiguration());

            if (!used) {
                mWorkingCopies.remove(checkConfig);

                // reset default check config
                if (mDefaultCheckConfig == checkConfig) {
                    mDefaultCheckConfig = null;
                }

                mDeletedConfigurations.add(checkConfig);
            }
        }
        catch (CheckstylePluginException e) {
            CheckstyleLog.log(e);
        }
        return !used;
    }

    /**
     * {@inheritDoc}
     */
    public void store() throws CheckstylePluginException {
        updateProjectConfigurations();
        storeToPersistence();
        notifyDeletedCheckConfigs();
        CheckConfigurationFactory.refresh();
    }

    /**
     * {@inheritDoc}
     */
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
    public Collection<IProject> getAffectedProjects() throws CheckstylePluginException {

        Set<IProject> projects = new HashSet<IProject>();

        CheckConfigurationWorkingCopy[] workingCopies = this.getWorkingCopies();
        for (int i = 0; i < workingCopies.length; i++) {

            // skip non dirty configurations
            if (!workingCopies[i].hasConfigurationChanged()) {
                continue;
            }

            List<IProject> usingProjects = ProjectConfigurationFactory
                    .getProjectsUsingConfig(workingCopies[i]);

            for (IProject proj : usingProjects) {
                projects.add(proj);
            }
        }

        return projects;
    }

    /**
     * {@inheritDoc}
     */
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
     * Updates the project configurations that use the changed check
     * configurations.
     * 
     * @param configurations the check configurations
     * @throws CheckstylePluginException an unexpected exception occurred
     */
    private void updateProjectConfigurations() throws CheckstylePluginException {

        for (CheckConfigurationWorkingCopy checkConfig : mWorkingCopies) {

            ICheckConfiguration original = checkConfig.getSourceCheckConfiguration();

            // only if the name of the check config differs from the original
            if (original != null && original.getName() != null
                    && !checkConfig.getName().equals(original.getName())) {

                List<IProject> projects = ProjectConfigurationFactory
                        .getProjectsUsingConfig(checkConfig);
                for (IProject project : projects) {

                    IProjectConfiguration projectConfig = ProjectConfigurationFactory
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

        BufferedOutputStream out = null;

        try {

            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CheckConfigurationFactory.CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();

            Document doc = createCheckConfigurationsDocument(mWorkingCopies, mDefaultCheckConfig);

            // write to the file after the document creation was successful
            // prevents corrupted files in case of error
            out = new BufferedOutputStream(new FileOutputStream(configFile));
            out.write(XMLUtil.toByteArray(doc));
        }
        catch (IOException e) {
            CheckstylePluginException.rethrow(e, Messages.errorWritingConfigFile);
        }
        finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Notifies the check configurations that have been deleted.
     * 
     * @throws CheckstylePluginException an exception while notifiing for
     *             deletion
     */
    private void notifyDeletedCheckConfigs() throws CheckstylePluginException {

        for (ICheckConfiguration checkConfig : mDeletedConfigurations) {
            checkConfig.getType().notifyCheckConfigRemoved(checkConfig);
        }
    }

    /**
     * Transforms the check configurations to a document.
     */
    private static Document createCheckConfigurationsDocument(
            List<CheckConfigurationWorkingCopy> configurations,
            CheckConfigurationWorkingCopy defaultConfig) {

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(XMLTags.CHECKSTYLE_ROOT_TAG);
        root.addAttribute(XMLTags.VERSION_TAG,
                CheckConfigurationFactory.CURRENT_CONFIG_FILE_FORMAT_VERSION);

        if (defaultConfig != null) {
            root.addAttribute(XMLTags.DEFAULT_CHECK_CONFIG_TAG, defaultConfig.getName());
        }

        for (ICheckConfiguration config : configurations) {

            // don't store built-in configurations to persistence or local
            // configurations
            if (config.getType() instanceof BuiltInConfigurationType || !config.isGlobal()) {
                continue;
            }

            Element configEl = root.addElement(XMLTags.CHECK_CONFIG_TAG);
            configEl.addAttribute(XMLTags.NAME_TAG, config.getName());
            configEl.addAttribute(XMLTags.LOCATION_TAG, config.getLocation());
            configEl.addAttribute(XMLTags.TYPE_TAG, config.getType().getInternalName());
            if (config.getDescription() != null) {
                configEl.addAttribute(XMLTags.DESCRIPTION_TAG, config.getDescription());
            }

            // Write resolvable properties
            for (ResolvableProperty prop : config.getResolvableProperties()) {

                Element propEl = configEl.addElement(XMLTags.PROPERTY_TAG);
                propEl.addAttribute(XMLTags.NAME_TAG, prop.getPropertyName());
                propEl.addAttribute(XMLTags.VALUE_TAG, prop.getValue());
            }

            for (Map.Entry<String, String> entry : config.getAdditionalData().entrySet()) {

                Element addEl = configEl.addElement(XMLTags.ADDITIONAL_DATA_TAG);
                addEl.addAttribute(XMLTags.NAME_TAG, entry.getKey());
                addEl.addAttribute(XMLTags.VALUE_TAG, entry.getValue());
            }
        }
        return doc;
    }
}
