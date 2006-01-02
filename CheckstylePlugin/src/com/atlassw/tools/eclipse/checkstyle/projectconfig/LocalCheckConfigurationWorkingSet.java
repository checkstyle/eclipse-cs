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

package com.atlassw.tools.eclipse.checkstyle.projectconfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Working set implementation that manages global configurations configured for
 * the Eclipse workspace.
 * 
 * @author Lars Ködderitzsch
 */
public class LocalCheckConfigurationWorkingSet implements ICheckConfigurationWorkingSet
{

    //
    // attributes
    //

    /** The internal list of working copies belonging to this working set. */
    private List mWorkingCopies;

    /** List of working copies that were deleted from the working set. */
    private List mDeletedConfigurations;

    //
    // constructors
    //

    /**
     * Creates a working set to manage global configurations.
     */
    LocalCheckConfigurationWorkingSet()
    {
        mWorkingCopies = new ArrayList();
        mDeletedConfigurations = new ArrayList();
    }

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
        return new CheckConfigurationWorkingCopy(configType, this, false);
    }

    /**
     * {@inheritDoc}
     */
    public CheckConfigurationWorkingCopy[] getWorkingCopies()
    {
        return (CheckConfigurationWorkingCopy[]) mWorkingCopies
                .toArray(new CheckConfigurationWorkingCopy[mWorkingCopies.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addCheckConfiguration(CheckConfigurationWorkingCopy checkConfig)
    {
        mWorkingCopies.add(checkConfig);
    }

    /**
     * {@inheritDoc}
     */
    public void removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig)
    {
        mWorkingCopies.remove(checkConfig);
        mDeletedConfigurations.add(checkConfig);
    }

    /**
     * {@inheritDoc}
     */
    public void store() throws CheckstylePluginException
    {
        notifyDeletedCheckConfigs();
    }

    /**
     * Check to see if a check configuration is using an already existing name.
     * 
     * @param configuration The check configuration
     * 
     * @return <code>true</code>= in use, <code>false</code>= not in use.
     */
    public boolean isNameCollision(CheckConfigurationWorkingCopy configuration)

    {
        boolean result = false;
        Iterator it = mWorkingCopies.iterator();
        while (it.hasNext())
        {
            CheckConfigurationWorkingCopy tmp = (CheckConfigurationWorkingCopy) it.next();
            if (tmp != configuration && tmp.getName().equals(configuration.getName()))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Notifies the check configurations that have been deleted.
     * 
     * @throws CheckstylePluginException an exception while notifiing for
     *             deletion
     */
    private void notifyDeletedCheckConfigs() throws CheckstylePluginException
    {

        Iterator it = mDeletedConfigurations.iterator();
        while (it.hasNext())
        {

            ICheckConfiguration checkConfig = (ICheckConfiguration) it.next();
            checkConfig.getType().notifyCheckConfigRemoved(checkConfig);
        }
    }
}
