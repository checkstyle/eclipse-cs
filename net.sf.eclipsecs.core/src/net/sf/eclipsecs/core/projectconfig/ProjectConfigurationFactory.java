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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ResolvableProperty;
import net.sf.eclipsecs.core.config.configtypes.ConfigurationTypes;
import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.config.configtypes.ProjectConfigurationType;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Used to manage the life cycle of FileSet objects.
 */
public final class ProjectConfigurationFactory {

    /** The project configuration file name. */
    static final String PROJECT_CONFIGURATION_FILE = ".checkstyle";

    /** The current file format version. */
    static final String CURRENT_FILE_FORMAT_VERSION = "1.2.0";

    /** Constant list of supported file versions. */
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("1.0.0",
        "1.1.0", CURRENT_FILE_FORMAT_VERSION);

    private ProjectConfigurationFactory() {
    }

    /**
     * Creates a default project configuration for the given projects, using the default globbal
     * check configuration.
     *
     * @param project
     *            the project
     * @return the default project configuration
     * @throws RuntimeException
     *             an unexpected runtime exception occurred
     */
    public static IProjectConfiguration createDefaultProjectConfiguration(IProject project) {

        final FileSet standardFileSet = new FileSet(Messages.SimpleFileSetsEditor_nameAllFileset,
            CheckConfigurationFactory.getDefaultCheckConfiguration());
        try {
            standardFileSet.getFileMatchPatterns().add(new FileMatchPattern(".*"));
        }
        catch (CheckstylePluginException ex) {
            throw new RuntimeException(ex);
        }

        final List<FileSet> fileSets = Arrays.asList(standardFileSet);

        final IFilter[] filters = PluginFilters.getConfiguredFilters();
        final List<IFilter> defaultFilters = new ArrayList<>();
        for (IFilter filter : filters) {
            if (filter.isEnabled()) {
                defaultFilters.add(filter);
            }
        }

        return new ProjectConfiguration(project, null, fileSets, defaultFilters, true, false);
    }

    /**
     * Get the <code>ProjectConfiguration</code> object for the specified project.
     *
     * @param project
     *            The project to get <code>FileSet</code>'s for.
     * @return The <code>ProjectConfiguration</code> instance.
     * @throws CheckstylePluginException
     *             Error during processing.
     */
    public static IProjectConfiguration getConfiguration(IProject project)
            throws CheckstylePluginException {
        return loadFromPersistence(project);
    }

    /**
     * Check to see if a check configuration is currently in use by any projects.
     *
     * @param checkConfig
     *            The check configuration to check for.
     * @return <code>true</code>= in use, <code>false</code>= not in use.
     * @throws CheckstylePluginException
     *             Error during processing.
     */
    public static boolean isCheckConfigInUse(ICheckConfiguration checkConfig)
            throws CheckstylePluginException {
        return !getProjectsUsingConfig(checkConfig).isEmpty();
    }

