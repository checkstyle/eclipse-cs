//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui;

import java.util.Locale;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstyleUIPlugin extends AbstractUIPlugin {

  /** Identifier of the plug-in. */
  public static final String PLUGIN_ID = "net.sf.eclipsecs.ui"; //$NON-NLS-1$

  /** The shared instance. */
  private static CheckstyleUIPlugin sPlugin;

  private static Boolean isEclipse3;

  /**
   * The constructor.
   */
  public CheckstyleUIPlugin() {
    sPlugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    // free cached images
    CheckstyleUIPluginImages.clearCachedImages();
    super.stop(context);
  }

  /**
   * @return <code>true</code> if we're running on an Eclipse 3 platform.
   */
  public static boolean isE3() {

    if (isEclipse3 == null) {

      // previous checking on the platform product version has not been
      // reliable, since there are e4
      // based
      // products with a 3 as major version (e.g. Spring Tools Suite).
      try {

        // instead now check for the presence of a known e4 class
        Class.forName("org.eclipse.e4.ui.model.application.MApplicationElement");
        isEclipse3 = false;
      } catch (ClassNotFoundException ex) {
        isEclipse3 = true;
      }
    }

    return isEclipse3;
  }

  /**
   * Returns the shared instance.
   *
   * @return The shared plug-in instance.
   */
  public static CheckstyleUIPlugin getDefault() {
    return sPlugin;
  }

  /**
   * Returns the workspace instance.
   *
   * @return Workspace instance.
   */
  public static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /**
   * Helper method to get the current plattform locale.
   *
   * @return the platform locale
   */
  public static Locale getPlatformLocale() {

    String locale = Platform.getNL();
    String[] parts = locale.split("_"); //$NON-NLS-1$

    String language = parts.length > 0 ? parts[0] : ""; //$NON-NLS-1$
    String country = parts.length > 1 ? parts[1] : ""; //$NON-NLS-1$
    String variant = parts.length > 2 ? parts[2] : ""; //$NON-NLS-1$

    return new Locale(language, country, variant);
  }

  /**
   * Open an error dialog for an exception that occurred within the plugin.
   *
   * @param shell
   *          the shell
   * @param message
   *          the exception message
   * @param throwable
   *          the exception
   * @param log
   *          <code>true</code> if the exception should be logged
   */
  public static void errorDialog(Shell shell, String message, Throwable throwable, boolean log) {

    Status status = new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK,
            message != null ? message : "", throwable); //$NON-NLS-1$

    String msg = NLS.bind(Messages.errorDialogMainMessage, message);
    ErrorDialog.openError(shell, Messages.CheckstyleLog_titleInternalError, msg, status);

    if (log) {
      CheckstyleLog.log(throwable);
    }
  }

  /**
   * Open an error dialog for an exception that occurred within the plugin.
   *
   * @param shell
   *          the shell
   * @param throwable
   *          the exception
   * @param log
   *          <code>true</code> if the exception should be logged
   */
  public static void errorDialog(Shell shell, Throwable throwable, boolean log) {
    errorDialog(shell, throwable.getLocalizedMessage(), throwable, log);
  }

  /**
   * Open an warning dialog for an exception that occurred within the plugin.
   *
   * @param shell
   *          the shell
   * @param message
   *          the exception message
   * @param throwable
   *          the exception
   */
  public static void warningDialog(Shell shell, String message, Throwable throwable) {
    Status status = new Status(IStatus.WARNING, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK,
            throwable.getLocalizedMessage(), throwable);

    ErrorDialog.openError(shell, Messages.CheckstyleLog_titleWarning, message, status);
  }

}
