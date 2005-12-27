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

package net.sf.eclipsecs.stats;

import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Fabrice BELLINGARD
 */
public class StatsCheckstylePlugin extends AbstractUIPlugin
{

    //
    // constants
    //

    /** The plugin id. */
    public static final String PLUGIN_ID = "net.sf.eclipsecs.stats"; //$NON-NLS-1$

    /**
     * The shared instance.
     */
    private static StatsCheckstylePlugin sPlugin;

    //
    // constructors
    //

    /**
     * The constructor.
     */
    public StatsCheckstylePlugin()
    {
        super();
        sPlugin = this;
    }

    //
    // methods
    //

    /**
     * Returns the shared instance.
     * 
     * @return lthe shared instance
     */
    public static StatsCheckstylePlugin getDefault()
    {
        return sPlugin;
    }

    /**
     * Helper method to get the current plattform locale.
     * 
     * @return the platform locale
     */
    public static Locale getPlatformLocale()
    {

        String nl = Platform.getNL();
        String[] parts = nl.split("_"); //$NON-NLS-1$

        String language = parts.length > 0 ? parts[0] : ""; //$NON-NLS-1$
        String country = parts.length > 1 ? parts[1] : ""; //$NON-NLS-1$
        String variant = parts.length > 2 ? parts[2] : ""; //$NON-NLS-1$

        return new Locale(language, country, variant);
    }

    /**
     * Permet de loguer plus facilement dans la log du plugin.
     * 
     * @param severity : la gravité
     * @param message : le message à loguer
     * @param throwable : l'exception à loguer
     */
    public static void log(int severity, String message, Throwable throwable)
    {
        IStatus status = new Status(severity, getDefault().getBundle().getSymbolicName(),
                IStatus.OK, message, throwable);
        getDefault().getLog().log(status);
    }
}
