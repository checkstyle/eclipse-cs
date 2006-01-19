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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfigurationType extends ConfigurationType
{

    /**
     * {@inheritDoc}
     */
    public URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        IResource configFileResource = ResourcesPlugin.getWorkspace().getRoot().findMember(
                checkConfiguration.getLocation());

        try
        {
            if (configFileResource != null)
            {
                return configFileResource.getLocation().toFile().toURL();
            }
            else
            {
                throw new CheckstylePluginException(NLS.bind(Messages.ProjectConfigurationType_msgFileNotFound,
                        checkConfiguration.getLocation()));
            }
        }
        catch (MalformedURLException e)
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
        // The configuration can be changed when the external configuration file
        // can is writable
        try
        {
            return new File(resolveLocation(checkConfiguration).getFile()).canWrite();
        }
        catch (CheckstylePluginException e)
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        String resolvedLocation = resolveLocation(checkConfiguration).getFile();

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new StandardPropertyResolver(resolvedLocation));

        ResourceBundle bundle = getBundle(resolvedLocation);
        if (bundle != null)
        {
            multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
        }

        return multiResolver;
    }

    /**
     * Helper method to get the resource bundle for this configuration.
     * 
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

            File f = new File(propsLocation + ".properties"); //$NON-NLS-1$

            URL propertyFile = f.toURL();

            bundle = new PropertyResourceBundle(new BufferedInputStream(propertyFile.openStream()));
        }
        catch (Exception ioe)
        {
            // we won't load the bundle then
        }

        return bundle;
    }
}