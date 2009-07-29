//============================================================================
//
// Copyright (C) 2002-2009  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;
import org.dom4j.io.SAXReader;

/**
 * Utitlity class to read a checkstyle configuration and transform to the
 * plugins module objects.
 * 
 * @author Lars Ködderitzsch
 */
public final class ConfigurationReader {

    private static final Pattern PROPERTY_REF_PATTERN = Pattern
        .compile("^.*\\$\\{.*\\}.*$");

    //
    // Mapping from public DTD to internal dtd resource.
    // @see com.puppycrawl.tools.checkstyle.api.AbstractLoader
    //

    /** Map containing the public - internal DTD mapping. */
    private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP = new HashMap<String, String>();

    static {

        PUBLIC2INTERNAL_DTD_MAP.put(
            "-//Puppy Crawl//DTD Check Configuration 1.0//EN", //$NON-NLS-1$
            "com/puppycrawl/tools/checkstyle/configuration_1_0.dtd"); //$NON-NLS-1$
        PUBLIC2INTERNAL_DTD_MAP.put(
            "-//Puppy Crawl//DTD Check Configuration 1.1//EN", //$NON-NLS-1$
            "com/puppycrawl/tools/checkstyle/configuration_1_1.dtd"); //$NON-NLS-1$
        PUBLIC2INTERNAL_DTD_MAP.put(
            "-//Puppy Crawl//DTD Check Configuration 1.2//EN", //$NON-NLS-1$
            "com/puppycrawl/tools/checkstyle/configuration_1_2.dtd"); //$NON-NLS-1$
        PUBLIC2INTERNAL_DTD_MAP.put(
            "-//Puppy Crawl//DTD Check Configuration 1.3//EN", //$NON-NLS-1$
            "com/puppycrawl/tools/checkstyle/configuration_1_3.dtd"); //$NON-NLS-1$
    }

    //
    // constructors
    //

    /** Hidden default constructor to prevent instantiation. */
    private ConfigurationReader() {
        // NOOP
    }

    //
    // methods
    //

    /**
     * Reads the checkstyle configuration from the given stream an returs a list
     * of all modules within this configuration.
     * 
     * @param in
     *            the stream the configuration is loaded from
     * @return the list of modules
     * @throws CheckstylePluginException
     *             error while reading the configuration
     */
    public static List<Module> read(InputStream in)
        throws CheckstylePluginException {

        List<Module> rules = null;
        try {

            SAXReader reader = new SAXReader();
            reader.setEntityResolver(new XMLUtil.InternalDtdEntityResolver(
                PUBLIC2INTERNAL_DTD_MAP));
            Document document = reader.read(in);

            rules = getModules(document);
        }
        catch (DocumentException ex) {
            CheckstylePluginException.rethrow(ex);
        }

        return rules != null ? rules : new ArrayList<Module>();
    }

