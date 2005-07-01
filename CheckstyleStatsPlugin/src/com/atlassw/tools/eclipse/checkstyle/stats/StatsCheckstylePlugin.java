//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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
package com.atlassw.tools.eclipse.checkstyle.stats;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Fabrice BELLINGARD
 */
public class StatsCheckstylePlugin extends AbstractUIPlugin
{
    /**
     * The shared instance.
     */
    private static StatsCheckstylePlugin sPlugin;

    /**
     * Resource bundle.
     */
    private ResourceBundle mResourceBundle;

    /**
     * The constructor.
     */
    public StatsCheckstylePlugin()
    {
        super();
        sPlugin = this;
        try
        {
            mResourceBundle = ResourceBundle
                .getBundle("com.atlassw.tools.eclipse.checkstyle.stats.StatsCheckstylePluginResources"); //$NON-NLS-1$
        }
        catch (MissingResourceException x)
        {
            mResourceBundle = null;
        }
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * Cf. méthode surchargée.
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return l'instance
     */
    public static StatsCheckstylePlugin getDefault()
    {
        return sPlugin;
    }

    /**
     * 
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     * 
     * @param key
     *            la clé
     * @return le message correspondant
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = StatsCheckstylePlugin.getDefault()
            .getResourceBundle();
        try
        {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e)
        {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle.
     * 
     * @return le bundle du plugin
     */
    public ResourceBundle getResourceBundle()
    {
        return mResourceBundle;
    }

    /**
     * Permet de loguer plus facilement dans la log du plugin.
     * 
     * @param severity :
     *            la gravité
     * @param message :
     *            le message à loguer
     * @param throwable :
     *            l'exception à loguer
     */
    public static void log(int severity, String message, Throwable throwable)
    {
        IStatus status = new Status(severity, getDefault().getBundle()
            .getSymbolicName(), IStatus.OK, message, throwable);
        getDefault().getLog().log(status);
    }
}
