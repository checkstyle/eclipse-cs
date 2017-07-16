//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * Class for writing the checkstyle configuration to a xml-file.
 *
 * @author Lukas Frena
 *
 */
public class CheckstyleFileWriter {

  /** An object containing all settings for the checkstyle-file. */
  private final CheckstyleSetting mCheckstyleSetting;

  /**
   * Creates new instance of class CheckstyleFileWriter.
   *
   * @param setting
   *          The settings for the checkstyle-file.
   * @param file
   *          Path where the checkstyle-file should be stored.
   */
  public CheckstyleFileWriter(final CheckstyleSetting setting, final String file) {
    mCheckstyleSetting = setting;

    try (FileOutputStream fw = new FileOutputStream(file)) {
      writeXMLFile(fw);
    } catch (final IOException e) {
      CheckstyleLog.log(e);
    }
  }

  /**
   * Method for writing the xml-file.
   *
   * @param bw
   *          BufferedWriter to outputfile.
   */
  private void writeXMLFile(final OutputStream bw) throws IOException {
    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
    bw.write("<module name=\"Checker\">\n".getBytes("UTF-8"));
    bw.write("<property name=\"severity\" value=\"warning\"/>\n".getBytes("UTF-8"));
    writeModules(mCheckstyleSetting.getmCheckerModules(), bw);
    bw.write("<module name=\"TreeWalker\">\n".getBytes("UTF-8"));
    writeModules(mCheckstyleSetting.getmTreeWalkerModules(), bw);
    bw.write("</module>\n".getBytes("UTF-8"));
    bw.write("</module>\n".getBytes("UTF-8"));
  }

  /**
   * Method for writing all modules to file.
   *
   * @param bw
   *          BufferedWriter to xml-file.
   */
  private void writeModules(final HashMap<String, HashMap<String, String>> modules,
          final OutputStream bw) throws IOException {

    final Iterator<String> modit = modules.keySet().iterator();
    String module;

    while (modit.hasNext()) {
      module = modit.next();
      if (modules.get(module) == null) {
        bw.write(("<module name=\"" + module + "\"/>\n").getBytes("UTF-8"));
      } else {
        bw.write(("<module name=\"" + module + "\">\n").getBytes("UTF-8"));
        writeProperty(modules.get(module), bw);
        bw.write("</module>\n".getBytes("UTF-8"));
      }
    }
  }

  /**
   * Method for writing a propterty to file.
   *
   * @param properties
   *          A HashMap containing all properties.
   */
  private void writeProperty(final HashMap<String, String> properties, final OutputStream bw)
          throws IOException {
    final Iterator<String> propit = properties.keySet().iterator();
    String prop;

    while (propit.hasNext()) {
      prop = propit.next();
      bw.write(("<property name=\"" + prop + "\" value=\"" + properties.get(prop) + "\"/>\n")
              .getBytes("UTF-8"));
    }
  }
}
