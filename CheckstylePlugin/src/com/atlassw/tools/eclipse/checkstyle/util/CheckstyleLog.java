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
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    private static ILog sLog;

    private static final String NEWLINE = System.getProperty("line.separator");

    private static final String ADDED_MSG = "See the Error Log view for additional details.";

    //=================================================
    // Instance member variables.
    //=================================================

    //=================================================
    // Constructors & finalizer.
    //=================================================

    private CheckstyleLog()
    {}

    static
    {
        sLog = CheckstylePlugin.getDefault().getLog();
    }

    //=================================================
    // Methods.
    //=================================================

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
                "Checkstyle-Plugin: {0}", message), t);
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

        Status status = new Status(Status.ERROR, CheckstylePlugin.PLUGIN_ID, Status.OK, t
                .getLocalizedMessage(), t);

        String msg = NLS.bind(ErrorMessages.errorDialogMainMessage, message);
        ErrorDialog.openError(shell, "Internal Checkstyle-Plugin error", msg, status);

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

        ErrorDialog.openError(shell, "Internal Checkstyle-Plugin error", message, status);
    }

    //    /**
    //     * Log an error message.
    //     *
    //     * @param message Log message.
    //     */
    //    public static void error(String message)
    //    {
    //        error(message, null);
    //    }
    //
    //    /**
    //     * Log an error message.
    //     *
    //     * @param message Log message.
    //     *
    //     * @param exception Ecxeption that caused the error.
    //     */
    //    public static void error(String message, Throwable exception)
    //    {
    //        Status status = new Status(Status.ERROR, CheckstylePlugin.PLUGIN_ID,
    // Status.OK,
    //                "Checkstyle: " + message, exception);
    //        sLog.log(status);
    //    }
    //
    //    /**
    //     * Log a warning message.
    //     *
    //     * @param message Log message.
    //     */
    //    public static void warning(String message)
    //    {
    //        warning(message, null);
    //    }
    //
    //    /**
    //     * Log a warning message.
    //     *
    //     * @param message Log message.
    //     *
    //     * @param exception Ecxeption that caused the error.
    //     */
    //    public static void warning(String message, Throwable exception)
    //    {
    //        Status status = new Status(Status.WARNING, CheckstylePlugin.PLUGIN_ID,
    // Status.OK,
    //                "Checkstyle: " + message, exception);
    //        sLog.log(status);
    //    }
    //
    //    /**
    //     * Log an information message.
    //     *
    //     * @param message Log message.
    //     */
    //    public static void info(String message)
    //    {
    //        Status status = new Status(Status.INFO, CheckstylePlugin.PLUGIN_ID,
    // Status.OK,
    //                "Checkstyle: " + message, null);
    //        sLog.log(status);
    //    }
    //
    //    /**
    //     * Displays a simple error dialog indicating there was a Checkstyle
    // internal
    //     * error.
    //     *
    //     * @param shell Shell the use for the dialog.
    //     */
    //    public static void internalErrorDialog(Shell shell)
    //    {
    //        errorDialog(shell, "A Checkstyle internal error occured.");
    //    }
    //
    //    /**
    //     * Displays a simple error dialog indicating there was a Checkstyle
    // internal
    //     * error.
    //     */
    //    public static void internalErrorDialog()
    //    {
    //        errorDialog("A Checkstyle internal error occured.");
    //    }
    //
    //    /**
    //     * Displays a simple error dialog indicating there was a Checkstyle
    // internal
    //     * error.
    //     *
    //     * @param msg Message to display.
    //     */
    //    public static void errorDialog(String msg)
    //    {
    //        errorDialog(getShell(), msg);
    //    }
    //
    //    /**
    //     * Displays a simple error dialog indicating there was a Checkstyle
    // internal
    //     * error.
    //     *
    //     * @param shell Shell to use for the dialog.
    //     *
    //     * @param msg Message to display.
    //     */
    //    public static void errorDialog(Shell shell, String msg)
    //    {
    //        if (shell != null)
    //        {
    //            String logMsg = msg + NEWLINE + ADDED_MSG;
    //            MessageDialog.openError(shell, "Checkstyle Error", logMsg);
    //        }
    //    }
    //
    //    /**
    //     * Displays a simple error dialog indicating there was a Checkstyle
    // internal
    //     * error.
    //     *
    //     * @param shell Shell to use for the dialog.
    //     *
    //     * @param msg Message to display.
    //     * @param throwable the throwable
    //     */
    //    public static void errorDialog(Shell shell, String msg, Throwable
    // throwable)
    //    {
    //        if (shell != null)
    //        {
    //            String logMsg = msg + NEWLINE + ADDED_MSG;
    //            MessageDialog.openError(shell, "Checkstyle Error", logMsg);
    //        }
    //    }
    //
    //    // ad, 7.Jan.2004, Bug #872279
    //    /**
    //     * ad, 7.Jan.2004, Bug #872279 Displays a simple yes/no dialog.
    //     * <p>
    //     *
    //     * @param shell the Shell object
    //     * @param msg Message to display.
    //     * @return boolean true if the user pressed OK. false if the user
    // cancelled
    //     * the dialog.
    //     */
    //    public static boolean questionDialog(Shell shell, String msg)
    //    {
    //        if (shell != null)
    //        {
    //            String logMsg = msg;
    //            return MessageDialog.openQuestion(shell, "Checkstyle Question", logMsg);
    //        }
    //        else
    //        {
    //            return false;
    //        }
    //    }
    //
    //    // ad, 7.Jan.2004, Bug #872279
    //    // end change
    //
    //    private static Shell getShell()
    //    {
    //        Shell shell = null;
    //
    //        IWorkbenchWindow window =
    // PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    //        if (window != null)
    //        {
    //            shell = window.getShell();
    //        }
    //
    //        return shell;
    //    }
}