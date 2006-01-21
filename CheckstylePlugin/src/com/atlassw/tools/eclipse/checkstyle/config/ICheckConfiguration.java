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

package com.atlassw.tools.eclipse.checkstyle.config;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Interface for a check configuration object.
 * 
 * @author Lars Ködderitzsch
 */
public interface ICheckConfiguration
{

    /**
     * Returns the displayable name of the configuration.
     * 
     * @return the displayable name of the configuration
     */
    String getName();

    /**
     * Return a description of the check configuration.
     * 
     * @return a description.
     */
    String getDescription();

    /**
     * Returns the location of the checkstyle configuration file.
     * 
     * @return the location of the configuration file
     */
    String getLocation();

    /**
     * Return the type of the configuration.
     * 
     * @return the configuration type
     */
    IConfigurationType getType();

    /**
     * Gets additional data for this configuration.
     * 
     * @return the additional data in form of a Map
     */
    Map getAdditionalData();

    /**
     * Returns the list of properties added to the configuration.
     * 
     * @return the list of configured properties
     */
    List getResolvableProperties();

    /**
     * Gets the property resolver for this configuration used to expand property
     * values within the checkstyle configuration.
     * 
     * @return the property resolver
     * @throws CheckstylePluginException error creating the property resolver
     */
    PropertyResolver getPropertyResolver() throws CheckstylePluginException;

    /**
     * Checks if the checkstyle configuration file is available. If the
     * configuration file is available the method call exits normally. Otherwise
     * a CheckstylePluginException will be thrown, specifing what went wrong.
     * 
     * @return the URL of the configuration file
     * @throws CheckstylePluginException if the Checkstyle configuration file is
     *             not available
     */
    URL isConfigurationAvailable() throws CheckstylePluginException;

    /**
     * Opens an input stream to the configuration file.
     * 
     * @return input stream to the configuration file
     * @throws CheckstylePluginException error opening the stream
     */
    InputStream openConfigurationFileStream() throws CheckstylePluginException;

    /**
     * Determines if the configuration properties are editable by the user.
     * 
     * @return <code>true</code>, if the configuration is editable
     */
    boolean isEditable();

    /**
     * Determines if the checkstyle configuration associates with this check
     * configuration can be configured.
     * 
     * @return <code>true</code> if the checkstyle configuration can be
     *         configured.
     */
    boolean isConfigurable();

    /**
     * Returns if the check configuration is a global configuration, configured
     * for the workspace, or a local configuration for a single project.
     * 
     * @return <code>true</code> if the check configuration is configured
     *         globally
     */
    boolean isGlobal();
}