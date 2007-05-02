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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.BuiltInConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ProjectConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;

/**
 * A modifiable project configuration implementation.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfigurationWorkingCopy implements Cloneable, IProjectConfiguration
{

    //
    // attributes
    //

    /** The original, unmodified project configuration. */
    private IProjectConfiguration mProjectConfig;

    /** The local check configurations. */
    private ICheckConfigurationWorkingSet mLocalConfigWorkingSet;

    /** The global check configurations. */
    private ICheckConfigurationWorkingSet mGlobalConfigWorkingSet;

    /** the file sets. */
    private List mFileSets = new LinkedList();

    /** the filters. */
    private List mFilters = new LinkedList();

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
    public ProjectConfigurationWorkingCopy(IProjectConfiguration projectConfig)
    {

        mProjectConfig = projectConfig;

        mLocalConfigWorkingSet = new LocalCheckConfigurationWorkingSet(this, projectConfig
                .getLocalCheckConfigurations());
        mGlobalConfigWorkingSet = CheckConfigurationFactory.newWorkingSet();

        // clone file sets of the original config
        Iterator it = projectConfig.getFileSets().iterator();
        while (it.hasNext())
        {
            mFileSets.add(((FileSet) it.next()).clone());
        }

        // build list of filters
        List standardFilters = Arrays.asList(PluginFilters.getConfiguredFilters());
        mFilters = new ArrayList(standardFilters);

        // merge with filters configured for the project
        List configuredFilters = projectConfig.getFilters();
        for (int i = 0, size = mFilters.size(); i < size; i++)
        {

            IFilter standardFilter = (IFilter) mFilters.get(i);

            for (int j = 0, size2 = configuredFilters.size(); j < size2; j++)
            {
                IFilter configuredFilter = (IFilter) configuredFilters.get(j);

                if (standardFilter.getInternalName().equals(configuredFilter.getInternalName()))
                {
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
    public ICheckConfigurationWorkingSet getLocalCheckConfigWorkingSet()
    {
        return mLocalConfigWorkingSet;
    }

    /**
     * Returns the check configuration working set for global configurations.
     * 
     * @return the local configurations working set
     */
    public ICheckConfigurationWorkingSet getGlobalCheckConfigWorkingSet()
    {
        return mGlobalConfigWorkingSet;
    }

    /**
     * Returns a project local check configuration by its name.
     * 
     * @param name the configurations name
     * @return the check configuration or <code>null</code>, if no local
     *         configuration with this name exists
     */
    public ICheckConfiguration getLocalCheckConfigByName(String name)
    {
        ICheckConfiguration config = null;
        ICheckConfiguration[] configs = mLocalConfigWorkingSet.getWorkingCopies();
        for (int i = 0; i < configs.length; i++)
        {
            if (configs[i].getName().equals(name))
            {
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
    public ICheckConfiguration getGlobalCheckConfigByName(String name)
    {
        ICheckConfiguration config = null;
        ICheckConfiguration[] configs = mGlobalConfigWorkingSet.getWorkingCopies();
        for (int i = 0; i < configs.length; i++)
        {
            if (configs[i].getName().equals(name))
            {
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
    public void setUseSimpleConfig(boolean useSimpleConfig)
    {
        mUseSimpleConfig = useSimpleConfig;
    }

    /**
     * Determines if the project configuration changed.
     * 
     * @return <code>true</code> if changed
     */
    public boolean isDirty()
    {
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
    public boolean isRebuildNeeded() throws CheckstylePluginException
    {
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
    public void store() throws CheckstylePluginException
    {
        storeToPersistence(this);
    }

    //
    // implementation of IProjectConfiguration interface
    //

    /**
     * {@inheritDoc}
     */
    public IProject getProject()
    {
        return mProjectConfig.getProject();
    }

    /**
     * {@inheritDoc}
     */
    public List getLocalCheckConfigurations()
    {
        return Arrays.asList(mLocalConfigWorkingSet.getWorkingCopies());
    }

    /**
     * {@inheritDoc}
     */
    public List getFileSets()
    {
        return mFileSets;
    }

    /**
     * {@inheritDoc}
     */
    public List getFilters()
    {
        return mFilters;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUseSimpleConfig()
    {
        return mUseSimpleConfig;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigInUse(ICheckConfiguration configuration)
    {

        boolean result = false;

        Iterator iter = getFileSets().iterator();
        while (iter.hasNext())
        {
            FileSet fileSet = (FileSet) iter.next();
            ICheckConfiguration checkConfig = fileSet.getCheckConfig();
            if (configuration.equals(checkConfig)
                    || (checkConfig instanceof CheckConfigurationWorkingCopy && configuration
                            .equals(((CheckConfigurationWorkingCopy) checkConfig)
                                    .getSourceCheckConfiguration())))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        ProjectConfigurationWorkingCopy clone = null;
        try
        {
            clone = (ProjectConfigurationWorkingCopy) super.clone();
            clone.mFileSets = new LinkedList();
            clone.setUseSimpleConfig(this.isUseSimpleConfig());

            // clone file sets
            Iterator iter = getFileSets().iterator();
            while (iter.hasNext())
            {
                clone.getFileSets().add(((FileSet) iter.next()).clone());
            }

            // clone filters
            List clonedFilters = new ArrayList();
            iter = getFilters().iterator();
            while (iter.hasNext())
            {
                clonedFilters.add(((IFilter) iter.next()).clone());
            }
            clone.mFilters = clonedFilters;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError();
        }

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {

        if (obj == null || !(obj instanceof IProjectConfiguration))
        {
            return false;
        }
        if (this == obj)
        {
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
    public int hashCode()
    {
        return new HashCodeBuilder(984759323, 1000003).append(mProjectConfig).append(
                mUseSimpleConfig).append(mFileSets).append(mFilters).toHashCode();
    }

    /**
     * Store the audit configurations to the persistent state storage.
     */
    private void storeToPersistence(ProjectConfigurationWorkingCopy config)
        throws CheckstylePluginException
    {

        ByteArrayOutputStream pipeOut = null;
        InputStream pipeIn = null;
        try
        {

            pipeOut = new ByteArrayOutputStream();

            // Write the configuration document by pushing sax events through
            // the transformer handler
            TransformerHandler xmlOut = XMLUtil.writeWithSax(pipeOut, null, null);

            writeProjectConfig(config, xmlOut);

            pipeIn = new ByteArrayInputStream(pipeOut.toByteArray());

            // create or overwrite the .checkstyle file
            IProject project = config.getProject();
            IFile file = project.getFile(ProjectConfigurationFactory.PROJECT_CONFIGURATION_FILE);
            if (!file.exists())
            {
                file.create(pipeIn, true, null);
                file.setLocal(true, IResource.DEPTH_INFINITE, null);
            }
            else
            {
                file.setContents(pipeIn, true, true, null);
            }

            config.getLocalCheckConfigWorkingSet().store();
        }
        catch (Exception e)
        {
            CheckstylePluginException.rethrow(e, NLS.bind(
                    ErrorMessages.errorWritingCheckConfigurations, e.getLocalizedMessage()));
        }
        finally
        {
            IOUtils.closeQuietly(pipeIn);
            IOUtils.closeQuietly(pipeOut);
        }
    }

    /**
     * Produces the sax events to write a project configuration.
     * 
     * @param config the configuration
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private void writeProjectConfig(ProjectConfigurationWorkingCopy config,
            TransformerHandler xmlOut) throws SAXException, CheckstylePluginException
    {

        xmlOut.startDocument();

        String emptyString = new String();

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(emptyString, XMLTags.FORMAT_VERSION_TAG, XMLTags.FORMAT_VERSION_TAG,
                emptyString, ProjectConfigurationFactory.CURRENT_FILE_FORMAT_VERSION);
        attr.addAttribute(emptyString, XMLTags.SIMPLE_CONFIG_TAG, XMLTags.SIMPLE_CONFIG_TAG,
                emptyString, emptyString + config.isUseSimpleConfig());

        xmlOut.startElement(emptyString, XMLTags.FILESET_CONFIG_TAG, XMLTags.FILESET_CONFIG_TAG,
                attr);

        ICheckConfiguration[] workingCopies = config.getLocalCheckConfigWorkingSet()
                .getWorkingCopies();
        for (int i = 0; i < workingCopies.length; i++)
        {
            writeLocalConfiguration(workingCopies[i], xmlOut);
        }

        List fileSets = config.getFileSets();
        int size = fileSets != null ? fileSets.size() : 0;
        for (int i = 0; i < size; i++)
        {
            writeFileSet((FileSet) fileSets.get(i), config.getProject(), xmlOut);
        }
        // write filters
        List filters = config.getFilters();
        size = filters != null ? filters.size() : 0;
        for (int i = 0; i < size; i++)
        {
            writeFilter((IFilter) filters.get(i), xmlOut);
        }

        xmlOut.endElement(emptyString, XMLTags.FILESET_CONFIG_TAG, XMLTags.FILESET_CONFIG_TAG);
        xmlOut.endDocument();
    }

    /**
     * Writes a local check configuration.
     * 
     * @param checkConfig the local check configuration
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     * @throws CheckstylePluginException
     */
    private void writeLocalConfiguration(ICheckConfiguration checkConfig, TransformerHandler xmlOut)
        throws SAXException, CheckstylePluginException
    {

        // TODO refactor to avoid code duplication with
        // GlobalCheckConfigurationWorkingSet

        // don't store built-in configurations to persistence or local
        // configurations
        if (checkConfig.getType() instanceof BuiltInConfigurationType || checkConfig.isGlobal())
        {
            return;
        }

        // RFE 1420212
        String location = checkConfig.getLocation();
        if (checkConfig.getType() instanceof ProjectConfigurationType)
        {
            IProject project = mProjectConfig.getProject();
            IWorkspaceRoot root = project.getWorkspace().getRoot();
            IFile configFile = root.getFile(new Path(location));
            IProject configFileProject = configFile.getProject();

            // if the configuration is in *same* project don't store project
            // path part
            if (project.equals(configFileProject))
            {
                location = configFile.getProjectRelativePath().toString();
            }
        }

        String emptyString = new String();

        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString,
                checkConfig.getName());
        attrs.addAttribute(emptyString, XMLTags.LOCATION_TAG, XMLTags.LOCATION_TAG, emptyString,
                location);
        attrs.addAttribute(emptyString, XMLTags.TYPE_TAG, XMLTags.TYPE_TAG, emptyString,
                checkConfig.getType().getInternalName());
        if (checkConfig.getDescription() != null)
        {
            attrs.addAttribute(emptyString, XMLTags.DESCRIPTION_TAG, XMLTags.DESCRIPTION_TAG,
                    emptyString, checkConfig.getDescription());
        }

        xmlOut.startElement(emptyString, XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG, attrs);

        // Write resolvable properties
        Iterator propsIterator = checkConfig.getResolvableProperties().iterator();
        while (propsIterator.hasNext())
        {

            ResolvableProperty prop = (ResolvableProperty) propsIterator.next();

            attrs = new AttributesImpl();
            attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, prop
                    .getPropertyName());
            attrs.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString, prop
                    .getValue());

            xmlOut.startElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG, attrs);
            xmlOut.endElement(emptyString, XMLTags.PROPERTY_TAG, XMLTags.PROPERTY_TAG);
        }

        Iterator addDataIterator = checkConfig.getAdditionalData().keySet().iterator();
        while (addDataIterator.hasNext())
        {
            String key = (String) addDataIterator.next();
            String value = (String) checkConfig.getAdditionalData().get(key);

            attrs = new AttributesImpl();
            attrs.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, key);
            attrs.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    value);

            xmlOut.startElement(emptyString, XMLTags.ADDITIONAL_DATA_TAG,
                    XMLTags.ADDITIONAL_DATA_TAG, attrs);
            xmlOut
                    .endElement(emptyString, XMLTags.ADDITIONAL_DATA_TAG,
                            XMLTags.ADDITIONAL_DATA_TAG);
        }

        xmlOut.endElement(emptyString, XMLTags.CHECK_CONFIG_TAG, XMLTags.CHECK_CONFIG_TAG);
    }

    /**
     * Produces the sax events to write a file set to xml.
     * 
     * @param fileSet the file set
     * @param project the project
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private void writeFileSet(FileSet fileSet, IProject project, TransformerHandler xmlOut)
        throws SAXException, CheckstylePluginException
    {

        if (fileSet.getCheckConfig() == null)
        {
            throw new CheckstylePluginException(ErrorMessages.bind(
                    ErrorMessages.errorFilesetWithoutCheckConfig, fileSet.getName(), project
                            .getName()));
        }

        String emptyString = new String();

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, fileSet
                .getName());

        attr.addAttribute(emptyString, XMLTags.ENABLED_TAG, XMLTags.ENABLED_TAG, emptyString,
                emptyString + fileSet.isEnabled());

        ICheckConfiguration checkConfig = fileSet.getCheckConfig();
        if (checkConfig != null)
        {

            attr.addAttribute(emptyString, XMLTags.CHECK_CONFIG_NAME_TAG,
                    XMLTags.CHECK_CONFIG_NAME_TAG, emptyString, checkConfig.getName());
            attr.addAttribute(emptyString, XMLTags.LOCAL_TAG, XMLTags.LOCAL_TAG, emptyString,
                    emptyString + !checkConfig.isGlobal());
        }

        xmlOut.startElement(emptyString, XMLTags.FILESET_TAG, XMLTags.FILESET_TAG, attr);

        // write patterns
        List patterns = fileSet.getFileMatchPatterns();
        int size = patterns != null ? patterns.size() : 0;
        for (int i = 0; i < size; i++)
        {
            writeMatchPattern((FileMatchPattern) patterns.get(i), xmlOut);
        }

        xmlOut.endElement(emptyString, XMLTags.FILESET_TAG, XMLTags.FILESET_TAG);
    }

    /**
     * Produces the sax events to write the file match pattern to xml.
     * 
     * @param pattern the pattern
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private void writeMatchPattern(FileMatchPattern pattern, TransformerHandler xmlOut)
        throws SAXException
    {

        String emptyString = new String();

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(emptyString, XMLTags.MATCH_PATTERN_TAG, XMLTags.MATCH_PATTERN_TAG,
                emptyString, pattern.getMatchPattern() != null ? pattern.getMatchPattern()
                        : emptyString);
        attr.addAttribute(emptyString, XMLTags.INCLUDE_PATTERN_TAG, XMLTags.INCLUDE_PATTERN_TAG,
                emptyString, emptyString + pattern.isIncludePattern());

        xmlOut.startElement(emptyString, XMLTags.FILE_MATCH_PATTERN_TAG,
                XMLTags.FILE_MATCH_PATTERN_TAG, attr);
        xmlOut.endElement(emptyString, XMLTags.FILE_MATCH_PATTERN_TAG,
                XMLTags.FILE_MATCH_PATTERN_TAG);
    }

    /**
     * Produces the sax events to write a filter to xml.
     * 
     * @param filter the filter
     * @param xmlOut the transformer handler receiving the events
     * @throws SAXException error writing
     */
    private void writeFilter(IFilter filter, TransformerHandler xmlOut) throws SAXException
    {

        // write only filters that are actually changed
        // (enabled or contain data)
        IFilter prototype = PluginFilters.getByInternalName(filter.getInternalName());
        if (prototype.equals(filter))
        {
            return;
        }

        String emptyString = new String();

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(emptyString, XMLTags.NAME_TAG, XMLTags.NAME_TAG, emptyString, filter
                .getInternalName());
        attr.addAttribute(emptyString, XMLTags.ENABLED_TAG, XMLTags.ENABLED_TAG, emptyString,
                emptyString + filter.isEnabled());

        xmlOut.startElement(emptyString, XMLTags.FILTER_TAG, XMLTags.FILTER_TAG, attr);

        List data = filter.getFilterData();
        int size = data != null ? data.size() : 0;
        for (int i = 0; i < size; i++)
        {

            attr = new AttributesImpl();
            attr.addAttribute(emptyString, XMLTags.VALUE_TAG, XMLTags.VALUE_TAG, emptyString,
                    (String) data.get(i));

            xmlOut
                    .startElement(emptyString, XMLTags.FILTER_DATA_TAG, XMLTags.FILTER_DATA_TAG,
                            attr);
            xmlOut.endElement(emptyString, XMLTags.FILTER_DATA_TAG, XMLTags.FILTER_DATA_TAG);
        }

        xmlOut.endElement(emptyString, XMLTags.FILTER_TAG, XMLTags.FILTER_TAG);
    }
}