    /**
     * Returns a list of projects using this check configuration.
     *
     * @param checkConfig
     *            the check configuration
     * @return the list of projects using this configuration
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    public static List<IProject> getProjectsUsingConfig(ICheckConfiguration checkConfig)
            throws CheckstylePluginException {

        final List<IProject> result = new ArrayList<>();

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            if (ProjectConfigurationFactory.getConfiguration(projects[i])
                .isConfigInUse(checkConfig)) {
                result.add(projects[i]);
            }
        }

        return result;
    }

    /**
     * Load the audit configurations from the persistent state storage.
     *
     * @param project
     *            the project
     * @return the project configuration
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private static IProjectConfiguration loadFromPersistence(IProject project)
            throws CheckstylePluginException {
        IProjectConfiguration configuration = null;

        //
        // Make sure the files exists, it might not.
        //
        final IFile file = project.getFile(PROJECT_CONFIGURATION_FILE);
        final boolean exists = file.exists();
        if (exists) {
            try (InputStream inStream = file.getContents(true)) {
                configuration = getProjectConfiguration(inStream, project);
            }
            catch (DocumentException | CoreException | IOException ex) {
                CheckstylePluginException.rethrow(ex);
            }
        }
        else {
            configuration = createDefaultProjectConfiguration(project);
        }
        return configuration;
    }

    private static IProjectConfiguration getProjectConfiguration(InputStream input,
        IProject project) throws DocumentException, CheckstylePluginException {

        final SAXReader reader = new SAXReader();
        final Document document = reader.read(input);

        final Element root = document.getRootElement();

        final String version = root.attributeValue(XMLTags.FORMAT_VERSION_TAG);
        if (!SUPPORTED_VERSIONS.contains(version)) {
            throw new CheckstylePluginException(NLS.bind(Messages.errorUnknownFileFormat, version));
        }

        final boolean useSimpleConfig =
            Boolean.parseBoolean(root.attributeValue(XMLTags.SIMPLE_CONFIG_TAG));
        final boolean syncFormatter =
            Boolean.parseBoolean(root.attributeValue(XMLTags.SYNC_FORMATTER_TAG));

        final List<ICheckConfiguration> checkConfigs = getLocalCheckConfigs(root, project);
        final List<FileSet> fileSets = getFileSets(root, checkConfigs);
        final List<IFilter> filters = getFilters(root);

        return new ProjectConfiguration(project, checkConfigs, fileSets, filters, useSimpleConfig,
            syncFormatter);
    }

    private static List<ICheckConfiguration> getLocalCheckConfigs(Element root, IProject project) {

        final List<ICheckConfiguration> configurations = new ArrayList<>();

        final List<Element> configElements = root.elements(XMLTags.CHECK_CONFIG_TAG);

        for (Element configEl : configElements) {

            final String name = configEl.attributeValue(XMLTags.NAME_TAG);
            final String description = configEl.attributeValue(XMLTags.DESCRIPTION_TAG);
            String location = configEl.attributeValue(XMLTags.LOCATION_TAG);

            final String type = configEl.attributeValue(XMLTags.TYPE_TAG);
            final IConfigurationType configType = ConfigurationTypes.getByInternalName(type);

            if (configType instanceof ProjectConfigurationType) {
                // RFE 1420212
                // treat config files relative to *THIS* project
                final IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
                // test if the location contains the project name
                if (workspaceRoot.findMember(location) == null) {
                    location = project.getFullPath().append(location).toString();
                }
            }

            // get resolvable properties
            final List<ResolvableProperty> props = new ArrayList<>();
            final List<Element> propertiesElements = configEl.elements(XMLTags.PROPERTY_TAG);
            for (Element propsEl : propertiesElements) {

                final ResolvableProperty prop =
                    new ResolvableProperty(propsEl.attributeValue(XMLTags.NAME_TAG),
                        propsEl.attributeValue(XMLTags.VALUE_TAG));
                props.add(prop);
            }

            // get additional data
            final Map<String, String> additionalData = new HashMap<>();
            final List<Element> dataElements = configEl.elements(XMLTags.ADDITIONAL_DATA_TAG);
            for (Element dataEl : dataElements) {

                additionalData.put(dataEl.attributeValue(XMLTags.NAME_TAG),
                    dataEl.attributeValue(XMLTags.VALUE_TAG));
            }

            final ICheckConfiguration checkConfig = new CheckConfiguration(name, location,
                description, configType, false, props, additionalData);
            configurations.add(checkConfig);
        }

        return configurations;
    }

    private static List<FileSet> getFileSets(Element root,
        List<ICheckConfiguration> localCheckConfigs) throws CheckstylePluginException {

        final List<FileSet> fileSets = new ArrayList<>();

        final List<Element> fileSetElements = root.elements(XMLTags.FILESET_TAG);
        for (Element fileSetEl : fileSetElements) {

            final boolean local = Boolean.parseBoolean(fileSetEl.attributeValue(XMLTags.LOCAL_TAG));

            final FileSet fileSet = new FileSet();
            fileSet.setName(fileSetEl.attributeValue(XMLTags.NAME_TAG));
            fileSet.setEnabled(Boolean.parseBoolean(fileSetEl.attributeValue(XMLTags.ENABLED_TAG)));

            // find the referenced check configuration
            ICheckConfiguration checkConfig = null;
            final String checkConfigName = fileSetEl.attributeValue(XMLTags.CHECK_CONFIG_NAME_TAG);
            if (local) {
                for (ICheckConfiguration tmp : localCheckConfigs) {
                    if (tmp.getName().equals(checkConfigName)) {
                        checkConfig = tmp;
                        break;
                    }
                }
            }
            else {
                checkConfig = CheckConfigurationFactory.getByName(checkConfigName);
            }

            fileSet.setCheckConfig(checkConfig);

            // get patterns
            final List<FileMatchPattern> patterns = new ArrayList<>();
            final List<Element> patternElements =
                fileSetEl.elements(XMLTags.FILE_MATCH_PATTERN_TAG);
            for (Element patternEl : patternElements) {
                final FileMatchPattern pattern =
                    new FileMatchPattern(patternEl.attributeValue(XMLTags.MATCH_PATTERN_TAG));
                pattern.setIsIncludePattern(
                    Boolean.parseBoolean(patternEl.attributeValue(XMLTags.INCLUDE_PATTERN_TAG)));
                patterns.add(pattern);
            }
            fileSet.setFileMatchPatterns(patterns);

            fileSets.add(fileSet);
        }

        return fileSets;
    }

    private static List<IFilter> getFilters(Element root) {

        final List<IFilter> filters = new ArrayList<>();

        final List<Element> filterElements = root.elements(XMLTags.FILTER_TAG);
        for (Element filterEl : filterElements) {

            final IFilter filter =
                PluginFilters.getByInternalName(filterEl.attributeValue(XMLTags.NAME_TAG));

            // guard against unknown/retired filters
            if (filter != null) {
                filter
                    .setEnabled(Boolean.parseBoolean(filterEl.attributeValue(XMLTags.ENABLED_TAG)));

                // get the filter data
                final List<String> filterData = new ArrayList<>();
                final List<Element> dataElements = filterEl.elements(XMLTags.FILTER_DATA_TAG);
                for (Element dataEl : dataElements) {
                    filterData.add(dataEl.attributeValue(XMLTags.VALUE_TAG));
                }
                filter.setFilterData(filterData);

                filters.add(filter);
            }
        }

        return filters;
    }
}
