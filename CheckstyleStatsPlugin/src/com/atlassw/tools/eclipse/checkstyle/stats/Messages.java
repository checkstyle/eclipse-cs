//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch, Fabrice Bellingard
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

package com.atlassw.tools.eclipse.checkstyle.stats;

import org.eclipse.osgi.util.NLS;

/**
 * Class used for i18n.
 * 
 * @author Fabrice BELLINGARD
 */
public final class Messages extends NLS
{
    // CHECKSTYLE:OFF

    private static final String BUNDLE_NAME = "com.atlassw.tools.eclipse.checkstyle.stats.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String MarkerAnalyser_computingstats;

    public static String MarkerAnalyser_errorWhileComputingStats;

    public static String MarkerAnalyser_markerMessageShouldntBeEmpty;

    public static String PreferencePage_displayJavadocErrors;

    public static String PreferencePage_displayAllCategories;

    public static String DetailStatsView_fileColumn;

    public static String DetailStatsView_lineColumn;

    public static String DetailStatsView_messageColumn;

    public static String DetailStatsView_unableToShowMarker;

    public static String DetailStatsView_displayError;

    public static String DetailStatsView_displayErrorTooltip;

    public static String DetailStatsView_descriptionLabel;

    public static String StatsViewUtils_checkstyleErrorsCount;

    public static String StatsViewUtils_classElement;

    public static String StatsViewUtils_packageElement;

    public static String StatsViewUtils_fragmentRootElement;

    public static String StatsViewUtils_projectElement;

    public static String GraphPieDataset_otherCategories;

    public static String DetailStatsViewLabelProvider_unknown;

    public static String MarkerStatsView_kindOfErrorColumn;

    public static String MarkerStatsView_numberOfErrorsColumn;

    public static String MarkerStatsView_unableToOpenGraph;

    public static String MarkerStatsView_displayChart;

    public static String MarkerStatsView_displayChartTooltip;

    public static String MarkerStatsView_showDetails;

    public static String MarkerStatsView_showDetailsTooltip;

    public static String GraphStatsView_errorsRepartition;

    public static String GraphStatsView_noDataToDisplay;

    public static String GraphStatsView_javadocNotDisplayed;

    public static String GraphStatsView_unableToOpenListingView;

    public static String GraphStatsView_displayListing;

    public static String GraphStatsView_displayJavadocErrors;

    public static String GraphStatsView_displayAllCategories;

    // CHECKSTYLE:ON
}