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

import java.util.List;

/**
 * Base implementation of a filter.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class AbstractFilter implements IFilter
{

    //
    // attributes
    //

    /** name of the filter. */
    private String  mFilterName;

    /** internal name of the filter. */
    private String  mInternalName;

    /** description of the filter. */
    private String  mFilterDescription;

    /** class for the editor of this filter. */
    private Class   mFilterEditor;

    /** flags if the filter is selected. */
    private boolean mSelected;

    /** flags, if the filter is readonly. */
    private boolean mReadonly;

    //
    // methods
    //

    /**
     * @see IFilter#initialize(java.lang.String, sjava.lang.String,
     *      java.lang.Class, boolean)
     */
    public void initialize(String name, String internalName, String desc, Class editorClass,
            boolean readonly)
    {

        this.mFilterName = name;
        this.mInternalName = internalName;
        this.mFilterDescription = desc;
        this.mFilterEditor = editorClass;
        this.mReadonly = readonly;
    }

    /**
     * Gets the name of the filter.
     * 
     * @return the filter name
     */
    public final String getName()
    {
        return this.mFilterName;
    }

    /**
     * Gets the internal name of the filter.
     * 
     * @return the internal filter name
     */
    public final String getInternalName()
    {
        return this.mInternalName;
    }

    /**
     * Gets the description of the filter.
     * 
     * @return the description
     */
    public final String getDescription()
    {
        return this.mFilterDescription;
    }

    /**
     * Gets the editor class for the filter.
     * 
     * @return the editor class
     */
    public final Class getEditorClass()
    {
        return this.mFilterEditor;
    }

    /**
     * @see IFilter#isEditable()
     */
    public boolean isEditable()
    {

        return this.mFilterEditor != null;
    }

    /**
     * @see IFilter#isEnabled()
     */
    public boolean isEnabled()
    {
        return mSelected;
    }

    /**
     * @see IFilter#setEnabled(boolean)
     */
    public void setEnabled(boolean selected)
    {
        mSelected = selected;
    }

    /**
     * @see IFilter#isReadonly()
     */
    public boolean isReadonly()
    {
        return mReadonly;
    }

    /**
     * @see IFilter#getFilterData()
     */
    public List getFilterData()
    {
        //NOOP
        return null;
    }

    /**
     * @see IFilter#setFilterData(java.lang.String)
     */
    public void setFilterData(List filterData)
    {
    //NOOP
    }

    /**
     * @see IFilter#getPresentableFilterData()
     */
    public String getPresentableFilterData()
    {
        return null;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {

        Object o = null;
        try
        {
            o = super.clone();
        }
        catch (CloneNotSupportedException cnse)
        {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
        return o;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        return this.hashCode() == o.hashCode();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        //a "nice" prime number, see Java Report, April 2000
        final int prime = 1000003;

        int result = 1;

        result = (result * prime) + (this.mFilterName != null ? this.mFilterName.hashCode() : 0);
        result = (result * prime)
                + (this.mFilterDescription != null ? this.mFilterDescription.hashCode() : 0);
        result = (result * prime)
                + (this.mFilterEditor != null ? this.mFilterEditor.hashCode() : 0);
        result = (result * prime) + Boolean.valueOf(mSelected).hashCode();
        result = (result * prime) + Boolean.valueOf(mReadonly).hashCode();

        return result;
    }

}