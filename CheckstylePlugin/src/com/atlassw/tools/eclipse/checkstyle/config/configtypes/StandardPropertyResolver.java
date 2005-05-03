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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Property resolver that resolves some eclipse standard variables.
 * 
 * @author Lars Ködderitzsch
 */
public class StandardPropertyResolver implements PropertyResolver
{
    //
    // constants
    //

    /** constant for the workspace_loc variable. */
    private static final String WORKSPACE_LOC = "workspace_loc"; //$NON-NLS-1$

    /** constant for the project_loc variable. */
    private static final String PROJECT_LOC = "project_loc"; //$NON-NLS-1$

    /** constant for the basedir variable. */
    private static final String BASEDIR_LOC = "basedir"; //$NON-NLS-1$

    //
    // attributes
    //

    /** the context project. */
    private IProject mProject;

    //
    // methods
    //

    /**
     * @see com.puppycrawl.tools.checkstyle.PropertyResolver#resolve(java.lang.String)
     */
    public String resolve(String property) throws CheckstyleException
    {
        String value = null;
        if (WORKSPACE_LOC.equals(property))
        {
            value = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
        }
        else if ((PROJECT_LOC.equals(property) || BASEDIR_LOC.equals(property)) && mProject != null)
        {
            value = mProject.getLocation().toOSString();
        }

        return value;
    }

    /**
     * Sets the project context.
     * 
     * @param project the project
     */
    public void setContext(IProject project)
    {
        mProject = project;
    }
}