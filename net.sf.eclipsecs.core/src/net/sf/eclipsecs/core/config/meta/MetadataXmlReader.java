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

public class MetadataXmlReader {

  /** Map containing the public - internal DTD mapping. */
  private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP = Map.of(
          "-//eclipse-cs//DTD Check Metadata 1.0//EN",
          "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_0.dtd",
          "-//eclipse-cs//DTD Check Metadata 1.1//EN",
          "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_1.dtd");

  private MetadataXmlReader() {

  }

  public static Collection<RuleGroupMetadata> parseMetadata(InputStream metadataStream,
          ResourceBundle metadataBundle, String groupId)
          throws DocumentException, CheckstylePluginException {
    Map<String, RuleGroupMetadata> groups = new HashMap<>();

    SAXReader reader = new SAXReader();
    reader.setEntityResolver(new XMLUtil.InternalDtdEntityResolver(PUBLIC2INTERNAL_DTD_MAP));
    Document document = reader.read(metadataStream);

    List<Element> groupElements = document.getRootElement()
            .elements(XMLTags.RULE_GROUP_METADATA_TAG);

    for (Element groupEl : groupElements) {

      var groupName = groupEl.attributeValue(XMLTags.NAME_TAG).trim();
      groupName = MetadataXmlReader.localize(groupName, metadataBundle);

      // process description
      String groupDesc = groupEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
      groupDesc = MetadataXmlReader.localize(groupDesc, metadataBundle);

      RuleGroupMetadata group = groups.get(groupName);

      if (group == null) {

        boolean hidden = Boolean.parseBoolean(groupEl.attributeValue(XMLTags.HIDDEN_TAG));
        int priority = 0;
        try {
          priority = Integer.parseInt(groupEl.attributeValue(XMLTags.PRIORITY_TAG));
        } catch (Exception ex) {
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

  private static List<RuleMetadata> processModules(Element groupElement, RuleGroupMetadata groupMetadata,
          ResourceBundle metadataBundle) throws CheckstylePluginException {
    List<RuleMetadata> modules = new ArrayList<>();

    List<Element> moduleElements = groupElement.elements(XMLTags.RULE_METADATA_TAG);
    for (Element moduleEl : moduleElements) {
      // default severity
      String defaultSeverity = moduleEl.attributeValue(XMLTags.DEFAULT_SEVERITY_TAG);
      Severity severity = defaultSeverity == null || defaultSeverity.trim().length() == 0
              ? Severity.INHERIT
              : Severity.fromXmlValue(defaultSeverity);

      String name = moduleEl.attributeValue(XMLTags.NAME_TAG).trim();
      name = localize(name, metadataBundle);
      String internalName = moduleEl.attributeValue(XMLTags.INTERNAL_NAME_TAG).trim();

      String parentName = moduleEl.attributeValue(XMLTags.PARENT_TAG) != null
              ? moduleEl.attributeValue(XMLTags.PARENT_TAG).trim()
              : null;
      boolean hidden = Boolean.parseBoolean(moduleEl.attributeValue(XMLTags.HIDDEN_TAG));
      boolean hasSeverity = !"false".equals(moduleEl.attributeValue(XMLTags.HAS_SEVERITY_TAG));
      boolean deletable = !"false".equals(moduleEl.attributeValue(XMLTags.DELETABLE_TAG)); //$NON-NLS-1$
      boolean isSingleton = Boolean.parseBoolean(moduleEl.attributeValue(XMLTags.IS_SINGLETON_TAG));

      // process description
      String description = moduleEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
      description = localize(description, metadataBundle);

      // process alternative names
      List<String> alternativeNames = moduleEl.elements(XMLTags.ALTERNATIVE_NAME_TAG).stream()
              .map(altNameEl -> altNameEl.attributeValue(XMLTags.INTERNAL_NAME_TAG))
              .toList();

      // process message keys
      List<String> messageKeys = moduleEl.elements(XMLTags.MESSAGEKEY_TAG).stream()
              .map(quickfixEl -> quickfixEl.attributeValue(XMLTags.KEY_TAG))
              .toList();

      // process properties
      List<ConfigPropertyMetadata> properties = processProperties(moduleEl, metadataBundle);

      // create rule metadata
      modules.add(new RuleMetadata(
              new RuleIdentity(name, internalName, parentName, groupMetadata, description,
                      alternativeNames),
              severity, hidden, hasSeverity, deletable, isSingleton, messageKeys, properties));
    }
    return modules;
  }

  private static String localize(String localizationCandidate, ResourceBundle metadataBundle) {

    if (metadataBundle != null && localizationCandidate != null
            && localizationCandidate.startsWith("%")) {
      try {
        return metadataBundle.getString(localizationCandidate.substring(1));
      } catch (MissingResourceException ex) {
        return localizationCandidate;
      }
    }
    return localizationCandidate;
  }

  private static List<ConfigPropertyMetadata> processProperties(Element moduleElement,
          ResourceBundle metadataBundle) throws CheckstylePluginException {
    List<ConfigPropertyMetadata> properties = new ArrayList<>();

    List<Element> propertyElements = moduleElement.elements(XMLTags.PROPERTY_METADATA_TAG);
    for (Element propertyEl : propertyElements) {

      ConfigPropertyType type = ConfigPropertyType
              .fromXmlValue(propertyEl.attributeValue(XMLTags.DATATYPE_TAG));

      String name = propertyEl.attributeValue(XMLTags.NAME_TAG).trim();
      String defaultValue = propertyEl.attributeValue(XMLTags.DEFAULT_VALUE_TAG);
      if (defaultValue != null) {
        defaultValue = defaultValue.trim();
      }
      String overrideDefaultValue = propertyEl.attributeValue(XMLTags.DEFAULT_VALUE_OVERRIDE_TAG);
      if (overrideDefaultValue != null) {
        overrideDefaultValue = overrideDefaultValue.trim();
      }

      ConfigPropertyMetadata property = new ConfigPropertyMetadata(type, name, defaultValue,
              overrideDefaultValue);

      properties.add(property);

      // get description
      String description = propertyEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
      description = localize(description, metadataBundle);
      property.setDescription(description);

      // get property enumeration values
      Element enumEl = propertyEl.element(XMLTags.ENUMERATION_TAG);
      if (enumEl != null) {
        String optionProvider = enumEl.attributeValue(XMLTags.OPTION_PROVIDER);
        if (optionProvider != null) {

          try {
            Class<?> providerClass = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
                    .loadClass(optionProvider);

            if (IOptionProvider.class.isAssignableFrom(providerClass)) {

              IOptionProvider provider = (IOptionProvider) providerClass.getDeclaredConstructor().newInstance();
              property.getPropertyEnumeration().addAll(provider.getOptions());
            } else if (Enum.class.isAssignableFrom(providerClass)) {

              @SuppressWarnings({ "rawtypes", "unchecked" })
              EnumSet<?> values = EnumSet.allOf((Class<Enum>) providerClass);
              for (Enum<?> e : values) {
                property.getPropertyEnumeration().add(e.name().toLowerCase());
              }
            }
          } catch (ReflectiveOperationException ex) {
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
