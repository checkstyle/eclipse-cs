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

package net.sf.eclipsecs.core.projectconfig;

/**
 * Defines XML tages used in configuration files.
 */
public final class XMLTags {

    /** Common description tag. */
    public static final String DESCRIPTION_TAG = "description";

    /** Location tag. */
    public static final String LOCATION_TAG = "location";

    /** Check configuration type tag. */
    public static final String TYPE_TAG = "type";

    /** Additional data tag. */
    public static final String ADDITIONAL_DATA_TAG = "additional-data";

    /** Tag for a resolvable property. */
    public static final String PROPERTY_TAG = "property";

    /** Fileset config root tag. */
    static final String FILESET_CONFIG_TAG = "fileset-config";

    /** Local check config tag. */
    static final String CHECK_CONFIG_TAG = "local-check-config";

    /** Check config name tag. */
    static final String CHECK_CONFIG_NAME_TAG = "check-config-name";

    /** Local flag tag. */
    static final String LOCAL_TAG = "local";

    /** Enabled flag tag. */
    static final String ENABLED_TAG = "enabled";

    /** File match pattern tag. */
    static final String FILE_MATCH_PATTERN_TAG = "file-match-pattern";

    /** Fileset tag. */
    static final String FILESET_TAG = "fileset";

    /** File format version tag. */
    static final String FORMAT_VERSION_TAG = "file-format-version";

    /** Include pattern tag. */
    static final String INCLUDE_PATTERN_TAG = "include-pattern";

    /** Match pattern tag. */
    static final String MATCH_PATTERN_TAG = "match-pattern";

    /** Name attribute tag. */
    static final String NAME_TAG = "name";

    /** Filter tag. */
    static final String FILTER_TAG = "filter";

    /** Filter data tag. */
    static final String FILTER_DATA_TAG = "filter-data";

    /** Value tag. */
    static final String VALUE_TAG = "value";

    /** Simple config flag tag. */
    static final String SIMPLE_CONFIG_TAG = "simple-config";

    /** Sync formatter flag tag. */
    static final String SYNC_FORMATTER_TAG = "sync-formatter";

    private XMLTags() {
        // NOOP
    }

}
