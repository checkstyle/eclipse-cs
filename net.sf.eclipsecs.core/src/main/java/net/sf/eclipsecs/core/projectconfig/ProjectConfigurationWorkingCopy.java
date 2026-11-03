///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.projectconfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.CheckConfigurationXmlWriter;
import net.sf.eclipsecs.core.config.configtypes.BuiltInConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.ProjectConfigurationType;
import net.sf.eclipsecs.core.projectconfig.filters.AuditFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XmlUtil;

/**
 * A modifiable project configuration implementation.
 *
 */
public class ProjectConfigurationWorkingCopy implements ProjectConfiguration {

    /** The original, unmodified project configuration. */
    private final ProjectConfiguration projectConfig;

    /** The local check configurations. */
    private final CheckConfigurationWorkingSet localConfigWorkingSet;

    /** The global check configurations. */
    private final CheckConfigurationWorkingSet globalConfigWorkingSet;

    /** The file sets. */
    private List<FileSet> fileSets = new LinkedList<>();

    /** The filters. */
    private List<AuditFilter> filters = new LinkedList<>();

    /** Flags if the simple file set editor should be used. */
    private boolean useSimpleConfig;

    /** If the formatter synching feature is enabled. */
    private boolean syncFormatter;

    /**
     * Creates a working copy of a given project configuration.
     *
     * @param projectConfig
     *            the project configuration
     */
    public ProjectConfigurationWorkingCopy(ProjectConfiguration projectConfig) {
        this.projectConfig = projectConfig;

        localConfigWorkingSet = new LocalCheckConfigurationWorkingSet(this,
            projectConfig.getLocalCheckConfigurations());
        globalConfigWorkingSet = CheckConfigurationFactory.newWorkingSet();

        // clone file sets of the original config

        for (FileSet fileSet : projectConfig.getFileSets()) {
            fileSets.add(new FileSet(fileSet));
        }

        // build list of filters
        final List<AuditFilter> standardFilters =
            Arrays.asList(PluginFilters.getConfiguredFilters());
        filters = new ArrayList<>(standardFilters);

        // merge with filters configured for the project
        final List<AuditFilter> configuredFilters = projectConfig.getFilters();
        for (int index = 0, size = filters.size(); index < size; index++) {

            final AuditFilter standardFilter = filters.get(index);

            for (AuditFilter configuredFilter : configuredFilters) {
                if (standardFilter.getInternalName().equals(configuredFilter.getInternalName())) {
                    filters.set(index, configuredFilter.copy());
                }
            }
        }

        useSimpleConfig = projectConfig.isUseSimpleConfig();
        syncFormatter = projectConfig.isSyncFormatter();
    }

    /**
     * Returns the check configuration working set for local configurations.
     *
     * @return the local configurations working set
     */
    public CheckConfigurationWorkingSet getLocalCheckConfigWorkingSet() {
        return localConfigWorkingSet;
    }

    /**
     * Returns the check configuration working set for global configurations.
     *
     * @return the local configurations working set
     */
    public CheckConfigurationWorkingSet getGlobalCheckConfigWorkingSet() {
        return globalConfigWorkingSet;
    }

    /**
     * Returns a project local check configuration by its name.
     *
     * @param name
     *            the configurations name
     * @return the check configuration or <code>null</code>, if no local configuration with this
     *         name exists
     */
    public CheckConfiguration getLocalCheckConfigByName(String name) {
        CheckConfiguration config = null;
        final CheckConfiguration[] configs = localConfigWorkingSet.getWorkingCopies();
        for (CheckConfiguration conf : configs) {
            if (conf.getName().equals(name)) {
                config = conf;
                break;
            }
        }

        return config;
    }

    /**
     * Returns a project local check configuration by its name.
     *
     * @param name
     *            the configurations name
     * @return the check configuration or <code>null</code>, if no local configuration with this
     *         name exists
     */
    public CheckConfiguration getGlobalCheckConfigByName(String name) {
        CheckConfiguration config = null;
        final CheckConfiguration[] configs = globalConfigWorkingSet.getWorkingCopies();
        for (CheckConfiguration conf : configs) {
            if (conf.getName().equals(name)) {
                config = conf;
                break;
            }
        }

        return config;
    }

    /**
     * Sets if the simple configuration should be used.
     *
     * @param useSimpleConfig
     *            true if the project uses the simple fileset configuration
     */
    public void setUseSimpleConfig(boolean useSimpleConfig) {
        this.useSimpleConfig = useSimpleConfig;
    }

