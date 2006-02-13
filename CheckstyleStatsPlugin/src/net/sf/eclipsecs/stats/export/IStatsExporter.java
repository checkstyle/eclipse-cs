//============================================================================
//
// Copyright (C) 2002-2006  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package net.sf.eclipsecs.stats.export;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.stats.data.Stats;

/**
 * Objects that implement this interface know how to export Checkstyle stats in
 * a specific file format.
 * 
 * @author Fabrice BELLINGARD
 */
public interface IStatsExporter
{
    /** Key for the main font used to generate the report. Must be a String. */
    static final String PROPS_MAIN_FONT_NAME = "generator.main.font.name";

    /** Key for the main font size. Must be an Integer. */
    static final String PROPS_MAIN_FONT_SIZE = "generator.main.font.size";

    /** The default main font */
    static final String DEFAULT_MAIN_FONT_NAME = "Verdana";

    /** The default font size */
    static final Integer DEFAULT_MAIN_FONT_SIZE = new Integer(12);

    /**
     * Initializes the stats exporter with the properties specified in the map.
     * Each property must have one of this class constant begining by "PROPS_"
     * as a key.
     * 
     * @param props
     *            a map containing key-value pairs {String, Object}
     * @throws StatsExporterException
     *             if a properties does not have a valid type (for instance, the
     *             font size is a boolean)
     */
    void initialize(Map props) throws StatsExporterException;

    /**
     * Generates the document containing the exported stats.
     * 
     * @param stats
     *            the Checkstyle statistics we want to export
     * @param details
     *            the list of markers detailed for one of a Checkstyle error
     *            category
     * @param outputFile
     *            the file into which the stats will be exported
     * @throws StatsExporterException
     *             if an error occurs while generating the document
     */
    void generate(Stats stats, List details, File outputFile)
        throws StatsExporterException;

}