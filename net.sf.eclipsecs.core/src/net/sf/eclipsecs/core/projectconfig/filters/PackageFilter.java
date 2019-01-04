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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * filters resources that lie within excluded packages. This filter is used for the checkstyle audit
 * function of this plugin.
 *
 * @author Lars Ködderitzsch
 */
public class PackageFilter extends AbstractFilter {

  /**
   * Marker string in the filter data, if present the subpackes of a filtered package are not
   * recursively excluded, but only the filtered package itself.
   */
  public static final String RECURSE_OFF_MARKER = "<recurse=false>";

  private List<String> mData = new ArrayList<>();

  private boolean mExcludeSubPackages = true;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean accept(Object element) {

    boolean goesThrough = true;

    if (element instanceof IResource) {

      IResource resource = (IResource) element;

      IContainer folder = null;

      if (resource instanceof IContainer) {
        folder = (IContainer) resource;
      } else {
        folder = resource.getParent();
      }

      IPath projRelativPath = folder.getProjectRelativePath();

      int size = mData != null ? mData.size() : 0;
      for (int i = 0; i < size; i++) {

        String el = mData.get(i);

        if (RECURSE_OFF_MARKER.equals(el)) {
          continue;
        }

        IPath filteredPath = new Path(el);
        if (mExcludeSubPackages && filteredPath.isPrefixOf(projRelativPath)) {
          goesThrough = false;
          break;
        } else if (!mExcludeSubPackages && filteredPath.equals(projRelativPath)) {
          goesThrough = false;
          break;
        }
      }
    }
    return goesThrough;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFilterData(List<String> filterData) {
    if (filterData == null) {
      mData = new ArrayList<>();
    }

    mData = filterData;

    if (mData.contains(RECURSE_OFF_MARKER)) {
      mExcludeSubPackages = false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFilterData() {
    return mData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getPresentableFilterData() {

    StringBuffer buf = new StringBuffer();

    int size = mData != null ? mData.size() : 0;
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", "); //$NON-NLS-1$
      }

      buf.append(mData.get(i));
    }

    return buf.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {

    if (o == null || !(o instanceof PackageFilter)) {
      return false;
    }
    if (this == o) {
      return true;
    }

    PackageFilter rhs = (PackageFilter) o;
    return super.equals(o) && Objects.equals(mData, rhs.mData);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), mData);
  }
}
