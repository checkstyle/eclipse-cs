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

package net.sf.eclipsecs.stats.export.internal;

import java.io.File;

import net.sf.eclipsecs.stats.data.Stats;
import net.sf.eclipsecs.stats.export.IStatsExporter;
import net.sf.eclipsecs.stats.export.StatsExporterException;

/**
 * Abstract exporter.
 * 
 * @author Fabrice BELLINGARD
 */
public abstract class AbstractStatsExporter implements IStatsExporter
{
    /**
     * {@inheritDoc}
     * 
     * @see net.sf.eclipsecs.stats.export.IStatsExporter#generate(net.sf.eclipsecs.stats.data.Stats,
     *      java.io.File)
     */
    public void generate(Stats stats, File outputFile)
        throws StatsExporterException
    {
        // checks if the values provided are correct
        if (stats == null)
        {
            throw new StatsExporterException("No statistics to export...");
        }
        if (outputFile == null)
        {
            throw new StatsExporterException(
                "The output file is null...");
        }

        // and if everything's fine, go!
        doGenerate(stats, outputFile);
    }

    /**
     * Generates the document containing the exported stats.
     * 
     * @param stats
     *            the Checkstyle statistics we want to export
     * @param outputFile
     *            the file into which the stats will be exported
     * @throws StatsExporterException
     *             if an error occurs while generating the document
     */
    protected abstract void doGenerate(Stats stats, File outputFile)
        throws StatsExporterException;
}