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
import com.puppycrawl.tools.checkstyle.meta.ModuleDetails;
import com.puppycrawl.tools.checkstyle.meta.ModulePropertyDetails;
import com.puppycrawl.tools.checkstyle.meta.XmlMetaReader;

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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

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
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.yaml.snakeyaml.Yaml;

/**
 * This class is the factory for all Checkstyle rule metadata.
 */
public final class MetadataFactory {

  /** Map containing the public - internal DTD mapping. */
  private static final Map<String, String> PUBLIC2INTERNAL_DTD_MAP = new HashMap<>();

  /** Pattern for eclipse extension configuration files. */
  private static final Pattern ECLIPSE_EXTENSION_CONFIG_FILE
      = Pattern.compile(".*eclipse-metadata.*\\.yml");

  /** Pattern for dot. */
  private static final String DOT_PATTERN = "\\.";

  /** Other rule group name. */
  private static final String OTHER_GROUP_NAME = "Other";

  /** Metadata for the rule groups. */
  private static Map<String, RuleGroupMetadata> sRuleGroupMetadata;

  /** Metadata for all rules, keyed by internal rule name. */
  private static Map<String, RuleMetadata> sRuleMetadata;

  /**
   * Mapping for all rules, keyed by alternative rule names (full qualified, old full qualified).
   */
  private static Map<String, RuleMetadata> sAlternativeNamesMap;

  /**
   * Mapping for all module property datatype acquired from checkstyle metadata
   *  to the internal property impletation.
   */
  private static Map<String, ConfigPropertyType> sPropertyTypeMap;

  /**
   * Mapping of all module package name to the internal module names.
   */
  private static Map<String, String> sPackageToGroupNameMap;

  /**
   * Repository of all the the checkstyle metadata, with their name as key.
   */
  private static Map<String, ModuleDetails> sModuleDetailsRepo;

  /**
   * Set containing all the packages in the classloader.
   */
  private static Set<String> sPackageNameSet;

  /**
   * Mapping of third party extension package name to rule group name.
   */
  private static Map<String, Map<String, String>> sThirdPartyRuleGroupMap;

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

  /*
   * Fetch third party checks extension metadata from YML files.
   */
  private static void loadThirdPartyModuleExtensionMetadata() {
    Set<String> eclipseMetaDataFiles = new HashSet<>();

    for (String packageName : sPackageNameSet) {
      final String topMostPackageName = packageName.split(DOT_PATTERN)[0];
      Reflections reflections = new Reflections(topMostPackageName, new ResourcesScanner());
      eclipseMetaDataFiles.addAll(reflections.getResources(ECLIPSE_EXTENSION_CONFIG_FILE));
    }
    eclipseMetaDataFiles.forEach(file -> loadThirdPartyData(file));
  }

