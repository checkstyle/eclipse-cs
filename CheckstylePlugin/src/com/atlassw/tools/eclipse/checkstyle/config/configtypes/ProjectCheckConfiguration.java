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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectCheckConfiguration extends AbstractCheckConfiguration
{

    //
    // attributes
    //

    /** the property resolver for this check configuration. */
    private PropertyResolver mPropertyResolver;

    //
    // methods
    //

    /**
     * @see AbstractCheckConfiguration#setLocation(java.lang.String)
     */
    public void setLocation(String location) throws CheckstylePluginException
    {
        if (location == null || location.trim().length() == 0)
        {
            throw new CheckstylePluginException(ErrorMessages.errorLocationEmpty);
        }

        String oldLocation = getLocation();
        mLocation = location;

        if (getContext() != null)
        {
            try
            {
                handleCanResolveLocation();
            }
            catch (Exception e)
            {
                mLocation = oldLocation;
                CheckstylePluginException.rethrow(e, NLS
                        .bind(ErrorMessages.errorResolveConfigLocation, location, e
                                .getLocalizedMessage()));
            }
        }
    }

    /**
     * @see AbstractCheckConfiguration#handleGetLocation()
     */
    protected URL handleGetLocation() throws MalformedURLException
    {
        String location = getLocation();
        File configFile = null;

        IProject project = getContext();
        if (project != null)
        {
            IResource member = project.findMember(new Path(location));

            configFile = member != null ? member.getLocation().toFile() : new File(project
                    .getLocation().toFile(), location);
        }
        else
        {
            configFile = new File(location);
        }

        return configFile.toURL();
    }

    /**
     * @see AbstractCheckConfiguration#handleIsEditable()
     */
    protected boolean handleIsEditable()
    {
        // External check configurations can be edited
        return true;
    }

    /**
     * @see AbstractCheckConfiguration#handleIsConfigurable()
     */
    protected boolean handleIsConfigurable()
    {
        // The configuration can be changed when the external configuration file
        // can is writable
        try
        {
            return new File(handleGetLocation().getFile()).canWrite();
        }
        catch (MalformedURLException e)
        {
            return false;
        }
    }

    /**
     * @see AbstractCheckConfiguration#isContextNeeded()
     */
    public boolean isContextNeeded()
    {
        // a project relative configuration needs project context
        return true;
    }

    /**
     * External file based configurations support property expansion. The
     * precondition is that the .properties file containing the property values
     * lies in the same location as the configuration file, with the same file
     * name exept the file extension (.properties).
     * 
     * @see AbstractCheckConfiguration#handleGetPropertyResolver()
     */
    protected PropertyResolver handleGetPropertyResolver()
    {

        if (mPropertyResolver == null)
        {

            ResourceBundle bundle = null;

            try
            {
                String location = handleGetLocation().getFile();

                // Strip file extension
                String propsLocation = null;
                if (location.lastIndexOf(".") > -1)
                {
                    propsLocation = location.substring(0, location.lastIndexOf(".")); //$NON-NLS-1$
                }
                else
                {
                    propsLocation = location;
                }

                File f = new File(propsLocation + ".properties");

                URL propertyFile = f.toURL(); //$NON-NLS-1$

                bundle = new PropertyResourceBundle(new BufferedInputStream(propertyFile
                        .openStream()));
            }
            catch (Exception ioe)
            {
                // we won't load the bundle then
            }

            mPropertyResolver = new ResourceBundlePropertyResolver(bundle);
        }
        return mPropertyResolver;
    }
}