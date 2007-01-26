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

package com.atlassw.tools.eclipse.checkstyle.config.meta;

import java.util.Arrays;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Scope;

/**
 * Option provider for the standard visibility kinds used by a lot of checks.
 * 
 * @author Lars Ködderitzsch
 */
public class VisibilityOptionsProvider implements IOptionProvider
{
    /** the list of options. */
    private static List sAllOptions = Arrays.asList(new String[] { Scope.NOTHING.getName(),
        Scope.PUBLIC.getName(), Scope.PROTECTED.getName(), Scope.PACKAGE.getName(),
        Scope.PRIVATE.getName(), Scope.ANONINNER.getName(), });

    /**
     * Returns all options.
     * 
     * @return the options
     */
    public List getOptions()
    {
        return sAllOptions;
    }
}