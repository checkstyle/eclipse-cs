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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationReader;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationWriter;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Abstract base implementation of a check configuration. Leaves the specific
 * tasks to the concrete subclasses.
 * 
 * @author Lars Ködderitzsch
 */
public abstract class AbstractCheckConfiguration implements ICheckConfiguration
{

    //
    // attributes
    //

    /** the displayable name of the configuration. */
    private String mName;

    /** the location of the checkstyle configuration file. */
    protected String mLocation;

    /** the description of the configuration. */
    private String mDescription;

    /** the configuration type. */
    private IConfigurationType mConfigType;

    /** the current project context. */
    private IProject mContext;

    /** the original check configuration. */
    private ICheckConfiguration mOriginal;

    /** flags if the configuration is dirty. */
    private boolean mIsDirty;

    /** flags if the configuration is currently initializing. */
    private boolean mIsInitializing;

    //
    // methods
    //

    /**
     * @see ICheckConfiguration#initialize( java.lang.String, java.lang.String,
     *      com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType,
     *      java.lang.String)
     */
    public void initialize(String name, String location, IConfigurationType type, String description)
        throws CheckstylePluginException
    {
        //set the flag to enforce the changes done here do not mark the object
        // dirty
        mIsInitializing = true;
        try
        {

            setName(name);
            setLocation(location);
            mConfigType = type;
            setDescription(description);
        }
        finally
        {
            mIsInitializing = false;
        }
    }

