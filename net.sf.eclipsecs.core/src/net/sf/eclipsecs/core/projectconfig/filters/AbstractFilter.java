//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Objects;

/**
 * Base implementation of a filter.
 *
 * @author Lars Ködderitzsch
 */
public abstract class AbstractFilter implements IFilter {

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

  /**
   * {@inheritDoc}
   */
  @Override
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
  @Override
  public final String getName() {
    return this.mFilterName;
  }

  /**
   * Gets the internal name of the filter.
   *
   * @return the internal filter name
   */
  @Override
  public final String getInternalName() {
    return this.mInternalName;
  }

  /**
   * Gets the description of the filter.
   *
   * @return the description
   */
  @Override
  public final String getDescription() {
    return this.mFilterDescription;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEnabled() {
    return mSelected;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEnabled(boolean selected) {
    mSelected = selected;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isReadonly() {
    return mReadonly;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFilterData() {
    // NOOP
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFilterData(List<String> filterData) {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPresentableFilterData() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IFilter clone() {

    IFilter o = null;
    try {
      o = (IFilter) super.clone();
    } catch (CloneNotSupportedException cnse) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
    return o;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof AbstractFilter)) {
      return false;
    }
    if (this == o) {
      return true;
    }

    AbstractFilter rhs = (AbstractFilter) o;
    return Objects.equals(mFilterName, rhs.mFilterName)
            && Objects.equals(mInternalName, rhs.mInternalName)
            && Objects.equals(mFilterDescription, rhs.mFilterDescription)
            && Objects.equals(mSelected, rhs.mSelected) && Objects.equals(mReadonly, rhs.mReadonly);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(mFilterName, mInternalName, mFilterDescription, mSelected, mReadonly);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("filterName", mFilterName)
            .add("internalName", mInternalName).add("filterDescription", mFilterDescription)
            .add("selected", mSelected).add("readonly", mReadonly).toString();
  }
}
