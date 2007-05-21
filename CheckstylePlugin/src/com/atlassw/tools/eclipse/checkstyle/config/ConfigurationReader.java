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

package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Utitlity class to read a checkstyle configuration and transform to the
 * plugins module objects.
 * 
 * @author Lars Ködderitzsch
 */
public final class ConfigurationReader
{

    //
    // Mapping from public DTD to internal dtd resource.
    // @see com.puppycrawl.tools.checkstyle.api.AbstractLoader
    //

    /** Map containing the public - internal DTD mapping. */
    private static final Map PUBLIC2INTERNAL_DTD_MAP = new HashMap();

    static
    {

        PUBLIC2INTERNAL_DTD_MAP.put("-//Puppy Crawl//DTD Check Configuration 1.0//EN", //$NON-NLS-1$
                "com/puppycrawl/tools/checkstyle/configuration_1_0.dtd"); //$NON-NLS-1$
        PUBLIC2INTERNAL_DTD_MAP.put("-//Puppy Crawl//DTD Check Configuration 1.1//EN", //$NON-NLS-1$
                "com/puppycrawl/tools/checkstyle/configuration_1_1.dtd"); //$NON-NLS-1$
        PUBLIC2INTERNAL_DTD_MAP.put("-//Puppy Crawl//DTD Check Configuration 1.2//EN", //$NON-NLS-1$
                "com/puppycrawl/tools/checkstyle/configuration_1_2.dtd"); //$NON-NLS-1$
    }

    //
    // constructors
    //

    /** Hidden default constructor to prevent instantiation. */
    private ConfigurationReader()
    {
    // NOOP
    }

    //
    // methods
    //

