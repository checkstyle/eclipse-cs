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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.puppycrawl.tools.checkstyle.meta.ModuleDetails;
import com.puppycrawl.tools.checkstyle.meta.ModulePropertyDetails;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.util.CheckstyleLog;

public class CheckstyleMetadataAdapter {

  /**
   * metadata type or validation type for regular expressions
   */
  private static final String TYPE_ID_PATTERN = "java.util.regex.Pattern";
  /** Pattern for dot. */
  private static final String DOT_PATTERN = "\\.";
  /** Other rule group name. */
  private static final String OTHER_GROUP_NAME = "Other";

  /** Mapping of all module package name to the internal module names. */
  private final Map<String, String> packageToGroupName;
  /**
   * Mapping for all module property datatype acquired from checkstyle metadata
   *  to the internal property impletation.
   */
  private final Map<String, ConfigPropertyType> propertyTypes;

  public CheckstyleMetadataAdapter() {
    this.packageToGroupName = createPackageToGroupNameMapping();
    this.propertyTypes = createPropertyTypeMapping();
  }

  /**
   * Creates RuleMetadata.
   */
  public List<RuleMetadata> loadRuleMetadata(
          Map<String, RuleGroupMetadata> groups, Collection<ModuleDetails> allModuleDetails,
          Map<String, Map<String, String>> thirdPartyGroups) {
    List<RuleMetadata> rules = new ArrayList<>();
    for (ModuleDetails moduleDetails : allModuleDetails) {
      RuleGroupMetadata group;
      String moduleClassName = moduleDetails.getFullQualifiedName();
      // standard checkstyle module
      if (moduleClassName.startsWith("com.puppycrawl.tools.checkstyle")) {
        final String[] packageTokens = moduleDetails.getFullQualifiedName().split(DOT_PATTERN);
        group = groups.get(packageToGroupName
                .get(packageTokens[packageTokens.length - 2]));
      }
      // third party extension modules
      else {
        String lookupKey = findLookupKey(thirdPartyGroups, moduleClassName);
        String ruleGroupName = thirdPartyGroups.get(lookupKey).get("name");
        group = groups.get(ruleGroupName);
        // if the group of the new check hasn't been formed yet
        // and put into the sRuleGroupMetadata map
        if (group == null) {
          final String ruleDescription = thirdPartyGroups.get(lookupKey).get("description");
          final int rulePriority = Integer.parseInt(thirdPartyGroups.get(lookupKey)
                  .get("priority"));
          group = new RuleGroupMetadata(ruleGroupName, ruleGroupName, ruleDescription, false, rulePriority);
          groups.put(ruleGroupName, group);
        }
      }
      final RuleMetadata createdRuleMetadata = createRuleMetadata(group, moduleDetails);
      group.getRuleMetadata().add(createdRuleMetadata);
      rules.add(createdRuleMetadata);
    }
    return rules;
  }

  /**
   * Create metadata for modules not present in the previously eclipse provided metadata.
   * Work in progress.
   *
   * @param moduleDetails module details fetched from checkstyle metadata
   * @return ruleMetadata for the module
   */
  private RuleMetadata createRuleMetadata(RuleGroupMetadata group, ModuleDetails moduleDetails) {
    final String[] packageTokens = moduleDetails.getParent().split(DOT_PATTERN);
    List<String> alternativeNames = List.of(moduleDetails.getFullQualifiedName());
    List<ConfigPropertyMetadata> properties = moduleDetails.getProperties().stream()
            .map(modulePropertyDetails -> createPropertyConfig(moduleDetails, modulePropertyDetails))
            .toList();
    RuleMetadata ruleMeta = new RuleMetadata(
            new RuleIdentity(moduleDetails.getName(), moduleDetails.getName(),
                    packageTokens[packageTokens.length - 1], group, moduleDetails.getDescription(),
                    alternativeNames),
            MetadataFactory.getDefaultSeverity(), false, true, true, false, Collections.emptyList(),
            properties);
    return ruleMeta;
  }

