//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Class providing messages for the checkstyle plugin. Uses the eclipse new nls
 * mechanism.
 * 
 * @author Lars Ködderitzsch
 */
public final class Messages extends NLS {
  // CHECKSTYLE:OFF

  private static final String BUNDLE_NAME = "net.sf.eclipsecs.ui.messages"; //$NON-NLS-1$

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String ActivateProjectsPrintAction_msgActivateSelectedProjects;

  public static String CheckConfigurationConfigureDialog_btnAdd;

  public static String CheckConfigurationConfigureDialog_btnOpen;

  public static String CheckConfigurationConfigureDialog_btnOpenModuleOnAdd;

  public static String CheckConfigurationConfigureDialog_btnRemove;

  public static String CheckConfigurationConfigureDialog_colComment;

  public static String CheckConfigurationConfigureDialog_colEnabled;

  public static String CheckConfigurationConfigureDialog_colModule;

  public static String CheckConfigurationConfigureDialog_colSeverity;

  public static String CheckConfigurationConfigureDialog_defaultFilterText;

  public static String CheckConfigurationConfigureDialog_lblConfiguredModules;

  public static String CheckConfigurationConfigureDialog_lblDescription;

  public static String CheckConfigurationConfigureDialog_lblKnownModules;

  public static String CheckConfigurationConfigureDialog_msgEditConfig;

  public static String CheckConfigurationConfigureDialog_msgReadonlyConfig;

  public static String CheckConfigurationConfigureDialog_msgRemoveModules;

  public static String CheckConfigurationConfigureDialog_titleCheckConfigurationDialog;

  public static String CheckConfigurationConfigureDialog_titleMessageArea;

  public static String CheckConfigurationConfigureDialog_titleModuleConfigEditor;

  public static String CheckConfigurationConfigureDialog_titleNewModule;

  public static String CheckConfigurationConfigureDialog_titleRemoveModules;

  public static String CheckConfigurationConfigureDialog_txtNoDescription;

  public static String CheckConfigurationPropertiesDialog_lblConfigType;

  public static String CheckConfigurationPropertiesDialog_lblDescription;

  public static String CheckConfigurationPropertiesDialog_lblLocation;

  public static String CheckConfigurationPropertiesDialog_lblName;

  public static String CheckConfigurationPropertiesDialog_msgCopyConfiguration;

  public static String CheckConfigurationPropertiesDialog_msgCreateNewCheckConfig;

  public static String CheckConfigurationPropertiesDialog_msgEditCheckConfig;

  public static String CheckConfigurationPropertiesDialog_titleCheckConfig;

  public static String CheckConfigurationPropertiesDialog_titleCheckProperties;

  public static String CheckConfigurationPropertiesDialog_titleCopyConfiguration;

  public static String CheckstylePreferencePage_btnConfigure;

  public static String CheckstylePreferencePage_btnCopy;

  public static String CheckstylePreferencePage_btnExport;

  public static String CheckstylePreferencePage_btnNew;

  public static String CheckstylePreferencePage_btnProperties;

  public static String CheckstylePreferencePage_btnRefreshCheckerCache;

  public static String CheckstylePreferencePage_btnRemove;

  public static String CheckstylePreferencePage_btnDefault;

  public static String CheckstylePreferencePage_txtBackgroundFullBuild0;

  public static String CheckstylePreferencePage_txtDefault;

  public static String CheckstylePreferencePage_colCheckConfig;

  public static String CheckstylePreferencePage_colLocation;

  public static String CheckstylePreferencePage_colType;

  public static String CheckstylePreferencePage_colDefault;

  public static String CheckConfigurationPropertiesDialog_CopyOfAddition;

  public static String CheckstylePreferencePage_lblDescription;

  public static String CheckstylePreferencePage_lblDisableClassloader;

  public static String CheckstylePreferencePage_lblDisableClassloaderNote;

  public static String CheckstylePreferencePage_lblGeneralSettings;

  public static String CheckstylePreferencePage_lblIncludeModuleIds;

  public static String CheckstylePreferencePage_lblIncludeRulenames;

  public static String CheckstylePreferencePage_lblLimitMarker;

  public static String CheckstylePreferencePage_lblProjectUsage;

  public static String CheckstylePreferencePage_lblRebuild;

  public static String CheckstylePreferencePage_lblWarnFilesets;

  public static String CheckstylePreferencePage_msgCantDelete;

  public static String CheckstylePreferencePage_msgDelete;

  public static String CheckstylePreferencePage_msgProjectRelativeConfigNoFound;

  public static String CheckstylePreferencePage_msgRebuild;

  public static String CheckstylePreferencePage_nagRebuild;

  public static String CheckstylePreferencePage_titleCantDelete;

  public static String CheckstylePreferencePage_titleCheckConfigs;

  public static String CheckstylePreferencePage_titleDelete;

  public static String CheckstylePreferencePage_titleExportConfig;

  public static String CheckstylePreferencePage_titleRebuild;

  public static String CheckstylePreferencePage_txtSuggestRebuild;

  public static String CheckstylePreferencePage_version;

  public static String CheckstylePropertyPage_btnActivateCheckstyle;

