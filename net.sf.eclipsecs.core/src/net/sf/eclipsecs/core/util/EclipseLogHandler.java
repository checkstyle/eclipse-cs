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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.Messages;

/**
 * Logging-Handler implementation for the java.util.logging API to allow using the logging API to
 * log events that get directly passed into the internal eclipse logging.<br/>
 * This class act as bridge from java.util.logging to eclipse logging.
 *
 */
public class EclipseLogHandler extends Handler {

  /** The plugin Id. */
  private final String mPluginID;

  /** The eclipse log to log into. */
  private final ILog mPluginLog;

  /**
   * Creates an handler that passes java.util.logging messages to the eclipse log of a certain
   * plugin.
   *
   * @param loggingPlugin
   *          the plugin for which should be logged
   */
  public EclipseLogHandler(Plugin loggingPlugin) {
    mPluginLog = loggingPlugin.getLog();
    mPluginID = loggingPlugin.getBundle().getSymbolicName();
  }

  @Override
  public void publish(LogRecord record) {

    // translate log levels into severity
    int severity = 0;
    Level level = record.getLevel();
    if (Level.CONFIG.equals(level) || Level.INFO.equals(level) || Level.FINE.equals(level)
            || Level.FINER.equals(level) || Level.FINEST.equals(level)) {
      severity = IStatus.INFO;
    } else if (Level.WARNING.equals(level)) {
      severity = IStatus.WARNING;
    } else if (Level.SEVERE.equals(level)) {
      severity = IStatus.ERROR;
    }

    // get message
    String message = record.getMessage();

    // get throwable
    Throwable thrown = record.getThrown();

    Status status = new Status(severity, mPluginID, IStatus.OK,
            NLS.bind(Messages.CheckstyleLog_msgStatusPrefix, message), thrown);
    mPluginLog.log(status);
  }

  @Override
  public void close() {
    // NOOP
  }

  @Override
  public void flush() {
    // NOOP

  }

}
