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

package net.sf.eclipsecs.core.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;

/**
 * Logging utility for the Checkstyle plug-in.
 */
public final class CheckstyleLog {

  private static ILog log;

  private CheckstyleLog() {
  }

  static {
    log = CheckstylePlugin.getDefault().getLog();
  }

  /**
   * Logs the exceptions.
   *
   * @param error
   *          the exception to log
   */
  public static void log(Throwable error) {
    log(error, error.getLocalizedMessage());
  }

  /**
   * Logs the exception, describing it with the given message.
   *
   * @param throwable
   *          the exception to log
   * @param message
   *          the message
   */
  public static void log(Throwable throwable, String message) {
    Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.OK,
            NLS.bind(Messages.CheckstyleLog_msgStatusPrefix, message), throwable);
    log.log(status);
  }

}
