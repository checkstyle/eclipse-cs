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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class ProjectConfigurationType extends ConfigurationType
{

    /** Key to access the information if the configuration is protected. */
    public static final String KEY_PROTECT_CONFIG = "protect-config-file"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException
    {
        IResource configFileResource = ResourcesPlugin.getWorkspace().getRoot().findMember(
                checkConfiguration.getLocation());

        if (configFileResource != null)
        {
            return configFileResource.getLocation().toFile().toURL();
        }
        else
        {
            throw new FileNotFoundException(NLS.bind(
                    Messages.ProjectConfigurationType_msgFileNotFound, checkConfiguration
                            .getLocation()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isConfigurable(ICheckConfiguration checkConfiguration)
    {
        boolean isConfigurable = true;

        boolean isProtected = Boolean.valueOf(
                (String) checkConfiguration.getAdditionalData().get(KEY_PROTECT_CONFIG))
                .booleanValue();
        isConfigurable = !isProtected;

        if (!isProtected)
        {

            // The configuration can be changed when the external configuration
            // file can is writable
            try
            {
                isConfigurable = new File(checkConfiguration.getResolvedConfigurationFileURL()
                        .getFile()).canWrite();
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.log(e);
                isConfigurable = false;
            }

        }
        return isConfigurable;
    }
}