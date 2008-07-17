//============================================================================
//
// Copyright (C) 2002-2008  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.core.projectconfig.filters;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Base implementation of a filter.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class AbstractFilter implements IFilter {

    //
    // attributes
    //

    /** name of the filter. */
    private String mFilterName;

    /** internal name of the filter. */
    private String mInternalName;

    /** description of the filter. */
    private String mFilterDescription;

    /** flags if the filter is selected. */
    private boolean mSelected;

    /** flags, if the filter is readonly. */
    private boolean mReadonly;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public void initialize(String name, String internalName, String desc, boolean readonly) {

        this.mFilterName = name;
        this.mInternalName = internalName;
        this.mFilterDescription = desc;
        this.mReadonly = readonly;
    }

    /**
     * Gets the name of the filter.
     * 
     * @return the filter name
     */
    public final String getName() {
        return this.mFilterName;
    }

    /**
     * Gets the internal name of the filter.
     * 
     * @return the internal filter name
     */
    public final String getInternalName() {
        return this.mInternalName;
    }

    /**
     * Gets the description of the filter.
     * 
     * @return the description
     */
    public final String getDescription() {
        return this.mFilterDescription;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return mSelected;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean selected) {
        mSelected = selected;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadonly() {
        return mReadonly;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getFilterData() {
        // NOOP
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setFilterData(List<String> filterData) {
    // NOOP
    }

    /**
     * {@inheritDoc}
     */
    public String getPresentableFilterData() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IFilter clone() {

        IFilter o = null;
        try {
            o = (IFilter) super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
        return o;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AbstractFilter)) {
            return false;
        }
        if (this == o) {
            return true;
        }

        AbstractFilter rhs = (AbstractFilter) o;
        return new EqualsBuilder().append(mFilterName, rhs.mFilterName).append(mInternalName,
                rhs.mInternalName).append(mFilterDescription, rhs.mFilterDescription).append(
                mSelected, rhs.mSelected).append(mReadonly, rhs.mReadonly).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(389793, 1000003).append(mFilterName).append(mInternalName)
                .append(mFilterDescription).append(mSelected).append(mReadonly).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}