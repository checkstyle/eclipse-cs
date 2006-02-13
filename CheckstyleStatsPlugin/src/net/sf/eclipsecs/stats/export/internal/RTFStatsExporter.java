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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.stats.Messages;
import net.sf.eclipsecs.stats.data.CreateStatsJob;
import net.sf.eclipsecs.stats.data.MarkerStat;
import net.sf.eclipsecs.stats.data.Stats;
import net.sf.eclipsecs.stats.export.StatsExporterException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfFont;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooterGroup;

/**
 * Exporter that generates an RTF file.
 * 
 * @author Fabrice BELLINGARD
 */
public class RTFStatsExporter extends AbstractStatsExporter
{
    private String mMainFontName;

    private int mMainFontSize;

    private RtfFont mainFont;

    private RtfFont pageHeaderAndFooterFont;

    private RtfFont tableHeaderAndFooterFont;

    /**
     * {@inheritDoc}
     * 
     * @see net.sf.eclipsecs.stats.export.IStatsExporter#initialize(java.util.Map)
     */
    public void initialize(Map props) throws StatsExporterException
    {
        mMainFontName = "Verdana";
        mMainFontSize = 10;
        // String fontName;
        // Object font = props.get(PROPS_MAIN_FONT_NAME);
        // if (font instanceof String)
        // {
        // fontName = (String) font;
        // }
        // else
        // {
        // fontName = DEFAULT_MAIN_FONT_NAME;
        // }
        //
        // Integer fontSize;
        // // TO BE CONTINUED...
    }

    /**
     * {@inheritDoc}
     * 
     * @see net.sf.eclipsecs.stats.export.internal.AbstractStatsExporter#doGenerate(net.sf.eclipsecs.stats.data.Stats,
     *      java.util.List, java.io.File)
     */
    protected void doGenerate(Stats stats, List details, File outputFile)
        throws StatsExporterException
    {
        File exportFile = getRealExportFile(outputFile);

        try
        {
            Document doc = new Document();
            RtfWriter2.getInstance(doc, new FileOutputStream(exportFile));

            // init the fonts
            mainFont = new RtfFont(mMainFontName, mMainFontSize);
            pageHeaderAndFooterFont = new RtfFont(mMainFontName, 9,
                Font.BOLDITALIC, new Color(200, 200, 200));
            tableHeaderAndFooterFont = new RtfFont(mMainFontName,
                mMainFontSize, Font.BOLD);

            // creates headers and footers
            createHeaderAndFooter(doc);

            doc.open();

            // introduces the report
            doc.add(new Paragraph(""));

            String intro = NLS.bind(
                Messages.MarkerStatsView_lblOverviewMessage, new Object[] {
                        new Integer(stats.getMarkerCount()),
                        new Integer(stats.getMarkerStats().size()),
                        new Integer(stats.getMarkerCountAll()) });
            Paragraph p = new Paragraph(intro, mainFont);
            doc.add(p);

            doc.add(new Paragraph(""));

            // generates the summary of the stats
            createSummaryTable(stats, doc);

            // if there are details, print the details
            if (details.size() > 0)
            {
                try
                {
                    // introduces the detail section
                    String category = CreateStatsJob
                        .getUnlocalizedMessage((IMarker) details.get(0));
                    category = CreateStatsJob.cleanMessage(category);
                    String detailText = NLS.bind(
                        Messages.MarkerStatsView_lblDetailMessage,
                        new Object[] { category, new Integer(details.size()) });
                    p = new Paragraph(detailText, mainFont);
                    doc.add(p);

                    // and generates the table with the details
                    createDetailSection(details, doc);

                }
                catch (CoreException e)
                {
                    // TODO improve the message and i18n...
                    doc.add(new Paragraph(
                        "An error occured while reading the selected markers",
                        mainFont));
                    CheckstyleLog.log(e,
                        "An error occured while reading the selected markers");
                }
            }

            doc.close();
        }
        catch (FileNotFoundException e)
        {
            throw new StatsExporterException(e);
        }
        catch (DocumentException e)
        {
            throw new StatsExporterException(e);
        }
    }

