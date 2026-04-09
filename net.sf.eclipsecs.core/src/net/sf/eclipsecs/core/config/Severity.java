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

/**
 * Enumeration for Checkstyle's severity levels. The intent is to decouple highler level funtions
 * (UI) from dealing with Checkstyle code API.
 *
 */
public enum Severity {

  /** Unspecified severity level, inherited from parent module. */
  INHERIT,

  /** Severity level 'ignore'. */
  IGNORE,

  /** Severity level 'info'. */
  INFO,

  /** Severity level 'warning'. */
  WARNING,

  /** Severity level 'error'. */
  ERROR;

  public static Severity fromXmlValue(String xmlValue) {
    return switch (xmlValue) {
      case "inherit" -> INHERIT;
      case "ignore" -> IGNORE;
      case "info" -> INFO;
      case "warning" -> WARNING;
      case "error" -> ERROR;
      default -> throw new IllegalArgumentException("Unexpected value: " + xmlValue);
    };
  }

  public String toXmlValue() {
    return switch (this) {
      case INHERIT -> "inherit";
      case IGNORE -> "ignore";
      case INFO -> "info";
      case WARNING -> "warning";
      case ERROR -> "error";
    };
  }
}
