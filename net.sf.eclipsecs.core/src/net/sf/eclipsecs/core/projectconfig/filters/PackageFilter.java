//============================================================================
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
 */
public class PackageFilter extends AbstractFilter {

  /**
   * Marker string in the filter data, if present the subpackes of a filtered package are not
   * recursively excluded, but only the filtered package itself.
   */
  public static final String RECURSE_OFF_MARKER = "<recurse=false>";

  private List<String> mData = new ArrayList<>();

  private boolean mExcludeSubPackages = true;

  @Override
  public boolean accept(Object object) {
    if (object instanceof IResource resource) {
      IContainer folder;
      if (resource instanceof IContainer) {
        folder = (IContainer) resource;
      } else {
        folder = resource.getParent();
      }

      IPath projRelativPath = folder.getProjectRelativePath();

      int size = mData != null ? mData.size() : 0;
      for (int i = 0; i < size; i++) {
        String element = mData.get(i);

        if (RECURSE_OFF_MARKER.equals(element)) {
          continue;
        }

        IPath filteredPath = new Path(element);
        if (mExcludeSubPackages && filteredPath.isPrefixOf(projRelativPath)
                || !mExcludeSubPackages && filteredPath.equals(projRelativPath)) {
          return false;
        }
      }
    }
    return true;
  }

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

  @Override
  public List<String> getFilterData() {
    return mData;
  }

  @Override
  public String getPresentableFilterData() {

    StringBuilder buf = new StringBuilder();

    int size = mData != null ? mData.size() : 0;
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", "); //$NON-NLS-1$
      }

      buf.append(mData.get(i));
    }

    return buf.toString();
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), mData);
  }
}
