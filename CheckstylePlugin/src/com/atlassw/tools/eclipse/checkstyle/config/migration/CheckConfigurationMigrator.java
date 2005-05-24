//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.InternalCheckConfiguration;
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
     * @return the list of migrated configurations
     * @throws CheckstylePluginException error migrating the configurations
     */
    public static List getMigratedConfigurations(InputStream checkConfigsFile)
        throws CheckstylePluginException
    {

        List result = null;

        try
        {

            OldConfigurationHandler handler = new OldConfigurationHandler();
            XMLUtil.parseWithSAX(checkConfigsFile, handler);

            result = handler.getMigratedConfigurations();
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

        return result;
    }

    /**
     * SAX-Handler for parsing the old check configuration file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class OldConfigurationHandler extends DefaultHandler
    {

        /** List containing the migrated configurations. */
        private List mMigratedConfigurations = new ArrayList();

        /** List containing the modules of the current configuration. */
        private List mCurrentConfigModules = new ArrayList();

        /** the check configuration currently migrating. */
        private ICheckConfiguration mCurrentConfiguration;

        /** the module currently being built. */
        private Module mCurrentModule;

        /**
         * Returns the list of migrated configurations.
         * 
         * @return the migrated configurations
         */
        public List getMigratedConfigurations()
        {
            return mMigratedConfigurations;
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

                    // create an internal configuration
                    mCurrentConfiguration = new InternalCheckConfiguration();
                    IConfigurationType internalType = ConfigurationTypes
                            .getByInternalName("internal"); //$NON-NLS-1$

                    try
                    {
                        mCurrentConfiguration.initialize(name, null, internalType, new String());
                    }
                    catch (CheckstylePluginException cpe)
                    {
                        // we probably got a name collision so we try to use a
                        // unique name
                        String nameAddition = NLS.bind(
                                Messages.CheckConfigurationMigrator_txtMigrationAddition,
                                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
                                        .format(new Date()));
                        mCurrentConfiguration.initialize(name + nameAddition, null, internalType,
                                new String());
                    }
                }
                else if (XMLTags.RULE_CONFIG_TAG.equals(qName))
                {

                    String classname = attributes.getValue(XMLTags.CLASSNAME_TAG);
                    String severity = attributes.getValue(XMLTags.SEVERITY_TAG);

                    RuleMetadata metaData = MetadataFactory.getRuleMetadata(classname);

                    if (metaData != null)
                    {
                        mCurrentModule = new Module(metaData);
                    }
                    else
                    {
                        mCurrentModule = new Module(classname);
                    }

                    mCurrentModule.setSeverity(SeverityLevel.getInstance(severity));
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
                    mMigratedConfigurations.add(mCurrentConfiguration);
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