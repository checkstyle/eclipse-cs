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

package net.sf.eclipsecs.core.config;

import net.sf.eclipsecs.core.CheckstylePlugin;

/**
 * Defines XML tages used in configuration files.
 */
public final class XMLTags {

    //
    // common tags
    //

    /** Common name tag. */
    public static final String NAME_TAG = "name";

    /** Common value tag. */
    public static final String VALUE_TAG = "value";

    /** Common description tag. */
    public static final String DESCRIPTION_TAG = "description";

    //
    // tags for the internal check configuration file
    //

    /** Root tag for the internal configurations file. */
    public static final String CHECKSTYLE_ROOT_TAG = "checkstyle-configurations";

    /** Version tag. */
    public static final String VERSION_TAG = "file-format-version";

    /** Tag for a check configuration. */
    public static final String CHECK_CONFIG_TAG = "check-configuration";

    /** Check configuration type tag. */
    public static final String TYPE_TAG = "type";

    /** Location tag. */
    public static final String LOCATION_TAG = "location";

    /** Default tag. */
    public static final String DEFAULT_CHECK_CONFIG_TAG = "default-check-configuration";

    /** Additional data tag. */
    public static final String ADDITIONAL_DATA_TAG = "additional-data";

    //
    // tags for the metadata file
    //

    /** Root tag for the meta data defintion file. */
    public static final String CHECKSTYLE_METADATA_TAG = "checkstyle-metadata";

    /** Tag for a module group. */
    public static final String RULE_GROUP_METADATA_TAG = "rule-group-metadata";

    /** Tag for the priority of a rule group. */
    public static final String PRIORITY_TAG = "priority";

    /** Tag for a module metadata. */
    public static final String RULE_METADATA_TAG = "rule-metadata";

    /** Tag for a module property meta data. */
    public static final String PROPERTY_METADATA_TAG = "property-metadata";

    /** Tag for an alternative module name. */
    public static final String ALTERNATIVE_NAME_TAG = "alternative-name";

    /** Tag for the module parent. */
    public static final String PARENT_TAG = "parent";

    /** Tag for the internal name of the module. */
    public static final String INTERNAL_NAME_TAG = "internal-name";

    /** Tag for the optional checkstyle module name. */
    public static final String CHECKSTYLE_MODULE_NAME_TAG = "checkstyle-module-name";

    /** Tag for a property value enumeration. */
    public static final String ENUMERATION_TAG = "enumeration";

    /** Tag for an option provider. */
    public static final String OPTION_PROVIDER = "option-provider";

    /** Tag for a enumeration value. */
    public static final String PROPERTY_VALUE_OPTIONS_TAG = "property-value-option";

    /** Tag for the type of a property. */
    public static final String DATATYPE_TAG = "datatype";

    /** Tag for the default severity of a module. */
    public static final String DEFAULT_SEVERITY_TAG = "default-severity";

    /** Tag for a default property value. */
    public static final String DEFAULT_VALUE_TAG = "default-value";

    /** Tag for a default property value differing from Checkstyle's default. */
    public static final String DEFAULT_VALUE_OVERRIDE_TAG = "override-default-value";

    /** Tag for the hidden flag. */
    public static final String HIDDEN_TAG = "hidden";

    /** Tag for a flag if a module has a severity (filters have none). */
    public static final String HAS_SEVERITY_TAG = "hasSeverity";

    /** Tag to flag if a module is deletable. */
    public static final String DELETABLE_TAG = "deletable";

    /** Tag to flag if a module is a singleton. */
    public static final String IS_SINGLETON_TAG = "singleton";

    /** Tag for the message key element. */
    public static final String MESSAGEKEY_TAG = "message-key";

    //
    // tags for the checkstyle configuration
    //

    /** Constant for the name of the Checker module. */
    public static final String CHECKER_MODULE = "Checker";

    /** Constant for the name of the TreeWalker module. */
    public static final String TREEWALKER_MODULE = "TreeWalker";

    /** Constant for the name of the SuppressionCommentFiler module. */
    public static final String SUPRESSIONCOMMENTFILTER_MODULE = "SuppressionCommentFilter";

    /** Constant for the name of the SuppressWithNearbyCommentFilter module. */
    public static final String SUPRESSWITHNEARBYCOMMENTFILTER_MODULE =
        "SuppressWithNearbyCommentFilter";

    /** Constant for the name of the SuppressWarningsHolder module. */
    public static final String SUPPRESSWARNINGSHOLDER_MODULE = "SuppressWarningsHolder";

    /** Constant for the name of the SuppressWarningsFilter module. */
    public static final String SUPPRESSWARNINGSFILTER_MODULE = "SuppressWarningsFilter";

    /** Constant for the name of the Other group. */
    public static final String OTHER_GROUP = "Other";

    /** Tag for a checkstyle module. */
    public static final String MODULE_TAG = "module";

    /** Tag for a meta data element inside a module. */
    public static final String METADATA_TAG = "metadata";

    /** Tag for a checkstyle module property. */
    public static final String PROPERTY_TAG = "property";

    /** Tag for a checkstyle custom message. */
    public static final String MESSAGE_TAG = "message";

    /** Tag for a checkstyle message key. */
    public static final String KEY_TAG = "key";

    /** Tag for the severity property. */
    public static final String SEVERITY_TAG = "severity";

    /** Tag for the id property. */
    public static final String ID_TAG = "id";

    /** Constant for the comment meta data stored with the modules. */
    public static final String COMMENT_ID = CheckstylePlugin.PLUGIN_ID + ".comment";

    /** Constant for the lastEnabledSeverity meta data stored with the modules. */
    public static final String LAST_ENABLED_SEVERITY_ID =
        CheckstylePlugin.PLUGIN_ID + ".lastEnabledSeverity";

    //
    // tags for the old proprietary check configuration
    //

    /** Name tag for a old check configuration. */
    public static final String CHECK_CONFIG_NAME_TAG = "check-config-name";

    /** Tag for a rule configuration. */
    public static final String RULE_CONFIG_TAG = "rule-configuration";

    /** Comment tag inside the old config. */
    public static final String COMMENT_TAG = "comment";

    /** Property inside the old config. */
    public static final String CONFIG_PROPERTY_TAG = "config-property";

    /** Rule classname tag. */
    public static final String CLASSNAME_TAG = "classname";

    //
    // tags for the checkstyle_packages.xml file
    //

    /** Package tag. */
    public static final String PACKAGE_TAG = "package";

    //
    // custom tags used by eclipse-cs extension points
    //

    /** Default weight of a builtin configuration. */
    public static final String DEFAULT_WEIGHT = "default-weight";

    private XMLTags() {
        // NOOP
    }

}
