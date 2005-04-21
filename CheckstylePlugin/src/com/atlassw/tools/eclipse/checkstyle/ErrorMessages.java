
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
    //CHECKSTYLE:OFF

    private static final String BUNDLE_NAME = "com.atlassw.tools.eclipse.checkstyle.errormessages"; //$NON-NLS-1$

    private ErrorMessages()
    {}

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ErrorMessages.class);
    }

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

    //  CHECKSTYLE:ON
}