//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Base implementation of <code>IConfigurationType</code>.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class ConfigurationType implements IConfigurationType
{

    //
    // attributes
    //

    /** The name. */
    private String mName;

    /** The internal name. */
    private String mInternalName;

    /** The editor class. */
    private Class mEditorClass;

    /** The icon image. */
    private Image mIcon;

    /** Flag if creatable. */
    private boolean mIsCreatable;

    /** Flag if properties are editable. */
    private boolean mIsEditable;

    /** Flag if configuration file is configurable. */
    private boolean mIsConfigurable;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public void initialize(String name, String internalName, Class editorClass, String image,
            String definingPluginId, boolean creatable, boolean editable, boolean configurable)
    {
        mName = name;
        mInternalName = internalName;
        mEditorClass = editorClass;
        mIsCreatable = creatable;
        mIsEditable = editable;
        mIsConfigurable = configurable;

        if (image != null && definingPluginId != null)
        {
            ImageDescriptor imageDescriptor = CheckstylePlugin.imageDescriptorFromPlugin(
                    definingPluginId, image);
            mIcon = imageDescriptor.createImage(false);
        }
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
    public String getInternalName()
    {
        return mInternalName;
    }

    /**
     * {@inheritDoc}
     */
    public Class getLocationEditorClass()
    {
        return mEditorClass;
    }

    /**
     * {@inheritDoc}
     */
    public Image getTypeImage()
    {
        return mIcon;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCreatable()
    {
        return mIsCreatable;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditable()
    {
        return mIsEditable;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurable(ICheckConfiguration checkConfiguration)
    {
        return mIsConfigurable;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream openConfigurationFileStream(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        InputStream inStream = null;

        try
        {
            URL configURL = resolveLocation(checkConfiguration);
            inStream = new BufferedInputStream(configURL.openStream());
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return inStream;
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new ResolvablePropertyResolver(checkConfiguration));
        multiResolver.addPropertyResolver(new StandardPropertyResolver(resolveLocation(
                checkConfiguration).getFile()));
        multiResolver.addPropertyResolver(new ClasspathVariableResolver());
        multiResolver.addPropertyResolver(new SystemPropertyResolver());

        return multiResolver;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
    // standard is that nothing happens
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {

        if (obj == null || !(obj instanceof ConfigurationType))
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }
        ConfigurationType rhs = (ConfigurationType) obj;
        return new EqualsBuilder().append(mName, rhs.mName)
                .append(mInternalName, rhs.mInternalName).append(mEditorClass, rhs.mEditorClass)
                .append(mIcon, rhs.mIcon).append(mIsCreatable, rhs.mIsCreatable).append(
                        mIsEditable, rhs.mIsEditable).append(mIsConfigurable, rhs.mIsConfigurable)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return new HashCodeBuilder(82713903, 1000003).append(mName).append(mInternalName).append(
                mEditorClass).append(mIcon).append(mIsCreatable).append(mIsEditable).append(
                mIsConfigurable).toHashCode();
    }
}