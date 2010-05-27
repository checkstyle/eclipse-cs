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

import org.eclipse.core.resources.IProject;

/**
 * Loggingclass for writing infos to the logfile.
 * 
 * @author Lukas Frena
 */
public class Logger {
    /** BufferedWriter to the logfile */
    private static BufferedWriter bw = null;

    /**
     * Method for initializing the Logger. Has to be called before the first
     * logging-entry.
     * 
     * @param selection
     *            Selected Project in workspace.
     */
    public static void initialize(final IProject selection) {
        FileWriter fw = null;
        final String logfile = selection.getLocation() + "/.logfile.txt";
        System.out.println(logfile);

        try {
            fw = new FileWriter(logfile);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        bw = new BufferedWriter(fw);
    }

    /**
     * Method for writing to the console without newline at eol.
     * 
     * @param message
     *            The message which should be written on the console.
     */
    public static void write(final String message) {
        try {
            bw.write(message);
            bw.flush();
        }
        catch (final IOException e) {
            System.err.println("error on writing to logfile");
        }
    }

    /**
     * Method for writing to the console with newline at eol.
     * 
     * @param message
     *            The message which should be written on the console.
     */
    public static void writeln(final String message) {
        try {
            bw.write(message + "\n");
            bw.flush();
        }
        catch (final IOException e) {
            System.err.println("error on writing to logfile");
        }
    }

    /**
     * Method to close the opened stream.
     */
    public static void close() {
        try {
            bw.close();
        }
        catch (final IOException e) {
            System.err.println("couldn't close stream to logfile");
        }
    }
}
