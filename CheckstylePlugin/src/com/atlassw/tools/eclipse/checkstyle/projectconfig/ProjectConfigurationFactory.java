//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

/**
 * Used to manage the life cycle of FileSet objects.
 */
public final class ProjectConfigurationFactory
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static final String PROJECT_CONFIGURATION_FILE = ".checkstyle";

    private static final String CURRENT_FILE_FORMAT_VERSION = "1.1.0";

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    private ProjectConfigurationFactory()
    {}

    //=================================================
    // Methods.
    //=================================================

    /**
     * Get the <code>ProjectConfiguration</code> object for the specified
     * project.
     * 
     * @param project The project to get <code>FileSet</code>'s for.
     * 
     * @return The <code>ProjectConfiguration</code> instance.
     * 
     * @throws CheckstylePluginException Error during processing.
     */
    public static ProjectConfiguration getConfiguration(IProject project)
        throws CheckstylePluginException
    {
        return loadFromPersistence(project);
    }

    /**
     * Sets the <code>ProjectConfiguration</code> to a project.
     * 
     * @param config The <code>ProjectConfiguration</code> object to set.
     * 
     * @param project The project to add it too.
     * 
     * @throws CheckstylePluginException Error during processing.
     */
    public static void setConfiguration(ProjectConfiguration config, IProject project)
        throws CheckstylePluginException
    {
        storeToPersistence(config, project);
    }

    /**
     * Check to see if a check configuration is currently in use by any
     * projects.
     * 
     * @param configName The configuration name to check for.
     * 
     * @return <code>true</code>= in use, <code>false</code>= not in use.
     * 
     * @throws CheckstylePluginException Error during processing.
     */
    public static boolean isCheckConfigInUse(String configName) throws CheckstylePluginException
    {

        //TODO why not change the configuration name in the file sets??

        boolean result = false;

        if (configName == null)
        {
            return result;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; (i < projects.length) && !result; i++)
        {
            if (ProjectConfigurationFactory.getConfiguration(projects[i]).isConfigInUse(configName))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Load the audit configurations from the persistent state storage.
     */
    private static ProjectConfiguration loadFromPersistence(IProject project)
        throws CheckstylePluginException
    {
        ProjectConfiguration configuration = null;

        //
        //  Make sure the files exists, it might not.
        //
        IFile file = project.getFile(PROJECT_CONFIGURATION_FILE);
        boolean exists = file.exists();
        if (!exists)
        {
            return new ProjectConfiguration();
        }

        InputStream inStream = null;
        try
        {
            inStream = file.getContents();

            ProjectConfigFileHandler handler = new ProjectConfigFileHandler();
            XMLUtil.parseWithSAX(inStream, handler);

            configuration = handler.getConfiguration();
        }
        catch (Exception e)
        {
            String message = "Failed to read FileSets: " + e.getMessage();
            CheckstyleLog.warning(message, e);
            throw new CheckstylePluginException(message);
        }

        finally
        {
            if (inStream != null)
            {
                try
                {
                    inStream.close();
                }
                catch (IOException e)
                {
                    //  Nothing can be done about it.
                }
            }
        }

        return configuration;
    }

    /**
     * Store the audit configurations to the persistent state storage.
     */
    private static void storeToPersistence(ProjectConfiguration config, IProject project)
        throws CheckstylePluginException
    {

        ByteArrayOutputStream pipeOut = null;
        InputStream pipeIn = null;
        try
        {

            pipeOut = new ByteArrayOutputStream();

            //Write the configuration document by pushing sax events through
            // the transformer handler
            TransformerHandler xmlOut = XMLUtil.writeWithSax(pipeOut);

            writeProjectConfig(config, xmlOut);

            pipeIn = new ByteArrayInputStream(pipeOut.toByteArray());

            //create or overwrite the .checkstyle file
            IFile file = project.getFile(PROJECT_CONFIGURATION_FILE);
            if (!file.exists())
            {
                file.create(pipeIn, true, null);
                file.setLocal(true, IResource.DEPTH_INFINITE, null);
            }
            else
            {
                file.setContents(pipeIn, true, true, null);
            }
        }
        catch (Exception e)
        {
            String message = "Failed to write audit configuration file: " + e.getMessage();
            CheckstyleLog.error(message, e);
            throw new CheckstylePluginException(message);
        }
        finally
        {
            try
            {
                pipeOut.close();
            }
            catch (Exception e1)
            {
                //can nothing do about it
            }
            try
            {
                pipeIn.close();
            }
            catch (Exception e1)
            {
                //can nothing do about it
            }

        }
    }

    /**
     * Produces the sax events to write a project configuration.
     * 
     * @param config the configuration
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private static void writeProjectConfig(ProjectConfiguration config, TransformerHandler xmlOut)
        throws SAXException
    {

        xmlOut.startDocument();

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", XMLTags.FORMAT_VERSION_TAG, XMLTags.FORMAT_VERSION_TAG, null,
                CURRENT_FILE_FORMAT_VERSION);
        attr.addAttribute("", XMLTags.SIMPLE_CONFIG_TAG, XMLTags.SIMPLE_CONFIG_TAG, null, ""
                + config.isUseSimpleConfig());

        xmlOut.startElement("", XMLTags.FILESET_CONFIG_TAG, XMLTags.FILESET_CONFIG_TAG, attr);

        List fileSets = config.getFileSets();
        int size = fileSets != null ? fileSets.size() : 0;
        for (int i = 0; i < size; i++)
        {
            writeFileSet((FileSet) fileSets.get(i), xmlOut);
        }
        //write filters
        IFilter[] filters = config.getFilters();
        size = filters != null ? filters.length : 0;
        for (int i = 0; i < size; i++)
        {
            writeFilter(filters[i], xmlOut);
        }

        xmlOut.endElement("", XMLTags.FILESET_CONFIG_TAG, XMLTags.FILESET_CONFIG_TAG);
        xmlOut.endDocument();
    }

    /**
     * Produces the sax events to write a file set to xml.
     * 
     * @param fileSet the file set
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private static void writeFileSet(FileSet fileSet, TransformerHandler xmlOut)
        throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, fileSet.getName());
        attr.addAttribute("", XMLTags.CHECK_CONFIG_NAME_TAG, XMLTags.CHECK_CONFIG_NAME_TAG, null,
                fileSet.getCheckConfigName());
        attr.addAttribute("", XMLTags.ENABLED_TAG, XMLTags.ENABLED_TAG, null, ""
                + fileSet.isEnabled());

        xmlOut.startElement("", XMLTags.FILESET_TAG, XMLTags.FILESET_TAG, attr);

        //write patterns
        List patterns = fileSet.getFileMatchPatterns();
        int size = patterns != null ? patterns.size() : 0;
        for (int i = 0; i < size; i++)
        {
            writeMatchPattern((FileMatchPattern) patterns.get(i), xmlOut);
        }

        xmlOut.endElement("", XMLTags.FILESET_TAG, XMLTags.FILESET_TAG);
    }

    /**
     * Produces the sax events to write the file match pattern to xml.
     * 
     * @param pattern the pattern
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private static void writeMatchPattern(FileMatchPattern pattern, TransformerHandler xmlOut)
        throws SAXException
    {

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", XMLTags.MATCH_PATTERN_TAG, XMLTags.MATCH_PATTERN_TAG, null, pattern
                .getMatchPattern());
        attr.addAttribute("", XMLTags.INCLUDE_PATTERN_TAG, XMLTags.INCLUDE_PATTERN_TAG, null, ""
                + pattern.isIncludePattern());

        xmlOut.startElement("", XMLTags.FILE_MATCH_PATTERN_TAG, XMLTags.FILE_MATCH_PATTERN_TAG,
                attr);
        xmlOut.endElement("", XMLTags.FILE_MATCH_PATTERN_TAG, XMLTags.FILE_MATCH_PATTERN_TAG);
    }

    /**
     * Produces the sax events to write a filter to xml.
     * 
     * @param filter the filter
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private static void writeFilter(IFilter filter, TransformerHandler xmlOut) throws SAXException
    {

        //write only filters that are actually changed
        //(enabled or contain data)
        IFilter prototype = PluginFilters.getByName(filter.getName());
        if (prototype.equals(filter))
        {
            return;
        }

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", XMLTags.NAME_TAG, XMLTags.NAME_TAG, null, filter.getInternalName());
        attr.addAttribute("", XMLTags.ENABLED_TAG, XMLTags.ENABLED_TAG, null, ""
                + filter.isEnabled());

        xmlOut.startElement("", XMLTags.FILTER_TAG, XMLTags.FILTER_TAG, attr);

        List data = filter.getFilterData();
        int size = data != null ? data.size() : 0;
        for (int i = 0; i < size; i++)
        {

            attr = new AttributesImpl();
            attr.addAttribute("", XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, null, (String) data.get(i));

            xmlOut.startElement("", XMLTags.FILTER_DATA_TAG, XMLTags.FILTER_DATA_TAG, attr);
            xmlOut.endElement("", XMLTags.FILTER_DATA_TAG, XMLTags.FILTER_DATA_TAG);
        }

        xmlOut.endElement("", XMLTags.FILTER_TAG, XMLTags.FILTER_TAG);

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
        private static final List SUPPORTED_VERSIONS = Arrays.asList(new String[] { "1.0.0",
            CURRENT_FILE_FORMAT_VERSION });

        //
        // attributes
        //

        /** the project configuration. */
        private ProjectConfiguration mProjectConfig = new ProjectConfiguration();

        /** the file set currently built. */
        private FileSet mCurrentFileSet;

        /** the current filter. */
        private IFilter mCurrentFilter;

        //
        // methods
        //

        /**
         * Returns the project configuration.
         * 
         * @return the project configuration
         */
        public ProjectConfiguration getConfiguration()
        {
            return mProjectConfig;
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
                        throw new CheckstylePluginException("Unknown file format: " + version);
                    }

                    mProjectConfig.setUseSimpleConfig(Boolean.valueOf(
                            attributes.getValue(XMLTags.SIMPLE_CONFIG_TAG)).booleanValue());
                }

                else if (XMLTags.FILESET_TAG.equals(qName))
                {

                    mCurrentFileSet = new FileSet();
                    mCurrentFileSet.setName(attributes.getValue(XMLTags.NAME_TAG));
                    mCurrentFileSet.setEnabled(Boolean.valueOf(
                            attributes.getValue(XMLTags.ENABLED_TAG)).booleanValue());
                    mCurrentFileSet.setCheckConfig(CheckConfigurationFactory.getByName(attributes
                            .getValue(XMLTags.CHECK_CONFIG_NAME_TAG)));

                    //set an empty list for the patterns to store
                    mCurrentFileSet.setFileMatchPatterns(new ArrayList());

                    mProjectConfig.getFileSets().add(mCurrentFileSet);
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
                    mCurrentFilter = mProjectConfig.getFilterByIntenalName(attributes
                            .getValue(XMLTags.NAME_TAG));

                    if (mCurrentFilter != null)
                    {
                        mCurrentFilter.setEnabled(Boolean.valueOf(
                                attributes.getValue(XMLTags.ENABLED_TAG)).booleanValue());

                        //set an empty list for the filter data
                        mCurrentFilter.setFilterData(new ArrayList());
                    }
                }
                else if (XMLTags.FILTER_DATA_TAG.equals(qName) && mCurrentFilter != null)
                {
                    mCurrentFilter.getFilterData().add(attributes.getValue(XMLTags.VALUE_TAG));
                }

            }
            catch (CheckstylePluginException ce)
            {
                throw new SAXException(ce);
            }
        }
    }
}