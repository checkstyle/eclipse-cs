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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Base implementation of a check configuration. Leaves the specific tasks to
 * the concrete subclasses.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckConfiguration implements ICheckConfiguration
{

    //
    // attributes
    //

    /** the displayable name of the configuration. */
    private String mName;

    /** the location of the checkstyle configuration file. */
    private String mLocation;

    /** the description of the configuration. */
    private String mDescription;

    /** the configuration type. */
    private IConfigurationType mConfigType;

    /** flags if the configuration is global. */
    private boolean mIsGlobal;

    /** The list of resolvable properties. */
    private List mProperties;

    /** Map containing additional data for this check configuration. */
    private Map mAdditionalData;

    //
    // methods
    //

    /**
     * Creates a check configuration instance.
     * 
     * @param name the name of the check configuration
     * @param location the location of the check configuration
     * @param description the description of the check configuration
     * @param type the check configuration type
     * @param global determines if the check configuration is a global
     *            configuration
     * @param properties the list of properties configured for this check
     *            configuration
     * @param additionalData a map of additional data for this configuration
     */
    public CheckConfiguration(String name, String location, String description,
            IConfigurationType type, boolean global, List properties, Map additionalData)
    {
        mName = name;
        mLocation = location;
        mDescription = description;
        mConfigType = type;
        mIsGlobal = global;

        if (additionalData != null)
        {
            mAdditionalData = Collections.unmodifiableMap(additionalData);
        }
        else
        {
            mAdditionalData = Collections.unmodifiableMap(new HashMap());
        }

        mProperties = properties != null ? Collections.unmodifiableList(properties) : Collections
                .unmodifiableList(new ArrayList());
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return mName;
    }

    /**
     * {@inheritDoc}
     */
    public String getLocation()
    {
        return mLocation;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * {@inheritDoc}
     */
    public IConfigurationType getType()
    {
        return mConfigType;
    }

    /**
     * {@inheritDoc}
     */
    public Map getAdditionalData()
    {
        return mAdditionalData;
    }

    /**
     * {@inheritDoc}
     */
    public List getResolvableProperties()
    {
        return mProperties;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditable()
    {
        return mConfigType.isEditable();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurable()
    {
        return mConfigType.isConfigurable(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGlobal()
    {
        return mIsGlobal;
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver() throws CheckstylePluginException
    {
        return mConfigType.getPropertyResolver(this);
    }

    /**
     * {@inheritDoc}
     */
    public URL isConfigurationAvailable() throws CheckstylePluginException
    {
        URL configLocation = null;

        InputStream in = null;
        try
        {
            in = openConfigurationFileStream();
            configLocation = mConfigType.resolveLocation(this);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
        return configLocation;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream openConfigurationFileStream() throws CheckstylePluginException
    {
        return mConfigType.openConfigurationFileStream(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ICheckConfiguration))
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        ICheckConfiguration rhs = (ICheckConfiguration) obj;
        return new EqualsBuilder().append(getName(), rhs.getName()).append(getLocation(),
                rhs.getLocation()).append(getDescription(), rhs.getDescription()).append(getType(),
                rhs.getType()).append(isGlobal(), rhs.isGlobal()).append(getResolvableProperties(),
                rhs.getResolvableProperties()).append(getAdditionalData(), rhs.getAdditionalData())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return new HashCodeBuilder(928729, 1000003).append(getName()).append(getLocation()).append(
                getDescription()).append(getType()).append(isGlobal()).append(
                getResolvableProperties()).append(getAdditionalData()).toHashCode();
    }
}