  public static String CheckstylePropertyPage_btnChangeFilter;

  public static String CheckstylePropertyPage_btnUseSimpleConfig;

  public static String CheckstylePropertyPage_lblDescription;

  public static String CheckstylePropertyPage_mgsWarnFileSetNagOption;

  public static String CheckstylePropertyPage_msgWarnFilesets;

  public static String CheckstylePropertyPage_titleFilterGroup;

  public static String CheckstylePropertyPage_titleWarnFilesets;

  public static String CheckstylePropertyPage_tabMain;

  public static String CheckstylePropertyPage_tabCheckConfigs;

  public static String CheckstylePropertyPage_titleRebuild;

  public static String CheckstylePropertyPage_msgRebuild;

  public static String CheckstylePropertyPage_nagRebuild;

  public static String CheckstylePropertyPage_msgLocalConfigs;

  public static String CheckstylePropertyPage_btnSyncFormatter;

  public static String ClearSelectedFilesAction_title;

  public static String ComplexFileSetsEditor_btnAdd;

  public static String ComplexFileSetsEditor_btnEdit;

  public static String ComplexFileSetsEditor_btnRemove;

  public static String ComplexFileSetsEditor_colConfiguration;

  public static String ComplexFileSetsEditor_colEnabled;

  public static String ComplexFileSetsEditor_colFilesetName;

  public static String ComplexFileSetsEditor_titleAdvancedFilesetEditor;

  public static String ConfigPropertyWidgetFile_btnBrowse0;

  public static String ConfigPropertyWidgetRegex_msgRegexTestString;

  public static String ConfigureProjectFromBluePrintAction_msgConfiguringFromBluePrint;

  public static String ConfigureProjectFromBluePrintAction_msgSelectBlueprintProject;

  public static String ConfigureProjectFromBluePrintAction_titleSelectBlueprintProject;

  public static String DeactivateProjectsPrintAction_msgDeactivateSelectedProjects;

  public static String DuplicatedCodeView_openSourceAction;

  public static String DuplicatedCodeView_openSourceActionTooltip;

  public static String DuplicatedCodeView_openTargetAction;

  public static String DuplicatedCodeView_openTargetActionTooltip;

  public static String DuplicatedCodeView_refreshAction;

  public static String DuplicatedCodeView_refreshActionTooltip;

  public static String DuplicatedCodeView_runningCheckstyleToRefresh;

  public static String errorFailedRebuild;

  public static String errorFailedSavePreferences;

  public static String ExternalFileConfigurationEditor_msgFileDoesNotExist;

  public static String ExternalFileConfigurationEditor_titleFileDoesNotExist;

  public static String FileConfigurationLocationEditor_btnBrowse;

  public static String FileMatchPatternEditDialog_chkIncludesFiles;

  public static String FileMatchPatternEditDialog_lblRegex;

  public static String FileMatchPatternEditDialog_message;

  public static String FileMatchPatternEditDialog_title;

  public static String FileMatchPatternEditDialog_titleRegexEditor;

  public static String FileSetEditDialog_btnAdd;

  public static String FileSetEditDialog_btnConfigure;

  public static String FileSetEditDialog_btnDown;

  public static String FileSetEditDialog_btnEdit;

  public static String FileSetEditDialog_btnRemove;

  public static String FileSetEditDialog_btnUp;

  public static String FileSetEditDialog_colInclude;

  public static String FileSetEditDialog_colRegex;

  public static String FileSetEditDialog_lblCheckConfig;

  public static String FileSetEditDialog_lblName;

  public static String FileSetEditDialog_message;

  public static String FileSetEditDialog_msgNoFilesetName;

  public static String FileSetEditDialog_noCheckConfigSelected;

  public static String FileSetEditDialog_titleCreate;

  public static String FileSetEditDialog_titleEdit;

  public static String FileSetEditDialog_titleFilesetEditor;

  public static String FileSetEditDialog_titlePatternsTable;

  public static String FileSetEditDialog_titleTestResult;

  public static String FileTypesFilterEditor_btnAdd;

  public static String FileTypesFilterEditor_btnRemove;

  public static String FileTypesFilterEditor_title;

  public static String FixCheckstyleMarkersJob_title;

  public static String PackageFilterEditor_msgFilterPackages;

  public static String PackageFilterEditor_titleFilterPackages;

  public static String ProjectConfigurationEditor_msgNoProjectInWorkspace;

  public static String ProjectConfigurationLocationEditor_btnBrowse;

  public static String ProjectConfigurationLocationEditor_msgSelectConfigFile;

  public static String ProjectConfigurationLocationEditor_titleSelectConfigFile;

  public static String PropertiesContentAssistProcessor_basedir;

  public static String PropertiesContentAssistProcessor_configLoc;

  public static String PropertiesContentAssistProcessor_projectLoc;

  public static String PropertiesContentAssistProcessor_samedir;

  public static String PropertiesContentAssistProcessor_workspaceLoc;

  public static String RemoteConfigurationEditor_lblPassword;

  public static String RemoteConfigurationEditor_lblUserName;

  public static String RemoteConfigurationEditor_titleCredentialsGroup;