    /**
     * Sets if the formatter synching is enabled.
     *
     * @param syncFormatter
     *            true if the projects formatter settings should be synced with the Checkstyle
     *            config
     */
    public void setSyncFormatter(boolean syncFormatter) {
        this.syncFormatter = syncFormatter;
    }

    /**
     * Determines if the project configuration changed.
     *
     * @return <code>true</code> if changed
     */
    public boolean isDirty() {
        return !this.equals(projectConfig) || localConfigWorkingSet.isDirty();
    }

    /**
     * Determines if a rebuild is needed for the project of this project configuration. A rebuild is
     * not needed when only some local config was added which is not used by the project.
     *
     * @return <code>true</code> if rebuild is needed.
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    public boolean isRebuildNeeded() throws CheckstylePluginException {
        return !this.equals(projectConfig)
            || localConfigWorkingSet.getAffectedProjects().contains(getProject())
            || globalConfigWorkingSet.getAffectedProjects().contains(getProject());
    }

    /**
     * Stores the project configuration.
     *
     * @throws CheckstylePluginException
     *             error while storing the project configuration
     */
    public void store() throws CheckstylePluginException {
        storeToPersistence(this);
    }

    @Override
    public IProject getProject() {
        return projectConfig.getProject();
    }

    @Override
    public List<CheckConfiguration> getLocalCheckConfigurations() {

        final List<CheckConfiguration> list = new ArrayList<>();
        Collections.addAll(list, localConfigWorkingSet.getWorkingCopies());

        return list;
    }

    @Override
    public List<FileSet> getFileSets() {
        return fileSets;
    }

