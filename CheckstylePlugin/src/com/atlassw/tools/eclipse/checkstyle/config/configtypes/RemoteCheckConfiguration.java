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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class RemoteCheckConfiguration extends AbstractCheckConfiguration
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
     * @see AbstractCheckConfiguration#handleGetLocation()
     */
    protected URL handleGetLocation() throws MalformedURLException
    {
        return new URL(getLocation());
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
        // a remote configuration can not be configured
        return false;
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

            MultiPropertyResolver multiResolver = new MultiPropertyResolver();
            multiResolver.addPropertyResolver(new StandardPropertyResolver(getLocation()));

            ResourceBundle bundle = getBundle();
            if (bundle != null)
            {
                multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
            }

            mPropertyResolver = multiResolver;
        }
        return mPropertyResolver;
    }

    /**
     * Helper method to get the resource bundle for this configuration.
     * 
     * @return the resource bundle or <code>null</code> if no bundle exists
     */
    private ResourceBundle getBundle()
    {

        ResourceBundle bundle = null;

        try
        {

            String location = getLocation();

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

            URL propUrl = new URL(propsLocation + ".properties"); //$NON-NLS-1$

            bundle = new PropertyResourceBundle(new BufferedInputStream(propUrl.openStream()));
        }
        catch (IOException ioe)
        {
            // we won't load the bundle then
        }

        return bundle;
    }
}