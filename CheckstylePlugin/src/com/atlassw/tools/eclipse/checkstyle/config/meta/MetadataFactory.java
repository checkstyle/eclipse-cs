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

package com.atlassw.tools.eclipse.checkstyle.config.meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.builder.PackageNamesLoader;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.ICheckstyleMarkerResolution;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CustomLibrariesClassLoader;
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
    private static Map sRuleGroupMetadata;

    /** Metadata for all rules, keyed by internal rule name. */
    private static Map sRuleMetadata;

    /**
     * Mapping for all rules, keyed by alternative rule names (full qualified,
     * old full qualified).
     */
    private static Map sAlternativeNamesMap;

    /** the default severity level. */
    private static SeverityLevel sDefaultSeverity = SeverityLevel.WARNING;

    /** Name of the rules metadata XML file. */
    private static final String METADATA_FILENAME = "checkstyle-metadata.xml"; //$NON-NLS-1$

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
        refresh();
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
        List groups = new ArrayList(sRuleGroupMetadata.values());
        Collections.sort(groups, new Comparator()
        {

            public int compare(Object arg0, Object arg1)
            {
                int prio1 = ((RuleGroupMetadata) arg0).getPriority();
                int prio2 = ((RuleGroupMetadata) arg1).getPriority();

                return (prio1 < prio2 ? -1 : (prio1 == prio2 ? 0 : 1));
            }
        });

        return groups;
    }

    /**
     * Get metadata for a check rule.
     * 
     * @param name The rule's name within the checkstyle configuration file.
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
     * Returns the metadata for a rule group.
     * 
     * @param name the group name
     * @return the RuleGroupMetadata object or <code>null</code>
     */
    public static RuleGroupMetadata getRuleGroupMetadata(String name)
    {
        return (RuleGroupMetadata) sRuleGroupMetadata.get(name);
    }

    /**
     * Creates a set of generic metadata for a module that has no metadata
     * delivered with the plugin.
     * 
     * @param module the module
     * @return the generic metadata built
     */
    public static RuleMetadata createGenericMetadata(Module module)
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

        RuleGroupMetadata otherGroup = getRuleGroupMetadata(XMLTags.OTHER_GROUP);
        RuleMetadata ruleMeta = new RuleMetadata(module.getName(), module.getName(), parent,
                MetadataFactory.getDefaultSeverity(), false, true, true, false, otherGroup);
        module.setMetaData(ruleMeta);
        sRuleMetadata.put(ruleMeta.getInternalName(), ruleMeta);

        List properties = module.getProperties();
        int size = properties != null ? properties.size() : 0;
        for (int i = 0; i < size; i++)
        {

            ConfigProperty property = (ConfigProperty) properties.get(i);
            ConfigPropertyMetadata meta = new ConfigPropertyMetadata(ConfigPropertyType.STRING,
                    property.getName(), null, null);
            property.setMetaData(meta);
        }
        return ruleMeta;
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
     * Refreshes the metadata.
     */
    public static synchronized void refresh()
    {
        sRuleGroupMetadata = new TreeMap();
        sRuleMetadata = new HashMap();
        sAlternativeNamesMap = new HashMap();
        try
        {
            doInitialization();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.log(e);
        }
    }

    /**
     * Initializes the meta data from the xml file.
     * 
     * @throws CheckstylePluginException error loading the meta data file
     */
    private static void doInitialization() throws CheckstylePluginException
    {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {

            // get the classloader containing the extension libraries
            ClassLoader customsLoader = CustomLibrariesClassLoader.get();
            Thread.currentThread().setContextClassLoader(customsLoader);

            Collection potentialMetadataFiles = getAllPotentialMetadataFiles();
            Iterator it = potentialMetadataFiles.iterator();
            while (it.hasNext())
            {

                String metadataFile = (String) it.next();
                InputStream metadataStream = null;
                try
                {
                    metadataStream = customsLoader.getResourceAsStream(metadataFile);
                    if (metadataStream != null)
                    {
                        MetaDataHandler metadataHandler = new MetaDataHandler(
                                getMetadataI18NBundle(metadataFile));
                        XMLUtil.parseWithSAX(metadataStream, metadataHandler, true);
                    }
                }
                catch (SAXParseException e)
                {
                    CheckstyleLog.log(e, NLS.bind("Could not parse metadata file {0} at {1}:{2}", //$NON-NLS-1$
                            new Object[] { metadataFile, new Integer(e.getLineNumber()),
                                new Integer(e.getColumnNumber()) }));
                }
                catch (SAXException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile); //$NON-NLS-1$
                }
                catch (ParserConfigurationException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile); //$NON-NLS-1$
                }
                catch (IOException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile); //$NON-NLS-1$
                }
                finally
                {
                    IOUtils.closeQuietly(metadataStream);
                }
            }
        }
        finally
        {
            // restore the original classloader
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Helper method to get all potential metadata files using the
     * checkstyle_packages.xml as base where to look. It is not guaranteed that
     * the files returned acutally exist.
     * 
     * @return the collection of potential metadata files.
     * @throws CheckstylePluginException an unexpected exception ocurred
     */
    private static Collection getAllPotentialMetadataFiles() throws CheckstylePluginException
    {

        Collection potentialMetadataFiles = new ArrayList();

        List packages = PackageNamesLoader.getPackageNames(CustomLibrariesClassLoader.get());

        for (int i = 0, size = packages.size(); i < size; i++)
        {
            String packageName = (String) packages.get(i);
            String metaFileLocation = packageName.replace('.', '/') + METADATA_FILENAME;
            potentialMetadataFiles.add(metaFileLocation);
        }

        return potentialMetadataFiles;
    }

    /**
     * Returns the ResourceBundle for the given meta data file contained i18n'ed
     * names and descriptions.
     * 
     * @param metadataFile
     * @return the corresponding ResourceBundle for the metadata file or
     *         <code>null</code> if none exists
     */
    private static ResourceBundle getMetadataI18NBundle(String metadataFile)
    {
        String bundle = metadataFile.substring(0, metadataFile.length() - 4).replace('/', '.');
        try
        {
            return PropertyResourceBundle.getBundle(bundle);
        }
        catch (MissingResourceException e)
        {
            return null;
        }
    }

    /**
     * SAX-Handler for parsing of the metadata file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class MetaDataHandler extends DefaultHandler
    {

        private static final String DTD_PUBLIC_ID = "-//eclipse-cs//DTD Check Metadata 1.0//EN"; //$NON-NLS-1$

        private static final String DTD_RESOURCE_NAME = "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_0.dtd"; //$NON-NLS-1$

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

        private ResourceBundle mI18NBundle;

        // /**
        // * @see
        // org.xml.sax.ext.EntityResolver2#getExternalSubset(java.lang.String,
        // * java.lang.String)
        // */
        // public InputSource getExternalSubset(String name, String baseURI)
        // throws SAXException,
        // IOException
        // {
        //
        // InputStream dtdIS =
        // getClass().getClassLoader().getResourceAsStream(DTD_RESOURCE_NAME);
        // return new InputSource(dtdIS);
        // }

        public MetaDataHandler(ResourceBundle i18nBundle)
        {
            mI18NBundle = i18nBundle;
        }

        /*
         * 
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException
        {
            if (DTD_PUBLIC_ID.equals(publicId))
            {

                final InputStream dtdIS = getClass().getClassLoader().getResourceAsStream(
                        DTD_RESOURCE_NAME);
                if (dtdIS == null)
                {
                    throw new SAXException("Unable to load internal dtd " + DTD_RESOURCE_NAME); //$NON-NLS-1$
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
            try
            {

                if (XMLTags.RULE_GROUP_METADATA_TAG.equals(qName))
                {

                    String groupName = attributes.getValue(XMLTags.NAME_TAG).trim();
                    groupName = localize(groupName);

                    mCurrentGroup = getRuleGroupMetadata(groupName);

                    if (mCurrentGroup == null)
                    {

                        boolean hidden = Boolean.valueOf(attributes.getValue(XMLTags.HIDDEN_TAG))
                                .booleanValue();
                        int priority = 0;
                        try
                        {
                            priority = Integer.parseInt(attributes.getValue(XMLTags.PRIORITY_TAG));
                        }
                        catch (Exception e)
                        {
                            CheckstyleLog.log(e);
                            priority = Integer.MAX_VALUE;
                        }

                        // Create the groups
                        mCurrentGroup = new RuleGroupMetadata(groupName, hidden, priority);
                        sRuleGroupMetadata.put(groupName, mCurrentGroup);
                    }
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
                    boolean isSingleton = Boolean.valueOf(
                            attributes.getValue(XMLTags.IS_SINGLETON_TAG)).booleanValue();

                    // create rule metadata
                    mCurrentRule = new RuleMetadata(localize(name), internalName, parentName,
                            severity, hidden, hasSeverity, deletable, isSingleton, mCurrentGroup);
                    mCurrentGroup.getRuleMetadata().add(mCurrentRule);

                    // register internal name
                    sRuleMetadata.put(internalName, mCurrentRule);
                }
                else if (XMLTags.PROPERTY_METADATA_TAG.equals(qName))
                {
                    ConfigPropertyType type = ConfigPropertyType.getConfigPropertyType(attributes
                            .getValue(XMLTags.DATATYPE_TAG));

                    String name = attributes.getValue(XMLTags.NAME_TAG).trim();
                    String defaultValue = StringUtils.trim(attributes
                            .getValue(XMLTags.DEFAULT_VALUE_TAG));
                    String overrideDefaultValue = StringUtils.trim(attributes
                            .getValue(XMLTags.DEFAULT_VALUE_OVERRIDE_TAG));

                    mCurrentProperty = new ConfigPropertyMetadata(type, name, defaultValue,
                            overrideDefaultValue);

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
                else if (XMLTags.QUCKFIX_TAG.equals(qName))
                {
                    String className = attributes.getValue(XMLTags.CLASSNAME_TAG);

                    Class quickfixClass = Class.forName(className);

                    ICheckstyleMarkerResolution quickfix = (ICheckstyleMarkerResolution) quickfixClass
                            .newInstance();
                    mCurrentRule.addQuickfix(quickfix);
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
                    mCurrentProperty.setDescription(localize(description));
                }
                else if (mCurrentRule != null)
                {
                    mCurrentRule.setDescription(localize(description));
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

        /**
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException e) throws SAXException
        {
            throw e;
        }

        private String localize(String localizationCandidate)
        {

            if (mI18NBundle != null && localizationCandidate.startsWith("%"))
            {
                try
                {
                    return mI18NBundle.getString(localizationCandidate.substring(1));
                }
                catch (MissingResourceException e)
                {
                    return localizationCandidate;
                }
            }
            return localizationCandidate;
        }

    }
}
