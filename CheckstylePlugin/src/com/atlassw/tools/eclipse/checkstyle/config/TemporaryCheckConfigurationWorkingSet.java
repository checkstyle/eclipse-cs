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

package com.atlassw.tools.eclipse.checkstyle.config;

import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Working set implementation that manages global configurations configured for
 * the Eclipse workspace.
 * 
 * @author Lars Ködderitzsch
 */
public class TemporaryCheckConfigurationWorkingSet implements ICheckConfigurationWorkingSet
{

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy newWorkingCopy(ICheckConfiguration checkConfig)
    {
        return new CheckConfigurationWorkingCopy(checkConfig, this);
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy newWorkingCopy(IConfigurationType configType)
    {
        return new CheckConfigurationWorkingCopy(configType, this, true);
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy[] getWorkingCopies()
    {
        return new CheckConfigurationWorkingCopy[0];
    }

    /**
     * {@inheritDoc}
     */
    public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig)
    {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig)
    {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public void store() throws CheckstylePluginException
    {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNameCollision(CheckConfigurationWorkingCopy configuration)

    {
        return false;
    }
}
