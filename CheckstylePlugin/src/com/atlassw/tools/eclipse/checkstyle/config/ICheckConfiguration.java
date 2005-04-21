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

package com.atlassw.tools.eclipse.checkstyle.config;

import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Interface for a check configuration object.
 * 
 * @author Lars Ködderitzsch
 */
public interface ICheckConfiguration extends Cloneable
{

    /**
     * Initializes the check configuration.
     * 
     * @param name the name of the configuration
     * @param location the location of the checkstyle configuration file
     * @param type the configuration type
     * @param description the description of the config or <code>null</code>
     * @throws CheckstylePluginException if a parameter is not correct
     */
    void initialize(String name, String location, IConfigurationType type, String description)
        throws CheckstylePluginException;

    /**
     * Sets the actual project context.
     * 
     * @param context the context of the current project.
     */
    void setContext(IProject context);

    /**
     * Returns the displayable name of the configuration.
     * 
     * @return the displayable name of the configuration
     */
    String getName();

    /**
     * Sets the name of the configuration.
     * 
     * @param name the name
     * @throws CheckstylePluginException if the name is already in use
     */
    void setName(String name) throws CheckstylePluginException;

    /**
     * Return a description of the check configuration.
     * 
     * @return a description.
     */
    String getDescription();

    /**
     * Sets the description of the configuration.
     * 
     * @param description the description
     */
    void setDescription(String description);

    /**
     * Returns the location of the checkstyle configuration file.
     * 
     * @return the location of the configuration file
     */
    String getLocation();

    /**
     * Sets the location of the checkstyle configuration.
     * 
     * @param location the location
     * @throws CheckstylePluginException if the location is not valid or cannot
     *             be resolveds
     */
    void setLocation(String location) throws CheckstylePluginException;

    /**
     * Return the type of the configuration.
     * 
     * @return the configuration type
     */
    IConfigurationType getType();

    /**
     * Returns an id of the check configuration.
     * 
     * @return the id
     */
    String getId();

    /**
     * Gets the property resolver for this configuration used to expand property
     * values within the checkstyle configuration.
     * 
     * @return the property resolver
     */
    PropertyResolver getPropertyResolver();

    /**
     * Returns the URL of the checkstyle configuration file.
     * 
     * @return the URL of the checkstyle configuration file
     * @throws CheckstylePluginException error while creating the url
     */
    URL getCheckstyleConfigurationURL() throws CheckstylePluginException;

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
     * Determines if the specific check configuration needs a project context to
     * resolve.
     * 
     * @return <code>true</code> if project context is needed
     */
    boolean isContextNeeded();

    /**
     * Returns the modules configured within the checkstyle configuration.
     * 
     * @return all modules of the checkstyle configuration
     * @throws CheckstylePluginException error loading the configuration
     */
    List getModules() throws CheckstylePluginException;

    /**
     * Sets the modules for the checkstyle configuration. The modules are
     * immediatly written to the checkstyle configuration file.
     * 
     * @param modules the modules of the configuration.
     * @throws CheckstylePluginException error writing the configuration
     */
    void setModules(List modules) throws CheckstylePluginException;

    /**
     * Clone this Check Configuration.
     * 
     * @return ICheckConfiguration the clone
     */
    Object clone();
}