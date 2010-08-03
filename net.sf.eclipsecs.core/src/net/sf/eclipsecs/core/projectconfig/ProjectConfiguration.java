//============================================================================
//
// Copyright (C) 2002-2010  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.projectconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.core.resources.IProject;

/**
 * Represents the configuration for a project. Contains the file sets configured
 * for the project plus the additional filters.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfiguration implements Cloneable, IProjectConfiguration {

    //
    // attributes
    //

    /** The project. */
    private IProject mProject;

    /** The local check configurations. */
    private List<ICheckConfiguration> mLocalCheckConfigs;

    /** the file sets. */
    private List<FileSet> mFileSets;

    /** the filters. */
    private List<IFilter> mFilters;

    /** Flags if the simple file set editor should be used. */
    private boolean mUseSimpleConfig = true;

    /** if formatter synching is enabled. */
    private boolean mSyncFormatter;

    //
    // constructors
    //

    /**
     * Default constructor.
     * 
     * @param project
     *            the project
     * @param localConfigs
     *            the list of local check configurations
     * @param fileSets
     *            the list of configured file sets
     * @param filters
     *            the filters
     * @param useSimpleConfig
     *            <code>true</code> if simple configuration is used
     */
    public ProjectConfiguration(IProject project,
        List<ICheckConfiguration> localConfigs, List<FileSet> fileSets,
        List<IFilter> filters, boolean useSimpleConfig, boolean synchFormatter) {
        mProject = project;
        mLocalCheckConfigs = localConfigs != null ? Collections
            .unmodifiableList(localConfigs) : Collections
            .unmodifiableList(new ArrayList<ICheckConfiguration>());
        mFileSets = fileSets != null ? Collections.unmodifiableList(fileSets)
            : Collections.unmodifiableList(new ArrayList<FileSet>());

        // build list of filters
        List<IFilter> standardFilters = Arrays.asList(PluginFilters
            .getConfiguredFilters());
        mFilters = new ArrayList<IFilter>(standardFilters);

        if (filters != null) {
            // merge with filters configured for the project
            for (int i = 0, size = mFilters.size(); i < size; i++) {

                IFilter standardFilter = mFilters.get(i);

                for (int j = 0, size2 = filters.size(); j < size2; j++) {
                    IFilter configuredFilter = filters.get(j);

                    if (standardFilter.getInternalName().equals(
                        configuredFilter.getInternalName())) {
                        mFilters.set(i, configuredFilter);
                    }
                }
            }
        }

        mFilters = Collections.unmodifiableList(mFilters);

        mUseSimpleConfig = useSimpleConfig;
        mSyncFormatter = synchFormatter;
    }

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public IProject getProject() {
        return mProject;
    }

    /**
     * {@inheritDoc}
     */
    public List<ICheckConfiguration> getLocalCheckConfigurations() {
        return mLocalCheckConfigs;
    }

    /**
     * {@inheritDoc}
     */
    public List<FileSet> getFileSets() {
        return mFileSets;
    }

    /**
     * {@inheritDoc}
     */
    public List<IFilter> getFilters() {
        return mFilters;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUseSimpleConfig() {
        return mUseSimpleConfig;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSyncFormatter() {
        return mSyncFormatter;
    }

    /**
     * Checks if this project configuration uses the given checkstyle
     * configuration.
     * 
     * @param configuration
     *            the check configuration
     * @return <code>true</code>, if the project config uses the checkstyle
     *         config, <code>false</code> otherwise
     */
    public boolean isConfigInUse(ICheckConfiguration configuration) {

        boolean result = false;

        for (FileSet fileSet : getFileSets()) {
            ICheckConfiguration checkConfig = fileSet.getCheckConfig();
            if (configuration.equals(checkConfig)
                || (checkConfig instanceof CheckConfigurationWorkingCopy && configuration
                    .equals(((CheckConfigurationWorkingCopy) checkConfig)
                        .getSourceCheckConfiguration()))) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public ProjectConfiguration clone() {
        ProjectConfiguration clone = null;
        try {
            clone = (ProjectConfiguration) super.clone();
            clone.mFileSets = new LinkedList<FileSet>();
            clone.mUseSimpleConfig = mUseSimpleConfig;
            clone.mSyncFormatter = mSyncFormatter;

            // clone file sets
            List<FileSet> clonedFileSets = new ArrayList<FileSet>();
            for (FileSet fileSet : getFileSets()) {
                clonedFileSets.add(fileSet.clone());
            }
            clone.mFileSets = clonedFileSets;

            // clone filters
            List<IFilter> clonedFilters = new ArrayList<IFilter>();
            for (IFilter filter : getFilters()) {
                clonedFilters.add(filter.clone());
            }
            clone.mFilters = clonedFilters;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(); // should never happen
        }

        return clone;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof ProjectConfiguration)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ProjectConfiguration rhs = (ProjectConfiguration) obj;
        return new EqualsBuilder().append(mProject, rhs.mProject).append(
            mLocalCheckConfigs, rhs.mLocalCheckConfigs).append(
            mUseSimpleConfig, rhs.mUseSimpleConfig).append(mSyncFormatter,
            rhs.mSyncFormatter).append(mFileSets, rhs.mFileSets).append(
            mFilters, rhs.mFilters).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(984759323, 1000003).append(mProject).append(
            mLocalCheckConfigs).append(mUseSimpleConfig).append(mFileSets)
            .append(mFilters).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE);
    }
}
