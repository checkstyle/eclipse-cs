//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * filters resources that lie within excluded packages. This filter is used for
 * the checkstyle audit funtion of this plugin.
 * 
 * @author Lars Ködderitzsch
 */
public class PackageFilter extends AbstractFilter
{

    private List mData = new ArrayList();

    /**
     * @see IFilter#accept(java.lang.Object)
     */
    public boolean accept(Object element)
    {

        boolean goesThrough = true;

        if (element instanceof IResource)
        {

            IResource resource = (IResource) element;

            IPath projRelativPath = resource.getProjectRelativePath();

            int size = mData != null ? mData.size() : 0;
            for (int i = 0; i < size; i++)
            {

                IPath filteredPath = new Path((String) mData.get(i));
                if (filteredPath.isPrefixOf(projRelativPath))
                {
                    goesThrough = false;
                    break;
                }
            }
        }
        return goesThrough;
    }

    /**
     * @see IFilter#setFilterData(java.lang.String)
     */
    public void setFilterData(List filterData)
    {
        if (filterData == null)
        {
            mData = new ArrayList();
        }

        mData = filterData;
    }

    /**
     * @see IFilter#getFilterData()
     */
    public List getFilterData()
    {
        return mData;
    }

    /**
     * @see IFilter#getPresentableFilterData()
     */
    public String getPresentableFilterData()
    {

        StringBuffer buf = new StringBuffer();

        int size = mData != null ? mData.size() : 0;
        for (int i = 0; i < size; i++)
        {
            if (i > 0)
            {
                buf.append(", ");
            }

            buf.append(mData.get(i));
        }

        return buf.toString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {

        //a "nice" prime number, see Java Report, April 2000
        final int prime = 1000003;

        int result = super.hashCode();
        result = (result * prime) + (this.mData != null ? this.mData.hashCode() : 0);

        return result;
    }
}