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

package com.atlassw.tools.eclipse.checkstyle.preferences;


//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigPropertyType;

//=================================================
// Imports from org namespace
//=================================================
import org.eclipse.swt.widgets.Composite;


/**
 *  Create <code>ConfigPropertyWidget</code> instances based on provided
 *  metadata.
 */
public final class ConfigPropertyWidgetFactory
{
    //=================================================
	// Public static final variables.
	//=================================================

	//=================================================
	// Static class variables.
	//=================================================

	//=================================================
	// Instance member variables.
	//=================================================

	//=================================================
	// Constructors & finalizer.
	//=================================================
    
    private ConfigPropertyWidgetFactory()
    {}

	//=================================================
	// Methods.
	//=================================================
    
    static IConfigPropertyWidget createWidget(Composite parent, 
                                              ConfigPropertyMetadata metadata,
                                              ConfigProperty prop)
    {
        IConfigPropertyWidget widget = null;
        
        ConfigPropertyType type = metadata.getDatatype();
        
        if (type.equals(ConfigPropertyType.STRING))
        {
            widget = new ConfigPropertyWidgetString(parent, prop, metadata);
        }
        else if (type.equals(ConfigPropertyType.INTEGER))
        {
            widget = new ConfigPropertyWidgetString(parent, prop, metadata);
        }
        else if (type.equals(ConfigPropertyType.SINGLE_SELECT))
        {
            widget = new ConfigPropertyWidgetSingleSelect(parent, prop, metadata);
        }
        else if (type.equals(ConfigPropertyType.BOOLEAN))
        {
            widget = new ConfigPropertyWidgetBoolean(parent, prop, metadata);
        }
        else if (type.equals(ConfigPropertyType.MULTI_CHECK))
        {
            widget = new ConfigPropertyWidgetMultiCheck(parent, prop, metadata);
        }
        else if (type.equals(ConfigPropertyType.HIDDEN))
        {
            widget = new ConfigPropertyWidgetHidden(parent, prop, metadata);
        }
        
        return widget;
    }
}
