//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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
    } catch (final IOException ex) {
      CheckstyleLog.log(ex);
    }
  }

  /**
   * Method for writing the xml-file.
   *
   * @param outStream
   *          BufferedWriter to outputfile.
   */
  private void writeXMLFile(final OutputStream outStream) throws IOException {
    outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
    outStream.write("<module name=\"Checker\">\n".getBytes("UTF-8"));
    outStream.write("<property name=\"severity\" value=\"warning\"/>\n".getBytes("UTF-8"));
    writeModules(mCheckstyleSetting.getmCheckerModules(), outStream);
    outStream.write("<module name=\"TreeWalker\">\n".getBytes("UTF-8"));
    writeModules(mCheckstyleSetting.getmTreeWalkerModules(), outStream);
    outStream.write("</module>\n".getBytes("UTF-8"));
    outStream.write("</module>\n".getBytes("UTF-8"));
  }

  /**
   * Method for writing all modules to file.
   *
   * @param outStream
   *          BufferedWriter to xml-file.
   */
  private static void writeModules(final HashMap<String, HashMap<String, String>> modules,
          final OutputStream outStream) throws IOException {

    final Iterator<String> modit = modules.keySet().iterator();
    String module;

    while (modit.hasNext()) {
      module = modit.next();
      if (modules.get(module) == null) {
        outStream.write(("<module name=\"" + module + "\"/>\n").getBytes("UTF-8"));
      } else {
        outStream.write(("<module name=\"" + module + "\">\n").getBytes("UTF-8"));
        writeProperty(modules.get(module), outStream);
        outStream.write("</module>\n".getBytes("UTF-8"));
      }
    }
  }

  /**
   * Method for writing a propterty to file.
   *
   * @param properties
   *          A HashMap containing all properties.
   */
  private static void writeProperty(final HashMap<String, String> properties, final OutputStream outStream)
          throws IOException {
    final Iterator<String> propit = properties.keySet().iterator();
    String prop;

    while (propit.hasNext()) {
      prop = propit.next();
      outStream.write(("<property name=\"" + prop + "\" value=\"" + properties.get(prop) + "\"/>\n")
              .getBytes("UTF-8"));
    }
  }
}
