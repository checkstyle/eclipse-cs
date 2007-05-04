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
import java.net.URL;

import org.eclipse.core.runtime.Path;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.CheckstyleConfigurationFile;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of the configuration type for a built in check configuration,
 * that is located inside the plugin.
 * 
 * @author Lars Ködderitzsch
 */
public class BuiltInConfigurationType extends ConfigurationType
{

    /**
     * {@inheritDoc}
     */
    protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException
    {
        return CheckstylePlugin.getDefault().find(new Path(checkConfiguration.getLocation()));
    }

    /**
     * {@inheritDoc}
     */
    protected byte[] getAdditionPropertiesBundleBytes(URL checkConfigURL) throws IOException
    {
        // just returns null since additional property file is not needed nor
        // supported
        return null;
    }

    protected PropertyResolver getPropertyResolver(ICheckConfiguration config,
            CheckstyleConfigurationFile configFile) throws IOException
    {
        // no properties to resolve with builtin configurations
        return null;
    }
}