    /**
     * Reads the checkstyle configuration from the given stream an returs a list
     * of all modules within this configuration.
     * 
     * @param in the stream the configuration is loaded from
     * @return the list of modules
     * @throws CheckstylePluginException error while reading the configuration
     */
    public static List read(InputStream in) throws CheckstylePluginException
    {

        List rules = null;
        try
        {
            ConfigurationHandler handler = new ConfigurationHandler();
            XMLUtil.parseWithSAX(in, handler);
            rules = handler.getRules();
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

        return rules != null ? rules : new ArrayList();
    }

    /**
     * Gets additional data about the Checkstyle configuration. This data is
     * used by the plugin for special purposes, like determining the correct
     * offset of a checkstyle violation.
     * 
     * @param in the input stream
     * @return the additional configuration data
     * @throws CheckstylePluginException error while reading the configuration
     */
    public static AdditionalConfigData getAdditionalConfigData(InputStream in)
        throws CheckstylePluginException
    {

        List modules = read(in);

        Map messages = new HashMap();
        int tabWidth = 8;

        Iterator it = modules.iterator();
        while (it.hasNext())
        {

            Module module = (Module) it.next();

            if (module.getCustomMessage() != null)
            {
                String id = module.getId();

                if (id == null && module.getMetaData() != null)
                {
                    id = module.getMetaData().getInternalName();
                }

                if (id == null)
                {
                    id = module.getName();
                }

                messages.put(id, module.getCustomMessage());
            }
            if (module.getMetaData() != null
                    && module.getMetaData().getInternalName().equals(XMLTags.TREEWALKER_MODULE))
            {

                ConfigProperty prop = module.getProperty("tabWidth"); //$NON-NLS-1$

                String tabWidthProp = prop.getValue() != null ? prop.getValue() : prop
                        .getMetaData().getDefaultValue();
                try
                {
                    tabWidth = Integer.parseInt(tabWidthProp);
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }

        return new AdditionalConfigData(tabWidth, messages);
    }

    /**
     * SAX-Handler for parsing checkstyle configurations and building modules.
     * 
     * @author Lars Ködderitzsch
     */
    private static class ConfigurationHandler extends DefaultHandler
    {

        /** The list of modules. */
        private List mRules = new ArrayList();

        /** The current modules being built. */
        private Stack mCurrentStack = new Stack();

        public List getRules()
        {
            return mRules;
        }

        /**
         * {@inheritDoc}
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException
        {
            if (PUBLIC2INTERNAL_DTD_MAP.containsKey(publicId))
            {
                final String dtdResourceName = (String) PUBLIC2INTERNAL_DTD_MAP.get(publicId);
                final InputStream dtdIS = getClass().getClassLoader().getResourceAsStream(
                        dtdResourceName);
                if (dtdIS == null)
                {
                    throw new SAXException(NLS.bind(ErrorMessages.msgErrorLoadingCheckstyleDTD,
                            dtdResourceName));
                }
                return new InputSource(dtdIS);
            }
            // This is a hack to workaround problem with SAX
            // DefaultHeader.resolveEntity():
            // sometimes it throws SAX- and IO- exceptions
            // sometime SAX only :(
            try
            {
                if (false)
                {
                    throw new IOException(""); //$NON-NLS-1$
                }
                return super.resolveEntity(publicId, systemId);
            }
            catch (IOException e)
            {
                throw new SAXException("" + e, e); //$NON-NLS-1$
            }
        }

        /**
         * {@inheritDoc}
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {

            if (XMLTags.MODULE_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);

                RuleMetadata metadata = MetadataFactory.getRuleMetadata(name);
                Module module = null;
                if (metadata != null)
                {
                    module = new Module(metadata, true);
                }
                else
                {
                    module = new Module(name);
                }

                mRules.add(module);
                mCurrentStack.push(module);
            }
            else if (XMLTags.PROPERTY_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);
                String value = attributes.getValue(XMLTags.VALUE_TAG);

                Module module = (Module) mCurrentStack.peek();
                if (name.equals(XMLTags.SEVERITY_TAG) && module.getMetaData() != null
                        && module.getMetaData().hasSeverity())
                {
                    try
                    {
                        module.setSeverity(SeverityLevel.getInstance(value));
                    }
                    catch (IllegalArgumentException e)
                    {
                        module.setSeverity(SeverityLevel.WARNING);
                    }
                }
                else if (name.equals(XMLTags.ID_TAG))
                {
                    module.setId(StringUtils.trimToNull(value));
                }
                else if (module.getMetaData() != null)
                {
                    ConfigProperty property = module.getProperty(name);
                    if (property != null)
                    {
                        property.setValue(value);
                    }
                    // properties that are not within the meta data are omitted
                }
                else
                {
                    // if module has no meta data defined create property
                    ConfigProperty property = new ConfigProperty(name, value);
                    module.getProperties().add(property);
                }
            }
            else if (XMLTags.METADATA_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);
                String value = attributes.getValue(XMLTags.VALUE_TAG);

                Module module = (Module) mCurrentStack.peek();
                if (XMLTags.COMMENT_ID.equals(name))
                {
                    module.setComment(value);
                }
                else if (XMLTags.CUSTOM_MESSAGE_ID.equals(name))
                {
                    module.setCustomMessage(value);
                }
                else if (XMLTags.LAST_ENABLED_SEVERITY_ID.equals(name))
                {
                    module.setLastEnabledSeverity(SeverityLevel.getInstance(value));
                }
                else
                {
                    module.getCustomMetaData().put(name, value);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {

            if (XMLTags.MODULE_TAG.equals(qName))
            {

                // if the module has not metadata attached we create some
                // generic metadata
                Module module = (Module) mCurrentStack.pop();
                if (module.getMetaData() == null)
                {
                    MetadataFactory.createGenericMetadata(module);
                }
            }
        }
    }

    /**
     * Holds additional data about the Checkstyle configuration file, for
     * special uses.
     * 
     * @author Lars Koedderitzsch
     */
    public static class AdditionalConfigData
    {

        private int mTabWidth;

        private Map mCustomMessages;

        /**
         * @param tabWidth the tab width setting of the Checkstyle configuration
         * @param customMessages the custom messages defined in the
         *            configuration
         */
        public AdditionalConfigData(int tabWidth, Map customMessages)
        {
            super();
            mTabWidth = tabWidth;
            mCustomMessages = customMessages;
        }

        /**
         * The tab width of the check configuration.
         * 
         * @return the tab width setting
         */
        public int getTabWidth()
        {
            return mTabWidth;
        }

        /**
         * Returns the custom messages of the check configuration, keyed by
         * module id or module name if no id is set.
         * 
         * @return the custom messages
         */
        public Map getCustomMessages()
        {
            return mCustomMessages;
        }
    }
}