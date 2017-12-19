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

package net.sf.eclipsecs.core.config;

import net.sf.eclipsecs.core.CheckstylePlugin;

/**
 * Defines XML tages used in configuration files.
 */
public final class XMLTags {

  private XMLTags() {
    // NOOP
  }

  //
  // common tags
  //

  /** common name tag. */
  public static final String NAME_TAG = "name"; //$NON-NLS-1$

  /** common value tag. */
  public static final String VALUE_TAG = "value"; //$NON-NLS-1$

  /** common description tag. */
  public static final String DESCRIPTION_TAG = "description"; //$NON-NLS-1$

  //
  // tags for the internal check configuration file
  //

  /** root tag for the internal configurations file. */
  public static final String CHECKSTYLE_ROOT_TAG = "checkstyle-configurations"; //$NON-NLS-1$

  /** version tag. */
  public static final String VERSION_TAG = "file-format-version"; //$NON-NLS-1$

  /** tag for a check configuration. */
  public static final String CHECK_CONFIG_TAG = "check-configuration"; //$NON-NLS-1$

  /** check configuration type tag. */
  public static final String TYPE_TAG = "type"; //$NON-NLS-1$

  /** location tag. */
  public static final String LOCATION_TAG = "location"; //$NON-NLS-1$

  /** default tag. */
  public static final String DEFAULT_CHECK_CONFIG_TAG = "default-check-configuration"; //$NON-NLS-1$

  /** additional data tag. */
  public static final String ADDITIONAL_DATA_TAG = "additional-data"; //$NON-NLS-1$

  //
  // tags for the metadata file
  //

  /** root tag for the meta data defintion file. */
  public static final String CHECKSTYLE_METADATA_TAG = "checkstyle-metadata"; //$NON-NLS-1$

  /** tag for a module group. */
  public static final String RULE_GROUP_METADATA_TAG = "rule-group-metadata"; //$NON-NLS-1$

  /** tag for the priority of a rule group. */
  public static final String PRIORITY_TAG = "priority"; //$NON-NLS-1$

  /** tag for a module metadata. */
  public static final String RULE_METADATA_TAG = "rule-metadata"; //$NON-NLS-1$

  /** tag for a module property meta data. */
  public static final String PROPERTY_METADATA_TAG = "property-metadata"; //$NON-NLS-1$

  /** tag for an alternative module name. */
  public static final String ALTERNATIVE_NAME_TAG = "alternative-name"; //$NON-NLS-1$

  /** tag for the module parent. */
  public static final String PARENT_TAG = "parent"; //$NON-NLS-1$

  /** tag for the internal name of the module. */
  public static final String INTERNAL_NAME_TAG = "internal-name"; //$NON-NLS-1$

  /** tag for the optional checkstyle module name. */
  public static final String CHECKSTYLE_MODULE_NAME_TAG = "checkstyle-module-name"; //$NON-NLS-1$

  /** tag for a property value enumeration. */
  public static final String ENUMERATION_TAG = "enumeration"; //$NON-NLS-1$

  /** tag for an option provider. */
  public static final String OPTION_PROVIDER = "option-provider"; //$NON-NLS-1$

  /** tag for a enumeration value. */
  public static final String PROPERTY_VALUE_OPTIONS_TAG = "property-value-option"; //$NON-NLS-1$

  /** tag for the type of a property. */
  public static final String DATATYPE_TAG = "datatype"; //$NON-NLS-1$

  /** tag for the default severity of a module. */
  public static final String DEFAULT_SEVERITY_TAG = "default-severity"; //$NON-NLS-1$

  /** tag for a default property value. */
  public static final String DEFAULT_VALUE_TAG = "default-value"; //$NON-NLS-1$

  /** tag for a default property value differing from Checkstyle's default. */
  public static final String DEFAULT_VALUE_OVERRIDE_TAG = "override-default-value"; //$NON-NLS-1$

  /** tag for the hidden flag. */
  public static final String HIDDEN_TAG = "hidden"; //$NON-NLS-1$

  /** tag for a flag if a module has a severity (filters have none). */
  public static final String HAS_SEVERITY_TAG = "hasSeverity"; //$NON-NLS-1$