    private void createDetailSection(List details, Document doc)
        throws CoreException, DocumentException
    {
        Table table = new Table(4);
        table.setSpaceInsideCell(10);
        table.setAlignment(Element.ALIGN_LEFT);
        table.setWidth(100);
        table.setWidths(new float[] { 30, 30, 10, 30 });

        Cell resourceHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_fileColumn, tableHeaderAndFooterFont));
        resourceHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        resourceHeader.setHeader(true);
        table.addCell(resourceHeader);
        Cell folderHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_folderColumn, tableHeaderAndFooterFont));
        folderHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        folderHeader.setHeader(true);
        table.addCell(folderHeader);
        Cell lineHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_lineColumn, tableHeaderAndFooterFont));
        lineHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        lineHeader.setHeader(true);
        table.addCell(lineHeader);
        Cell messageHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_messageColumn, tableHeaderAndFooterFont));
        messageHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        messageHeader.setHeader(true);
        table.addCell(messageHeader);

        Cell resourceCell;
        Cell fileCell;
        Cell lineCell;
        Cell messageCell;
        for (Iterator iter = details.iterator(); iter.hasNext();)
        {
            IMarker marker = (IMarker) iter.next();
            resourceCell = new Cell(new Chunk(marker.getResource().getName(),
                mainFont));
            table.addCell(resourceCell);
            fileCell = new Cell(new Chunk(marker.getResource().getParent()
                .getFullPath().toString(), mainFont));
            table.addCell(fileCell);
            lineCell = new Cell(new Chunk(marker.getAttribute(
                IMarker.LINE_NUMBER).toString(), mainFont));
            lineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(lineCell);
            messageCell = new Cell(new Chunk(marker.getAttribute(
                IMarker.MESSAGE).toString(), mainFont));
            table.addCell(messageCell);
        }

        doc.add(table);
    }

    private void createSummaryTable(Stats stats, Document doc)
        throws DocumentException
    {
        Table table = new Table(2);
        table.setSpaceInsideCell(10);
        table.setAlignment(Element.ALIGN_LEFT);
        table.setWidth(100);
        table.setWidths(new float[] { 80, 20 });

        Cell typeHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_kindOfErrorColumn,
            tableHeaderAndFooterFont));
        typeHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        typeHeader.setHeader(true);
        table.addCell(typeHeader);
        Cell countHeader = new Cell(new Chunk(
            Messages.MarkerStatsView_numberOfErrorsColumn,
            tableHeaderAndFooterFont));
        countHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        countHeader.setHeader(true);
        table.addCell(countHeader);

        ArrayList markerStatsSortedList = new ArrayList(stats.getMarkerStats());
        Collections.sort(markerStatsSortedList, new Comparator()
        {
            public int compare(Object arg0, Object arg1)
            {
                MarkerStat markerStat0 = (MarkerStat) arg0;
                MarkerStat markerStat1 = (MarkerStat) arg1;
                return markerStat1.getCount() - markerStat0.getCount();
            }
        });
        Cell typeCell;
        Cell countCell;
        for (Iterator iter = markerStatsSortedList.iterator(); iter.hasNext();)
        {
            MarkerStat markerStat = (MarkerStat) iter.next();
            typeCell = new Cell(
                new Chunk(markerStat.getIdentifiant(), mainFont));
            table.addCell(typeCell);
            countCell = new Cell(
                new Chunk(markerStat.getCount() + "", mainFont));
            countCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(countCell);
        }

        doc.add(table);
    }

    private void createHeaderAndFooter(Document doc)
    {
        Paragraph p = new Paragraph("Checkstyle statistics",
            pageHeaderAndFooterFont);
        p.setAlignment(Element.ALIGN_CENTER);
        HeaderFooter header = new RtfHeaderFooter(p);
        doc.setHeader(header);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        p = new Paragraph("Généré le : " + simpleDateFormat.format(new Date()),
            pageHeaderAndFooterFont);
        p.setAlignment(Element.ALIGN_CENTER);
        RtfHeaderFooterGroup footer = new RtfHeaderFooterGroup();
        footer
            .setHeaderFooter(
                new RtfHeaderFooter(p),
                com.lowagie.text.rtf.headerfooter.RtfHeaderFooter.DISPLAY_ALL_PAGES);
        doc.setFooter(footer);
    }

    /**
     * From the File given by the user, computes the real name of the output
     * file
     * 
     * @param outputFile
     *            the output file as given by the user
     * @return the final name of the output file
     */
    private File getRealExportFile(File outputFile)
    {
        File exportFile;
        IPath filePath = new Path(outputFile.getAbsolutePath());
        String fileExt = filePath.getFileExtension();
        if (fileExt == null)
        {
            exportFile = new File(outputFile.getAbsolutePath() + ".rtf");
        }
        else if (!fileExt.equals("rtf"))
        {
            exportFile = new File(outputFile.getAbsolutePath() + ".rtf");
        }
        else
        {
            // the output file is RTF
            exportFile = outputFile;
        }
        return exportFile;
    }
}