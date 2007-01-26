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

package com.atlassw.tools.eclipse.checkstyle.config.savefilter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Register for the filters thats use the
 * <i>com.atlassw.tools.eclipse.checkstyle.checkstyleFilter </i> extension
 * point. Checkstyle filters can be enabled per project.
 * 
 * @author Lars Ködderitzsch
 */
public final class SaveFilters
{

    //
    // constants
    //

    /** constant for the extension point id. */
    private static final String FILTER_EXTENSION_POINT = CheckstylePlugin.PLUGIN_ID
            + ".saveFilters"; //$NON-NLS-1$

    /** constant for the class attribute. */
    private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    /** the filter prototypes configured to the extension point. */
    private static final ISaveFilter[] SAVE_FILTERS;

    //
    // Initializer
    //

    /**
     * Initialize the configured to the filter extension point.
     */
    static
    {

        IExtensionRegistry pluginRegistry = Platform.getExtensionRegistry();

        IConfigurationElement[] elements = pluginRegistry
                .getConfigurationElementsFor(FILTER_EXTENSION_POINT);

        List filters = new ArrayList();

        for (int i = 0; i < elements.length; i++)
        {

            try
            {

                Class filterClass = Class.forName(elements[i].getAttributeAsIs(ATTR_CLASS));

                ISaveFilter filter = (ISaveFilter) filterClass.newInstance();
                filters.add(filter);
            }
            catch (Exception e)
            {
                CheckstyleLog.log(e);
            }
        }

        SAVE_FILTERS = (ISaveFilter[]) filters.toArray(new ISaveFilter[filters.size()]);
    }

    //
    // constructor
    //

    /** Hidden default constructor. */
    private SaveFilters()
    {
    // NOOP
    }

    //
    // methods
    //

    /**
     * Passes the configured modules through the known save filters.
     * 
     * @param configuredModules the configured modules of a configuration to be
     *            written
     */
    public static void process(List configuredModules)
    {
        for (int i = 0; i < SAVE_FILTERS.length; i++)
        {
            SAVE_FILTERS[i].postProcessConfiguredModules(configuredModules);
        }
    }
}