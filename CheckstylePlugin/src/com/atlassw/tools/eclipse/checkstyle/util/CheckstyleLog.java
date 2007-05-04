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

package com.atlassw.tools.eclipse.checkstyle.util;

//=================================================
// Imports from java namespace
//=================================================

//=================================================
// Imports from javax namespace
//=================================================

//=================================================
// Imports from com namespace
//=================================================
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;

/**
 * Logging utility for the Checkstyle plug-in.
 */
public final class CheckstyleLog
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    private static ILog sLog;

    // =================================================
    // Instance member variables.
    // =================================================

    // =================================================
    // Constructors & finalizer.
    // =================================================

    private CheckstyleLog()
    {}

    static
    {
        sLog = CheckstylePlugin.getDefault().getLog();
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Logs the exceptions.
     * 
     * @param t the exception to log
     */
    public static void log(Throwable t)
    {
        log(t, t.getLocalizedMessage());
    }

    /**
     * Logs the exception, describing it with the given message.
     * 
     * @param t the exception to log
     * @param message the message
     */
    public static void log(Throwable t, String message)
    {
        Status status = new Status(Status.ERROR, CheckstylePlugin.PLUGIN_ID, Status.OK, NLS.bind(
                ErrorMessages.CheckstyleLog_msgStatusPrefix, message), t);
        sLog.log(status);
    }

    /**
     * Open an error dialog for an exception that occurred within the plugin.
     * 
     * @param shell the shell
     * @param message the exception message
     * @param t the exception
     * @param log <code>true</code> if the exception should be logged
     */
    public static void errorDialog(Shell shell, String message, Throwable t, boolean log)
    {

        Status status = new Status(Status.ERROR, CheckstylePlugin.PLUGIN_ID, Status.OK,
                message != null ? message : "", t); //$NON-NLS-1$

        String msg = NLS.bind(ErrorMessages.errorDialogMainMessage, message);
        ErrorDialog.openError(shell, ErrorMessages.CheckstyleLog_titleInternalError, msg, status);

        if (log)
        {
            log(t);
        }
    }

    /**
     * Open an error dialog for an exception that occurred within the plugin.
     * 
     * @param shell the shell
     * @param t the exception
     * @param log <code>true</code> if the exception should be logged
     */
    public static void errorDialog(Shell shell, Throwable t, boolean log)
    {
        CheckstyleLog.errorDialog(shell, t.getLocalizedMessage(), t, log);
    }

    /**
     * Open an warning dialog for an exception that occurred within the plugin.
     * 
     * @param shell the shell
     * @param message the exception message
     * @param t the exception
     */
    public static void warningDialog(Shell shell, String message, Throwable t)
    {
        Status status = new Status(Status.WARNING, CheckstylePlugin.PLUGIN_ID, Status.OK, t
                .getLocalizedMessage(), t);

        ErrorDialog.openError(shell, ErrorMessages.CheckstyleLog_titleWarning, message, status);
    }
}