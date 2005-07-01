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

package com.atlassw.tools.eclipse.checkstyle.config.meta;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * This class is the factory for all Checkstyle rule metadata.
 */
public final class MetadataFactory
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    /** Metadata for the rule groups. */
    private static List sRuleGroupMetadata = new LinkedList();

    /** Metadata for all rules, keyed by internal rule name. */
    private static HashMap sRuleMetadata = new HashMap();

    /**
     * Mapping for all rules, keyed by alternative rule names (full qualified,
     * old full qualified).
     */
    private static HashMap sAlternativeNamesMap = new HashMap();

    /** the default severity level. */
    private static SeverityLevel sDefaultSeverity;

    /** Name of the rules metadata XML file. */
    private static final String METADATA_FILENAME = "/CheckstyleMetadata.xml"; //$NON-NLS-1$

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Private constructor to prevent instantiation.
     */
    private MetadataFactory()
    {}

    /**
     * Static initializer.
     */
    static
    {
        try
        {
            doInitialization();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.log(e);
        }
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Get a list of metadata objects for all rule groups.
     * 
     * @return List of <code>RuleGroupMetadata</code> objects.
     */
    public static List getRuleGroupMetadata()
    {
        return sRuleGroupMetadata;
    }

    /**
     * Get metadata for a check rule.
     * 
     * @param name The rule's name within the checkstyle configuration file.
     * 
     * @return The metadata.
     */
    public static RuleMetadata getRuleMetadata(String name)
    {

        RuleMetadata metadata = null;

        // first try the internal name mapping
        metadata = (RuleMetadata) sRuleMetadata.get(name);

        // try the alternative names
        if (metadata == null)
        {
            metadata = (RuleMetadata) sAlternativeNamesMap.get(name);
        }

        return metadata;
    }

    /**
     * Creates a set of generic metadata for a module that has no metadata
     * delivered with the plugin.
     * 
     * @param module the module
     */
    public static void createGenericMetadata(Module module)
    {

        String parent = null;
        try
        {

            Class checkClass = Class.forName(module.getName());
            Object moduleInstance = checkClass.newInstance();

            if (moduleInstance instanceof AbstractFileSetCheck)
            {
                parent = XMLTags.CHECKER_MODULE;
            }
            else
            {
                parent = XMLTags.TREEWALKER_MODULE;
            }
        }
        catch (Exception e)
        {
            // Ok we tried... default to TreeWalker
            parent = XMLTags.TREEWALKER_MODULE;
        }

        RuleMetadata ruleMeta = new RuleMetadata(module.getName(), module.getName(), parent,
                MetadataFactory.getDefaultSeverity(), false, true, true, null);

        module.setMetaData(ruleMeta);

        List properties = module.getProperties();
        int size = properties != null ? properties.size() : 0;
        for (int i = 0; i < size; i++)
        {

            ConfigProperty property = (ConfigProperty) properties.get(i);
            ConfigPropertyMetadata meta = new ConfigPropertyMetadata(ConfigPropertyType.STRING,
                    property.getName(), null);
            property.setMetaData(meta);
        }
    }

    /**
     * Returns the default severity level.
     * 
     * @return the default severity.
     */
    public static SeverityLevel getDefaultSeverity()
    {
        return sDefaultSeverity;
    }

    /**
     * Initializes the meta data from the xml file.
     * 
     * @throws CheckstylePluginException error loading the meta data file
     */
    private static void doInitialization() throws CheckstylePluginException
    {
        InputStream metadataStream = null;
        try
        {
            //
            // Get the metadata file's input stream.
            //
            metadataStream = new BufferedInputStream(MetadataFactory.class
                    .getResourceAsStream(METADATA_FILENAME));

            XMLUtil.parseWithSAX(metadataStream, new MetaDataHandler());
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
            try
            {
                metadataStream.close();
            }
            catch (Exception e)
            {
                // We tried to be nice and close the stream.
            }
        }
    }

    /**
     * SAX-Handler for parsing of the metadata file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class MetaDataHandler extends DefaultHandler
    {

        /** the current rule group. */
        private RuleGroupMetadata mCurrentGroup;

        /** the current rule meta data. */
        private RuleMetadata mCurrentRule;

        /** the current property. */
        private ConfigPropertyMetadata mCurrentProperty;

        /** flags if we're inside a description element. */
        private boolean mInDescriptionElement;

        /** StringBuffer containing the description. */
        private StringBuffer mDescription;

        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {
            try
            {

                if (XMLTags.CHECKSTYLE_METADATA_TAG.equals(qName))
                {
                    sDefaultSeverity = SeverityLevel.getInstance(attributes
                            .getValue(XMLTags.DEFAULT_SEVERITY_TAG));
                }
                else if (XMLTags.RULE_GROUP_METADATA_TAG.equals(qName))
                {

                    String groupName = attributes.getValue(XMLTags.NAME_TAG).trim();
                    boolean hidden = Boolean.valueOf(attributes.getValue(XMLTags.HIDDEN_TAG))
                            .booleanValue();

                    // Create the groups
                    mCurrentGroup = new RuleGroupMetadata(groupName, hidden);
                    sRuleGroupMetadata.add(mCurrentGroup);
                }
                else if (XMLTags.RULE_METADATA_TAG.equals(qName))
                {
                    // default severity
                    String defaultSeverity = attributes.getValue(XMLTags.DEFAULT_SEVERITY_TAG);
                    SeverityLevel severity = defaultSeverity == null
                            || defaultSeverity.trim().length() == 0 ? sDefaultSeverity
                            : SeverityLevel.getInstance(defaultSeverity);

                    String name = attributes.getValue(XMLTags.NAME_TAG).trim();
                    String internalName = attributes.getValue(XMLTags.INTERNAL_NAME_TAG).trim();
                    String parentName = attributes.getValue(XMLTags.PARENT_TAG) != null ? attributes
                            .getValue(XMLTags.PARENT_TAG).trim()
                            : null;
                    boolean hidden = Boolean.valueOf(attributes.getValue(XMLTags.HIDDEN_TAG))
                            .booleanValue();
                    boolean hasSeverity = !"false".equals(attributes //$NON-NLS-1$
                            .getValue(XMLTags.HAS_SEVERITY_TAG));
                    boolean deletable = !"false".equals(attributes.getValue(XMLTags.DELETABLE_TAG)); //$NON-NLS-1$

                    // create rule metadata
                    mCurrentRule = new RuleMetadata(name, internalName, parentName, severity,
                            hidden, hasSeverity, deletable, mCurrentGroup);
                    mCurrentGroup.getRuleMetadata().add(mCurrentRule);

                    // register internal name
                    sRuleMetadata.put(internalName, mCurrentRule);
                }
                else if (XMLTags.PROPERTY_METADATA_TAG.equals(qName))
                {
                    ConfigPropertyType type = ConfigPropertyType.getConfigPropertyType(attributes
                            .getValue(XMLTags.DATATYPE_TAG));

                    String name = attributes.getValue(XMLTags.NAME_TAG).trim();
                    String defaultValue = attributes.getValue(XMLTags.DEFAULT_VALUE_TAG);
                    if (defaultValue != null)
                    {
                        defaultValue = defaultValue.trim();
                    }

                    mCurrentProperty = new ConfigPropertyMetadata(type, name, defaultValue);

                    // add to current rule
                    mCurrentRule.getPropertyMetadata().add(mCurrentProperty);
                }
                else if (XMLTags.ALTERNATIVE_NAME_TAG.equals(qName))
                {
                    // register alternative name
                    sAlternativeNamesMap.put(attributes.getValue(XMLTags.INTERNAL_NAME_TAG),
                            mCurrentRule);
                    mCurrentRule.addAlternativeName(attributes.getValue(XMLTags.INTERNAL_NAME_TAG));
                }
                else if (XMLTags.DESCRIPTION_TAG.equals(qName))
                {
                    mInDescriptionElement = true;
                    mDescription = new StringBuffer();
                }
                else if (XMLTags.ENUMERATION_TAG.equals(qName))
                {
                    String optionProvider = attributes.getValue(XMLTags.OPTION_PROVIDER);
                    if (optionProvider != null)
                    {

                        Class providerClass = Class.forName(optionProvider);

                        IOptionProvider provider = (IOptionProvider) providerClass.newInstance();
                        mCurrentProperty.getPropertyEnumeration().addAll(provider.getOptions());
                    }
                }
                else if (XMLTags.PROPERTY_VALUE_OPTIONS_TAG.equals(qName))
                {
                    mCurrentProperty.getPropertyEnumeration().add(
                            attributes.getValue(XMLTags.VALUE_TAG));
                }
            }
            catch (CheckstylePluginException e)
            {
                throw new SAXException(e.getLocalizedMessage(), e);
            }
            catch (ClassNotFoundException e)
            {
                throw new SAXException(e.getLocalizedMessage(), e);
            }
            catch (InstantiationException e)
            {
                throw new SAXException(e.getLocalizedMessage(), e);
            }
            catch (IllegalAccessException e)
            {
                throw new SAXException(e.getLocalizedMessage(), e);
            }
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (XMLTags.RULE_METADATA_TAG.equals(qName))
            {
                mCurrentRule = null;
            }

            else if (XMLTags.PROPERTY_METADATA_TAG.equals(qName))
            {
                mCurrentProperty = null;
            }
            else if (XMLTags.DESCRIPTION_TAG.equals(qName))
            {
                mInDescriptionElement = false;
                // Set the description to the current element
                String description = mDescription.toString();
                if (mCurrentProperty != null)
                {
                    mCurrentProperty.setDescription(description);
                }
                else if (mCurrentRule != null)
                {
                    mCurrentRule.setDescription(description);
                }
            }
        }

        /**
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) throws SAXException
        {

            if (mInDescriptionElement)
            {
                mDescription.append(ch, start, length);
            }
        }
    }
}