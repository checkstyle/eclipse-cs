//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.util.ResourceBundle;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Property resolver that resolves properties from a resource bundle.
 * 
 * @author Lars Ködderitzsch
 */
class ResourceBundleProperyResolver extends StandardPropertyResolver
{

    /** the resource bundle. */
    private ResourceBundle mBundle;

    /**
     * Creates the property resolver.
     * 
     * @param bundle the resource bundle
     */
    public ResourceBundleProperyResolver(ResourceBundle bundle)
    {
        mBundle = bundle;
    }

    /**
     * @see com.puppycrawl.tools.checkstyle.PropertyResolver#resolve(java.lang.String)
     */
    public String resolve(String property) throws CheckstyleException
    {
        //first look for the standard variables
        String value = super.resolve(property);

        if (value == null && mBundle != null)
        {
            value = mBundle.getString(property);
        }
        return value;
    }
}