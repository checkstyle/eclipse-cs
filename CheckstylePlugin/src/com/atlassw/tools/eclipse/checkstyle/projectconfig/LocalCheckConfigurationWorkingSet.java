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

package com.atlassw.tools.eclipse.checkstyle.projectconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

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

    /** The project configuration. */
    private IProjectConfiguration mProjectConfig;

    /** The internal list of working copies belonging to this working set. */
    private List mWorkingCopies;

    /** List of working copies that were deleted from the working set. */
    private List mDeletedConfigurations;

    //
    // constructors
    //

    /**
     * Creates a working set to manage local configurations.
     * 
     * @param projectConfig the project configuration
     * @param checkConfigs the list of local check configurations
     */
    LocalCheckConfigurationWorkingSet(IProjectConfiguration projectConfig, List checkConfigs)
    {

        mProjectConfig = projectConfig;
        mWorkingCopies = new ArrayList();
        mDeletedConfigurations = new ArrayList();

        Iterator iter = checkConfigs.iterator();
        while (iter.hasNext())
        {
            ICheckConfiguration cfg = (ICheckConfiguration) iter.next();
            CheckConfigurationWorkingCopy workingCopy = new CheckConfigurationWorkingCopy(cfg, this);
            mWorkingCopies.add(workingCopy);
        }
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
    public boolean removeCheckConfiguration(CheckConfigurationWorkingCopy checkConfig)
    {

        boolean inUse = mProjectConfig.isConfigInUse(checkConfig);

        if (!inUse)
        {
            mWorkingCopies.remove(checkConfig);
            mDeletedConfigurations.add(checkConfig);
        }

        return !inUse;
    }

    /**
     * {@inheritDoc}
     */
    public void store() throws CheckstylePluginException
    {
        notifyDeletedCheckConfigs();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDirty()
    {
        if (mDeletedConfigurations.size() > 0)
        {
            return true;
        }

        boolean dirty = false;
        Iterator it = mWorkingCopies.iterator();
        while (it.hasNext())
        {

            CheckConfigurationWorkingCopy workingCopy = (CheckConfigurationWorkingCopy) it.next();
            dirty = workingCopy.isDirty();

            if (dirty)
            {
                break;
            }
        }
        return dirty;
    }

    /**
     * {@inheritDoc}
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
     * Returns the project of the local check configuration working set.
     * 
     * @return the project
     */
    public IProject getProject()
    {
        return mProjectConfig.getProject();
    }

    /**
     * {@inheritDoc}
     */
    public Collection getAffectedProjects() throws CheckstylePluginException
    {
        Set projects = new HashSet();

        CheckConfigurationWorkingCopy[] workingCopies = this.getWorkingCopies();
        for (int i = 0; i < workingCopies.length; i++)
        {

            // skip non dirty configurations
            if (workingCopies[i].hasConfigurationChanged()
                    && mProjectConfig.isConfigInUse(workingCopies[i]))
            {
                projects.add(mProjectConfig.getProject());
                break;
            }
        }

        return projects;
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
