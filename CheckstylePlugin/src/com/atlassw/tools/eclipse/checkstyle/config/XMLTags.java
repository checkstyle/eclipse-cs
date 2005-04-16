//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.config;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================

//=================================================
// Imports from org namespace
//=================================================

/**
 * Defines XML tages used in configuration files.
 */
public class XMLTags
{
    //=================================================
    // Public static public static final variables.
    //=================================================

    //
    // common tags
    //

    /** common name tag. */
    public static final String NAME_TAG = "name";

    /** common value tag. */
    public static final String VALUE_TAG = "value";

    /** common description tag. */
    public static final String DESCRIPTION_TAG = "description";

    //
    // tags for the internal check configuration file
    //

    /** root tag for the internal configurations file. */
    public static final String CHECKSTYLE_ROOT_TAG = "checkstyle-configurations";

    /** version tag. */
    public static final String VERSION_TAG = "file-format-version";

    /** tag for a check configuration. */
    public static final String CHECK_CONFIG_TAG = "check-configuration";

    /** check configuration type tag. */
    public static final String TYPE_TAG = "type";

    /** location tag. */
    public static final String LOCATION_TAG = "location";

    //
    // tags for the metadata file
    //

    /** root tag for the meta data defintion file. */
    public static final String CHECKSTYLE_METADATA_TAG = "checkstyle-metadata";

    /** tag for a module group. */
    public static final String RULE_GROUP_METADATA_TAG = "rule-group-metadata";

    /** tag for a module metadata. */
    public static final String RULE_METADATA_TAG = "rule-metadata";

    /** tag for a module property meta data. */
    public static final String PROPERTY_METADATA_TAG = "property-metadata";

    /** tag for an alternative module name. */
    public static final String ALTERNATIVE_NAME_TAG = "alternative-name";

    /** tag for the module parent. */
    public static final String PARENT_TAG = "parent";

    /** tag for the internal name of the module. */
    public static final String INTERNAL_NAME_TAG = "internal-name";

    /** tag for a property value enumeration. */
    public static final String ENUMERATION_TAG = "enumeration";

    /** tag for a enumeration value. */
    public static final String PROPERTY_VALUE_OPTIONS_TAG = "property-value-option";

    /** tag for the type of a property. */
    public static final String DATATYPE_TAG = "datatype";

    /** tag for the default severity of a module. */
    public static final String DEFAULT_SEVERITY_TAG = "default-severity";

    /** tag for a default property value. */
    public static final String DEFAULT_VALUE_TAG = "default-value";

    /** tag for the hidden flag. */
    public static final String HIDDEN_TAG = "hidden";

    /** tag for a flag if a module has a severity (filters have none). */
    public static final String HAS_SEVERITY_TAG = "hasSeverity";

    /** tag to flag if a module is deletable. */
    public static final String DELETABLE_TAG = "deletable";

    //
    // tags for the checkstyle configuration
    //
    
    /** Constant for the name of the Checker module. */
    public static final String CHECKER_MODULE = "Checker";

    /** Constant for the name of the TreeWalker module. */
    public static final String TREEWALKER_MODULE = "TreeWalker";

    /** Constant for the name of the FileContentsHolder module. */
    public static final String FILECONTENTSHOLDER_MODULE = "FileContentsHolder";

    /** tag for a checkstyle module. */
    public static final String MODULE_TAG = "module";

    /** tag for a meta data element inside a module. */
    public static final String METADATA_TAG = "metadata";

    /** tag for a checkstyle module property. */
    public static final String PROPERTY_TAG = "property";

    /** tag for the severity property. */
    public static final String SEVERITY_TAG = "severity";

    /** constant for the comment meta data stored with the modules. */
    public static final String COMMENT_ID = CheckstylePlugin.PLUGIN_ID + ".comment";

    //
    // tags for the old proprietary check configuration
    //

    /** name tag for a old check configuration. */
    public static final String CHECK_CONFIG_NAME_TAG = "check-config-name";

    /** tag for a rule configuration. */
    public static final String RULE_CONFIG_TAG = "rule-configuration";

    /** comment tag inside the old config. */
    public static final String COMMENT_TAG = "comment";

    /** property inside the old config. */
    public static final String CONFIG_PROPERTY_TAG = "config-property";
    
    /** rule classname tag. */
    public static final String CLASSNAME_TAG = "classname";
}