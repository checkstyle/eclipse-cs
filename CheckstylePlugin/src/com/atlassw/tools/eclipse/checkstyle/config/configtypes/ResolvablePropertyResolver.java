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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.util.Iterator;
import java.util.List;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ResolvableProperty;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Resolves properties set up with the check configuration.
 * 
 * @author Lars Ködderitzsch
 */
public class ResolvablePropertyResolver implements PropertyResolver
{

    /** The check configuration to resolve from. */
    private ICheckConfiguration mCheckConfiguration;

    /**
     * Creates the resolver for the given check configuration.
     * 
     * @param checkConfiguration the check configuration
     */
    public ResolvablePropertyResolver(ICheckConfiguration checkConfiguration)
    {
        mCheckConfiguration = checkConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    public String resolve(String aName) throws CheckstyleException
    {

        String value = null;

        List resolvableProperties = mCheckConfiguration.getResolvableProperties();
        Iterator it = resolvableProperties.iterator();

        while (it.hasNext())
        {

            ResolvableProperty prop = (ResolvableProperty) it.next();

            if (aName.equals(prop.getPropertyName()))
            {
                value = prop.getValue();
                break;
            }
        }

        return value;
    }

}
