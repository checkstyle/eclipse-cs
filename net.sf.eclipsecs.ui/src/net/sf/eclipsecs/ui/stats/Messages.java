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

package net.sf.eclipsecs.ui.stats;

import org.eclipse.osgi.util.NLS;

/**
 * Class used for i18n.
 * 
 * @author Fabrice BELLINGARD
 */
public final class Messages extends NLS {
  // CHECKSTYLE:OFF

  private static final String BUNDLE_NAME = "net.sf.eclipsecs.ui.stats.messages"; //$NON-NLS-1$

  private Messages() {
  }

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String PreferencePage_displayJavadocErrors;

  public static String PreferencePage_displayAllCategories;

  public static String MarkerStatsView_fileColumn;

  public static String MarkerStatsView_lineColumn;

  public static String MarkerStatsView_messageColumn;

  public static String MarkerStatsView_unableToShowMarker;

  public static String MarkerStatsView_displayError;

  public static String MarkerStatsView_displayErrorTooltip;

  public static String StatsViewUtils_checkstyleErrorsCount;

  public static String StatsViewUtils_classElement;

  public static String StatsViewUtils_packageElement;

  public static String StatsViewUtils_fragmentRootElement;

  public static String StatsViewUtils_projectElement;

  public static String GraphPieDataset_otherCategories;

  public static String MarkerStatsView_unknownProblem;

  public static String MarkerStatsView_kindOfErrorColumn;

  public static String MarkerStatsView_numberOfErrorsColumn;

  public static String MarkerStatsView_unableToOpenGraph;

  public static String MarkerStatsView_displayChart;

  public static String MarkerStatsView_displayChartTooltip;

  public static String MarkerStatsView_chooseFileToExport;

  public static String MarkerStatsView_exportErrorsAsReport;

  public static String MarkerStatsView_exportErrorsAsReportTooltip;

  public static String MarkerStatsView_reportGenerationFailed;

  public static String MarkerStatsView_exportGraphAsImage;

  public static String MarkerStatsView_exportGraphAsImageTooltip;

  public static String MarkerStatsView_graphExportFailed;

  public static String MarkerStatsView_showDetails;

  public static String MarkerStatsView_showDetailsTooltip;

  public static String GraphStatsView_errorsRepartition;

  public static String GraphStatsView_noDataToDisplay;

  public static String GraphStatsView_javadocNotDisplayed;

  public static String GraphStatsView_unableToOpenListingView;

  public static String GraphStatsView_displayListing;

  public static String GraphStatsView_displayJavadocErrors;

  public static String GraphStatsView_displayAllCategories;

  public static String CreateStatsJob_msgAnalyzeMarkers;

  public static String CreateStatsJob_errorAnalyzingMarkers;

  public static String CreateStatsJob_markerMessageShouldntBeEmpty;

  public static String AbstractStatsView_msgRefreshStats;

  public static String FiltersAction_text;

  public static String FiltersAction_tooltip;

  public static String CheckstyleMarkerFilterDialog_btnEnabled;

  public static String CheckstyleMarkerFilterDialog_groupResourceSetting;

  public static String CheckstyleMarkerFilterDialog_btnOnAnyResource;

  public static String CheckstyleMarkerFilterDialog_btnOnAnyResourceInSameProject;

  public static String CheckstyleMarkerFilterDialog_btnOnSelectedResource;

  public static String CheckstyleMarkerFilterDialog_btnOnSelectedResourceAndChilds;

  public static String CheckstyleMarkerFilterDialog_btnOnWorkingSet;

  public static String CheckstyleMarkerFilterDialog_btnSelect;

  public static String CheckstyleMarkerFilterDialog_btnMarkerSeverity;

  public static String CheckstyleMarkerFilterDialog_btnSeverityError;

  public static String CheckstyleMarkerFilterDialog_btnSeverityWarning;

  public static String CheckstyleMarkerFilterDialog_btnSeverityInfo;

  public static String CheckstyleMarkerFilterDialog_title;

  public static String CheckstyleMarkerFilterDialog_titleMessage;

  public static String CheckstyleMarkerFilterDialog_btnRestoreDefault;

  public static String CheckstyleMarkerFilterDialog_btnShellTitle;

  public static String CheckstyleMarkerFilterDialog_msgNoWorkingSetSelected;

  public static String GraphStatsView_lblViewMessage;

  public static String MarkerStatsView_folderColumn;

  public static String MarkerStatsView_actionBack;

  public static String MarkerStatsView_actionBackTooltip;

  public static String MarkerStatsView_lblOverviewMessage;

  public static String MarkerStatsView_lblDetailMessage;

  public static String CheckstyleMarkerFilterDialog_lblExcludeMarkers;

  public static String CheckstyleMarkerFilterDialog_lblRegex;

  public static String CheckstyleMarkerFilterDialog_btnEdit;

  public static String CheckstyleMarkerFilterDialog_btnAdd;

  public static String CheckstyleMarkerFilterDialog_btnRemove;

  public static String CheckstyleMarkerFilterDialog_titleRegexEditor;

  public static String CheckstyleMarkerFilterDialog_msgNoRegexDefined;

  public static String CheckstyleMarkerFilterDialog_msgInvalidRegex;

  public static String CheckstyleMarkerFilterDialog_msgEditRegex;

  // CHECKSTYLE:ON
}