  public static String ResolvablePropertiesDialog_msgNoUnresolvedProps;

  public static String ResolvablePropertiesDialog_titleNoUnresolvedProps;

  public static String RuleConfigurationEditDialog_btnDefaul;

  public static String RuleConfigurationEditDialog_btnSortTokens;

  public static String RuleConfigurationEditDialog_btnTranslateTokens;

  public static String RuleConfigurationEditDialog_lblComment;

  public static String RuleConfigurationEditDialog_lblCustomMessage;

  public static String RuleConfigurationEditDialog_lblId;

  public static String RuleConfigurationEditDialog_lblProperties;

  public static String RuleConfigurationEditDialog_lblSeverity;

  public static String RuleConfigurationEditDialog_msgEditRuleConfig;

  public static String RuleConfigurationEditDialog_msgInvalidPropertyValue;

  public static String RuleConfigurationEditDialog_msgReadonlyModule;

  public static String RuleConfigurationEditDialog_msgRestoreDefault;

  public static String RuleConfigurationEditDialog_tabAdvanced;

  public static String RuleConfigurationEditDialog_tabGeneral;

  public static String RuleConfigurationEditDialog_titleCustMsg;

  public static String RuleConfigurationEditDialog_titleRestoreDefault;

  public static String RuleConfigurationEditDialog_titleRuleConfigEditor;

  public static String SimpleFileSetsEditor_btnManageConfigs;

  public static String SimpleFileSetsEditor_lblDescription;

  public static String SimpleFileSetsEditor_nameAllFileset;

  public static String SimpleFileSetsEditor_titleSimpleConfig;

  private Messages() {
  }

  public static String FileSetEditDialog_msgBuildTestResults;

  public static String RemoteConfigurationEditor_titleAdvancedOptions;

  public static String RemoteConfigurationEditor_btnCacheRemoteConfig;

  public static String ExternalFileConfigurationEditor_btnProtectConfigFile;

  public static String ProjectConfigurationEditor_chkProtectConfigFile;

  public static String InternalConfigurationEditor_btnImport;

  public static String InternalConfigurationEditor_titleImportDialog;

  public static String CheckConfigurationLabelProvider_suffixGlobal;

  public static String CheckConfigurationLabelProvider_suffixLocal;

  public static String CheckConfigurationPropertiesDialog_btnAdditionalProps;

  public static String CheckConfigurationPropertiesDialog_titleUnresolvedProps;

  public static String CheckConfigurationPropertiesDialog_msgUnresolvedProps;

  public static String CheckConfigurationPropertiesDialog_btnEditProps;

  public static String CheckConfigurationPropertiesDialog_btnContinue;

  public static String CheckConfigurationPropertiesDialog_btnCancel;

  public static String ResolvablePropertyEditDialog_titleMessageArea;

  public static String ResolvablePropertyEditDialog_msgEditProperty;

  public static String ResolvablePropertyEditDialog_lblName;

  public static String ResolvablePropertyEditDialog_lblValue;

  public static String ResolvablePropertyEditDialog_msgMissingName;

  public static String ResolvablePropertyEditDialog_msgMissingValue;

  public static String ResolvablePropertyEditDialog_titleDialog;

  public static String ResolvablePropertiesDialog_titleMessageArea;

  public static String ResolvablePropertiesDialog_msgAdditionalProperties;

  public static String ResolvablePropertiesDialog_colName;

  public static String ResolvablePropertiesDialog_colValue;

  public static String ResolvablePropertiesDialog_btnAdd;

  public static String ResolvablePropertiesDialog_btnEdit;

  public static String ResolvablePropertiesDialog_btnRemove;

  public static String ResolvablePropertiesDialog_btnFind;

  public static String ResolvablePropertiesDialog_titleDialog;

  public static String ResolvablePropertiesDialog_msgMissingPropertyValue;

  public static String ResolvablePropertiesDialog_titleRemoveConfirmation;

  public static String ResolvablePropertiesDialog_msgRemoveConfirmation;

  public static String ResolvablePropertiesDialog_titleFoundProperties;

  public static String ResolvablePropertiesDialog_msgFoundProperties;

  public static String AbstractASTResolution_msgErrorQuickfix;

  public static String errorDialogMainMessage;

  public static String errorFailedAddFileset;

  public static String errorFailedEditFileset;

  public static String errorOpeningPropertiesPage;

  public static String errorChangingFilesetEditor;

  public static String errorCannotResolveCheckLocation;

  public static String msgErrorFailedExportConfig;

  public static String CheckstyleLog_titleInternalError;

  public static String CheckstyleLog_titleWarning;

  public static String errorWhileBuildingProject;

  public static String errorWhileOpeningEditor;

  public static String errorWhileDisplayingDuplicates;

  public static String CheckstylePreferenceTransfer_name;

  public static String MarkerPropertyPage_Issue;
  
  public static String MarkerPropertyPage_Module;

  public static String MarkerPropertyPage_SuppressionHint;

  public static String MarkerPropertyPage_Group;

  public static String MarkerPropertyPage_Description;
  // CHECKSTYLE:ON
}
