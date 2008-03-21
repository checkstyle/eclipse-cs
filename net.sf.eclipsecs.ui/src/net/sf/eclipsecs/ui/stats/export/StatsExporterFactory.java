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

package net.sf.eclipsecs.ui.stats.export;

import net.sf.eclipsecs.ui.stats.export.internal.RTFStatsExporter;

/**
 * Factory used to create an IStatsExporter.
 * 
 * @author Fabrice BELLINGARD
 */
public final class StatsExporterFactory {
    /** RTF exporter type. */
    public static final String RTF = "rtf";

    /**
     * Constructor.
     */
    private StatsExporterFactory() {}

    /**
     * Creates an object that know how to export the Stats.
     * 
     * @param type the kind of file to export to. For example, "rtf" or "pdf".
     * @return the stats exporter object
     * @throws StatsExporterException exception when exporting stats
     */
    public static IStatsExporter createStatsExporter(String type) throws StatsExporterException {
        IStatsExporter exporter = null;
        if (RTF.equals(type)) {
            exporter = new RTFStatsExporter();
        }
        else {
            throw new StatsExporterException("Unsupported kind of file ot export the stats to.");
        }
        return exporter;
    }

}