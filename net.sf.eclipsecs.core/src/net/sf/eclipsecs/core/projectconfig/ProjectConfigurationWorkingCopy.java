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

package net.sf.eclipsecs.core.projectconfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.core.config.configtypes.BuiltInConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.ProjectConfigurationType;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

/**
 * A modifiable project configuration implementation.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfigurationWorkingCopy implements Cloneable, IProjectConfiguration {

    //
    // attributes
    //

    /** The original, unmodified project configuration. */
    private final IProjectConfiguration mProjectConfig;

    /** The local check configurations. */
    private final ICheckConfigurationWorkingSet mLocalConfigWorkingSet;

    /** The global check configurations. */
    private final ICheckConfigurationWorkingSet mGlobalConfigWorkingSet;

    /** the file sets. */
    private List<FileSet> mFileSets = new LinkedList<FileSet>();

    /** the filters. */
    private List<IFilter> mFilters = new LinkedList<IFilter>();

    /** Flags if the simple file set editor should be used. */
    private boolean mUseSimpleConfig;

    //
    // constructors
    //

    /**
     * Creates a working copy of a given project configuration.
     * 
     * @param projectConfig the project configuration
     */
    public ProjectConfigurationWorkingCopy(IProjectConfiguration projectConfig) {

        mProjectConfig = projectConfig;

        mLocalConfigWorkingSet = new LocalCheckConfigurationWorkingSet(this, projectConfig
                .getLocalCheckConfigurations());
        mGlobalConfigWorkingSet = CheckConfigurationFactory.newWorkingSet();

        // clone file sets of the original config

        for (FileSet fileSet : projectConfig.getFileSets()) {
            mFileSets.add(fileSet.clone());
        }

        // build list of filters
        List<IFilter> standardFilters = Arrays.asList(PluginFilters.getConfiguredFilters());
        mFilters = new ArrayList<IFilter>(standardFilters);

        // merge with filters configured for the project
        List<IFilter> configuredFilters = projectConfig.getFilters();
        for (int i = 0, size = mFilters.size(); i < size; i++) {

            IFilter standardFilter = mFilters.get(i);

            for (int j = 0, size2 = configuredFilters.size(); j < size2; j++) {
                IFilter configuredFilter = configuredFilters.get(j);

                if (standardFilter.getInternalName().equals(configuredFilter.getInternalName())) {
                    mFilters.set(i, configuredFilter.clone());
                }
            }
        }

        mUseSimpleConfig = projectConfig.isUseSimpleConfig();
    }

    //
    // methods
    //

    /**
     * Returns the check configuration working set for local configurations.
     * 
     * @return the local configurations working set
     */
    public ICheckConfigurationWorkingSet getLocalCheckConfigWorkingSet() {
        return mLocalConfigWorkingSet;
    }

    /**
     * Returns the check configuration working set for global configurations.
     * 
     * @return the local configurations working set
     */
    public ICheckConfigurationWorkingSet getGlobalCheckConfigWorkingSet() {
        return mGlobalConfigWorkingSet;
    }

    /**
     * Returns a project local check configuration by its name.
     * 
     * @param name the configurations name
     * @return the check configuration or <code>null</code>, if no local
     *         configuration with this name exists
     */
    public ICheckConfiguration getLocalCheckConfigByName(String name) {
        ICheckConfiguration config = null;
        ICheckConfiguration[] configs = mLocalConfigWorkingSet.getWorkingCopies();
        for (int i = 0; i < configs.length; i++) {
            if (configs[i].getName().equals(name)) {
                config = configs[i];
                break;
            }
        }

        return config;
    }

    /**
     * Returns a project local check configuration by its name.
     * 
     * @param name the configurations name
     * @return the check configuration or <code>null</code>, if no local
     *         configuration with this name exists
     */
    public ICheckConfiguration getGlobalCheckConfigByName(String name) {
        ICheckConfiguration config = null;
        ICheckConfiguration[] configs = mGlobalConfigWorkingSet.getWorkingCopies();
        for (int i = 0; i < configs.length; i++) {
            if (configs[i].getName().equals(name)) {
                config = configs[i];
                break;
            }
        }

        return config;
    }

    /**
     * Sets if the simple configuration should be used.
     * 
     * @param useSimpleConfig true if the project uses the simple fileset
     *            configuration
     */
    public void setUseSimpleConfig(boolean useSimpleConfig) {
        mUseSimpleConfig = useSimpleConfig;
    }

    /**
     * Determines if the project configuration changed.
     * 
     * @return <code>true</code> if changed
     */
    public boolean isDirty() {
        return !this.equals(mProjectConfig) || mLocalConfigWorkingSet.isDirty();
    }

    /**
     * Determines if a rebuild is needed for the project of this project
     * configuration. A rebuild is not needed when only some local config was
     * added which is not used by the project.
     * 
     * @return <code>true</code> if rebuild is needed.
     * @throws CheckstylePluginException an unexpected exception occurred
     */
    public boolean isRebuildNeeded() throws CheckstylePluginException {
        return !this.equals(mProjectConfig)
                || mLocalConfigWorkingSet.getAffectedProjects().contains(getProject())
                || mGlobalConfigWorkingSet.getAffectedProjects().contains(getProject());
    }

    /**
     * Stores the project configuration.
     * 
     * @throws CheckstylePluginException error while storing the project
     *             configuration
     */
    public void store() throws CheckstylePluginException {
        storeToPersistence(this);
    }

    //
    // implementation of IProjectConfiguration interface
    //

    /**
     * {@inheritDoc}
     */
    public IProject getProject() {
        return mProjectConfig.getProject();
    }

    /**
     * {@inheritDoc}
     */
    public List<CheckConfigurationWorkingCopy> getLocalCheckConfigurations() {
        return Arrays.asList(mLocalConfigWorkingSet.getWorkingCopies());
    }

    /**
     * {@inheritDoc}
     */
    public List<FileSet> getFileSets() {
        return mFileSets;
    }

    /**
     * {@inheritDoc}
     */
    public List<IFilter> getFilters() {
        return mFilters;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUseSimpleConfig() {
        return mUseSimpleConfig;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigInUse(ICheckConfiguration configuration) {

        boolean result = false;

        for (FileSet fileSet : getFileSets()) {
            ICheckConfiguration checkConfig = fileSet.getCheckConfig();
            if (configuration.equals(checkConfig)
                    || (checkConfig instanceof CheckConfigurationWorkingCopy && configuration
                            .equals(((CheckConfigurationWorkingCopy) checkConfig)
                                    .getSourceCheckConfiguration()))) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone() {
        ProjectConfigurationWorkingCopy clone = null;
        try {
            clone = (ProjectConfigurationWorkingCopy) super.clone();
            clone.mFileSets = new LinkedList<FileSet>();
            clone.setUseSimpleConfig(this.isUseSimpleConfig());

            // clone file sets
            for (FileSet fileSet : getFileSets()) {
                clone.getFileSets().add(fileSet.clone());
            }

            // clone filters
            List<IFilter> clonedFilters = new ArrayList<IFilter>();
            for (IFilter filter : getFilters()) {
                clonedFilters.add(filter.clone());
            }
            clone.mFilters = clonedFilters;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof IProjectConfiguration)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        IProjectConfiguration rhs = (IProjectConfiguration) obj;
        return new EqualsBuilder().append(getProject(), rhs.getProject()).append(
                isUseSimpleConfig(), rhs.isUseSimpleConfig()).append(getFileSets(),
                rhs.getFileSets()).append(getFilters(), rhs.getFilters()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(984759323, 1000003).append(mProjectConfig).append(
                mUseSimpleConfig).append(mFileSets).append(mFilters).toHashCode();
    }

    /**
     * Store the audit configurations to the persistent state storage.
     */
    private void storeToPersistence(ProjectConfigurationWorkingCopy config)
        throws CheckstylePluginException {

        ByteArrayOutputStream pipeOut = null;
        InputStream pipeIn = null;
        try {

            pipeOut = new ByteArrayOutputStream();

            Document docu = writeProjectConfig(config);
            pipeIn = new ByteArrayInputStream(XMLUtil.toByteArray(docu));

            // create or overwrite the .checkstyle file
            IProject project = config.getProject();
            IFile file = project.getFile(ProjectConfigurationFactory.PROJECT_CONFIGURATION_FILE);
            if (!file.exists()) {
                file.create(pipeIn, true, null);
                file.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
            else {
                file.setContents(pipeIn, true, true, null);
            }

            config.getLocalCheckConfigWorkingSet().store();
        }
        catch (Exception e) {
            CheckstylePluginException.rethrow(e, NLS.bind(Messages.errorWritingCheckConfigurations,
                    e.getLocalizedMessage()));
        }
        finally {
            IOUtils.closeQuietly(pipeIn);
            IOUtils.closeQuietly(pipeOut);
        }
    }

    /**
     * Produces the sax events to write a project configuration.
     * 
     * @param config the configuration
     */
    private Document writeProjectConfig(ProjectConfigurationWorkingCopy config)
        throws CheckstylePluginException {

        Document doc = DocumentHelper.createDocument();

        Element root = doc.addElement(XMLTags.FILESET_CONFIG_TAG);
        root.addAttribute(XMLTags.FORMAT_VERSION_TAG,
                ProjectConfigurationFactory.CURRENT_FILE_FORMAT_VERSION);
        root.addAttribute(XMLTags.SIMPLE_CONFIG_TAG, Boolean.toString(config.isUseSimpleConfig()));

        ICheckConfiguration[] workingCopies = config.getLocalCheckConfigWorkingSet()
                .getWorkingCopies();
        for (int i = 0; i < workingCopies.length; i++) {
            writeLocalConfiguration(workingCopies[i], root);
        }

        for (FileSet fileSet : config.getFileSets()) {
            writeFileSet(fileSet, config.getProject(), root);
        }

        // write filters
        for (IFilter filter : config.getFilters()) {
            writeFilter(filter, root);
        }

        return doc;
    }

    /**
     * Writes a local check configuration.
     * 
     * @param checkConfig the local check configuration
     * @param docRoot the root element of the project configuration
     */
    private void writeLocalConfiguration(ICheckConfiguration checkConfig, Element docRoot) {

        // TODO refactor to avoid code duplication with
        // GlobalCheckConfigurationWorkingSet

        // don't store built-in configurations to persistence or local
        // configurations
        if (checkConfig.getType() instanceof BuiltInConfigurationType || checkConfig.isGlobal()) {
            return;
        }

        // RFE 1420212
        String location = checkConfig.getLocation();
        if (checkConfig.getType() instanceof ProjectConfigurationType) {
            IProject project = mProjectConfig.getProject();
            IWorkspaceRoot root = project.getWorkspace().getRoot();
            IFile configFile = root.getFile(new Path(location));
            IProject configFileProject = configFile.getProject();

            // if the configuration is in *same* project don't store project
            // path part
            if (project.equals(configFileProject)) {
                location = configFile.getProjectRelativePath().toString();
            }
        }

        Element configEl = docRoot.addElement(XMLTags.CHECK_CONFIG_TAG);
        configEl.addAttribute(XMLTags.NAME_TAG, checkConfig.getName());
        configEl.addAttribute(XMLTags.LOCATION_TAG, location);
        configEl.addAttribute(XMLTags.TYPE_TAG, checkConfig.getType().getInternalName());
        if (checkConfig.getDescription() != null) {
            configEl.addAttribute(XMLTags.DESCRIPTION_TAG, checkConfig.getDescription());
        }

        // Write resolvable properties
        for (ResolvableProperty prop : checkConfig.getResolvableProperties()) {

            Element propEl = configEl.addElement(XMLTags.PROPERTY_TAG);
            propEl.addAttribute(XMLTags.NAME_TAG, prop.getPropertyName());
            propEl.addAttribute(XMLTags.VALUE_TAG, prop.getValue());
        }

        // Write additional data
        for (Map.Entry<String, String> entry : checkConfig.getAdditionalData().entrySet()) {

            Element addEl = configEl.addElement(XMLTags.ADDITIONAL_DATA_TAG);
            addEl.addAttribute(XMLTags.NAME_TAG, entry.getKey());
            addEl.addAttribute(XMLTags.VALUE_TAG, entry.getValue());
        }
    }

    /**
     * Produces the sax events to write a file set to xml.
     * 
     * @param fileSet the file set
     * @param project the project
     * @param docRoot the root element of the project configuration
     */
    private void writeFileSet(FileSet fileSet, IProject project, Element docRoot)
        throws CheckstylePluginException {

        if (fileSet.getCheckConfig() == null) {
            throw new CheckstylePluginException(NLS.bind(Messages.errorFilesetWithoutCheckConfig,
                    fileSet.getName(), project.getName()));
        }

        Element fileSetEl = docRoot.addElement(XMLTags.FILESET_TAG);
        fileSetEl.addAttribute(XMLTags.NAME_TAG, fileSet.getName());
        fileSetEl.addAttribute(XMLTags.ENABLED_TAG, Boolean.toString(fileSet.isEnabled()));

        ICheckConfiguration checkConfig = fileSet.getCheckConfig();
        if (checkConfig != null) {

            fileSetEl.addAttribute(XMLTags.CHECK_CONFIG_NAME_TAG, checkConfig.getName());
            fileSetEl.addAttribute(XMLTags.LOCAL_TAG, Boolean.toString(!checkConfig.isGlobal()));
        }

        // write patterns
        for (FileMatchPattern pattern : fileSet.getFileMatchPatterns()) {

            Element patternEl = fileSetEl.addElement(XMLTags.FILE_MATCH_PATTERN_TAG);
            patternEl.addAttribute(XMLTags.MATCH_PATTERN_TAG,
                    pattern.getMatchPattern() != null ? pattern.getMatchPattern() : "");
            patternEl.addAttribute(XMLTags.INCLUDE_PATTERN_TAG, Boolean.toString(pattern
                    .isIncludePattern()));
        }
    }

    /**
     * Produces the sax events to write a filter to xml.
     * 
     * @param filter the filter
     * @param docRoot the root element of the project configuration
     */
    private void writeFilter(IFilter filter, Element docRoot) {

        // write only filters that are actually changed
        // (enabled or contain data)
        IFilter prototype = PluginFilters.getByInternalName(filter.getInternalName());
        if (prototype.equals(filter)) {
            return;
        }

        Element filterEl = docRoot.addElement(XMLTags.FILTER_TAG);
        filterEl.addAttribute(XMLTags.NAME_TAG, filter.getInternalName());
        filterEl.addAttribute(XMLTags.ENABLED_TAG, Boolean.toString(filter.isEnabled()));

        List<String> data = filter.getFilterData();
        if (data != null) {
            for (String item : data) {

                Element dataEl = filterEl.addElement(XMLTags.FILTER_DATA_TAG);
                dataEl.addAttribute(XMLTags.VALUE_TAG, item);
            }
        }
    }
}