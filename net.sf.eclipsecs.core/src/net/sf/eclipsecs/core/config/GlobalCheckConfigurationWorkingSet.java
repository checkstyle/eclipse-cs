//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.sax.TransformerHandler;

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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
        ByteArrayOutputStream byteOut = null;
        try {

            IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
            configPath = configPath.append(CheckConfigurationFactory.CHECKSTYLE_CONFIG_FILE);
            File configFile = configPath.toFile();

            byteOut = new ByteArrayOutputStream();

            // Write the configuration document by pushing sax events through
            // the transformer handler
            TransformerHandler xmlOut = XMLUtil.writeWithSax(byteOut, null, null);

            writeConfigurations(xmlOut, mWorkingCopies, mDefaultCheckConfig);

            // write to the file after the serialization was successful
            // prevents corrupted files in case of error
            out = new BufferedOutputStream(new FileOutputStream(configFile));
            out.write(byteOut.toByteArray());
        }
        catch (Exception e) {
            CheckstylePluginException.rethrow(e, Messages.errorWritingConfigFile);
        }
        finally {
            IOUtils.closeQuietly(byteOut);
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
     * Writes to check configurations through the transformer handler by passing
     * SAX events to it.
     * 
     * @param handler the transformer handler
     * @throws SAXException error writing the configurations
     */
    private static void writeConfigurations(TransformerHandler handler,
            List<CheckConfigurationWorkingCopy> configurations,
            CheckConfigurationWorkingCopy defaultConfig) throws SAXException {

        String emptyString = new String();

        handler.startDocument();
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(emptyString, XMLTags.VERSION_TAG, XMLTags.VERSION_TAG, emptyString,
                CheckConfigurationFactory.CURRENT_CONFIG_FILE_FORMAT_VERSION);
        if (defaultConfig != null) {
            attrs.addAttribute(emptyString, XMLTags.DEFAULT_CHECK_CONFIG_TAG,
                    XMLTags.DEFAULT_CHECK_CONFIG_TAG, emptyString, defaultConfig.getName());
        }

        handler.startElement(emptyString, XMLTags.CHECKSTYLE_ROOT_TAG, XMLTags.CHECKSTYLE_ROOT_TAG,
                attrs);

        for (ICheckConfiguration config : configurations) {

            // don't store built-in configurations to persistence or local
            // configurations
            if (config.getType() instanceof BuiltInConfigurationType || !config.isGlobal()) {
                continue;
            }

            attrs = new AttributesImpl();
            attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, config
                    .getName());
            attrs.addAttribute(emptyString, XMLTags.LOCATION_TAG, XMLTags.LOCATION_TAG,
                    emptyString, config.getLocation());
            attrs.addAttribute(emptyString, XMLTags.TYPE_TAG, XMLTags.TYPE_TAG, emptyString, config
                    .getType().getInternalName());
            if (config.getDescription() != null) {
                attrs.addAttribute(emptyString, XMLTags.DESCRIPTION_TAG, XMLTags.DESCRIPTION_TAG,
                        emptyString, config.getDescription());
            }

            handler.startElement(emptyString, XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG,
                    attrs);

            // Write resolvable properties
            for (ResolvableProperty prop : config.getResolvableProperties()) {

                attrs = new AttributesImpl();
                attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                        prop.getPropertyName());
                attrs.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                        prop.getValue());

                handler
                        .startElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG,
                                attrs);
                handler.endElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
            }

            // Additional data
            for (Map.Entry<String, String> entry : config.getAdditionalData().entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                attrs = new AttributesImpl();
                attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                        key);
                attrs.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                        value);

                handler.startElement(emptyString, XMLTags.ADDITIONAL_DATA_TAG,
                        XMLTags.ADDITIONAL_DATA_TAG, attrs);
                handler.endElement(emptyString, XMLTags.ADDITIONAL_DATA_TAG,
                        XMLTags.ADDITIONAL_DATA_TAG);
            }

            handler.endElement(emptyString, XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG);
        }

        handler.endElement(emptyString, XMLTags.CHECKSTYLE_ROOT_TAG, XMLTags.CHECKSTYLE_ROOT_TAG);
        handler.endDocument();
    }
}
