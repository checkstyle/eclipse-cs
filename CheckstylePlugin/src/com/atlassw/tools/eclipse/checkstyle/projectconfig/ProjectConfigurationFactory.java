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

package com.atlassw.tools.eclipse.checkstyle.projectconfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ProjectConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

/**
 * Used to manage the life cycle of FileSet objects.
 */
public final class ProjectConfigurationFactory
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    static final String PROJECT_CONFIGURATION_FILE = ".checkstyle"; //$NON-NLS-1$

    static final String CURRENT_FILE_FORMAT_VERSION = "1.2.0"; //$NON-NLS-1$

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    private ProjectConfigurationFactory()
    {}

    // =================================================
    // Methods.
    // =================================================

    /**
     * Get the <code>ProjectConfiguration</code> object for the specified
     * project.
     * 
     * @param project The project to get <code>FileSet</code>'s for.
     * @return The <code>ProjectConfiguration</code> instance.
     * @throws CheckstylePluginException Error during processing.
     */
    public static IProjectConfiguration getConfiguration(IProject project)
        throws CheckstylePluginException
    {
        return loadFromPersistence(project);
    }

    /**
     * Check to see if a check configuration is currently in use by any
     * projects.
     * 
     * @param checkConfig The check configuration to check for.
     * @return <code>true</code>= in use, <code>false</code>= not in use.
     * @throws CheckstylePluginException Error during processing.
     */
    public static boolean isCheckConfigInUse(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {
        return getProjectsUsingConfig(checkConfig).size() > 0;
    }

    /**
     * Returns a list of projects using this check configuration.
     * 
     * @param checkConfig the check configuration
     * @return the list of projects using this configuration
     * @throws CheckstylePluginException an unexpected exception occurred
     */
    public static List getProjectsUsingConfig(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {

        List result = new ArrayList();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; (i < projects.length); i++)
        {
            if (ProjectConfigurationFactory.getConfiguration(projects[i])
                    .isConfigInUse(checkConfig))
            {
                result.add(projects[i]);
            }
        }

        return result;
    }

    /**
     * Load the audit configurations from the persistent state storage.
     */
    private static IProjectConfiguration loadFromPersistence(IProject project)
        throws CheckstylePluginException
    {
        IProjectConfiguration configuration = null;

        //
        // Make sure the files exists, it might not.
        //
        IFile file = project.getFile(PROJECT_CONFIGURATION_FILE);
        boolean exists = file.exists();
        if (!exists)
        {

            FileSet standardFileSet = new FileSet(Messages.SimpleFileSetsEditor_nameAllFileset,
                    CheckConfigurationFactory.getDefaultCheckConfiguration());
            standardFileSet.getFileMatchPatterns().add(new FileMatchPattern(".*"));

            List fileSets = Arrays.asList(new Object[] { standardFileSet });
            return new ProjectConfiguration(project, null, fileSets, null, true);
        }

        InputStream inStream = null;
        try
        {
            inStream = file.getContents(true);

            ProjectConfigFileHandler handler = new ProjectConfigFileHandler(project);
            XMLUtil.parseWithSAX(inStream, handler);

            configuration = handler.getConfiguration();
        }
        catch (CoreException ce)
        {
            CheckstylePluginException.rethrow(ce);
        }
        catch (SAXException se)
        {
            Exception ex = se.getException() != null ? se.getException() : se;
            CheckstylePluginException.rethrow(ex);
        }
        catch (ParserConfigurationException pe)
        {
            CheckstylePluginException.rethrow(pe);
        }
        catch (IOException ioe)
        {
            CheckstylePluginException.rethrow(ioe);
        }

        finally
        {
            IOUtils.closeQuietly(inStream);
        }

        return configuration;
    }

    /**
     * Sax-Handler for parsing the checkstyle plugin project configuration file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class ProjectConfigFileHandler extends DefaultHandler
    {

        //
        // constants
        //

        /** constant list of supported file versions. */
        private static final List SUPPORTED_VERSIONS = Arrays.asList(new String[] {
            "1.0.0", "1.1.0", //$NON-NLS-1$ //$NON-NLS-2$
            CURRENT_FILE_FORMAT_VERSION });

        //
        // attributes
        //

        /** The project. */
        private IProject mProject;

        /** the file set currently built. */
        private FileSet mCurrentFileSet;

        /** the current filter. */
        private IFilter mCurrentFilter;

        /** The name of the current check configuration. */
        private String mCurrentName;

        /** The location of the current check configuration. */
        private String mCurrentLocation;

        /** The description of the current check configuration. */
        private String mCurrentDescription;

        /** The configuration type of the current configuration. */
        private IConfigurationType mCurrentConfigType;

        /** if the project configuration uses simple configuration. */
        private boolean mUseSimpleConfig;

        /** The list of configurations. */
        private List mCheckConfigs;

        /** The list of file sets. */
        private List mFileSets;

        /** The filters. */
        private List mFilters;

        /** Additional data for the current configuration. */
        private Map mCurrentAddValues;

        /** List of resolvable properties for the current configuration. */
        private List mResolvableProperties;

        //
        // constructors
        //

        public ProjectConfigFileHandler(IProject project)
        {
            mProject = project;
        }

        //
        // methods
        //

        /**
         * Returns the project configuration.
         * 
         * @return the project configuration
         */
        public IProjectConfiguration getConfiguration()
        {
            return new ProjectConfiguration(mProject, mCheckConfigs, mFileSets, mFilters,
                    mUseSimpleConfig);
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {

            try
            {

                if (XMLTags.FILESET_CONFIG_TAG.equals(qName))
                {
                    String version = attributes.getValue(XMLTags.FORMAT_VERSION_TAG);
                    if (!SUPPORTED_VERSIONS.contains(version))
                    {
                        throw new CheckstylePluginException(NLS.bind(
                                ErrorMessages.errorUnknownFileFormat, version));
                    }

                    mUseSimpleConfig = Boolean.valueOf(
                            attributes.getValue(XMLTags.SIMPLE_CONFIG_TAG)).booleanValue();

                    mCheckConfigs = new ArrayList();
                    mFileSets = new ArrayList();
                    mFilters = new ArrayList();
                }
                else if (XMLTags.CHECK_CONFIG_TAG.equals(qName))
                {

                    mCurrentName = attributes.getValue(XMLTags.NAME_TAG);
                    mCurrentDescription = attributes.getValue(XMLTags.DESCRIPTION_TAG);
                    mCurrentLocation = attributes.getValue(XMLTags.LOCATION_TAG);

                    String type = attributes.getValue(XMLTags.TYPE_TAG);
                    mCurrentConfigType = ConfigurationTypes.getByInternalName(type);

                    if (mCurrentConfigType instanceof ProjectConfigurationType)
                    {
                        // RFE 1420212
                        // treat config files relative to *THIS* project
                        IWorkspaceRoot root = mProject.getWorkspace().getRoot();
                        // test if the location contains the project name
                        if (root.findMember(mCurrentLocation) == null)
                        {
                            mCurrentLocation = mProject.getFullPath().append(mCurrentLocation)
                                    .toString();
                        }
                    }

                    mCurrentAddValues = new HashMap();
                    mResolvableProperties = new ArrayList();
                }
                else if (XMLTags.ADDITIONAL_DATA_TAG.equalsIgnoreCase(qName))
                {
                    mCurrentAddValues.put(attributes.getValue(XMLTags.NAME_TAG), attributes
                            .getValue(XMLTags.VALUE_TAG));
                }
                else if (XMLTags.PROPERTY_TAG.equals(qName))
                {

                    String name = attributes.getValue(XMLTags.NAME_TAG);
                    String value = attributes.getValue(XMLTags.VALUE_TAG);

                    ResolvableProperty prop = new ResolvableProperty(name, value);
                    mResolvableProperties.add(prop);
                }
                else if (XMLTags.FILESET_TAG.equals(qName))
                {

                    String name = attributes.getValue(XMLTags.CHECK_CONFIG_NAME_TAG);
                    boolean local = Boolean.valueOf(attributes.getValue(XMLTags.LOCAL_TAG))
                            .booleanValue();

                    mCurrentFileSet = new FileSet();
                    mCurrentFileSet.setName(attributes.getValue(XMLTags.NAME_TAG));
                    mCurrentFileSet.setEnabled(Boolean.valueOf(
                            attributes.getValue(XMLTags.ENABLED_TAG)).booleanValue());

                    ICheckConfiguration checkConfig = null;

                    if (local)
                    {
                        Iterator it = mCheckConfigs.iterator();
                        while (it.hasNext())
                        {
                            ICheckConfiguration tmp = (ICheckConfiguration) it.next();
                            if (tmp.getName().equals(name))
                            {
                                checkConfig = tmp;
                            }
                        }
                    }
                    else
                    {
                        checkConfig = CheckConfigurationFactory.getByName(name);
                    }

                    mCurrentFileSet.setCheckConfig(checkConfig);

                    // set an empty list for the patterns to store
                    mCurrentFileSet.setFileMatchPatterns(new ArrayList());

                    mFileSets.add(mCurrentFileSet);
                }
                else if (XMLTags.FILE_MATCH_PATTERN_TAG.equals(qName))
                {

                    FileMatchPattern pattern = new FileMatchPattern(attributes
                            .getValue(XMLTags.MATCH_PATTERN_TAG));
                    pattern.setIsIncludePattern(Boolean.valueOf(
                            attributes.getValue(XMLTags.INCLUDE_PATTERN_TAG)).booleanValue());
                    mCurrentFileSet.getFileMatchPatterns().add(pattern);
                }

                else if (XMLTags.FILTER_TAG.equals(qName))
                {
                    mCurrentFilter = PluginFilters.getByInternalName(attributes
                            .getValue(XMLTags.NAME_TAG));

                    if (mCurrentFilter != null)
                    {
                        mCurrentFilter.setEnabled(Boolean.valueOf(
                                attributes.getValue(XMLTags.ENABLED_TAG)).booleanValue());

                        // set an empty list for the filter data
                        mCurrentFilter.setFilterData(new ArrayList());
                        mFilters.add(mCurrentFilter);
                    }
                }
                else if (XMLTags.FILTER_DATA_TAG.equals(qName) && mCurrentFilter != null)
                {
                    mCurrentFilter.getFilterData().add(attributes.getValue(XMLTags.VALUE_TAG));
                }

            }
            catch (CheckstylePluginException e)
            {
                throw new SAXException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (XMLTags.CHECK_CONFIG_TAG.equals(qName))
            {
                try
                {

                    ICheckConfiguration checkConfig = new CheckConfiguration(mCurrentName,
                            mCurrentLocation, mCurrentDescription, mCurrentConfigType, false,
                            mResolvableProperties, mCurrentAddValues);

                    mCheckConfigs.add(checkConfig);
                }
                catch (Exception e)
                {
                    throw new SAXException(e);
                }
            }
        }
    }
}