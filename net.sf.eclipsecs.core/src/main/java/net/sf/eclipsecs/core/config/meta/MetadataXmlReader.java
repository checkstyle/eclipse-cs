//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.config.meta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

/**
 * Reader that parses checkstyle-metadata XML files into rule group metadata.
 *
 */
public final class MetadataXmlReader {

    /** Map containing the public - internal DTD mapping. */
    private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP =
        Map.of("-//eclipse-cs//DTD Check Metadata 1.0//EN",
            "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_0.dtd",
            "-//eclipse-cs//DTD Check Metadata 1.1//EN",
            "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_1.dtd");

    /**
     * Utility class, not intended to be instantiated.
     */
    private MetadataXmlReader() {

    }

    /**
     * Parses the metadata from the given stream.
     *
     * @param metadataStream
     *            the stream containing the metadata
     * @param metadataBundle
     *            the resource bundle for localization
     * @param groupId
     *            the id of the rule group
     * @return the parsed rule groups
     * @throws DocumentException
     *             the metadata document could not be read
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    public static Collection<RuleGroupMetadata> parseMetadata(InputStream metadataStream,
        ResourceBundle metadataBundle, String groupId)
            throws DocumentException, CheckstylePluginException {
        final Map<String, RuleGroupMetadata> groups = new HashMap<>();

        final SAXReader reader = new SAXReader();
        reader.setEntityResolver(new XMLUtil.InternalDtdEntityResolver(PUBLIC2INTERNAL_DTD_MAP));
        final Document document = reader.read(metadataStream);

        final List<Element> groupElements =
            document.getRootElement().elements(XMLTags.RULE_GROUP_METADATA_TAG);

        for (Element groupEl : groupElements) {

            var groupName = groupEl.attributeValue(XMLTags.NAME_TAG).trim();
            groupName = MetadataXmlReader.localize(groupName, metadataBundle);

            // process description
            String groupDesc = groupEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
            groupDesc = MetadataXmlReader.localize(groupDesc, metadataBundle);

            RuleGroupMetadata group = groups.get(groupName);

            if (group == null) {

                final boolean hidden =
                    Boolean.parseBoolean(groupEl.attributeValue(XMLTags.HIDDEN_TAG));
                int priority = 0;
                try {
                    priority = Integer.parseInt(groupEl.attributeValue(XMLTags.PRIORITY_TAG));
                }
                catch (NumberFormatException ex) {
                    CheckstyleLog.log(ex);
                    priority = Integer.MAX_VALUE;
                }

                group = new RuleGroupMetadata(groupId, groupName, groupDesc, hidden, priority);
                groups.put(groupName, group);
            }

            group.getRuleMetadata().addAll(processModules(groupEl, group, metadataBundle));
        }

        return groups.values();
    }

    /**
     * Processes the modules declared in the given group element.
     *
     * @param groupElement
     *            the group element
     * @param groupMetadata
     *            the group metadata
     * @param metadataBundle
     *            the resource bundle for localization
     * @return the list of rule metadata
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private static List<RuleMetadata> processModules(Element groupElement,
        RuleGroupMetadata groupMetadata, ResourceBundle metadataBundle)
            throws CheckstylePluginException {
        final List<RuleMetadata> modules = new ArrayList<>();

        final List<Element> moduleElements = groupElement.elements(XMLTags.RULE_METADATA_TAG);
        for (Element moduleEl : moduleElements) {
            // default severity
            final String defaultSeverity = moduleEl.attributeValue(XMLTags.DEFAULT_SEVERITY_TAG);
            final Severity severity;
            if (defaultSeverity == null || defaultSeverity.trim().length() == 0) {
                severity = Severity.INHERIT;
            }
            else {
                severity = Severity.fromXmlValue(defaultSeverity);
            }

            String name = moduleEl.attributeValue(XMLTags.NAME_TAG).trim();
            name = localize(name, metadataBundle);
            final String internalName = moduleEl.attributeValue(XMLTags.INTERNAL_NAME_TAG).trim();

            final String parentTag = moduleEl.attributeValue(XMLTags.PARENT_TAG);
            final String parentName;
            if (parentTag != null) {
                parentName = parentTag.trim();
            }
            else {
                parentName = null;
            }
            final boolean hidden =
                Boolean.parseBoolean(moduleEl.attributeValue(XMLTags.HIDDEN_TAG));
            final boolean hasSeverity = isNotFalse(moduleEl, XMLTags.HAS_SEVERITY_TAG);
            final boolean deletable = isNotFalse(moduleEl, XMLTags.DELETABLE_TAG);
            final boolean isSingleton =
                Boolean.parseBoolean(moduleEl.attributeValue(XMLTags.IS_SINGLETON_TAG));

            // process description
            String description = moduleEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
            description = localize(description, metadataBundle);

            // process alternative names
            final List<String> alternativeNames =
                moduleEl.elements(XMLTags.ALTERNATIVE_NAME_TAG).stream()
                    .map(altNameEl -> altNameEl.attributeValue(XMLTags.INTERNAL_NAME_TAG)).toList();

            // process message keys
            final List<String> messageKeys = moduleEl.elements(XMLTags.MESSAGEKEY_TAG).stream()
                .map(quickfixEl -> quickfixEl.attributeValue(XMLTags.KEY_TAG)).toList();

            // process properties
            final List<ConfigPropertyMetadata> properties =
                processProperties(moduleEl, metadataBundle);

            // create rule metadata
            modules.add(new RuleMetadata(
                new RuleIdentity(name, internalName, parentName, groupMetadata, description,
                    alternativeNames),
                severity, hidden, hasSeverity, deletable, isSingleton, messageKeys, properties));
        }
        return modules;
    }

    /**
     * Returns whether the given attribute is not set to false, i.e. an absent or non-"false"
     * attribute value yields true.
     *
     * @param element
     *            the element holding the attribute
     * @param attributeName
     *            the name of the boolean attribute
     * @return false only if the attribute value equals "false", otherwise true
     */
    private static boolean isNotFalse(Element element, String attributeName) {
        return !"false".equals(element.attributeValue(attributeName));
    }

