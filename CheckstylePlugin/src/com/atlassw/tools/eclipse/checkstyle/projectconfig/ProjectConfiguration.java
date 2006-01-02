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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;

/**
 * Represents the configuration for a project. Contains the file sets configured
 * for the project plus the additional filters.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfiguration implements Cloneable
{

    //
    // attributes
    //

    /** The local check configurations. */
    private ICheckConfigurationWorkingSet mCheckConfigWorkingSet;

    /** the file sets. */
    private List mFileSets = new LinkedList();

    /** the filters. */
    private IFilter[] mFilters;

    /** Flags if the simple file set editor should be used. */
    private boolean mUseSimpleConfig = true;

    //
    // constructors
    //

    /**
     * Default constructor.
     */
    public ProjectConfiguration()
    {
        mFilters = PluginFilters.getConfiguredFilters();
        mCheckConfigWorkingSet = new LocalCheckConfigurationWorkingSet();
    }

    //
    // methods
    //

    /**
     * Returns the check configuration working set for local configurations.
     * 
     * @return the local configurations working set
     */
    public ICheckConfigurationWorkingSet getCheckConfigWorkingSet()
    {
        return mCheckConfigWorkingSet;
    }

    /**
     * Returns a project local check configuration by its name.
     * 
     * @param name the configurations name
     * @return the check configuration or <code>null</code>, if no local
     *         configuration with this name exists
     */
    public ICheckConfiguration getLocalCheckConfigByName(String name)
    {
        ICheckConfiguration config = null;
        ICheckConfiguration[] configs = mCheckConfigWorkingSet.getWorkingCopies();
        for (int i = 0; i < configs.length; i++)
        {
            if (configs[i].getName().equals(name))
            {
                config = configs[i];
                break;
            }
        }

        return config;
    }

    /**
     * Returns the file sets configured for the project.
     * 
     * @return the file sets
     */
    public List getFileSets()
    {
        return mFileSets;
    }

    /**
     * Gets all enabled file sets from this configuration.
     * 
     * @return all enabled file sets
     */
    public List getEnabledFileSets()
    {

        List fileSets = getFileSets();
        List result = new LinkedList();
        for (Iterator iter = fileSets.iterator(); iter.hasNext();)
        {
            FileSet fileSet = (FileSet) iter.next();
            if (fileSet.isEnabled())
            {
                result.add(fileSet);
            }
        }

        return result;
    }

    /**
     * Gets the filters of this file set.
     * 
     * @return the filters
     */
    public IFilter[] getFilters()
    {
        return mFilters;
    }

    /**
     * Gets the enabled filters of this file set.
     * 
     * @return the enabled filters
     */
    public IFilter[] getEnabledFilters()
    {
        List filters = new ArrayList();
        int size = mFilters != null ? mFilters.length : 0;
        for (int i = 0; i < size; i++)
        {
            if (mFilters[i].isEnabled())
            {
                filters.add(mFilters[i]);
            }
        }

        return (IFilter[]) filters.toArray(new IFilter[filters.size()]);
    }

    /**
     * Returns the filter that has the given internal name. If no filter has
     * this name <code>null</code> is returned.
     * 
     * @param name the internal name
     * @return the filter or <code>null</code>
     */
    public IFilter getFilterByIntenalName(String name)
    {

        IFilter filter = null;

        for (int i = 0; i < mFilters.length; i++)
        {
            if (mFilters[i].getInternalName().equals(name))
            {
                filter = mFilters[i];
                break;
            }
        }

        return filter;
    }

    /**
     * Sets the filters of this file set.
     * 
     * @param filters the filters
     */
    public void setFilters(IFilter[] filters)
    {
        mFilters = filters;
    }

    /**
     * Sets if the simple configuration should be used.
     * 
     * @param useSimpleConfig true if the project uses the simple fileset
     *            configuration
     */
    public void setUseSimpleConfig(boolean useSimpleConfig)
    {
        mUseSimpleConfig = useSimpleConfig;
    }

    /**
     * Returns if the simple configuration should be used.
     * 
     * @return <code>true</code>, if this project uses the simple
     *         configuration, <code>false</code> otherwise
     */
    public boolean isUseSimpleConfig()
    {
        return mUseSimpleConfig;
    }

    /**
     * Checks if this project configuration uses the given checkstyle
     * configuration.
     * 
     * @param configName the configuration name
     * @return <code>true</code>, if the project config uses the checkstyle
     *         config, <code>false</code> otherwise
     */
    public boolean isConfigInUse(ICheckConfiguration configuration)
    {

        boolean result = false;

        Iterator iter = getFileSets().iterator();
        while (iter.hasNext())
        {
            FileSet fileSet = (FileSet) iter.next();
            if (configuration.equals(fileSet.getCheckConfig()))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        ProjectConfiguration clone = null;
        try
        {
            clone = (ProjectConfiguration) super.clone();
            clone.mFileSets = new LinkedList();
            clone.setUseSimpleConfig(this.isUseSimpleConfig());

            // clone file sets
            Iterator iter = getFileSets().iterator();
            while (iter.hasNext())
            {
                clone.getFileSets().add(((FileSet) iter.next()).clone());
            }

            // clone filters
            IFilter[] filters = getFilters();
            int size = filters != null ? filters.length : 0;
            IFilter[] clonedFilters = new IFilter[size];
            for (int i = 0; i < size; i++)
            {
                clonedFilters[i] = (IFilter) filters[i].clone();
            }
            clone.setFilters(clonedFilters);
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError();
        }

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {

        if (obj == null || !(obj instanceof ProjectConfiguration))
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        ProjectConfiguration otherConfig = (ProjectConfiguration) obj;
        if (isUseSimpleConfig() != otherConfig.isUseSimpleConfig())
        {
            return false;
        }
        if (!getFileSets().equals(otherConfig.getFileSets()))
        {
            return false;
        }
        if (!Arrays.equals(getFilters(), otherConfig.getFilters()))
        {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        // a "nice" prime number, see Java Report, April 2000
        final int prime = 1000003;

        int result = 1;
        result = (result * prime) + Boolean.valueOf(mUseSimpleConfig).hashCode();
        result = (result * prime) + mFileSets.hashCode();

        int size = mFilters != null ? mFilters.length : 0;
        for (int i = 0; i < size; i++)
        {
            result = (result * prime) + (mFilters[i] != null ? mFilters[i].hashCode() : 0);
        }

        // could not use it because only available in jdk1.5
        // result = (result * prime) + Arrays.hashCode(mFilters);
        return result;
    }
}