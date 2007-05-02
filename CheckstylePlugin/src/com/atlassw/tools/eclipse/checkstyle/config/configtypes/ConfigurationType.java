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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.CheckstyleConfigurationFile;
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
    public URL getResolvedConfigurationFileURL(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        URL url = null;

        try
        {
            url = resolveLocation(checkConfiguration);
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        return url;
    }

    /**
     * {@inheritDoc}
     */
    public CheckstyleConfigurationFile getCheckstyleConfiguration(
            ICheckConfiguration checkConfiguration) throws CheckstylePluginException
    {

        CheckstyleConfigurationFile data = new CheckstyleConfigurationFile();

        try
        {

            // resolve the true configuration file URL
            data.setResolvedConfigFileURL(resolveLocation(checkConfiguration));

            URLConnection connection = data.getResolvedConfigFileURL().openConnection();
            connection.connect();

            // get last modification timestamp
            data.setModificationStamp(connection.getLastModified());

            // get the configuration file data
            byte[] configurationFileData = getBytesFromURLConnection(connection);
            data.setCheckConfigFileBytes(configurationFileData);

            // get the properties bundle
            byte[] additionalPropertiesBytes = getAdditionPropertiesBundleBytes(data
                    .getResolvedConfigFileURL());
            data.setAdditionalPropertyBundleBytes(additionalPropertiesBytes);

            // get the property resolver
            PropertyResolver resolver = getPropertyResolver(checkConfiguration, data);
            data.setPropertyResolver(resolver);

        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return data;
    }

    /**
     * Returns the URL of the checkstyle configuration file. Implementors are
     * not expected to open any connection to the URL.
     * 
     * @param checkConfiguration the actual check configuration
     * @return the URL of the checkstyle configuration file
     * @throws IOException error while resolving the url
     */
    protected abstract URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws IOException;

    protected byte[] getAdditionPropertiesBundleBytes(URL checkConfigURL) throws IOException
    {

        String location = checkConfigURL.toString();

        // Strip file extension
        String propsLocation = null;
        int lastPointIndex = location.lastIndexOf("."); //$NON-NLS-1$
        if (lastPointIndex > -1)
        {
            propsLocation = location.substring(0, lastPointIndex);
        }
        else
        {
            propsLocation = location;
        }

        propsLocation = propsLocation + ".properties"; //$NON-NLS-1$

        try
        {

            URL propertyFileURL = new URL(propsLocation);
            URLConnection connection = propertyFileURL.openConnection();

            return getBytesFromURLConnection(connection);
        }
        catch (IOException e)
        {
            // we won't load the bundle then
            // disabled logging bug #1647602
            // CheckstyleLog.log(ioe);
        }
        return null;
    }

    /**
     * Gets the property resolver for this configuration type used to expand
     * property values within the checkstyle configuration.
     * 
     * @param checkConfiguration the actual check configuration
     * @return the property resolver
     * @throws IOException error creating the property resolver
     */
    protected PropertyResolver getPropertyResolver(ICheckConfiguration config,
            CheckstyleConfigurationFile configFile) throws IOException
    {

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new ResolvablePropertyResolver(config));
        multiResolver.addPropertyResolver(new StandardPropertyResolver(configFile
                .getResolvedConfigFileURL().getFile()));
        multiResolver.addPropertyResolver(new ClasspathVariableResolver());
        multiResolver.addPropertyResolver(new SystemPropertyResolver());

        if (configFile.getAdditionalPropertiesBundleStream() != null)
        {
            ResourceBundle bundle = new PropertyResourceBundle(configFile
                    .getAdditionalPropertiesBundleStream());
            multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
        }

        return multiResolver;
    }

    protected byte[] getBytesFromURLConnection(URLConnection connection) throws IOException
    {

        byte[] configurationFileData = null;

        InputStream in = null;

        try
        {
            in = connection.getInputStream();
            configurationFileData = IOUtils.toByteArray(in);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
        return configurationFileData;
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