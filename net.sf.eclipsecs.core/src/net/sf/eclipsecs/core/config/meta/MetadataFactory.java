//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.config.meta;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeMap;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.core.util.XMLUtil;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * This class is the factory for all Checkstyle rule metadata.
 */
public final class MetadataFactory {

  /** Map containing the public - internal DTD mapping. */
  private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP = new HashMap<>();

  /** Metadata for the rule groups. */
  private static Map<String, RuleGroupMetadata> sRuleGroupMetadata;

  /** Metadata for all rules, keyed by internal rule name. */
  private static Map<String, RuleMetadata> sRuleMetadata;

  /**
   * Mapping for all rules, keyed by alternative rule names (full qualified, old full qualified).
   */
  private static Map<String, RuleMetadata> sAlternativeNamesMap;

  /** Name of the rules metadata XML file. */
  private static final String METADATA_FILENAME = "checkstyle-metadata.xml"; //$NON-NLS-1$

  /**
   * Private constructor to prevent instantiation.
   */
  private MetadataFactory() {
  }

  /**
   * Static initializer.
   */
  static {
    PUBLIC2INTERNAL_DTD_MAP.put("-//eclipse-cs//DTD Check Metadata 1.0//EN", //$NON-NLS-1$
            "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_0.dtd"); //$NON-NLS-1$
    PUBLIC2INTERNAL_DTD_MAP.put("-//eclipse-cs//DTD Check Metadata 1.1//EN", //$NON-NLS-1$
            "/com/puppycrawl/tools/checkstyle/checkstyle-metadata_1_1.dtd"); //$NON-NLS-1$

    refresh();
  }

  /**
   * Get a list of metadata objects for all rule groups.
   *
   * @return List of <code>RuleGroupMetadata</code> objects.
   */
  public static List<RuleGroupMetadata> getRuleGroupMetadata() {

    List<RuleGroupMetadata> groups = new ArrayList<>(sRuleGroupMetadata.values());
    Collections.sort(groups, new Comparator<RuleGroupMetadata>() {

      @Override
      public int compare(RuleGroupMetadata arg0, RuleGroupMetadata arg1) {
        int prio1 = arg0.getPriority();
        int prio2 = arg1.getPriority();

        return (prio1 < prio2 ? -1 : (prio1 == prio2 ? 0 : 1));
      }
    });

    return groups;
  }

  /**
   * Returns the metadata for a rule group.
   *
   * @param name
   *          the group name
   * @return the RuleGroupMetadata object or <code>null</code>
   */
  public static RuleGroupMetadata getRuleGroupMetadata(String name) {
    return sRuleGroupMetadata.get(name);
  }

  /**
   * Get metadata for a check rule.
   *
   * @param name
   *          The rule's name within the checkstyle configuration file.
   * @return The metadata.
   */
  public static RuleMetadata getRuleMetadata(String name) {

    RuleMetadata metadata = null;

    // first try the internal name mapping
    metadata = sRuleMetadata.get(name);

    // try the alternative names
    if (metadata == null) {
      metadata = sAlternativeNamesMap.get(name);
    }

    return metadata;
  }