    /**
     * @see ICheckConfiguration#setContext(org.eclipse.core.resources.IProject)
     */
    public void setContext(IProject context)
    {
        mContext = context;
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration#getName()
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @see ICheckConfiguration#setName(java.lang.String)
     */
    public void setName(String name) throws CheckstylePluginException
    {

        if (name == null || name.trim().length() == 0)
        {
            throw new CheckstylePluginException(ErrorMessages.errorConfigNameEmpty);
        }

        String oldName = getName();
        mName = name;

        // Check if the new name is in use
        if (CheckConfigurationFactory.isNameCollision(this))
        {
            mName = oldName;
            throw new CheckstylePluginException(NLS.bind(ErrorMessages.errorConfigNameInUse, name));
        }
    }

    /**
     * @see ICheckConfiguration#getLocation()
     */
    public String getLocation()
    {
        return mLocation;
    }

    /**
     * @see ICheckConfiguration#setLocation(java.lang.String)
     */
    public void setLocation(String location) throws CheckstylePluginException
    {
        if (location == null || location.trim().length() == 0)
        {
            throw new CheckstylePluginException(ErrorMessages.errorLocationEmpty);
        }

        String oldLocation = getLocation();
        mLocation = location;

        try
        {
            handleCanResolveLocation();

            //if the location validly changed set dirty to mark for rebuild
            if (!mIsInitializing && !location.equals(oldLocation))
            {
                mIsDirty = true;
            }
        }
        catch (Exception e)
        {
            mLocation = oldLocation;
            throw new CheckstylePluginException(NLS.bind(ErrorMessages.errorResolveConfigLocation,
                    location, e.getLocalizedMessage()));
        }
    }

    /**
     * @see ICheckConfiguration#getDescription()
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @see ICheckConfiguration#setDescription(java.lang.String)
     */
    public void setDescription(String description)
    {
        mDescription = description;
    }

    /**
     * @see ICheckConfiguration#getType()
     */
    public IConfigurationType getType()
    {
        return mConfigType;
    }

    /**
     * @see ICheckConfiguration#isEditable()
     */
    public boolean isEditable()
    {
        return handleIsEditable();
    }

    /**
     * @see ICheckConfiguration#isConfigurable()
     */
    public boolean isConfigurable()
    {
        return handleIsConfigurable();
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration#isContextNeeded()
     */
    public boolean isContextNeeded()
    {
        // By default no context is needed
        return false;
    }

    /**
     * @see ICheckConfiguration#getOriginalCheckConfig()
     */
    public ICheckConfiguration getOriginalCheckConfig()
    {
        if (mOriginal != null && mOriginal.getOriginalCheckConfig() == null)
        {
            return mOriginal;
        }
        else if (mOriginal != null)
        {
            return mOriginal.getOriginalCheckConfig();
        }
        else
        {
            return null;
        }
    }

    /**
     * @see ICheckConfiguration#setOriginalCheckConfig(ICheckConfiguration)
     */
    public void setOriginalCheckConfig(ICheckConfiguration original)
    {
        mOriginal = original;
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration#isDirty()
     */
    public boolean isDirty()
    {
        return mIsDirty;
    }


    /**
     * @see ICheckConfiguration#getPropertyResolver()
     */
    public PropertyResolver getPropertyResolver()
    {
        return handleGetPropertyResolver();
    }

    /**
     * @see ICheckConfiguration#getCheckstyleConfigurationURL()
     */
    public URL getCheckstyleConfigurationURL() throws CheckstylePluginException
    {
        URL location = null;

        try
        {
            // check if URL resolves
            URL configURL = handleGetLocation();
            configURL.openStream();

            location = configURL;
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return location;
    }

    /**
     * @see ICheckConfiguration#getModules()
     */
    public List getModules() throws CheckstylePluginException
    {
        List result = null;

        InputStream in = null;

        try
        {
            in = getCheckstyleConfigurationURL().openStream();
            result = ConfigurationReader.read(in);
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (Exception e)
            {
                // Can do nothing
            }
        }

        return result;
    }

    /**
     * @see ICheckConfiguration#setModules(java.util.List)
     */
    public void setModules(List modules) throws CheckstylePluginException
    {

        OutputStream out = null;
        ByteArrayOutputStream byteOut = null;
        try
        {
            System.out.println("Writing to: " + getCheckstyleConfigurationURL()); //$NON-NLS-1$

            // First write to a byte array outputstream
            // because otherwise in an error case the original
            // file would be destroyed
            byteOut = new ByteArrayOutputStream();

            ConfigurationWriter.write(byteOut, modules);

            // all went ok, write to the file
            out = new BufferedOutputStream(new FileOutputStream(getCheckstyleConfigurationURL()
                    .getFile()));

            out.write(byteOut.toByteArray());

            //if the checkstyle configuration validly changed set dirty to mark
            // for rebuild
            if (!mIsInitializing)
            {
                mIsDirty = true;
            }
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        finally
        {
            try
            {
                byteOut.close();
            }
            catch (Exception e)
            {
                // Can do nothing
            }
            try
            {
                out.close();
            }
            catch (Exception e)
            {
                // Can do nothing
            }
        }
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration#clone()
     */
    public Object clone()
    {
        AbstractCheckConfiguration clone = null;
        try
        {
            clone = (AbstractCheckConfiguration) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError();
        }
        return clone;
    }

    /**
     * Returns the context of the configuration.
     * 
     * @return
     */
    protected IProject getContext()
    {
        return mContext;
    }

    /**
     * Returns the location of the checkstyle configuration file as URL.
     * 
     * @return the location of the configuration file
     * @throws MalformedURLException error creating the URL
     */
    protected abstract URL handleGetLocation() throws MalformedURLException;

    /**
     * Checks if the configuration is editable.
     * 
     * @return <code>true</code> if editable
     */
    protected abstract boolean handleIsEditable();

    /**
     * Checks if the configuration is configurable.
     * 
     * @return <code>true</code> if configurable
     */
    protected abstract boolean handleIsConfigurable();

    /**
     * Checks if the location of the configuration can be resolved.
     * 
     * @return <code>true</code> if the configuration can be resolved, false
     *         otherwise.
     */
    protected boolean handleCanResolveLocation() throws IOException, MalformedURLException
    {

        URL location = handleGetLocation();
        boolean result = location != null;

        if (result)
        {
            InputStream stream = null;
            try
            {
                stream = location.openStream();
            }
            catch (IOException e)
            {
                result = false;
                throw e;
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch (Exception e)
                {
                    // Nothing we can do about it
                }
            }
        }
        return result;
    }

    /**
     * Get the property resolver for the actual configuration implementation.
     * 
     * @return the property resolver.
     */
    protected abstract PropertyResolver handleGetPropertyResolver();
}