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

package net.sf.eclipsecs.core;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.EclipseLogHandler;
import net.sf.eclipsecs.core.util.ExtensionClassLoader;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CheckstylePlugin extends Plugin {

  /** Identifier of the plug-in. */
  public static final String PLUGIN_ID = "net.sf.eclipsecs.core"; //$NON-NLS-1$

  /** Extension point id for Checkstyle addon providers. */
  private static final String ADDON_PROVIDER_EXT_PT_ID = PLUGIN_ID + ".checkstyleAddonProvider"; //$NON-NLS-1$

  /** The shared instance. */
  private static CheckstylePlugin sPlugin;

  private ClassLoader mAddonExtensionClassLoader;

  /**
   * The constructor.
   */
  public CheckstylePlugin() {
    super();
    sPlugin = this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    mAddonExtensionClassLoader = new ExtensionClassLoader(context.getBundle(),
            ADDON_PROVIDER_EXT_PT_ID);

    try {
      Logger checkstyleErrorLog = Logger.getLogger("com.puppycrawl.tools.checkstyle.ExceptionLog"); //$NON-NLS-1$

      checkstyleErrorLog.addHandler(new EclipseLogHandler(this));
      checkstyleErrorLog.setLevel(Level.ALL);

    } catch (Exception ioe) {
      CheckstyleLog.log(ioe);
    }
  }

  /**
   * Returns the shared instance.
   *
   * @return The shared plug-in instance.
   */
  public static CheckstylePlugin getDefault() {
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
   * Returns the extension classloader.
   *
   * @return the classloader to use when potentially accessing classes from extending plugins.
   */
  public ClassLoader getAddonExtensionClassLoader() {
    return mAddonExtensionClassLoader;
  }
}
