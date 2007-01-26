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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationWriter;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Implementation of the configuration type for a internal check configuration,
 * that is located inside the plugin.
 * 
 * @author Lars Ködderitzsch
 */
public class InternalConfigurationType extends ConfigurationType
{

    /**
     * {@inheritDoc}
     */
    public URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        IPath configPath = CheckstylePlugin.getDefault().getStateLocation();
        configPath = configPath.append(checkConfiguration.getLocation());
        File configFile = configPath.toFile();

        if (!configFile.exists())
        {
            // create a new basic configuration file
            OutputStream out = null;

            try
            {
                out = new BufferedOutputStream(new FileOutputStream(configFile));
                ConfigurationWriter.writeNewConfiguration(out, checkConfiguration);
            }
            catch (IOException e)
            {
                // Should not happen
                CheckstyleLog.log(e);
            }
            catch (CheckstylePluginException e)
            {
                // Should not happen
                CheckstyleLog.log(e);
            }
            finally
            {
                IOUtils.closeQuietly(out);
            }
        }

        try
        {
            return configFile.toURL();
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
    public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        super.notifyCheckConfigRemoved(checkConfiguration);

        // remove the configuration file from the workspace metadata
        URL configFileURL = resolveLocation(checkConfiguration);

        if (configFileURL != null)
        {

            File configFile = new File(configFileURL.getFile());
            configFile.delete();
        }
    }
}