//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle;

import org.eclipse.osgi.util.NLS;

/**
 * Class providing error messages for the checkstyle plugin. Uses the eclipse
 * new nls mechanism.
 * 
 * @author Lars Ködderitzsch
 */
public final class ErrorMessages extends NLS
{
    // CHECKSTYLE:OFF

    private static final String BUNDLE_NAME = "com.atlassw.tools.eclipse.checkstyle.errormessages"; //$NON-NLS-1$

    private ErrorMessages()
    {}

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ErrorMessages.class);
    }

    public static String AbstractASTResolution_msgErrorQuickfix;

    public static String errorDialogMainMessage;

    public static String errorUnknownInternalError;

    public static String errorEmptyPattern;

    public static String errorFailedCreatePattern;

    public static String errorNoValidRegex;

    public static String errorFailedGenerateTestResult;

    public static String errorFailedAddFileset;

    public static String errorFailedEditFileset;

    public static String errorOpeningPropertiesPage;

    public static String errorChangingFilesetEditor;

    public static String errorCannotResolveCheckLocation;

    public static String errorUnknownFileFormat;

    public static String errorWritingCheckConfigurations;

    public static String errorNoRootModule;

    public static String errorMoreThanOneRootModule;

    public static String errorLoadingConfigFile;

    public static String errorMigratingConfig;

    public static String errorWritingConfigFile;

    public static String errorUnknownPropertyType;

    public static String msgErrorLoadingCheckstyleDTD;

    public static String errorLocationEmpty;

    public static String errorResolveConfigLocation;

    public static String errorConfigNameEmpty;

    public static String errorConfigNameInUse;

    public static String errorUnknownClasspathEntry;

    public static String errorNoCheckConfig;

    public static String errorFailedCreatePreferencesPage;

    public static String errorFailedSavePreferences;

    public static String errorFailedRebuild;

    public static String msgErrorFailedExportConfig;

    public static String CheckstyleLog_msgStatusPrefix;

    public static String CheckstyleLog_titleInternalError;

    public static String CheckstyleLog_titleWarning;

    public static String errorWhileBuildingProject;

    public static String errorWhileOpeningEditor;

    public static String errorWhileDisplayingDuplicates;

    public static String errorFilesetWithoutCheckConfig;

    public static String CheckstyleBuilder_msgErrorUnknown;

    public static String RemoteConfigurationType_errorFileNotFound;

    public static String RemoteConfigurationType_errorUnknownHost;

    public static String RemoteConfigurationType_msgRemoteCachingFailed;

    public static String RemoteConfigurationType_msgUnAuthorized;

    // CHECKSTYLE:ON
}