//============================================================================
//
// Copyright (C) 2002-2003  David Schneider
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
 *  Defines XML tages used in configuration files.
 */
public interface XMLTags
{
    //=================================================
	// Public static final variables.
	//=================================================

    public static final String CHECK_CONFIG_TAG             = "check-configuration";

    public static final String CHECK_CONFIG_NAME_TAG        = "check-config-name";
    
    public static final String CHECKSTYLE_ROOT_TAG          = "checkstyle-configurations";
    
    public static final String CLASSNAME_TAG                = "classname";
    
    public static final String COMMENT_TAG                  = "comment";
    
    public static final String CONFIG_PROPERTIES_TAG        = "config-properties";
    
    public static final String CONFIG_PROPERTY_TAG          = "config-property";

    public static final String DATATYPE_TAG                 = "datatype";
    
    public static final String DEFAULT_SEVERITY_TAG         = "default-severity";

    public static final String DEFAULT_VALUE_TAG            = "default-value";
    
    public static final String DESCRIPTION_TAG              = "description";
    
    public static final String ENABLED_TAG                  = "enabled";
    
    public static final String ENUMERATION_TAG              = "enumeration";
    
    public static final String FILE_MATCH_PATTERN_TAG       = "file-match-pattern";
    
    public static final String FILESET_TAG                  = "fileset";
    
    public static final String FILESET_CONFIG_TAG           = "fileset-config";
    
    public static final String FORMAT_VERSION_TAG           = "file-format-version";
    
    public static final String INCLUDE_PATTERN_TAG          = "include-pattern";

    public static final String MATCH_PATTERN_TAG            = "match-pattern";

    public static final String METADATA_TAG                 = "metadata";
    
    public static final String MODULE_TAG                   = "module";

    public static final String NAME_TAG                     = "name";
    
    public static final String PROPERTY_TAG                 = "property";
    
    public static final String PROPERTY_METADATA_TAG        = "property-metadata";

    public static final String PROPERTY_VALUE_OPTIONS_TAG   = "property-value-option";
    
    public static final String RULE_METADATA_TAG            = "rule-metadata";
    
    public static final String RULE_CONFIG_TAG              = "rule-configuration";
    
    public static final String RULE_GROUP_METADATA_TAG      = "rule-group-metadata";

    public static final String SEVERITY_TAG                 = "severity";

    public static final String VALUE_TAG                    = "value";

   
	//=================================================
	// Static class variables.
	//=================================================

	//=================================================
	// Instance member variables.
	//=================================================

	//=================================================
	// Constructors & finalizer.
	//=================================================

	//=================================================
	// Methods.
	//=================================================
}
