//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.ExtensionClassLoader;
import net.sf.eclipsecs.ui.properties.filter.CheckFileOnOpenPartListener;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstyleUIPlugin extends AbstractUIPlugin {

  /** Identifier of the plug-in. */
  public static final String PLUGIN_ID = "net.sf.eclipsecs.ui"; //$NON-NLS-1$

  /** Extension point id for Checkstyle quickfix providers. */
  public static final String QUICKFIX_PROVIDER_EXT_PT_ID = PLUGIN_ID
          + ".checkstyleQuickfixProvider"; //$NON-NLS-1$

  /** The shared instance. */
  private static CheckstyleUIPlugin sPlugin;

  private static Boolean sIsE3;

  private ClassLoader mQuickfixExtensionClassLoader;

  /**
   * The constructor.
   */
  public CheckstyleUIPlugin() {
    super();
    sPlugin = this;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    mQuickfixExtensionClassLoader = new ExtensionClassLoader(context.getBundle(),
            QUICKFIX_PROVIDER_EXT_PT_ID);

    // add listeners for the Check-On-Open support
    final IWorkbench workbench = getWorkbench();
    workbench.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {

        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

        for (IWorkbenchWindow window : windows) {

          if (window != null) {

            // collect open editors and have then run against Checkstyle if
            // appropriate
            Collection<IWorkbenchPartReference> parts = new HashSet<>();

            // add already opened files to the filter
            // bugfix for 2923044
            IWorkbenchPage[] pages = window.getPages();
            for (IWorkbenchPage page : pages) {

              IEditorReference[] editorRefs = page.getEditorReferences();
              for (IEditorReference ref : editorRefs) {
                parts.add(ref);
              }
            }

            mPartListener.partsOpened(parts);

            // remove listener first for safety, we don't want
            // register the same listener twice accidently
            window.getPartService().removePartListener(mPartListener);
            window.getPartService().addPartListener(mPartListener);
          }
        }

        workbench.addWindowListener(mWindowListener);
      }
    });

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

    if (sIsE3 == null) {

      // previous checking on the platform product version has not been
      // reliable, since there are e4
      // based
      // products with a 3 as major version (e.g. Spring Tools Suite).
      try {

        // instead now check for the presence of a known e4 class
        Class.forName("org.eclipse.e4.ui.model.application.MApplicationElement");
        sIsE3 = false;
      } catch (ClassNotFoundException e) {
        sIsE3 = true;
      }
    }

    return sIsE3;
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

    String nl = Platform.getNL();
    String[] parts = nl.split("_"); //$NON-NLS-1$

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
   * @param t
   *          the exception
   * @param log
   *          <code>true</code> if the exception should be logged
   */
  public static void errorDialog(Shell shell, String message, Throwable t, boolean log) {

    Status status = new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK,
            message != null ? message : "", t); //$NON-NLS-1$

    String msg = NLS.bind(Messages.errorDialogMainMessage, message);
    ErrorDialog.openError(shell, Messages.CheckstyleLog_titleInternalError, msg, status);

    if (log) {
      CheckstyleLog.log(t);
    }
  }

  /**
   * Open an error dialog for an exception that occurred within the plugin.
   *
   * @param shell
   *          the shell
   * @param t
   *          the exception
   * @param log
   *          <code>true</code> if the exception should be logged
   */
  public static void errorDialog(Shell shell, Throwable t, boolean log) {
    errorDialog(shell, t.getLocalizedMessage(), t, log);
  }

  /**
   * Open an warning dialog for an exception that occurred within the plugin.
   *
   * @param shell
   *          the shell
   * @param message
   *          the exception message
   * @param t
   *          the exception
   */
  public static void warningDialog(Shell shell, String message, Throwable t) {
    Status status = new Status(IStatus.WARNING, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK,
            t.getLocalizedMessage(), t);

    ErrorDialog.openError(shell, Messages.CheckstyleLog_titleWarning, message, status);
  }

  /**
   * Returns the classloader containing quickfix extensions.
   *
   * @return the classloader containing all registered quickfix extensions.
   */
  public ClassLoader getQuickfixExtensionClassLoader() {
    return mQuickfixExtensionClassLoader;
  }

  private final CheckFileOnOpenPartListener mPartListener = new CheckFileOnOpenPartListener();

  private final IWindowListener mWindowListener = new IWindowListener() {

    @Override
    public void windowOpened(IWorkbenchWindow window) {
      window.getPartService().addPartListener(mPartListener);
    }

    @Override
    public void windowActivated(IWorkbenchWindow window) {
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
      window.getPartService().removePartListener(mPartListener);

    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
    }

  };
}
