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

package net.sf.eclipsecs.core.projectconfig;

/**
 * Defines XML tages used in configuration files.
 */
public final class XMLTags {

  private XMLTags() {
    // NOOP
  }

  static final String FILESET_CONFIG_TAG = "fileset-config"; //$NON-NLS-1$

  static final String CHECK_CONFIG_TAG = "local-check-config"; //$NON-NLS-1$

  static final String CHECK_CONFIG_NAME_TAG = "check-config-name"; //$NON-NLS-1$

  static final String LOCAL_TAG = "local"; //$NON-NLS-1$

  static final String ENABLED_TAG = "enabled"; //$NON-NLS-1$

  static final String FILE_MATCH_PATTERN_TAG = "file-match-pattern"; //$NON-NLS-1$

  static final String FILESET_TAG = "fileset"; //$NON-NLS-1$

  static final String FORMAT_VERSION_TAG = "file-format-version"; //$NON-NLS-1$

  static final String INCLUDE_PATTERN_TAG = "include-pattern"; //$NON-NLS-1$

  static final String MATCH_PATTERN_TAG = "match-pattern"; //$NON-NLS-1$

  static final String NAME_TAG = "name"; //$NON-NLS-1$

  static final String FILTER_TAG = "filter"; //$NON-NLS-1$

  static final String FILTER_DATA_TAG = "filter-data"; //$NON-NLS-1$

  static final String VALUE_TAG = "value"; //$NON-NLS-1$

  static final String SIMPLE_CONFIG_TAG = "simple-config"; //$NON-NLS-1$

  static final String SYNC_FORMATTER_TAG = "sync-formatter"; //$NON-NLS-1$

  /** common description tag. */
  public static final String DESCRIPTION_TAG = "description"; //$NON-NLS-1$

  /** location tag. */
  public static final String LOCATION_TAG = "location"; //$NON-NLS-1$

  /** check configuration type tag. */
  public static final String TYPE_TAG = "type"; //$NON-NLS-1$

  /** additional data tag. */
  public static final String ADDITIONAL_DATA_TAG = "additional-data"; //$NON-NLS-1$

  /** tag for a resolvable property. */
  public static final String PROPERTY_TAG = "property"; //$NON-NLS-1$

}