    /**
     * Gets additional data about the Checkstyle configuration. This data is
     * used by the plugin for special purposes, like determining the correct
     * offset of a checkstyle violation.
     * 
     * @param in
     *            the input stream
     * @return the additional configuration data
     * @throws CheckstylePluginException
     *             error while reading the configuration
     */
    public static AdditionalConfigData getAdditionalConfigData(InputStream in)
        throws CheckstylePluginException {

        List<Module> modules = read(in);

        int tabWidth = 8;

        for (Module module : modules) {

            if (module.getMetaData() != null
                && module.getMetaData().getInternalName().equals(
                    XMLTags.TREEWALKER_MODULE)) {

                ConfigProperty prop = module.getProperty("tabWidth"); //$NON-NLS-1$

                String tabWidthProp = prop != null && prop.getValue() != null ? prop
                    .getValue()
                    : prop.getMetaData().getDefaultValue();
                try {
                    tabWidth = Integer.parseInt(tabWidthProp);
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }

        return new AdditionalConfigData(tabWidth);
    }

    private static List<Module> getModules(Document document) {

        final List<Module> modules = new ArrayList<Module>();

        document.accept(new VisitorSupport() {

            @Override
            public void visit(Element node) {

                if (XMLTags.MODULE_TAG.equals(node.getName())) {

                    String name = node.attributeValue(XMLTags.NAME_TAG);

                    RuleMetadata metadata = MetadataFactory
                        .getRuleMetadata(name);
                    Module module = null;
                    if (metadata != null) {
                        module = new Module(metadata, true);
                    }
                    else {
                        module = new Module(name);
                    }

                    addProperties(node, module);
                    addMessages(node, module);
                    addMetadata(node, module);

                    // if the module has not metadata attached we create some
                    // generic metadata
                    if (module.getMetaData() == null) {
                        MetadataFactory.createGenericMetadata(module);
                    }

                    modules.add(module);
                }
            }
        });
        return modules;
    }

    private static void addProperties(Element moduleEl, Module module) {

        @SuppressWarnings("unchecked")
        List<Element> propertyEls = moduleEl.elements(XMLTags.PROPERTY_TAG);

        for (Element propertyEl : propertyEls) {

            String name = propertyEl.attributeValue(XMLTags.NAME_TAG);
            String value = propertyEl.attributeValue(XMLTags.VALUE_TAG);

            boolean isPropertyRef = value != null
                && PROPERTY_REF_PATTERN.matcher(value).matches();

            if (name.equals(XMLTags.SEVERITY_TAG)
                && module.getMetaData() != null
                && module.getMetaData().hasSeverity()) {
                try {
                    module.setSeverity(Severity.valueOf(value));
                }
                catch (IllegalArgumentException e) {
                    module.setSeverity(Severity.warning);
                }
            }
            else if (name.equals(XMLTags.ID_TAG)) {
                module.setId(StringUtils.trimToNull(value));
            }
            else if (module.getMetaData() != null) {

                ConfigProperty property = module.getProperty(name);
                if (property != null) {
                    property.setValue(value);
                    property.setPropertyReference(isPropertyRef);
                }

                // properties that are not within the meta data are omitted
            }
            else {
                // if module has no meta data defined create property
                ConfigProperty property = new ConfigProperty(name, value);
                property.setPropertyReference(isPropertyRef);
                module.getProperties().add(property);
            }
        }
    }

    private static void addMessages(Element moduleEl, Module module) {

        @SuppressWarnings("unchecked")
        List<Element> messageEls = moduleEl.elements(XMLTags.MESSAGE_TAG);

        for (Element messageEl : messageEls) {

            String key = messageEl.attributeValue(XMLTags.KEY_TAG);
            String value = messageEl.attributeValue(XMLTags.VALUE_TAG);

            module.getCustomMessages().put(key, value);
        }
    }

    private static void addMetadata(Element moduleEl, Module module) {

        @SuppressWarnings("unchecked")
        List<Element> metaEls = moduleEl.elements(XMLTags.METADATA_TAG);

        for (Element metaEl : metaEls) {

            String name = metaEl.attributeValue(XMLTags.NAME_TAG);
            String value = metaEl.attributeValue(XMLTags.VALUE_TAG);

            if (XMLTags.COMMENT_ID.equals(name)) {
                module.setComment(value);
            }
            else if (XMLTags.LAST_ENABLED_SEVERITY_ID.equals(name)) {
                module.setLastEnabledSeverity(Severity.valueOf(value));
            }
            else {
                module.getCustomMetaData().put(name, value);
            }
        }
    }

    /**
     * Holds additional data about the Checkstyle configuration file, for
     * special uses.
     * 
     * @author Lars Koedderitzsch
     */
    public static class AdditionalConfigData {

        private final int mTabWidth;

        /**
         * @param tabWidth
         *            the tab width setting of the Checkstyle configuration
         */
        public AdditionalConfigData(int tabWidth) {
            super();
            mTabWidth = tabWidth;
        }

        /**
         * The tab width of the check configuration.
         * 
         * @return the tab width setting
         */
        public int getTabWidth() {
            return mTabWidth;
        }
    }
}
