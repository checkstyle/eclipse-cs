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

/**
 * This represents the possible data types for a rule's configuration property.
 */
public enum ConfigPropertyType {

  /** A String. */
  STRING,

  /** An array of strings. */
  STRING_ARRAY,

  /** An integer. */
  INTEGER,

  /** Select a single items from a list. */
  SINGLE_SELECT,

  /** Boolean value. */
  BOOLEAN,

  /** Select multiple items from a set. */
  MULTI_CHECK,

  /** A value that is not configured. */
  HIDDEN,

  /** A value that contains a file name. */
  FILE,

  /** A value that contains a regular expression. */
  REGEX;

  static ConfigPropertyType fromXmlValue(String xmlValue) {
    return switch (xmlValue) {
      case "String" -> STRING;
      case "StringArray" -> STRING_ARRAY;
      case "Integer" -> INTEGER;
      case "SingleSelect" -> SINGLE_SELECT;
      case "Boolean" -> BOOLEAN;
      case "MultiCheck" -> MULTI_CHECK;
      case "Hidden" -> HIDDEN;
      case "File" -> FILE;
      case "Regex" -> REGEX;
      default -> throw new IllegalArgumentException("Unexpected value: " + xmlValue);
    };
  }
}