  private static void loadThirdPartyData(String metadataFile) {
    InputStream inputStream = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
             .getResourceAsStream(metadataFile);
    Map<String, List<Map<String, Object>>> objects = new Yaml().load(inputStream);
    for (Map<String, Object> obj : objects.get("ruleGroups")) {
      Map<String, String> ruleGroupData = new HashMap<>();
      ruleGroupData.put("name", (String) obj.get("name"));
      ruleGroupData.put("description", (String) obj.get("description"));
      ruleGroupData.put("priority", Integer.toString((Integer) obj.get("priority")));
      sThirdPartyRuleGroupMap.put((String) obj.get("package"), ruleGroupData);
    }
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
   * Create metadata for modules not present in the previously eclipse provided metadata.
   * Work in progress.
   *
   * @param moduleDetails module details fetched from checkstyle metadata
   * @return ruleMetadata for the module
   */
  public static RuleMetadata createRuleMetadata(ModuleDetails moduleDetails) {
    RuleGroupMetadata group;
    String moduleClassName = moduleDetails.getFullQualifiedName();
    // standard checkstyle module
    if (moduleClassName.startsWith("com.puppycrawl.tools.checkstyle")) {
      final String[] packageTokens = moduleDetails.getFullQualifiedName().split(DOT_PATTERN);
      group = getRuleGroupMetadata(sPackageToGroupNameMap
              .get(packageTokens[packageTokens.length - 2]));
    }
    // third party extension modules
    else {
      String lookupKey = findLookupKey(moduleClassName);
      String ruleGroupName = sThirdPartyRuleGroupMap.get(lookupKey).get("name");
      group = getRuleGroupMetadata(ruleGroupName);
      // if the group of the new check hasn't been formed yet
      // and put into the sRuleGroupMetadata map
      if (group == null) {
        final String ruleDescription = sThirdPartyRuleGroupMap.get(lookupKey).get("description");
        final int rulePriority = Integer.parseInt(sThirdPartyRuleGroupMap.get(lookupKey)
                .get("priority"));
        group = new RuleGroupMetadata(ruleGroupName, ruleDescription, false, rulePriority);
        sRuleGroupMetadata.put(ruleGroupName, group);
      }
    }
    final String[] packageTokens = moduleDetails.getParent().split(DOT_PATTERN);
    RuleMetadata ruleMeta = new RuleMetadata(moduleDetails.getName(), moduleDetails.getName(),
            packageTokens[packageTokens.length - 1], MetadataFactory.getDefaultSeverity(),
            false, true, true, false, group);
    ruleMeta.setDescription(moduleDetails.getDescription());
    moduleDetails.getProperties().forEach(modulePropertyDetails -> ruleMeta.getPropertyMetadata()
            .add(createPropertyConfig(moduleDetails, modulePropertyDetails)));

    return ruleMeta;
  }

  /**
   * Generate all prefix strings from the packageName and find which is a valid key in
   * {@code sThirdPartyRuleGroupMap}.
   */
  private static String findLookupKey(String packageName) {
    final String[] packageTokens = packageName.split(DOT_PATTERN);
    List<String> prefixList = new ArrayList<>();
    String lookupKey = packageTokens[0];
    prefixList.add(lookupKey);
    for (int i = 1; i < packageTokens.length; i++) {
      lookupKey += "." + packageTokens[i];
      prefixList.add(lookupKey);
    }

    String result = null;
    Collections.reverse(prefixList);
    for (String candidate : prefixList) {
      if (sThirdPartyRuleGroupMap.containsKey(candidate)) {
        result = candidate;
        break;
      }
    }
    return result;
  }

  /**
   * Create module property config data based on current/default data,
   * which are overridden partially with all the metadata fetched from checkstyle.
   *
   * @param moduleDetails checkstyle metadata of the parent module of the property
   * @param modulePropertyDetails checkstyle metadata of the property
   * @return ConfigPropertyMetadata of the module property
   */
  public static ConfigPropertyMetadata createPropertyConfig(ModuleDetails moduleDetails,
          ModulePropertyDetails modulePropertyDetails) {
    ConfigPropertyType dataType = null;
    // if the data type ends with option, the result is singleselect enum value
    // if the property validationType is tokenSet, the result is multicheck
    // everything else is String/String[] depth depending on the presence of "[]"
    if (sPropertyTypeMap.get(modulePropertyDetails.getType()) != null) {
      if (modulePropertyDetails.getValidationType() != null) {
        if (modulePropertyDetails.getValidationType().equals("java.util.regex.Pattern")) {
          dataType = ConfigPropertyType.Regex;
        } else if (modulePropertyDetails.getValidationType().equals("tokenSet")) {
          dataType = ConfigPropertyType.MultiCheck;
        }
      } else {
        dataType = sPropertyTypeMap.get(modulePropertyDetails.getType());
      }
    } else {
      if (modulePropertyDetails.getType().endsWith("Option")) {
        dataType = ConfigPropertyType.SingleSelect;
      } else {
        if (modulePropertyDetails.getType().endsWith("[]")) {
          dataType = ConfigPropertyType.StringArray;
        } else {
          dataType = ConfigPropertyType.String;
        }
      }
    }
    ConfigPropertyMetadata modifiedConfigPropertyMetadata = new ConfigPropertyMetadata(dataType,
            modulePropertyDetails.getName(), modulePropertyDetails.getDefaultValue(), null);
    modifiedConfigPropertyMetadata.setDescription(modulePropertyDetails.getDescription());

    if (dataType == ConfigPropertyType.SingleSelect) {
      List<String> resultList = getEnumValues(modulePropertyDetails.getType());
      resultList.forEach(val -> modifiedConfigPropertyMetadata.getPropertyEnumeration().add(val));
    } else if (dataType == ConfigPropertyType.MultiCheck) {
      String result = CheckUtil.getModifiableTokens(moduleDetails.getName());
      for (String val : result.split(",")) {
        modifiedConfigPropertyMetadata.getPropertyEnumeration().add(val);
      }
    }
    return modifiedConfigPropertyMetadata;

  }

  /**
   * Get all values from the fully qualified enum name.
   *
   * @param className enum name
   * @return list of values of enum
   */
  public static List<String> getEnumValues(String className) {
    List<String> resultList = new ArrayList<>();
    Class<?> providerClass = null;
    try {
      providerClass = CheckstylePlugin.getDefault().getAddonExtensionClassLoader()
              .loadClass(className);
      @SuppressWarnings({ "rawtypes", "unchecked" })
      EnumSet<?> values = EnumSet.allOf((Class<Enum>) providerClass);
      for (Enum<?> value : values) {
        resultList.add(value.name().toLowerCase());
      }
    } catch (ClassNotFoundException exc) {
      CheckstyleLog.log(exc, "Class " + className + " not found.");
    }

    return resultList;
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
    sPropertyTypeMap = new HashMap<>();
    sPackageToGroupNameMap = new HashMap<>();
    sModuleDetailsRepo = new HashMap<>();
    sThirdPartyRuleGroupMap = new HashMap<>();
    sPackageNameSet = new HashSet<>();
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
    createPropertyTypeMapping();
    createPackageToGroupNameMapping();

    ClassLoader classLoader = CheckstylePlugin.getDefault().getAddonExtensionClassLoader();
    Collection<String> potentialMetadataFiles = getAllPotentialMetadataFiles(classLoader);

    loadThirdPartyModuleExtensionMetadata();
    createMetadataMap();

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

    loadRuleMetadata();
  }

  /**
   * Create repository of Module Details from checkstyle metadata and third party extension checks metadata.
   */
  public static void createMetadataMap() {
    XmlMetaReader.readAllModulesIncludingThirdPartyIfAny(
        sPackageNameSet.toArray(new String[sPackageNameSet.size()]))
        .forEach(moduleDetail -> sModuleDetailsRepo.put(moduleDetail.getName(), moduleDetail));
  }

  /**
   * Creates RuleMetadata.
   */
  public static void loadRuleMetadata() {
    for (Entry<String, ModuleDetails> entry : sModuleDetailsRepo.entrySet()) {
      final ModuleDetails moduleDetails = entry.getValue();
      final RuleMetadata createdRuleMetadata = createRuleMetadata(moduleDetails);
      sRuleMetadata.put(moduleDetails.getName(), createdRuleMetadata);
      sRuleGroupMetadata.get(createdRuleMetadata.getGroup().getGroupName())
         .getRuleMetadata().add(createdRuleMetadata);
    }
  }

  /**
   * Create mapping between {@code ModulePropertyDetails} datatype and {@code ConfigPropertyType}.
   */
  private static void createPropertyTypeMapping() {
    sPropertyTypeMap.put("java.lang.String", ConfigPropertyType.String);
    sPropertyTypeMap.put("java.lang.String[]", ConfigPropertyType.StringArray);
    sPropertyTypeMap.put("boolean", ConfigPropertyType.Boolean);
    sPropertyTypeMap.put("int", ConfigPropertyType.Integer);
    sPropertyTypeMap.put("java.util.regex.Pattern", ConfigPropertyType.Regex);
    sPropertyTypeMap.put("java.util.regex.Pattern[]", ConfigPropertyType.StringArray);
    sPropertyTypeMap.put("File", ConfigPropertyType.File);
  }

  /**
   * Create a mapping between checkstyle package names and {@code RuleGroupMetadata} group names.
   */
  private static void createPackageToGroupNameMapping() {
    sPackageToGroupNameMap.put("annotation", "Annotations");
    sPackageToGroupNameMap.put("checks", "Miscellaneous");
    sPackageToGroupNameMap.put("checkstyle", OTHER_GROUP_NAME);
    sPackageToGroupNameMap.put("blocks", "Blocks");
    sPackageToGroupNameMap.put("coding", "Coding Problems");
    sPackageToGroupNameMap.put("design", "Class Design");
    sPackageToGroupNameMap.put("header", "Headers");
    sPackageToGroupNameMap.put("imports", "Imports");
    sPackageToGroupNameMap.put("indentation", "Indentation");
    sPackageToGroupNameMap.put("javadoc", "Javadoc Comments");
    sPackageToGroupNameMap.put("metrics", "Metrics");
    sPackageToGroupNameMap.put("modifier", "Modifiers");
    sPackageToGroupNameMap.put("naming", "Naming Conventions");
    sPackageToGroupNameMap.put("regexp", "Regexp");
    sPackageToGroupNameMap.put("sizes", "Size Violations");
    sPackageToGroupNameMap.put("whitespace", "Whitespace");
    sPackageToGroupNameMap.put("filters", "Filters");
    sPackageToGroupNameMap.put("filefilters", "File Filters");
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
    sPackageNameSet.addAll(packages);

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

      groupMetadata.getRuleMetadata().add(module);
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
