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

package com.atlassw.tools.eclipse.checkstyle.config;

import com.atlassw.tools.eclipse.checkstyle.config.meta.ConfigPropertyMetadata;

/**
 * A simple configuration consisting of a name/value pair.
 */
public class ConfigProperty implements Comparable, Cloneable
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    /** The name of the property. */
    private String mName;

    /** The value of the property. */
    private String mValue;

    /** The meta data of the property. */
    private ConfigPropertyMetadata mMetaData;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Constructor.
     * 
     * @param metaData the property meta data
     */
    public ConfigProperty(ConfigPropertyMetadata metaData)
    {

        this(metaData.getName(), metaData.getOverrideDefault() != null ? metaData
                .getOverrideDefault() : metaData.getDefaultValue());
        setMetaData(metaData);
    }

    /**
     * Constructor.
     * 
     * @param name Property name.
     * @param value Property value.
     */
    public ConfigProperty(String name, String value)
    {
        setName(name);
        setValue(value);
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Get the property's name.
     * 
     * @return The name
     */
    public String getName()
    {
        return mName;
    }

    /**
     * Set the property's name.
     * 
     * @param name The new name.
     */
    public void setName(String name)
    {
        mName = name;
    }

    /**
     * Returns the value.
     * 
     * @return String
     */
    public String getValue()
    {
        return mValue;
    }

    /**
     * Sets the value.
     * 
     * @param value The value to set
     */
    public void setValue(String value)
    {
        mValue = value;
    }

    /**
     * Returns the meta data for this property.
     * 
     * @return the meta data
     */
    public ConfigPropertyMetadata getMetaData()
    {
        return mMetaData;
    }

    /**
     * Sets the meta data for this property.
     * 
     * @param metaData the meta data
     */
    public void setMetaData(ConfigPropertyMetadata metaData)
    {
        mMetaData = metaData;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object obj)
    {
        return this.mName.compareTo(((ConfigProperty) obj).mName);
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        try
        {
            Object clone = super.clone();
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(); // Should not happen
        }
    }
}