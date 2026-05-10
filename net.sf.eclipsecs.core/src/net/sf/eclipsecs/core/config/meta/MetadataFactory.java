//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.yaml.snakeyaml.Yaml;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.meta.ModuleDetails;
import com.puppycrawl.tools.checkstyle.meta.XmlMetaReader;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * This class is the factory for all Checkstyle rule metadata.
 */
public final class MetadataFactory {

  /** eclipse extension configuration file name. */
  private static final String ECLIPSE_EXTENSION_CONFIG_FILE = "eclipse-metadata.yml";

  /** Name of the rules metadata XML file. */
  private static final String METADATA_FILENAME = "checkstyle-metadata.xml"; //$NON-NLS-1$

  /** Metadata for the rule groups. */
  private static Map<String, RuleGroupMetadata> sRuleGroupMetadata;

  /** Metadata for all rules, keyed by internal rule name. */
  private static Map<String, RuleMetadata> sRuleMetadata;

  /**
   * Mapping for all rules, keyed by alternative rule names (full qualified, old full qualified).
   */
  private static Map<String, RuleMetadata> sAlternativeNamesMap;

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

  /**
   * Private constructor to prevent instantiation.
   */
  private MetadataFactory() {
  }

  /**
   * Static initializer.
   */
  static {
    refresh();
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

      Object moduleInstance = checkClass.getDeclaredConstructor().newInstance();

      if (moduleInstance instanceof AbstractFileSetCheck) {
        parent = XMLTags.CHECKER_MODULE;
      } else {
        parent = XMLTags.TREEWALKER_MODULE;
      }
    } catch (Exception ex) {
      // Ok we tried... default to TreeWalker
      parent = XMLTags.TREEWALKER_MODULE;
    }

    RuleGroupMetadata otherGroup = getRuleGroupMetadata(XMLTags.OTHER_GROUP);
    RuleMetadata ruleMeta = new RuleMetadata(
            new RuleIdentity(module.getName(), module.getName(), parent, otherGroup, null,
                    Collections.emptyList()),
            MetadataFactory.getDefaultSeverity(), false, true, true, false, Collections.emptyList(),
            Collections.emptyList());
    module.setMetaData(ruleMeta);
    sRuleMetadata.put(ruleMeta.identity().internalName(), ruleMeta);

