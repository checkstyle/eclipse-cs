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

package com.atlassw.tools.eclipse.checkstyle.config.migration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationWriter;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.InternalConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Migrates old plugin check configurations.
 * 
 * @author Lars Ködderitzsch
 */
public final class CheckConfigurationMigrator
{

    /** hidden default constructor. */
    private CheckConfigurationMigrator()
    {}

    /**
     * Reads the old configuration file from the stream and migrates the
     * contained configurations.
     * 
     * @param checkConfigsFile the old configuration file stream
     * @param workingSet the configuration working set
     * @throws CheckstylePluginException error migrating the configurations
     */
    public static void migrate(InputStream checkConfigsFile,
            ICheckConfigurationWorkingSet workingSet) throws CheckstylePluginException
    {

        try
        {

            OldConfigurationHandler handler = new OldConfigurationHandler(workingSet);
            XMLUtil.parseWithSAX(checkConfigsFile, handler);
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
    }

    /**
     * SAX-Handler for parsing the old check configuration file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class OldConfigurationHandler extends DefaultHandler
    {
        /** the working set. */
        private ICheckConfigurationWorkingSet mWorkingSet;

        /** List containing the modules of the current configuration. */
        private List mCurrentConfigModules = new ArrayList();

        /** the check configuration currently migrating. */
        private CheckConfigurationWorkingCopy mCurrentConfiguration;

        /** the module currently being built. */
        private Module mCurrentModule;

        /**
         * Creates the handler.
         * 
         * @param workingSet the working set
         */
        OldConfigurationHandler(ICheckConfigurationWorkingSet workingSet)
        {
            mWorkingSet = workingSet;
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

                if (XMLTags.CHECK_CONFIG_TAG.equals(qName))
                {

                    String name = attributes.getValue(XMLTags.NAME_TAG);

                    // create an new check config working copy
                    try
                    {

                        IConfigurationType internalType = ConfigurationTypes
                                .getByInternalName("internal"); //$NON-NLS-1$

                        mCurrentConfiguration = mWorkingSet.newWorkingCopy(internalType);
                        mCurrentConfiguration.setName(name);
                    }
                    catch (CheckstylePluginException cpe)
                    {
                        // we probably got a name collision so we try to use a
                        // unique name
                        String nameAddition = NLS.bind(
                                Messages.CheckConfigurationMigrator_txtMigrationAddition,
                                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                                        .format(new Date()));
                        mCurrentConfiguration.setName(name + nameAddition);
                    }

                    // create the location of the new internal configuration
                    if (mCurrentConfiguration.getLocation() == null)
                    {
                        String location = "internal_config_" + "_" + System.currentTimeMillis() //$NON-NLS-1$ //$NON-NLS-2$
                                + ".xml"; //$NON-NLS-1$

                        //ensureFileExists(location);

                        mCurrentConfiguration.setLocation(location);
                    }
                }
                else if (XMLTags.RULE_CONFIG_TAG.equals(qName))
                {

                    String classname = attributes.getValue(XMLTags.CLASSNAME_TAG);
                    String severity = attributes.getValue(XMLTags.SEVERITY_TAG);

                    RuleMetadata metaData = MetadataFactory.getRuleMetadata(classname);

                    if (metaData != null)
                    {
                        mCurrentModule = new Module(metaData, false);
                    }
                    else
                    {
                        mCurrentModule = new Module(classname);
                    }

                    SeverityLevel level = SeverityLevel.getInstance(severity);

                    mCurrentModule.setSeverity(level != null ? level : SeverityLevel.WARNING);
                    mCurrentConfigModules.add(mCurrentModule);
                }
                else if (XMLTags.CONFIG_PROPERTY_TAG.equals(qName))
                {
                    String name = attributes.getValue(XMLTags.NAME_TAG);
                    String value = attributes.getValue(XMLTags.VALUE_TAG);

                    if (mCurrentModule.getMetaData() != null)
                    {
                        ConfigProperty property = mCurrentModule.getProperty(name);
                        if (property != null)
                        {
                            property.setValue(value);
                        }
                        // properties that are not within the meta data are
                        // omitted
                    }
                    else
                    {
                        // if module has no meta data defined create property
                        ConfigProperty property = new ConfigProperty(name, value);
                        mCurrentModule.getProperties().add(property);
                    }
                }
            }
            catch (CheckstylePluginException e)
            {
                throw new SAXException(e);
            }
        }

        /**
         * Helper method trying to ensure that the file location provided by the
         * user exists. If that is not the case it prompts the user if an empty
         * configuration file should be created.
         * 
         * @param location the configuration file location
         * @throws CheckstylePluginException error when trying to ensure the
         *             location file existance
         */
        private boolean ensureFileExists(String location) throws CheckstylePluginException
        {

            String resolvedLocation = InternalConfigurationType
                    .resolveLocationInWorkspace(location);

            File file = new File(resolvedLocation);
            if (!file.exists())
            {

                OutputStream out = null;
                try
                {
                    if (file.getParentFile() != null)
                    {
                        file.getParentFile().mkdirs();
                    }
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    ConfigurationWriter.writeNewConfiguration(out, mCurrentConfiguration);
                }
                catch (IOException ioe)
                {
                    CheckstylePluginException.rethrow(ioe);
                }
                finally
                {
                    IOUtils.closeQuietly(out);
                }
                return true;
            }

            return true;
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            try
            {
                if (XMLTags.CHECK_CONFIG_TAG.equals(qName))
                {

                    // store the moduless
                    mCurrentConfiguration.setModules(mCurrentConfigModules);
                    mWorkingSet.addCheckConfiguration(mCurrentConfiguration);
                }

                else if (XMLTags.RULE_CONFIG_TAG.equals(qName))
                {

                    // if the module has not metadata attached we create some
                    // generic metadata
                    if (mCurrentModule.getMetaData() == null)
                    {

                        MetadataFactory.createGenericMetadata(mCurrentModule);
                    }
                }
            }
            catch (CheckstylePluginException e)
            {
                throw new SAXException(e);
            }
        }

    }
}