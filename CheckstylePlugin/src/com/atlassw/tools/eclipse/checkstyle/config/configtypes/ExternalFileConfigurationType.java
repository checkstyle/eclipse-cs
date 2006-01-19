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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class ExternalFileConfigurationType extends ConfigurationType
{

    /** Property resolver used to add dynamic location support. */
    private static final PropertyResolver DYNAMIC_LOC_RESOLVER;

    static
    {
        MultiPropertyResolver resolver = new MultiPropertyResolver();
        resolver.addPropertyResolver(new ClasspathVariableResolver());
        resolver.addPropertyResolver(new SystemPropertyResolver());
        DYNAMIC_LOC_RESOLVER = resolver;
    }

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        try
        {
            String location = checkConfiguration.getLocation();

            // support dynamic locations for external configurations
            while (PropertyUtil.hasUnresolvedProperties(location))
            {
                location = PropertyUtil.replaceProperties(location, DYNAMIC_LOC_RESOLVER);
            }

            return new File(location).toURL();
        }
        catch (MalformedURLException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        catch (CheckstyleException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurable(ICheckConfiguration checkConfiguration)
    {

        String location = checkConfiguration.getLocation();

        try
        {
            // support dynamic locations for external configurations

            while (PropertyUtil.hasUnresolvedProperties(location))
            {
                location = PropertyUtil.replaceProperties(location, DYNAMIC_LOC_RESOLVER);
            }
        }
        catch (CheckstyleException e)
        {
            CheckstyleLog.log(e);
            return false;
        }
        
        // The configuration can be changed when the external configuration file
        // can is writable
        return new File(location).canWrite();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        String location = checkConfiguration.getLocation();

        try
        {
            // support dynamic locations for external configurations
            while (PropertyUtil.hasUnresolvedProperties(location))
            {
                location = PropertyUtil.replaceProperties(location, DYNAMIC_LOC_RESOLVER);
            }
        }
        catch (CheckstyleException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new StandardPropertyResolver(location));
        multiResolver.addPropertyResolver(new ClasspathVariableResolver());
        multiResolver.addPropertyResolver(new SystemPropertyResolver());

        ResourceBundle bundle = getBundle(location);
        if (bundle != null)
        {
            multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
        }

        return multiResolver;
    }

    /**
     * Helper method to get the resource bundle for this configuration.
     * 
     * @param location the location of the configuration file
     * @return the resource bundle or <code>null</code> if no bundle exists
     */
    private static ResourceBundle getBundle(String location)
    {

        ResourceBundle bundle = null;

        try
        {

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

            File propertyFile = new File(propsLocation + ".properties"); //$NON-NLS-1$

            bundle = new PropertyResourceBundle(new BufferedInputStream(new FileInputStream(
                    propertyFile)));
        }
        catch (IOException ioe)
        {
            // we won't load the bundle then
        }

        return bundle;
    }

}