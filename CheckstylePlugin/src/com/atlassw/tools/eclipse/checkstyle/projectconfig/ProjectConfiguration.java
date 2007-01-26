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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.core.resources.IProject;

import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;

/**
 * Represents the configuration for a project. Contains the file sets configured
 * for the project plus the additional filters.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfiguration implements Cloneable, IProjectConfiguration
{

    //
    // attributes
    //

    /** The project. */
    private IProject mProject;

    /** The local check configurations. */
    private List mLocalCheckConfigs;

    /** the file sets. */
    private List mFileSets;

    /** the filters. */
    private List mFilters;

    /** Flags if the simple file set editor should be used. */
    private boolean mUseSimpleConfig = true;

    //
    // constructors
    //

    /**
     * Default constructor.
     * 
     * @param project the project
     * @param localConfigs the list of local check configurations
     * @param fileSets the list of configured file sets
     * @param filters the filters
     * @param useSimpleConfig <code>true</code> if simple configuration is
     *            used
     */
    public ProjectConfiguration(IProject project, List localConfigs, List fileSets, List filters,
            boolean useSimpleConfig)
    {
        mProject = project;
        mLocalCheckConfigs = localConfigs != null ? Collections.unmodifiableList(localConfigs)
                : Collections.unmodifiableList(new ArrayList());
        mFileSets = fileSets != null ? Collections.unmodifiableList(fileSets) : Collections
                .unmodifiableList(new ArrayList());

        // build list of filters
        List standardFilters = Arrays.asList(PluginFilters.getConfiguredFilters());
        mFilters = new ArrayList(standardFilters);

        if (filters != null)
        {
            // merge with filters configured for the project
            for (int i = 0, size = mFilters.size(); i < size; i++)
            {

                IFilter standardFilter = (IFilter) mFilters.get(i);

                for (int j = 0, size2 = filters.size(); j < size2; j++)
                {
                    IFilter configuredFilter = (IFilter) filters.get(j);

                    if (standardFilter.getInternalName().equals(configuredFilter.getInternalName()))
                    {
                        mFilters.set(i, configuredFilter);
                    }
                }
            }
        }

        mFilters = Collections.unmodifiableList(mFilters);

        mUseSimpleConfig = useSimpleConfig;
    }

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public IProject getProject()
    {
        return mProject;
    }

    /**
     * {@inheritDoc}
     */
    public List getLocalCheckConfigurations()
    {
        return mLocalCheckConfigs;
    }

    /**
     * {@inheritDoc}
     */
    public List getFileSets()
    {
        return mFileSets;
    }

    /**
     * {@inheritDoc}
     */
    public List getFilters()
    {
        return mFilters;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUseSimpleConfig()
    {
        return mUseSimpleConfig;
    }

    /**
     * Checks if this project configuration uses the given checkstyle
     * configuration.
     * 
     * @param configuration the check configuration
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
            ICheckConfiguration checkConfig = fileSet.getCheckConfig();
            if (configuration.equals(checkConfig)
                    || (checkConfig instanceof CheckConfigurationWorkingCopy && configuration
                            .equals(((CheckConfigurationWorkingCopy) checkConfig)
                                    .getSourceCheckConfiguration())))
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
            clone.mUseSimpleConfig = mUseSimpleConfig;

            // clone file sets
            List clonedFileSets = new ArrayList();
            Iterator iter = getFileSets().iterator();
            while (iter.hasNext())
            {
                clonedFileSets.add(((FileSet) iter.next()).clone());
            }
            clone.mFileSets = clonedFileSets;

            // clone filters
            List clonedFilters = new ArrayList();
            iter = getFilters().iterator();
            while (iter.hasNext())
            {
                clonedFilters.add(((IFilter) iter.next()).clone());
            }
            clone.mFilters = clonedFilters;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(); // should never happen
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
        ProjectConfiguration rhs = (ProjectConfiguration) obj;
        return new EqualsBuilder().append(mProject, rhs.mProject).append(mLocalCheckConfigs,
                rhs.mLocalCheckConfigs).append(mUseSimpleConfig, rhs.mUseSimpleConfig).append(
                mFileSets, rhs.mFileSets).append(mFilters, rhs.mFilters).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return new HashCodeBuilder(984759323, 1000003).append(mProject).append(mLocalCheckConfigs)
                .append(mUseSimpleConfig).append(mFileSets).append(mFilters).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}