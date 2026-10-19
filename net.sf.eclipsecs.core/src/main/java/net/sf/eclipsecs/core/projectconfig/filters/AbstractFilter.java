///////////////////////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.projectconfig.filters;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Base implementation of a filter.
 *
 */
public abstract class AbstractFilter implements IFilter {

    /** Name of the filter. */
    private String filterName;

    /** Internal name of the filter. */
    private String internalName;

    /** Description of the filter. */
    private String filterDescription;

    /** Flags if the filter is selected. */
    private boolean selected;

    /** Flags, if the filter is readonly. */
    private boolean readonly;

    /**
     * Creates a new abstract filter.
     */
    protected AbstractFilter() {

    }

    /**
     * Copy constructor.
     *
     * @param other
     *            the filter to copy
     */
    protected AbstractFilter(AbstractFilter other) {
        this.filterName = other.filterName;
        this.internalName = other.internalName;
        this.filterDescription = other.filterDescription;
        this.selected = other.selected;
        this.readonly = other.readonly;
    }

    @Override
    public void initialize(String initFilterName, String initInternalName, String initDesc,
        boolean initReadonly) {
        this.filterName = initFilterName;
        this.internalName = initInternalName;
        this.filterDescription = initDesc;
        this.readonly = initReadonly;
    }

    /**
     * Gets the name of the filter.
     *
     * @return the filter name
     */
    @Override
    public final String getName() {
        return this.filterName;
    }

    /**
     * Gets the internal name of the filter.
     *
     * @return the internal filter name
     */
    @Override
    public final String getInternalName() {
        return this.internalName;
    }

    /**
     * Gets the description of the filter.
     *
     * @return the description
     */
    @Override
    public final String getDescription() {
        return this.filterDescription;
    }

    @Override
    public boolean isEnabled() {
        return selected;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.selected = enabled;
    }

    @Override
    public boolean isReadonly() {
        return readonly;
    }

    @Override
    public List<String> getFilterData() {
        // NOOP
        return Collections.emptyList();
    }

    @Override
    public void setFilterData(List<String> filterData) {
        // NOOP
    }

    @Override
    public String getPresentableFilterData() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AbstractFilter)) {
            return false;
        }
        if (this == o) {
            return true;
        }

        final AbstractFilter rhs = (AbstractFilter) o;
        return Objects.equals(filterName, rhs.filterName)
            && Objects.equals(internalName, rhs.internalName)
            && Objects.equals(filterDescription, rhs.filterDescription)
            && Objects.equals(selected, rhs.selected) && Objects.equals(readonly, rhs.readonly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterName, internalName, filterDescription, selected, readonly);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("filterName", filterName)
            .add("internalName", internalName).add("filterDescription", filterDescription)
            .add("selected", selected).add("readonly", readonly).toString();
    }

}