    List<ConfigProperty> properties = module.getProperties();
    int size = properties != null ? properties.size() : 0;
    for (int i = 0; i < size; i++) {

      ConfigProperty property = properties.get(i);
      ConfigPropertyMetadata meta = new ConfigPropertyMetadata(ConfigPropertyType.STRING,
              property.getName(), null, null);
      property.setMetaData(meta);
    }
    return ruleMeta;
  }

  private static void registerAlternativeNames(RuleMetadata ruleMetadata) {
    ruleMetadata.identity().alternativeNames()
            .forEach(alternativeName -> sAlternativeNamesMap.put(alternativeName, ruleMetadata));
  }

  /**
   * Create repository of Module Details from checkstyle metadata and third party extension checks metadata.
   */
  private static void createMetadataMap() {
    List<ModuleDetails> moduleDetails = XmlMetaReader.readAllModulesIncludingThirdPartyIfAny(sPackageNameSet.toArray(new String[0]));
    if (moduleDetails.isEmpty()) {
      CheckstyleLog.log(null, "Cannot read module details");
    }
    moduleDetails.forEach(moduleDetail -> sModuleDetailsRepo.put(moduleDetail.getName(), moduleDetail));
  }

  /**
   * Fetch third party checks extension metadata from YML files.
   *
   * @implNote The class graph scanner is configured to use all known packages, but only to the
   *           second package element. That seems to be a good compromise between scanning far too
   *           many classes and potentially missing a YML file when using the packages of the
   *           registered check classes only.
   */
  private static void loadThirdPartyModuleExtensionMetadata() {
    var rootPackages = sPackageNameSet.stream()
            .map(pack -> {
              int secondDot = StringUtils.ordinalIndexOf(pack, ".", 2);
              if (secondDot < 0) {
                return pack;
              }
              return pack.substring(0, secondDot);
            })
            .distinct()
            .toArray(String[]::new);
    Set<String> eclipseMetaDataFiles = new HashSet<>();

    try (ScanResult scanResult = new ClassGraph()
            .acceptPackages(rootPackages)
            .scan()) {
      try {
        scanResult.getResourcesWithLeafName(ECLIPSE_EXTENSION_CONFIG_FILE)
                .forEachInputStreamThrowingIOException((Resource res, InputStream inputStream) -> {
                  eclipseMetaDataFiles
                          .add(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                });
      } catch (IOException ex) {
        CheckstyleLog.log(ex, "Cannot read metadata YML");
      }
    }
    eclipseMetaDataFiles.forEach(MetadataFactory::loadThirdPartyData);
  }

  private static void loadThirdPartyData(String metadataContent) {
    Map<String, List<Map<String, Object>>> objects = new Yaml().load(metadataContent);
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

        return prio1 < prio2 ? -1 : (prio1 == prio2 ? 0 : 1);
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
  private static RuleGroupMetadata getRuleGroupMetadata(String name) {
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
   * Returns the default severity level.
   *
   * @return the default severity.
   */
  public static Severity getDefaultSeverity() {
    return Severity.INHERIT;
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
  private static String getStandardMessage(String messageKey, RuleMetadata rule) {

    // for some unknown reason there is no metadata or key
    if (messageKey == null || rule == null) {
      return null;
    }

    List<String> namesToCheck = new ArrayList<>();
    namesToCheck.add(rule.identity().internalName());
    namesToCheck.addAll(rule.identity().alternativeNames());

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

        return resourceBundle.getString(messageKey);
      } catch (MissingResourceException ex) {
        // let's continue to check the other alternative names
      }
    }
    return null;
  }

  /**
   * Refreshes the metadata.
   */
  private static synchronized void refresh() {
    sRuleGroupMetadata = new TreeMap<>();
    sRuleMetadata = new HashMap<>();
    sAlternativeNamesMap = new HashMap<>();
    sModuleDetailsRepo = new HashMap<>();
    sThirdPartyRuleGroupMap = new HashMap<>();
    sPackageNameSet = new HashSet<>();
    try {
      doInitialization();
    } catch (CheckstylePluginException ex) {
      CheckstyleLog.log(ex);
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

    loadThirdPartyModuleExtensionMetadata();
    createMetadataMap();

    for (String metadataFile : potentialMetadataFiles) {

      try (InputStream metadataStream = classLoader.getResourceAsStream(metadataFile)) {

        if (metadataStream != null) {
          ResourceBundle metadataBundle = getMetadataI18NBundle(metadataFile, classLoader);
          parseMetadata(metadataStream, metadataBundle, groupId(metadataFile));
        }
      } catch (DocumentException | IOException ex) {
        CheckstyleLog.log(ex, "Could not read metadata " + metadataFile); //$NON-NLS-1$
      }
    }

    loadRuleMetadata();
  }

  private static String groupId(String metadataFile) {
    String res = StringUtils.substringBetween(metadataFile, "/checks/", "/");
    res = StringUtils.defaultString(res, metadataFile);
    return res;
  }

  /**
   * Creates RuleMetadata.
   */
  private static void loadRuleMetadata() {
    List<RuleMetadata> rules = new CheckstyleMetadataAdapter().loadRuleMetadata(sRuleGroupMetadata,
            sModuleDetailsRepo.values(), sThirdPartyRuleGroupMap);
    for (RuleMetadata module : rules) {
      sRuleMetadata.put(module.identity().internalName(), module);
      registerAlternativeNames(module);
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
    } catch (CheckstyleException ex) {
      CheckstylePluginException.rethrow(ex);
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
    } catch (MissingResourceException ex) {
      return null;
    }
  }

  private static void parseMetadata(InputStream metadataStream, ResourceBundle metadataBundle, String groupId)
          throws DocumentException, CheckstylePluginException {
    Collection<RuleGroupMetadata> groups = MetadataXmlReader.parseMetadata(metadataStream,
            metadataBundle, groupId);
    groups.forEach(
            group -> sRuleGroupMetadata.merge(group.getGroupName(), group, (groupA, groupB) -> {
              groupA.getRuleMetadata().addAll(groupB.getRuleMetadata());
              return groupA;
            }));
    for (RuleGroupMetadata group : groups) {
      for (RuleMetadata module : group.getRuleMetadata()) {
        sRuleMetadata.put(module.identity().internalName(), module);
        registerAlternativeNames(module);
      }
    }
  }

  /**
   * Custom ResourceBundle.Control implementation which allows explicitly read the properties files
   * as UTF-8.
   *
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

}
