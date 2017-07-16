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

/**
 * Enumeration for Checkstyle's severity levels. The intent is to decouple highler level funtions
 * (UI) from dealing with Checkstyle code API.
 * 
 * @author Lars Ködderitzsch
 */
public enum Severity {

  /** Unspecified severity level, inherited from parent module. */
  inherit,

  /** Severity level 'ignore'. */
  ignore,

  /** Severity level 'info'. */
  info,

  /** Severity level 'warning'. */
  warning,

  /** Severity level 'error'. */
  error
}
