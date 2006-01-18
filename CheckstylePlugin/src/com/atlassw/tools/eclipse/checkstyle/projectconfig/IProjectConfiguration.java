
package com.atlassw.tools.eclipse.checkstyle.projectconfig;

import java.util.List;

import org.eclipse.core.resources.IProject;

import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;

/**
 * The public interface of a project configuration.
 * 
 * @author Lars Ködderitzsch
 */
public interface IProjectConfiguration
{

    /**
     * Returns the project which this configuration belongs to.
     * 
     * @return the project
     */
    IProject getProject();

    /**
     * Returns the list of check configurations locally configured for this
     * project.
     * 
     * @return the list of local check configurations
     */
    List getLocalCheckConfigurations();

    /**
     * Returns the file sets configured for the project.
     * 
     * @return the file sets
     */
    List getFileSets();

    /**
     * Gets the filters of this file set.
     * 
     * @return the filters
     */
    List getFilters();

    /**
     * Returns if the simple configuration should be used.
     * 
     * @return <code>true</code>, if this project uses the simple
     *         configuration, <code>false</code> otherwise
     */
    boolean isUseSimpleConfig();

    /**
     * Checks if this project configuration uses the given checkstyle
     * configuration.
     * 
     * @param configuration the check configuration
     * @return <code>true</code>, if the project config uses the checkstyle
     *         config, <code>false</code> otherwise
     */
    boolean isConfigInUse(ICheckConfiguration configuration);
}