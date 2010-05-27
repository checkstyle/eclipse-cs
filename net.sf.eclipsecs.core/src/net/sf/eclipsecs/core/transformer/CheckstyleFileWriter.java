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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class for writing the checkstyle configuration to a xml-file.
 * 
 * @author Lukas Frena
 * 
 */
public class CheckstyleFileWriter {
    /** An object containing all settings for the checkstyle-file. */
    private final CheckstyleSetting mCheckstyleSetting;;

    /**
     * Creates new instance of class CheckstyleFileWriter.
     * 
     * @param setting
     *            The settings for the checkstyle-file.
     * @param file
     *            Path where the checkstyle-file should be stored.
     */
    public CheckstyleFileWriter(final CheckstyleSetting setting,
        final String file) {
        mCheckstyleSetting = setting;

        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
        }
        catch (final IOException e) {
            Logger.writeln("unable to write file " + file);
        }
        final BufferedWriter bw = new BufferedWriter(fw);

        try {
            writeXMLFile(bw);
        }
        catch (final IOException e) {
            Logger.writeln("error on writing to file " + file);
        }

        try {
            bw.flush();
            bw.close();
        }
        catch (final IOException e) {
            Logger.writeln("couldn't close checkstyle xml-file" + file);
        }
    }

    /**
     * Method for writing the xml-file.
     * 
     * @param bw
     *            BufferedWriter to outputfile.
     */
    private void writeXMLFile(final BufferedWriter bw) throws IOException {
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        bw.write("<module name=\"Checker\">\n");
        bw.write("<property name=\"severity\" value=\"warning\"/>\n");
        writeModules(mCheckstyleSetting.getmCheckerModules(), bw);
        bw.write("<module name=\"TreeWalker\">\n");
        writeModules(mCheckstyleSetting.getmTreeWalkerModules(), bw);
        bw.write("</module>\n");
        bw.write("</module>\n");
    }

    /**
     * Method for writing all modules to file.
     * 
     * @param bw
     *            BufferedWriter to xml-file.
     */
    private void writeModules(
        final HashMap<String, HashMap<String, String>> modules,
        final BufferedWriter bw) throws IOException {

        final Iterator<String> modit = modules.keySet().iterator();
        String module;

        while (modit.hasNext()) {
            module = modit.next();
            if (modules.get(module) == null) {
                bw.write("<module name=\"" + module + "\"/>\n");
            }
            else {
                bw.write("<module name=\"" + module + "\">\n");
                writeProperty(modules.get(module), bw);
                bw.write("</module>\n");
            }
        }
    }

    /**
     * Method for writing a propterty to file.
     * 
     * @param properties
     *            A HashMap containing all properties.
     */
    private void writeProperty(final HashMap<String, String> properties,
        final BufferedWriter bw) throws IOException {
        final Iterator<String> propit = properties.keySet().iterator();
        String prop;

        while (propit.hasNext()) {
            prop = propit.next();
            bw.write("<property name=\"" + prop + "\" value=\""
                + properties.get(prop) + "\"/>\n");
        }
    }
}
