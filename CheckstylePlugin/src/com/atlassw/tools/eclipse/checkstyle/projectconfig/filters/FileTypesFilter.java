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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.resources.IResource;

/**
 * Filter that excludes all but some specifically defined file types from the
 * checks.
 * 
 * @author Lars Ködderitzsch
 */
public class FileTypesFilter extends AbstractFilter
{

    //
    // attributes
    //

    /** List containing the passing file types. */
    private List mFileTypes;

    //
    // attributes
    //

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object element)
    {
        boolean goesThrough = false;

        if (element instanceof IResource)
        {

            IResource resource = (IResource) element;
            int type = resource.getType();

            // java files go through
            goesThrough = (IResource.FILE == type)
                    && (mFileTypes.contains(resource.getFileExtension()));
        }
        return goesThrough;
    }

    /**
     * {@inheritDoc}
     */
    public void setFilterData(List filterData)
    {
        mFileTypes = filterData;
    }

    /**
     * {@inheritDoc}
     */
    public List getFilterData()
    {
        return mFileTypes;
    }

    /**
     * {@inheritDoc}
     */
    public String getPresentableFilterData()
    {
        StringBuffer buf = new StringBuffer();

        int size = mFileTypes != null ? mFileTypes.size() : 0;
        for (int i = 0; i < size; i++)
        {
            if (i > 0)
            {
                buf.append(", "); //$NON-NLS-1$
            }

            buf.append(mFileTypes.get(i));
        }

        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o)
    {

        if (o == null || !(o instanceof FileTypesFilter))
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }

        FileTypesFilter rhs = (FileTypesFilter) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(mFileTypes, rhs.mFileTypes)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return new HashCodeBuilder(7834681, 1000003).appendSuper(super.hashCode()).append(
                mFileTypes).toHashCode();
    }
}