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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CustomLibrariesClassLoader;
import com.atlassw.tools.eclipse.checkstyle.util.XMLUtil;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractLoader;
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

    /**
     * Name of default checkstyle package names resource file. The file must be
     * in the classpath.
     */
    private static final String DEFAULT_PACKAGES = "/com/puppycrawl/tools/checkstyle/checkstyle_packages.xml";

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
        sRuleMetadata.put(ruleMeta.getInternalName(), ruleMeta);

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

            MetaDataHandler metadataHandler = new MetaDataHandler();

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
                        XMLUtil.parseWithSAX(metadataStream, metadataHandler, true);
                    }
                }
                catch (SAXParseException e)
                {
                    CheckstyleLog.log(e, NLS.bind("Could not parse metadata file {0} at {1}:{2}",
                            new Object[] { metadataFile, new Integer(e.getLineNumber()),
                                new Integer(e.getColumnNumber()) }));
                }
                catch (SAXException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile);
                }
                catch (ParserConfigurationException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile);
                }
                catch (IOException e)
                {
                    CheckstyleLog.log(e, "Could not read metadata " + metadataFile);
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

        Collection potentialMetadataFiles = null;

        InputStream packageFileStream = null;

        try
        {
            PackageNamesHandler packagesHandler = new PackageNamesHandler();
            XMLUtil.parseWithSAX(getPackageNamesFileStream(), packagesHandler, true);

            potentialMetadataFiles = packagesHandler.getPotentialMetadataFiles();
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
                packageFileStream.close();
            }
            catch (Exception e)
            {
                // We tried to be nice and close the stream.
            }
        }
        return potentialMetadataFiles;
    }

    /**
     * Gets a stream to the checkstyle_packages.xml file. Either the custom
     * checkstyle_package.xml inside the extension-libraries folder is returned
     * if specified or the default packages file from checkstyle is loaded.
     * 
     * @return the input stream containing the checkstyle_packages.xml
     * @throws IOException error reading the packages file
     */
    private static InputStream getPackageNamesFileStream() throws IOException
    {
        // load the package name file
        URL packagesFile = CheckstylePlugin.getDefault().find(
                new Path(CheckstylePlugin.PACKAGE_NAMES_FILE));

        if (packagesFile != null)
        {
            packagesFile = Platform.resolve(packagesFile);
        }
        else
        {
            packagesFile = Checker.class.getResource(DEFAULT_PACKAGES);
        }

        return new BufferedInputStream(packagesFile.openStream());
    }

    /**
     * SAX-Handler for parsing of the metadata file.
     * 
     * @author Lars Ködderitzsch
     */
    private static class MetaDataHandler extends DefaultHandler implements EntityResolver2
    {

        private static final String DTD_PUBLIC_ID = "-//eclipse-cs//DTD Check Metadata 1.0//EN";

        private static final String DTD_RESOURCE_NAME = "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_0.dtd";

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
         * @see org.xml.sax.ext.EntityResolver2#getExternalSubset(java.lang.String,
         *      java.lang.String)
         */
        public InputSource getExternalSubset(String name, String baseURI) throws SAXException,
            IOException
        {

            InputStream dtdIS = getClass().getClassLoader().getResourceAsStream(DTD_RESOURCE_NAME);
            return new InputSource(dtdIS);
        }

        /**
         * @see org.xml.sax.ext.EntityResolver2#resolveEntity(java.lang.String,
         *      java.lang.String, java.lang.String, java.lang.String)
         */
        public InputSource resolveEntity(String name, String publicId, String baseURI,
                String systemId) throws SAXException, IOException
        {
            if (DTD_PUBLIC_ID.equals(publicId))
            {

                final InputStream dtdIS = getClass().getClassLoader().getResourceAsStream(
                        DTD_RESOURCE_NAME);
                if (dtdIS == null)
                {
                    throw new SAXException("Unable to load internal dtd " + DTD_RESOURCE_NAME);
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
                    throw new IOException("");
                }
                return super.resolveEntity(publicId, systemId);
            }
            catch (IOException e)
            {
                throw new SAXException("" + e, e);
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

        /**
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException e) throws SAXException
        {
            throw e;
        }
    }

    /**
     * SAX-Handler able to read the checkstyle_packages.xml file and to build
     * all potential metadata locations.
     * 
     * @author Lars Ködderitzsch
     */
    private static class PackageNamesHandler extends AbstractLoader
    {

        //
        // attributes
        //

        /** the public ID for the configuration dtd. */
        private static final String DTD_PUBLIC_ID = "-//Puppy Crawl//DTD Package Names 1.0//EN";

        /** the resource for the configuration dtd. */
        private static final String DTD_RESOURCE_NAME = "com/puppycrawl/tools/checkstyle/packages_1_0.dtd";

        /** the package stack. */
        private Stack mPackageStack = new Stack();

        /** the list of potential locations for checkstyle-metadata files. */
        private Collection mMetadataLocations = new TreeSet();

        /**
         * Creates the package names handler.
         * 
         * @throws ParserConfigurationException no one will ever know
         * @throws SAXException no one will ever know
         */
        public PackageNamesHandler() throws SAXException, ParserConfigurationException
        {
            super(DTD_PUBLIC_ID, DTD_RESOURCE_NAME);
        }

        /**
         * Returns the collection of potiential metadata file locations based on
         * the checkstyle_packages.xml.
         * 
         * @return the potential metatdata locations
         */
        public Collection getPotentialMetadataFiles()
        {
            return mMetadataLocations;
        }

        /**
         * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
         *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
        {

            if (XMLTags.PACKAGE_TAG.equals(qName))
            {
                String packageName = attributes.getValue(XMLTags.NAME_TAG);
                mPackageStack.push(packageName);

                String fullPackage = getPackageName();
                String metaFileLocation = fullPackage.replace('.', '/') + METADATA_FILENAME;
                mMetadataLocations.add(metaFileLocation);
            }
        }

        /**
         * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
         *      java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (XMLTags.PACKAGE_TAG.equals(qName))
            {
                mPackageStack.pop();
            }
        }

        /**
         * Creates a full package name from the package names on the stack.
         * 
         * @return the full name of the current package.
         */
        private String getPackageName()
        {
            final StringBuffer buf = new StringBuffer();
            final Iterator it = mPackageStack.iterator();
            while (it.hasNext())
            {
                final String subPackage = (String) it.next();
                buf.append(subPackage);
                if (!subPackage.endsWith("."))
                {
                    buf.append(".");
                }
            }
            return buf.toString();
        }
    }
}