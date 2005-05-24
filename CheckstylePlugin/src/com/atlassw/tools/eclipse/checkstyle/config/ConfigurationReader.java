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

package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

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
    //NOOP
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
     * SAX-Handler for parsing checkstyle configurations and building modules.
     * 
     * @author Lars Ködderitzsch
     */
    private static class ConfigurationHandler extends DefaultHandler
    {

        /** The list of modules. */
        private List mRules = new ArrayList();

        /** The current module being built. */
        private Module mCurrentModule;

        public List getRules()
        {
            return mRules;
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String,
         *      java.lang.String)
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
                    throw new SAXException(NLS.bind(ErrorMessages.errorResolveConfigLocation,
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
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {

            if (XMLTags.MODULE_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);

                RuleMetadata metadata = MetadataFactory.getRuleMetadata(name);

                if (metadata != null)
                {
                    mCurrentModule = new Module(metadata);
                }
                else
                {
                    mCurrentModule = new Module(name);
                }

                mRules.add(mCurrentModule);
            }
            else if (XMLTags.PROPERTY_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);
                String value = attributes.getValue(XMLTags.VALUE_TAG);

                if (name.equals(XMLTags.SEVERITY_TAG) && mCurrentModule.getMetaData() != null
                        && mCurrentModule.getMetaData().hasSeverity())
                {
                    mCurrentModule.setSeverity(SeverityLevel.getInstance(value));
                }
                else if (mCurrentModule.getMetaData() != null)
                {
                    ConfigProperty property = mCurrentModule.getProperty(name);
                    if (property != null)
                    {
                        property.setValue(value);
                    }
                    //properties that are not within the meta data are omitted
                }
                else
                {
                    //if module has no meta data defined create property
                    ConfigProperty property = new ConfigProperty(name, value);
                    mCurrentModule.getProperties().add(property);
                }
            }
            else if (XMLTags.METADATA_TAG.equals(qName))
            {
                String name = attributes.getValue(XMLTags.NAME_TAG);
                String value = attributes.getValue(XMLTags.VALUE_TAG);

                if (XMLTags.COMMENT_ID.equals(name))
                {
                    mCurrentModule.setComment(value);
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {

            if (XMLTags.MODULE_TAG.equals(qName))
            {

                //if the module has not metadata attached we create some
                // generic metadata
                if (mCurrentModule.getMetaData() == null)
                {
                    MetadataFactory.createGenericMetadata(mCurrentModule);
                }
            }
        }
    }

}