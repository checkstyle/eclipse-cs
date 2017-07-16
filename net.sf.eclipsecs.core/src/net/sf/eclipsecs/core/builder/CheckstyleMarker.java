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

package net.sf.eclipsecs.core.builder;

import net.sf.eclipsecs.core.CheckstylePlugin;

/**
 * Represents the Checkstyle file marker.
 */
public final class CheckstyleMarker {

  private CheckstyleMarker() {
    // NOOP
  }

  /** ID for the CheckstyleMarker. */
  public static final String MARKER_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleMarker"; //$NON-NLS-1$

  /** Constant for module info additionally stored. */
  public static final String MODULE_NAME = "ModuleName"; //$NON-NLS-1$

  /** Constant for message key info additionally stored. */
  public static final String MESSAGE_KEY = "MessageKey"; //$NON-NLS-1$

  /** Constant for the error marker type. */
  public static final String ERROR_TYPE = CheckstylePlugin.PLUGIN_ID + ".error"; //$NON-NLS-1$

  /** Constant for the warning marker type. */
  public static final String WARNING_TYPE = CheckstylePlugin.PLUGIN_ID + ".warning"; //$NON-NLS-1$

  /** Constant for the info marker type. */
  public static final String INFO_TYPE = CheckstylePlugin.PLUGIN_ID + ".info"; //$NON-NLS-1$

}