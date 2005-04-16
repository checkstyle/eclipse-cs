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
import java.rmi.server.UID;
import java.util.List;

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

    /** String containing the unique id of the configuration. */
    private String mId;

    /** the displayable name of the configuration. */
    private String mName;

    /** the location of the configuration file. */
    private String mLocation;

    /** the description of the configuration. */
    private String mDescription;

    /** the configuration type. */
    private IConfigurationType mConfigType;

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
        setName(name);
        setLocation(location);
        mConfigType = type;
        setDescription(description);
        mId = new UID().toString();
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
            throw new CheckstylePluginException("Name must not be empty.");
        }

        String oldName = getName();
        mName = name;

        //Check if the new name is in use
        if (CheckConfigurationFactory.isNameCollision(this))
        {
            mName = oldName;
            throw new CheckstylePluginException("Check configuration name '" + name
                    + "' is already in use.");
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
            throw new CheckstylePluginException("Location must not be empty.");
        }

        String oldLocation = getLocation();
        mLocation = location;

        try
        {
            handleCanResolveLocation();
        }
        catch (Exception e)
        {
            mLocation = oldLocation;
            throw new CheckstylePluginException("Location '" + location + "' can not be resolved ("
                    + e.getLocalizedMessage() + ").");
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
     * @see ICheckConfiguration#getId()
     */
    public String getId()
    {
        return mId;
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
        try
        {
            return handleGetLocation();
        }
        catch (MalformedURLException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see ICheckConfiguration#getModules()
     */
    public List getModules() throws CheckstylePluginException
    {
        InputStream in = null;

        try
        {
            in = getCheckstyleConfigurationURL().openStream();
            return ConfigurationReader.read(in);
        }
        catch (IOException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (Exception e)
            {
                //Can do nothing
            }
        }
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
            System.out.println("Writing to: " + getCheckstyleConfigurationURL());

            //First write to a byte array outputstream
            //because otherwise in an error case the original
            //file would be destroyed
            byteOut = new ByteArrayOutputStream();

            ConfigurationWriter.write(byteOut, modules);

            //all went ok, write to the file
            out = new BufferedOutputStream(new FileOutputStream(getCheckstyleConfigurationURL()
                    .getFile()));

            out.write(byteOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new CheckstylePluginException(e.getLocalizedMessage(), e);
        }
        finally
        {
            try
            {
                byteOut.close();
            }
            catch (Exception e)
            {
                //Can do nothing
            }
            try
            {
                out.close();
            }
            catch (Exception e)
            {
                //Can do nothing
            }
        }
    }

    /**
     * @see com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        ICheckConfiguration clone = (ICheckConfiguration) super.clone();
        return clone;
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
                    //Nothing we can do about it
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