  /**
   * Creates a set of generic metadata for a module that has no metadata delivered with the plugin.
   *
   * @param module
   *          the module
   * @return the generic metadata built
   */
  public static RuleMetadata createGenericMetadata(Module module) {

    String parent = null;
    try {

      Class<?> checkClass = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
              .loadClass(module.getName());

      Object moduleInstance = checkClass.newInstance();

      if (moduleInstance instanceof AbstractFileSetCheck) {
        parent = XMLTags.CHECKER_MODULE;
      } else {
        parent = XMLTags.TREEWALKER_MODULE;
      }
    } catch (Exception e) {
      // Ok we tried... default to TreeWalker
      parent = XMLTags.TREEWALKER_MODULE;
    }

    RuleGroupMetadata otherGroup = getRuleGroupMetadata(XMLTags.OTHER_GROUP);
    RuleMetadata ruleMeta = new RuleMetadata(module.getName(), module.getName(), parent,
            MetadataFactory.getDefaultSeverity(), false, true, true, false, otherGroup);
    module.setMetaData(ruleMeta);
    sRuleMetadata.put(ruleMeta.getInternalName(), ruleMeta);

    List<ConfigProperty> properties = module.getProperties();
    int size = properties != null ? properties.size() : 0;
    for (int i = 0; i < size; i++) {

      ConfigProperty property = properties.get(i);
      ConfigPropertyMetadata meta = new ConfigPropertyMetadata(ConfigPropertyType.String,
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
  public static Severity getDefaultSeverity() {
    return Severity.inherit;
  }

  /**
   * Returns Checkstyles standard message for a given module name and message key.
   *
   * @param messageKey
   *          the message key
   * @param moduleInternalName
   *          the module name
   * @return Checkstyles standard message for this module and key
   */
  public static String getStandardMessage(String messageKey, String moduleInternalName) {

    RuleMetadata rule = getRuleMetadata(moduleInternalName);
    return getStandardMessage(messageKey, rule);
  }

  /**
   * Returns Checkstyles standard message for a given module and message key.
   *
   * @param messageKey
   *          the message key
   * @param rule
   *          the module metadata
   * @return Checkstyles standard message for this module and key
   */
  public static String getStandardMessage(String messageKey, RuleMetadata rule) {

    // for some unknown reason there is no metadata or key
    if (messageKey == null || rule == null) {
      return null;
    }

    List<String> namesToCheck = new ArrayList<>();
    namesToCheck.add(rule.getInternalName());
    namesToCheck.addAll(rule.getAlternativeNames());

    for (String moduleClass : namesToCheck) {
      try {

        int endIndex = moduleClass.lastIndexOf('.');
        String messages = "messages"; //$NON-NLS-1$
        if (endIndex >= 0) {
          String packageName = moduleClass.substring(0, endIndex);
          messages = packageName + "." + messages; //$NON-NLS-1$
        }
        ResourceBundle resourceBundle = ResourceBundle.getBundle(messages,
                CheckstylePlugin.getPlatformLocale(), CheckstylePlugin.class.getClassLoader(),
                new UTF8Control());

        String message = resourceBundle.getString(messageKey);
        return message;
      } catch (MissingResourceException e) {
        // let's continue to check the other alternative names
      }
    }
    return null;
  }

  /**
   * Custom ResourceBundle.Control implementation which allows explicitly read the properties files
   * as UTF-8.
   *
   * @author <a href="mailto:nesterenko-aleksey@list.ru">Aleksey Nesterenko</a>
   */
  private static class UTF8Control extends Control {
    @Override
    public ResourceBundle newBundle(String aBaseName, Locale aLocale, String aFormat,
            ClassLoader aLoader, boolean aReload) throws IOException {
      // The below is a copy of the default implementation.
      final String bundleName = toBundleName(aBaseName, aLocale);
      final String resourceName = toResourceName(bundleName, "properties");
      ResourceBundle bundle = null;
      InputStream stream = null;
      if (aReload) {
        final URL url = aLoader.getResource(resourceName);
        if (url != null) {
          final URLConnection connection = url.openConnection();
          if (connection != null) {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      } else {
        stream = aLoader.getResourceAsStream(resourceName);
      }
      if (stream != null) {
        Reader streamReader = null;
        try {
          streamReader = new InputStreamReader(stream, "UTF-8");
          // Only this line is changed to make it to read properties files as
          // UTF-8.
          bundle = new PropertyResourceBundle(streamReader);
        } finally {
          streamReader.close();
          stream.close();
        }
      }
      return bundle;
    }
  }

  /**
   * Refreshes the metadata.
   */
  public static synchronized void refresh() {
    sRuleGroupMetadata = new TreeMap<>();
    sRuleMetadata = new HashMap<>();
    sAlternativeNamesMap = new HashMap<>();
    try {
      doInitialization();
    } catch (CheckstylePluginException e) {
      CheckstyleLog.log(e);
    }
  }

  /**
   * Initializes the meta data from the xml file.
   *
   * @throws CheckstylePluginException
   *           error loading the meta data file
   */
  private static void doInitialization() throws CheckstylePluginException {

    ClassLoader classLoader = CheckstylePlugin.getDefault().getAddonExtensionClassLoader();
    Collection<String> potentialMetadataFiles = getAllPotentialMetadataFiles(classLoader);
    for (String metadataFile : potentialMetadataFiles) {

      try (InputStream metadataStream = classLoader.getResourceAsStream(metadataFile)) {

        if (metadataStream != null) {

          ResourceBundle metadataBundle = getMetadataI18NBundle(metadataFile, classLoader);
          parseMetadata(metadataStream, metadataBundle);
        }
      } catch (DocumentException | IOException e) {
        CheckstyleLog.log(e, "Could not read metadata " + metadataFile); //$NON-NLS-1$
      }
    }
  }

  /**
   * Helper method to get all potential metadata files using the checkstyle_packages.xml as base
   * where to look. It is not guaranteed that the files returned acutally exist.
   *
   * @return the collection of potential metadata files.
   * @throws CheckstylePluginException
   *           an unexpected exception ocurred
   */
  private static Collection<String> getAllPotentialMetadataFiles(ClassLoader classLoader)
          throws CheckstylePluginException {

    Collection<String> potentialMetadataFiles = new ArrayList<>();

    Set<String> packages = null;
    try {
      packages = PackageNamesLoader.getPackageNames(classLoader);
    } catch (CheckstyleException e) {
      CheckstylePluginException.rethrow(e);
    }

    for (String packageName : packages) {
      String metaFileLocation = packageName.replace('.', '/');
      if (!metaFileLocation.endsWith("/")) {
        metaFileLocation = metaFileLocation + "/";
      }
      metaFileLocation = metaFileLocation + METADATA_FILENAME;
      potentialMetadataFiles.add(metaFileLocation);
    }

    return potentialMetadataFiles;
  }

  /**
   * Returns the ResourceBundle for the given meta data file contained i18n'ed names and
   * descriptions.
   *
   * @param metadataFile
   *          the metadata xml file
   * @return the corresponding ResourceBundle for the metadata file or <code>null</code> if none
   *         exists
   */
  private static ResourceBundle getMetadataI18NBundle(String metadataFile,
          ClassLoader classLoader) {
    String bundle = metadataFile.substring(0, metadataFile.length() - 4).replace('/', '.');
    try {
      return ResourceBundle.getBundle(bundle, CheckstylePlugin.getPlatformLocale(), classLoader);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  private static void parseMetadata(InputStream metadataStream, ResourceBundle metadataBundle)
          throws DocumentException, CheckstylePluginException {

    SAXReader reader = new SAXReader();
    reader.setEntityResolver(new XMLUtil.InternalDtdEntityResolver(PUBLIC2INTERNAL_DTD_MAP));
    Document document = reader.read(metadataStream);

    List<Element> groupElements = document.getRootElement()
            .elements(XMLTags.RULE_GROUP_METADATA_TAG);

    for (Element groupEl : groupElements) {

      String groupName = groupEl.attributeValue(XMLTags.NAME_TAG).trim();
      groupName = localize(groupName, metadataBundle);

      // process description
      String groupDesc = groupEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
      groupDesc = localize(groupDesc, metadataBundle);

      RuleGroupMetadata group = getRuleGroupMetadata(groupName);

      if (group == null) {

        boolean hidden = Boolean.valueOf(groupEl.attributeValue(XMLTags.HIDDEN_TAG)).booleanValue();
        int priority = 0;
        try {
          priority = Integer.parseInt(groupEl.attributeValue(XMLTags.PRIORITY_TAG));
        } catch (Exception e) {
          CheckstyleLog.log(e);
          priority = Integer.MAX_VALUE;
        }

        group = new RuleGroupMetadata(groupName, groupDesc, hidden, priority);
        sRuleGroupMetadata.put(groupName, group);
      }

      // process the modules
      processModules(groupEl, group, metadataBundle);
    }
  }

  private static void processModules(Element groupElement, RuleGroupMetadata groupMetadata,
          ResourceBundle metadataBundle) throws CheckstylePluginException {

    List<Element> moduleElements = groupElement.elements(XMLTags.RULE_METADATA_TAG);
    for (Element moduleEl : moduleElements) {

      // default severity
      String defaultSeverity = moduleEl.attributeValue(XMLTags.DEFAULT_SEVERITY_TAG);
      Severity severity = defaultSeverity == null || defaultSeverity.trim().length() == 0
              ? getDefaultSeverity()
              : Severity.valueOf(defaultSeverity);

      String name = moduleEl.attributeValue(XMLTags.NAME_TAG).trim();
      name = localize(name, metadataBundle);
      String internalName = moduleEl.attributeValue(XMLTags.INTERNAL_NAME_TAG).trim();

      String parentName = moduleEl.attributeValue(XMLTags.PARENT_TAG) != null
              ? moduleEl.attributeValue(XMLTags.PARENT_TAG).trim()
              : null;
      boolean hidden = Boolean.valueOf(moduleEl.attributeValue(XMLTags.HIDDEN_TAG)).booleanValue();
      boolean hasSeverity = !"false".equals(moduleEl.attributeValue(XMLTags.HAS_SEVERITY_TAG));
      boolean deletable = !"false".equals(moduleEl.attributeValue(XMLTags.DELETABLE_TAG)); //$NON-NLS-1$
      boolean isSingleton = Boolean.valueOf(moduleEl.attributeValue(XMLTags.IS_SINGLETON_TAG))
              .booleanValue();

      // create rule metadata
      RuleMetadata module = new RuleMetadata(name, internalName, parentName, severity, hidden,
              hasSeverity, deletable, isSingleton, groupMetadata);
      groupMetadata.getRuleMetadata().add(module);

      // register internal name
      sRuleMetadata.put(internalName, module);

      // process description
      String description = moduleEl.elementTextTrim(XMLTags.DESCRIPTION_TAG);
      description = localize(description, metadataBundle);
      module.setDescription(description);

      // process properties
      processProperties(moduleEl, module, metadataBundle);

      // process alternative names
      for (Element altNameEl : moduleEl.elements(XMLTags.ALTERNATIVE_NAME_TAG)) {

        String alternativeName = altNameEl.attributeValue(XMLTags.INTERNAL_NAME_TAG);

        // register alternative name
        sAlternativeNamesMap.put(alternativeName, module);
        module.addAlternativeName(alternativeName);
      }

      // process quickfixes
      for (Element quickfixEl : moduleEl.elements(XMLTags.QUCKFIX_TAG)) {

        String quickfixClassName = quickfixEl.attributeValue(XMLTags.CLASSNAME_TAG);
        module.addQuickfix(quickfixClassName);
      }

      // process message keys
      for (Element quickfixEl : moduleEl.elements(XMLTags.MESSAGEKEY_TAG)) {

        String messageKey = quickfixEl.attributeValue(XMLTags.KEY_TAG);
        module.addMessageKey(messageKey);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void processProperties(Element moduleElement, RuleMetadata moduleMetadata,
          ResourceBundle metadataBundle) throws CheckstylePluginException {

    List<Element> propertyElements = moduleElement.elements(XMLTags.PROPERTY_METADATA_TAG);
    for (Element propertyEl : propertyElements) {

      ConfigPropertyType type = ConfigPropertyType
              .valueOf(propertyEl.attributeValue(XMLTags.DATATYPE_TAG));

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

      moduleMetadata.getPropertyMetadata().add(property);

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

              IOptionProvider provider = (IOptionProvider) providerClass.newInstance();
              property.getPropertyEnumeration().addAll(provider.getOptions());
            } else if (Enum.class.isAssignableFrom(providerClass)) {

              @SuppressWarnings("rawtypes")
              EnumSet<?> values = EnumSet.allOf((Class<Enum>) providerClass);
              for (Enum<?> e : values) {
                property.getPropertyEnumeration().add(e.name().toLowerCase());
              }
            }
          } catch (ReflectiveOperationException e) {
            CheckstylePluginException.rethrow(e);
          }

        }

        // get explicit enumeration option values
        for (Element optionEl : enumEl
                .elements(XMLTags.PROPERTY_VALUE_OPTIONS_TAG)) {
          property.getPropertyEnumeration().add(optionEl.attributeValue(XMLTags.VALUE_TAG));
        }
      }
    }
  }

  private static String localize(String localizationCandidate, ResourceBundle metadataBundle) {

    if (metadataBundle != null && localizationCandidate != null
            && localizationCandidate.startsWith("%")) {
      try {
        return metadataBundle.getString(localizationCandidate.substring(1));
      } catch (MissingResourceException e) {
        return localizationCandidate;
      }
    }
    return localizationCandidate;
  }
}
