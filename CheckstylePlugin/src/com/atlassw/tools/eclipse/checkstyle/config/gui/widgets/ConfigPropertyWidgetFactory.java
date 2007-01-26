//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.gui.widgets;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.meta.ConfigPropertyType;

/**
 * Create <code>ConfigPropertyWidget</code> instances based on provided
 * metadata.
 */
public final class ConfigPropertyWidgetFactory
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    private ConfigPropertyWidgetFactory()
    {}

    // =================================================
    // Methods.
    // =================================================

    /**
     * Creates a property widget for the given property.
     * 
     * @param parent the parent component
     * @param prop the property
     * @param shell the parent shell
     * @return the widget or <code>null</code> if the property type is unknown
     */
    public static IConfigPropertyWidget createWidget(Composite parent, ConfigProperty prop,
            Shell shell)
    {
        IConfigPropertyWidget widget = null;

        ConfigPropertyType type = prop.getMetaData().getDatatype();

        if (type.equals(ConfigPropertyType.STRING))
        {
            widget = new ConfigPropertyWidgetString(parent, prop);
        }
        if (type.equals(ConfigPropertyType.STRING_ARRAY))
        {
            widget = new ConfigPropertyWidgetString(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.INTEGER))
        {
            widget = new ConfigPropertyWidgetInteger(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.SINGLE_SELECT))
        {
            widget = new ConfigPropertyWidgetSingleSelect(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.BOOLEAN))
        {
            widget = new ConfigPropertyWidgetBoolean(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.MULTI_CHECK))
        {
            widget = new ConfigPropertyWidgetMultiCheck(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.HIDDEN))
        {
            widget = new ConfigPropertyWidgetHidden(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.FILE))
        {
            widget = new ConfigPropertyWidgetFile(parent, prop);
        }
        else if (type.equals(ConfigPropertyType.REGEX))
        {
            widget = new ConfigPropertyWidgetRegex(parent, prop);
        }

        widget.initialize();
        return widget;
    }
}