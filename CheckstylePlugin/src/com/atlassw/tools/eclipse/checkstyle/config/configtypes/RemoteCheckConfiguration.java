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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

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
            String location = getLocation();
            // Strip file extension
            location = location.substring(0, location.lastIndexOf(".")); //$NON-NLS-1$
            location = location + ".properties"; //$NON-NLS-1$

            ResourceBundle bundle = null;

            try
            {
                URL propUrl = new URL(location);
                bundle = new PropertyResourceBundle(new BufferedInputStream(propUrl.openStream()));
            }
            catch (IOException ioe)
            {
                // we won't load the bundle then
            }

            mPropertyResolver = new ResourceBundleProperyResolver(bundle);
        }
        return mPropertyResolver;
    }

    /**
     * Property resolver that resolves properties from a resource bundle.
     * 
     * @author Lars Ködderitzsch
     */
    private class ResourceBundleProperyResolver implements PropertyResolver
    {

        /** the resource bundle. */
        private ResourceBundle mBundle;

        /**
         * Creates the property resolver.
         * 
         * @param bundle the resource bundle
         */
        public ResourceBundleProperyResolver(ResourceBundle bundle)
        {
            mBundle = bundle;
        }

        /**
         * @see com.puppycrawl.tools.checkstyle.PropertyResolver#resolve(java.lang.String)
         */
        public String resolve(String property) throws CheckstyleException
        {

            String value = null;

            if (mBundle != null)
            {
                value = mBundle.getString(property);
            }
            return value;
        }
    }
}