    /**
     * Localizes the given candidate using the supplied resource bundle.
     *
     * @param localizationCandidate
     *            the candidate to localize
     * @param metadataBundle
     *            the resource bundle for localization
     * @return the localized value or the candidate if it could not be localized
     */
    private static String localize(String localizationCandidate, ResourceBundle metadataBundle) {
        String localized = localizationCandidate;
        if (metadataBundle != null && localizationCandidate != null
            && localizationCandidate.startsWith("%")) {
            try {
                localized = metadataBundle.getString(localizationCandidate.substring(1));
            }
            catch (MissingResourceException ex) {
                // no-op
            }
        }
        return localized;
    }

    /**
     * Processes the properties declared in the given module element.
     *
     * @param moduleElement
     *            the module element
     * @param metadataBundle
     *            the resource bundle for localization
     * @return the list of property metadata
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private static List<ConfigPropertyMetadata> processProperties(Element moduleElement,
        ResourceBundle metadataBundle) throws CheckstylePluginException {
        final List<ConfigPropertyMetadata> properties = new ArrayList<>();

        final List<Element> propertyElements =
            moduleElement.elements(XMLTags.PROPERTY_METADATA_TAG);
        for (Element propertyEl : propertyElements) {

            final ConfigPropertyType type =
                ConfigPropertyType.fromXmlValue(propertyEl.attributeValue(XMLTags.DATATYPE_TAG));

            final String name = propertyEl.attributeValue(XMLTags.NAME_TAG).trim();
            String defaultValue = propertyEl.attributeValue(XMLTags.DEFAULT_VALUE_TAG);
            if (defaultValue != null) {
                defaultValue = defaultValue.trim();
            }
            String overrideDefaultValue =
                propertyEl.attributeValue(XMLTags.DEFAULT_VALUE_OVERRIDE_TAG);
            if (overrideDefaultValue != null) {
                overrideDefaultValue = overrideDefaultValue.trim();
            }

            final ConfigPropertyMetadata property =
                new ConfigPropertyMetadata(type, name, defaultValue, overrideDefaultValue);

            properties.add(property);

            // get description
            String description = propertyEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
            description = localize(description, metadataBundle);
            property.setDescription(description);

            // get property enumeration values
            final Element enumEl = propertyEl.element(XMLTags.ENUMERATION_TAG);
            if (enumEl != null) {
                final String optionProvider = enumEl.attributeValue(XMLTags.OPTION_PROVIDER);
                if (optionProvider != null) {

                    try {
                        final Class<?> providerClass = CheckstylePlugin.getDefault()
                            .getAddonExtensionClassLoader().loadClass(optionProvider);

                        if (IOptionProvider.class.isAssignableFrom(providerClass)) {

                            final IOptionProvider provider = (IOptionProvider) providerClass
                                .getDeclaredConstructor().newInstance();
                            property.getPropertyEnumeration().addAll(provider.getOptions());
                        }
                        else if (Enum.class.isAssignableFrom(providerClass)) {

                            @SuppressWarnings({
                                "rawtypes", "unchecked"
                            })
                            final EnumSet<?> values = EnumSet.allOf((Class<Enum>) providerClass);
                            for (Enum<?> value : values) {
                                property.getPropertyEnumeration().add(value.name().toLowerCase());
                            }
                        }
                    }
                    catch (ReflectiveOperationException ex) {
                        CheckstylePluginException.rethrow(ex);
                    }

                }

                // get explicit enumeration option values
                enumEl.elements(XMLTags.PROPERTY_VALUE_OPTIONS_TAG).stream()
                    .map(optionEl -> optionEl.attributeValue(XMLTags.VALUE_TAG))
                    .forEach(property.getPropertyEnumeration()::add);
            }
        }
        return properties;
    }

}
