//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * This class acts as wrapper around check configurations to add editing
 * aspects. Check configurations by themself are not editable.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationWorkingCopy implements ICheckConfiguration
{

    //
    // attributes
    //

    /** The source check configuration of the working copy. */
    private ICheckConfiguration mCheckConfiguration;

    /** The working set this working copy belongs to. */
    private ICheckConfigurationWorkingSet mWorkingSet;

    /** The edited name of the configuration. */
    private String mEditedName;

    /** The edited location of the configuration. */
    private String mEditedLocation;

    /** The edited description of the configuration. */
    private String mEditedDescription;

    /** The map of additional data for this configuration. */
    private Map mAdditionalData = new HashMap();

    /** flags if the configuration is dirty. */
    private boolean mIsDirty;

    //
    // constructors
    //

    /*
     * NOTE: the constructors of this class are package-private because they
     * only should be called from the
     * ICheckConfigurationWorkingSet#newWorkingCopy methods of the enclosing
     * working set.
     */

    /**
     * Creates a new working copy from an existing check configuration.
     * 
     * @param checkConfigToEdit the existing check configuration
     * @param workingSet the working set this working copy belongs to
     */
    public CheckConfigurationWorkingCopy(ICheckConfiguration checkConfigToEdit,
            ICheckConfigurationWorkingSet workingSet)
    {
        mCheckConfiguration = checkConfigToEdit;
        mWorkingSet = workingSet;

        mAdditionalData.putAll(checkConfigToEdit.getAdditionalData());
    }

    /**
     * Creates a working copy for a new check configuration.
     * 
     * @param configType the type of the new configuration
     * @param workingSet the working set this working copy belongs to
     * @param global <code>true</code> if the new configuration is a global
     *            configuration
     */
    public CheckConfigurationWorkingCopy(IConfigurationType configType,
            ICheckConfigurationWorkingSet workingSet, boolean global)
    {

        mWorkingSet = workingSet;
        mCheckConfiguration = new CheckConfiguration(null, null, null, configType, global, null);
    }

    //
    // methods
    //

    /**
     * Returns the source check configuration of this working copy.
     * 
     * @return the source check configuration
     */
    public ICheckConfiguration getSourceCheckConfiguration()
    {
        return mCheckConfiguration;
    }

    /**
     * Changes the name of the check configuration.
     * 
     * @param name the new name
     * @throws CheckstylePluginException if name is <code>null</code> or empty
     *             or a name collision with an existing check configuration
     *             exists
     */
    public void setName(String name) throws CheckstylePluginException
    {

        if (name == null || name.trim().length() == 0)
        {
            throw new CheckstylePluginException(ErrorMessages.errorConfigNameEmpty);
        }

        String oldName = getName();
        if (!name.equals(oldName))
        {

            mEditedName = name;

            // Check if the new name is in use
            if (mWorkingSet.isNameCollision(this))
            {
                mEditedName = oldName;
                throw new CheckstylePluginException(NLS.bind(ErrorMessages.errorConfigNameInUse,
                        name));
            }
        }
    }

    /**
     * Changes the location of the Checkstyle configuration file.
     * 
     * @param location the new location of Checkstyle configuration file
     * @throws CheckstylePluginException if location is <code>null</code> or
     *             empty or the Checkstyle configuration file cannot be resolved
     */
    public void setLocation(String location) throws CheckstylePluginException
    {
        if (location == null || location.trim().length() == 0)
        {
            throw new CheckstylePluginException(ErrorMessages.errorLocationEmpty);
        }

        String oldLocation = getLocation();
        if (!location.equals(oldLocation))
        {

            try
            {
                mEditedLocation = location;
                isConfigurationAvailable();

                mIsDirty = true;
            }
            catch (Exception e)
            {
                mEditedLocation = oldLocation;
                throw new CheckstylePluginException(NLS
                        .bind(ErrorMessages.errorResolveConfigLocation, location, e
                                .getLocalizedMessage()));
            }
        }
    }

    /**
     * Sets a new description for the check configuration.
     * 
     * @param description the new description
     */
    public void setDescription(String description)
    {
        String oldDescription = getDescription();
        if (description == null || !description.equals(oldDescription))
        {
            mEditedDescription = description;
        }
    }

    /**
     * Flags if the working copy changed compared to the original check
     * configuration.
     * 
     * @return <code>true</code> if the working copy has changes over the
     *         original check configuration
     */
    public boolean isDirty()
    {
        return mIsDirty || !mAdditionalData.equals(mCheckConfiguration.getAdditionalData());
    }

    /**
     * Reads the Checkstyle configuration file and builds the list of configured
     * modules. Elements are of type
     * <code>com.atlassw.tools.eclipse.checkstyle.config.Module</code>.
     * 
     * @return the list of configured modules in this Checkstyle configuration
     * @throws CheckstylePluginException error when reading the Checkstyle
     *             configuration file
     */
    public List getModules() throws CheckstylePluginException
    {
        List result = null;

        InputStream in = null;

        try
        {
            in = openConfigurationFileStream();
            result = ConfigurationReader.read(in);
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
     * Stores the (edited) list of modules to the Checkstyle configuration file.
     * 
     * @param modules the list of modules to store into the Checkstyle
     *            configuration file
     * @throws CheckstylePluginException error storing the Checkstyle
     *             configuration
     */
    public void setModules(List modules) throws CheckstylePluginException
    {

        OutputStream out = null;
        ByteArrayOutputStream byteOut = null;
        try
        {
            System.out.println("Writing to: " + isConfigurationAvailable()); //$NON-NLS-1$

            // First write to a byte array outputstream
            // because otherwise in an error case the original
            // file would be destroyed
            byteOut = new ByteArrayOutputStream();

            ConfigurationWriter.write(byteOut, modules, this);

            // all went ok, write to the file
            String configFile = isConfigurationAvailable().getFile();
            out = new BufferedOutputStream(new FileOutputStream(configFile));
            out.write(byteOut.toByteArray());

            // refresh the files if within the workspace
            // Bug 1251194 - Resource out of sync after performing changes to
            // config
            IPath path = new Path(configFile);
            IFile[] files = CheckstylePlugin.getWorkspace().getRoot().findFilesForLocation(path);
            for (int i = 0; i < files.length; i++)
            {
                try
                {
                    files[i].refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
                }
                catch (CoreException e)
                {
                    // NOOP - just ignore
                }
            }

            mIsDirty = true;
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

    //
    // Implementation of ICheckConfiguration
    //

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return mEditedName != null ? mEditedName : getSourceCheckConfiguration().getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return mEditedDescription != null ? mEditedDescription : getSourceCheckConfiguration()
                .getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocation()
    {
        return mEditedLocation != null ? mEditedLocation : getSourceCheckConfiguration()
                .getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public IConfigurationType getType()
    {
        return getSourceCheckConfiguration().getType();
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
    public URL isConfigurationAvailable() throws CheckstylePluginException
    {
        URL configLocation = null;

        InputStream in = null;
        try
        {
            in = openConfigurationFileStream();
            configLocation = getType().resolveLocation(this);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // we tried to be nice
                    CheckstyleLog.log(e);
                }
            }
        }
        return configLocation;
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver() throws CheckstylePluginException
    {
        return getType().getPropertyResolver(this);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream openConfigurationFileStream() throws CheckstylePluginException
    {
        return getType().openConfigurationFileStream(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditable()
    {
        return getType().isEditable();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurable()
    {
        return getType().isConfigurable(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isGlobal()
    {
        return mCheckConfiguration.isGlobal();
    }
}
