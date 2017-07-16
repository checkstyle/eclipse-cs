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

package net.sf.eclipsecs.core;

import org.eclipse.osgi.util.NLS;

/**
 * Class providing messages for the checkstyle plugin. Uses the eclipse new nls mechanism.
 *
 * @author Lars Ködderitzsch
 */
public final class Messages extends NLS {
  // CHECKSTYLE:OFF

  private static final String BUNDLE_NAME = "net.sf.eclipsecs.core.messages"; //$NON-NLS-1$

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }

  public static String Auditor_msgCheckingConfig;

  public static String Auditor_msgCheckingFile;

  public static String Auditor_txtUnknownModule;

  public static String Auditor_msgMsgCheckstyleInternalError;

  public static String AuditorJob_msgBuildProject;

  public static String BuildProjectJob_msgBuildAllProjects;

  public static String BuildProjectJob_msgBuildProject;

  public static String CheckstyleBuilder_msgWrongBuilderOrder;

  public static String ConfigPropertyMetadata_txtNoDescription;

  public static String ConfigureDeconfigureNatureJob_msgTaksAddingNature;

  public static String RunCheckstyleOnFilesJob_title;

  public static String SimpleFileSetsEditor_nameAllFileset;

  public static String ProjectConfigurationType_msgFileNotFound;

  public static String errorEmptyPattern;

  public static String errorUnknownFileFormat;

  public static String errorWritingCheckConfigurations;

  public static String errorNoRootModule;

  public static String errorMoreThanOneRootModule;

  public static String errorLoadingConfigFile;

  public static String errorWritingConfigFile;

  public static String msgErrorLoadingCheckstyleDTD;

  public static String errorLocationEmpty;

  public static String errorResolveConfigLocation;

  public static String errorConfigNameEmpty;

  public static String errorConfigNameInUse;

  public static String errorUnknownClasspathEntry;

  public static String errorNoCheckConfig;

  public static String CheckstyleLog_msgStatusPrefix;

  public static String errorFilesetWithoutCheckConfig;

  public static String CheckstyleBuilder_msgErrorUnknown;

  public static String RemoteConfigurationType_errorFileNotFound;

  public static String RemoteConfigurationType_errorUnknownHost;

  public static String RemoteConfigurationType_msgRemoteCachingFailed;

  public static String RemoteConfigurationType_msgUnAuthorized;

  // CHECKSTYLE:ON
}
