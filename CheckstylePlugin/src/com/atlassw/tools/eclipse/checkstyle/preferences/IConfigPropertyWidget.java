//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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
import java.util.List;

import com.atlassw.tools.eclipse.checkstyle.config.ConfigProperty;
import com.atlassw.tools.eclipse.checkstyle.config.meta.ConfigPropertyType;

//=================================================
// Imports from org namespace
//=================================================

/**
 * Interface all configuration property input widget classes.
 */
interface IConfigPropertyWidget
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Methods.
    //=================================================

    ConfigPropertyType getConfigPropertyType();

    String getValue();

    ConfigProperty getConfigProperty();

    List getMetadata();

    void setEnabled(boolean enabled);

    void restorePropertyDefault();
}