  /** tag to flag if a module is deletable. */
  public static final String DELETABLE_TAG = "deletable"; //$NON-NLS-1$

  /** tag to flag if a module is a singleton. */
  public static final String IS_SINGLETON_TAG = "singleton"; //$NON-NLS-1$

  /** tag for the quickfix element. */
  public static final String QUCKFIX_TAG = "quickfix"; //$NON-NLS-1$

  /** tag for the message key element. */
  public static final String MESSAGEKEY_TAG = "message-key"; //$NON-NLS-1$

  //
  // tags for the checkstyle configuration
  //

  /** Constant for the name of the Checker module. */
  public static final String CHECKER_MODULE = "Checker"; //$NON-NLS-1$

  /** Constant for the name of the TreeWalker module. */
  public static final String TREEWALKER_MODULE = "TreeWalker"; //$NON-NLS-1$

  /** Constant for the name of the SuppressionCommentFiler module. */
  public static final String SUPRESSIONCOMMENTFILTER_MODULE = "SuppressionCommentFilter"; //$NON-NLS-1$

  /** Constant for the name of the SuppressWithNearbyCommentFilter module. */
  public static final String SUPRESSWITHNEARBYCOMMENTFILTER_MODULE = "SuppressWithNearbyCommentFilter"; //$NON-NLS-1$

  /** Constant for the name of the SuppressWarningsHolder module. */
  public static final String SUPPRESSWARNINGSHOLDER_MODULE = "SuppressWarningsHolder"; //$NON-NLS-1$

  /** Constant for the name of the SuppressWarningsFilter module. */
  public static final String SUPPRESSWARNINGSFILTER_MODULE = "SuppressWarningsFilter"; //$NON-NLS-1$

  /** Constant for the name of the Other group. */
  public static final String OTHER_GROUP = "Other"; //$NON-NLS-1$

  /** tag for a checkstyle module. */
  public static final String MODULE_TAG = "module"; //$NON-NLS-1$

  /** tag for a meta data element inside a module. */
  public static final String METADATA_TAG = "metadata"; //$NON-NLS-1$

  /** tag for a checkstyle module property. */
  public static final String PROPERTY_TAG = "property"; //$NON-NLS-1$

  /** tag for a checkstyle custom message. */
  public static final String MESSAGE_TAG = "message"; //$NON-NLS-1$

  /** tag for a checkstyle message key. */
  public static final String KEY_TAG = "key"; //$NON-NLS-1$

  /** tag for the severity property. */
  public static final String SEVERITY_TAG = "severity"; //$NON-NLS-1$

  /** tag for the id property. */
  public static final String ID_TAG = "id"; //$NON-NLS-1$

  /** constant for the comment meta data stored with the modules. */
  public static final String COMMENT_ID = CheckstylePlugin.PLUGIN_ID + ".comment"; //$NON-NLS-1$

  /** constant for the lastEnabledSeverity meta data stored with the modules. */
  public static final String LAST_ENABLED_SEVERITY_ID = CheckstylePlugin.PLUGIN_ID
          + ".lastEnabledSeverity"; //$NON-NLS-1$

  //
  // tags for the old proprietary check configuration
  //

  /** name tag for a old check configuration. */
  public static final String CHECK_CONFIG_NAME_TAG = "check-config-name"; //$NON-NLS-1$

  /** tag for a rule configuration. */
  public static final String RULE_CONFIG_TAG = "rule-configuration"; //$NON-NLS-1$

  /** comment tag inside the old config. */
  public static final String COMMENT_TAG = "comment"; //$NON-NLS-1$

  /** property inside the old config. */
  public static final String CONFIG_PROPERTY_TAG = "config-property"; //$NON-NLS-1$

  /** rule classname tag. */
  public static final String CLASSNAME_TAG = "classname"; //$NON-NLS-1$

  //
  // tags for the checkstyle_packages.xml file
  //

  /** package tag. */
  public static final String PACKAGE_TAG = "package"; //$NON-NLS-1$

  //
  // custom tags used by eclipse-cs extension points
  //

  /** default weight of a builtin configuration. */
  public static final String DEFAULT_WEIGHT = "default-weight"; //$NON-NLS-1$

}
