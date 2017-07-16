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

package net.sf.eclipsecs.core.util;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Logging utility for the Checkstyle plug-in.
 */
public final class CheckstyleLog {

  private static ILog sLog;

  private CheckstyleLog() {
  }

  static {
    sLog = CheckstylePlugin.getDefault().getLog();
  }

  /**
   * Logs the exceptions.
   * 
   * @param t
   *          the exception to log
   */
  public static void log(Throwable t) {
    log(t, t.getLocalizedMessage());
  }

  /**
   * Logs the exception, describing it with the given message.
   * 
   * @param t
   *          the exception to log
   * @param message
   *          the message
   */
  public static void log(Throwable t, String message) {
    Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.OK,
            NLS.bind(Messages.CheckstyleLog_msgStatusPrefix, message), t);
    sLog.log(status);
  }

}