  /**
   * Create module property config data based on current/default data,
   * which are overridden partially with all the metadata fetched from checkstyle.
   *
   * @param moduleDetails checkstyle metadata of the parent module of the property
   * @param modulePropertyDetails checkstyle metadata of the property
   * @return ConfigPropertyMetadata of the module property
   */
  private ConfigPropertyMetadata createPropertyConfig(ModuleDetails moduleDetails,
          ModulePropertyDetails modulePropertyDetails) {
    ConfigPropertyType dataType = null;
    String propertyType = modulePropertyDetails.getType();
    // if the data type ends with option, the result is singleselect enum value
    // if the property validationType is tokenSet, the result is multicheck
    // everything else is String/String[] depth depending on the presence of "[]"
    if (propertyTypes.get(propertyType) != null) {
      String validationType = modulePropertyDetails.getValidationType();
      if (validationType != null) {
        if (TYPE_ID_PATTERN.equals(validationType)) {
          dataType = ConfigPropertyType.REGEX;
        } else if ("tokenSet".equals(validationType) || "tokenTypesSet".equals(validationType)) {
          dataType = ConfigPropertyType.MULTI_CHECK;
        }
      } else {
        dataType = propertyTypes.get(propertyType);
      }
    } else {
      if (propertyType.endsWith("Option")) {
        dataType = ConfigPropertyType.SINGLE_SELECT;
      } else {
        if (propertyType.endsWith("[]")) {
          dataType = ConfigPropertyType.STRING_ARRAY;
        } else {
          dataType = ConfigPropertyType.STRING;
        }
      }
    }
    ConfigPropertyMetadata modifiedConfigPropertyMetadata = new ConfigPropertyMetadata(dataType,
            modulePropertyDetails.getName(), modulePropertyDetails.getDefaultValue(), null);
    modifiedConfigPropertyMetadata.setDescription(modulePropertyDetails.getDescription());

    if (dataType == ConfigPropertyType.SINGLE_SELECT) {
      List<String> resultList = getEnumValues(propertyType);
      resultList.forEach(modifiedConfigPropertyMetadata.getPropertyEnumeration()::add);
    } else if (dataType == ConfigPropertyType.MULTI_CHECK) {
      String result = CheckUtil.getModifiableTokens(moduleDetails.getName());
      Collections.addAll(modifiedConfigPropertyMetadata.getPropertyEnumeration(), result.split(","));
    }
    return modifiedConfigPropertyMetadata;

  }

  /**
   * Get all values from the fully qualified enum name.
   *
   * @param className enum name
   * @return list of values of enum
   */
  private static List<String> getEnumValues(String className) {
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
   * Generate all prefix strings from the packageName and find which is a valid key in
   * {@code sThirdPartyRuleGroupMap}.
   * @param thirdPartyGroups
   */
  private static String findLookupKey(Map<String, Map<String, String>> thirdPartyGroups, String packageName) {
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
      if (thirdPartyGroups.containsKey(candidate)) {
        result = candidate;
        break;
      }
    }
    return result;
  }

  /**
   * Create a mapping between checkstyle package names and {@code RuleGroupMetadata} group names.
   */
  private static Map<String, String> createPackageToGroupNameMapping() {
    Map<String, String> packageToGroupName = new HashMap<>();
    packageToGroupName.put("annotation", "Annotations");
    packageToGroupName.put("checks", "Miscellaneous");
    packageToGroupName.put("checkstyle", OTHER_GROUP_NAME);
    packageToGroupName.put("blocks", "Blocks");
    packageToGroupName.put("coding", "Coding Problems");
    packageToGroupName.put("design", "Class Design");
    packageToGroupName.put("header", "Headers");
    packageToGroupName.put("imports", "Imports");
    packageToGroupName.put("indentation", "Indentation");
    packageToGroupName.put("javadoc", "Javadoc Comments");
    packageToGroupName.put("metrics", "Metrics");
    packageToGroupName.put("modifier", "Modifiers");
    packageToGroupName.put("naming", "Naming Conventions");
    packageToGroupName.put("regexp", "Regexp");
    packageToGroupName.put("sizes", "Size Violations");
    packageToGroupName.put("whitespace", "Whitespace");
    packageToGroupName.put("filters", "Filters");
    packageToGroupName.put("filefilters", "File Filters");
    return packageToGroupName;
  }

  /**
   * Create mapping between {@code ModulePropertyDetails} datatype and {@code ConfigPropertyType}.
   * @return
   */
  private static Map<String, ConfigPropertyType> createPropertyTypeMapping() {
    Map<String, ConfigPropertyType> propertyTypes = new HashMap<>();
    propertyTypes.put("java.lang.String", ConfigPropertyType.STRING);
    propertyTypes.put("java.lang.String[]", ConfigPropertyType.STRING_ARRAY);
    propertyTypes.put("boolean", ConfigPropertyType.BOOLEAN);
    propertyTypes.put("int", ConfigPropertyType.INTEGER);
    propertyTypes.put(TYPE_ID_PATTERN, ConfigPropertyType.REGEX);
    propertyTypes.put("java.util.regex.Pattern[]", ConfigPropertyType.STRING_ARRAY);
    propertyTypes.put("File", ConfigPropertyType.FILE);
    return propertyTypes;
  }

}
