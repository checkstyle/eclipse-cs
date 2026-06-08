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

package net.sf.eclipsecs.core.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sf.eclipsecs.core.CheckstylePlugin;

/**
 * Represents the Checkstyle file marker.
 */
public final class CheckstyleMarker {
    /** ID for the CheckstyleMarker. */
    public static final String MARKER_ID = CheckstylePlugin.PLUGIN_ID + ".CheckstyleMarker";

    /** Module name key in marker attributes. */
    public static final String MODULE_NAME = "ModuleName";

    /** Module Id key in marker attributes. */
    public static final String MODULE_ID = "ModuleId";

    /** Constant for message key info additionally stored. */
    public static final String MESSAGE_KEY = "MessageKey";

    /** Constant for the error marker type. */
    public static final String ERROR_TYPE = CheckstylePlugin.PLUGIN_ID + ".error";

    /** Constant for the warning marker type. */
    public static final String WARNING_TYPE = CheckstylePlugin.PLUGIN_ID + ".warning";

    /** Constant for the info marker type. */
    public static final String INFO_TYPE = CheckstylePlugin.PLUGIN_ID + ".info";

    /**
     * Marker category ID for Java Problem Type. It matches the value in
     * net.sf.eclipsecs.ui/plugin.xml
     */
    public static final int CATEGORY_ID = 999;

    private CheckstyleMarker() {
        // utility class
    }

    public static boolean isCheckstyleMarker(IMarker marker) {
        boolean isCheckstyleMarker;
        try {
            isCheckstyleMarker = CheckstyleMarker.MARKER_ID.equals(marker.getType());
        } catch (CoreException ex) {
            isCheckstyleMarker = false;
        }
        return isCheckstyleMarker;
    }
}