    @Override
    public List<AuditFilter> getFilters() {
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

    @Override
    public boolean isConfigInUse(CheckConfiguration configuration) {

        boolean result = false;

        for (FileSet fileSet : getFileSets()) {
            final CheckConfiguration checkConfig = fileSet.getCheckConfig();
            if (configuration.equals(checkConfig)
                || checkConfig instanceof CheckConfigurationWorkingCopy && configuration.equals(
                    ((CheckConfigurationWorkingCopy) checkConfig).getSourceCheckConfiguration())) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof ProjectConfiguration)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final ProjectConfiguration rhs = (ProjectConfiguration) obj;
        return Objects.equals(getProject(), rhs.getProject())
            && isUseSimpleConfig() == rhs.isUseSimpleConfig()
            && isSyncFormatter() == rhs.isSyncFormatter()
            && Objects.equals(getFileSets(), rhs.getFileSets())
            && Objects.equals(getFilters(), rhs.getFilters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectConfig, useSimpleConfig, fileSets, filters);
    }

    /**
     * Store the audit configurations to the persistent state storage.
     *
     * @param config
     *            the project configuration to persist
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private void storeToPersistence(ProjectConfigurationWorkingCopy config)
            throws CheckstylePluginException {

        try {

            final Document docu = writeProjectConfig(config);
            final byte[] data = XmlUtil.toByteArray(docu);
            final InputStream pipeIn = new ByteArrayInputStream(data);

            // create or overwrite the .checkstyle file
            final IProject project = config.getProject();
            final IFile file =
                project.getFile(ProjectConfigurationFactory.PROJECT_CONFIGURATION_FILE);
            if (file.exists()) {
                if (file.isReadOnly()) {
                    final ResourceAttributes attrs =
                        ResourceAttributes.fromFile(file.getFullPath().toFile());
                    attrs.setReadOnly(true);
                    file.setResourceAttributes(attrs);
                }

                file.setContents(pipeIn, true, true, null);
            }
            else {
                file.create(pipeIn, true, null);
                file.refreshLocal(IResource.DEPTH_INFINITE, null);
            }

            config.getLocalCheckConfigWorkingSet().store();
        }
        catch (CheckstylePluginException | IOException | CoreException exception) {
            CheckstylePluginException.rethrow(exception,
                NLS.bind(Messages.errorWritingCheckConfigurations,
                    exception.getLocalizedMessage()));
        }
    }

    /**
     * Produces the sax events to write a project configuration.
     *
     * @param config
     *            the configuration
     * @return the XML document representing the project configuration
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private Document writeProjectConfig(ProjectConfigurationWorkingCopy config)
            throws CheckstylePluginException {

        final Document doc = DocumentHelper.createDocument();

        final Element root = doc.addElement(XmlTags.FILESET_CONFIG_TAG);
        root.addAttribute(XmlTags.FORMAT_VERSION_TAG,
            ProjectConfigurationFactory.CURRENT_FILE_FORMAT_VERSION);
        root.addAttribute(XmlTags.SIMPLE_CONFIG_TAG, Boolean.toString(config.isUseSimpleConfig()));
        root.addAttribute(XmlTags.SYNC_FORMATTER_TAG, Boolean.toString(config.isSyncFormatter()));

        final CheckConfiguration[] workingCopies =
            config.getLocalCheckConfigWorkingSet().getWorkingCopies();
        for (CheckConfiguration workingCopy : workingCopies) {
            writeLocalConfiguration(workingCopy, root);
        }

        for (FileSet fileSet : config.getFileSets()) {
            writeFileSet(fileSet, config.getProject(), root);
        }

        // write filters
        for (AuditFilter filter : config.getFilters()) {
            writeFilter(filter, root);
        }

        return doc;
    }

    /**
     * Writes a local check configuration.
     *
     * @param checkConfig
     *            the local check configuration
     * @param docRoot
     *            the root element of the project configuration
     */
    private void writeLocalConfiguration(CheckConfiguration checkConfig, Element docRoot) {

        // don't store built-in configurations to persistence or local
        // configurations
        if (!(checkConfig.getType() instanceof BuiltInConfigurationType)
            && !checkConfig.isGlobal()) {
            // RFE 1420212
            String location = checkConfig.getLocation();
            if (checkConfig.getType() instanceof ProjectConfigurationType) {
                final IProject project = projectConfig.getProject();
                final IWorkspaceRoot root = project.getWorkspace().getRoot();
                final IFile configFile = root.getFile(new Path(location));
                final IProject configFileProject = configFile.getProject();

                // if the configuration is in *same* project don't store project
                // path part
                if (project.equals(configFileProject)) {
                    location = configFile.getProjectRelativePath().toString();
                }
            }

            CheckConfigurationXmlWriter.writeCheckConfiguration(docRoot, checkConfig, location,
                XmlTags.CHECK_CONFIG_TAG);
        }
    }

    /**
     * Produces the sax events to write a file set to xml.
     *
     * @param fileSet
     *            the file set
     * @param project
     *            the project
     * @param docRoot
     *            the root element of the project configuration
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private void writeFileSet(FileSet fileSet, IProject project, Element docRoot)
            throws CheckstylePluginException {

        if (fileSet.getCheckConfig() == null) {
            throw new CheckstylePluginException(NLS.bind(Messages.errorFilesetWithoutCheckConfig,
                fileSet.getName(), project.getName()));
        }

        final Element fileSetEl = docRoot.addElement(XmlTags.FILESET_TAG);
        fileSetEl.addAttribute(XmlTags.NAME_TAG, fileSet.getName());
        fileSetEl.addAttribute(XmlTags.ENABLED_TAG, Boolean.toString(fileSet.isEnabled()));

        final CheckConfiguration checkConfig = fileSet.getCheckConfig();
        if (checkConfig != null) {

            fileSetEl.addAttribute(XmlTags.CHECK_CONFIG_NAME_TAG, checkConfig.getName());
            fileSetEl.addAttribute(XmlTags.LOCAL_TAG, Boolean.toString(!checkConfig.isGlobal()));
        }

        // write patterns
        for (FileMatchPattern pattern : fileSet.getFileMatchPatterns()) {

            final Element patternEl = fileSetEl.addElement(XmlTags.FILE_MATCH_PATTERN_TAG);
            final String matchPattern;
            if (pattern.getMatchPattern() != null) {
                matchPattern = pattern.getMatchPattern();
            }
            else {
                matchPattern = "";
            }
            patternEl.addAttribute(XmlTags.MATCH_PATTERN_TAG, matchPattern);
            patternEl.addAttribute(XmlTags.INCLUDE_PATTERN_TAG,
                Boolean.toString(pattern.isIncludePattern()));
        }
    }

    /**
     * Produces the sax events to write a filter to xml.
     *
     * @param filter
     *            the filter
     * @param docRoot
     *            the root element of the project configuration
     */
    private void writeFilter(AuditFilter filter, Element docRoot) {
        // write only filters that are actually changed
        // (enabled or contain data)
        final AuditFilter prototype = PluginFilters.getByInternalName(filter.getInternalName());
        if (!prototype.equals(filter)) {
            final Element filterEl = docRoot.addElement(XmlTags.FILTER_TAG);
            filterEl.addAttribute(XmlTags.NAME_TAG, filter.getInternalName());
            filterEl.addAttribute(XmlTags.ENABLED_TAG, Boolean.toString(filter.isEnabled()));

            final List<String> data = filter.getFilterData();
            for (String item : data) {
                final Element dataEl = filterEl.addElement(XmlTags.FILTER_DATA_TAG);
                dataEl.addAttribute(XmlTags.VALUE_TAG, item);
            }
        }
    }

}
