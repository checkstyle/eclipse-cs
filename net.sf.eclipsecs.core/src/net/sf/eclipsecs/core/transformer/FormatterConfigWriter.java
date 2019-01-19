//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.util.CheckstyleLog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;
import org.eclipse.jdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.jdt.internal.ui.preferences.cleanup.CleanUpProfileManager;
import org.eclipse.jdt.internal.ui.preferences.cleanup.CleanUpProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileVersioner;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Class for writing a new eclipse-configuration-file. Gets used by class Transformer. A new
 * eclipse-formatter-profile gets added.
 *
 * @author Lukas Frena
 * @author Lars Ködderitzsch
 */
@SuppressWarnings("restriction")
public class FormatterConfigWriter {

  private static final String JDT_UI_PLUGINID = "org.eclipse.jdt.ui";

  /** A eclipse-configuration. */
  private final FormatterConfiguration mConfiguration;

  /** Name of new createt profile. */
  private final String mNewProfileName;

  private IProject mProject;

  /**
   * Constructor to create a new instance of class FormatterConfigWriter.
   *
   * @param project
   *          the project whose formatter settings should be written
   * @param settings
   *          A eclipse-configuration.
   */
  public FormatterConfigWriter(IProject project, final FormatterConfiguration settings) {
    mConfiguration = settings;
    mProject = project;

    mNewProfileName = "eclipse-cs " + mProject.getName();
    writeSettings();
  }

  /**
   * Method for writing all settings to disc. Also activates new profile.
   */
  private void writeSettings() {
    // read the Eclipse-Preferences for manipulation
    writeCleanupSettings(mConfiguration.getCleanupSettings());
    writeFormatterSettings(mConfiguration.getFormatterSettings());
  }

  private void writeCleanupSettings(final Map<String, String> settings) {

    PreferencesAccess access = PreferencesAccess.getOriginalPreferences();

    IScopeContext instanceScope = access.getInstanceScope();
    IScopeContext scope = access.getProjectScope(mProject);

    IProfileVersioner versioner = new CleanUpProfileVersioner();
    ProfileStore profilesStore = new ProfileStore(CleanUpConstants.CLEANUP_PROFILES, versioner);
    try {

      List<Profile> profiles = profilesStore.readProfiles(instanceScope);

      if (profiles == null) {
        profiles = new ArrayList<>();
      }
      profiles.addAll(CleanUpPreferenceUtil.getBuiltInProfiles());

      ProfileManager manager = new CleanUpProfileManager(profiles, scope, access, versioner);

      CustomProfile myProfile = (CustomProfile) manager
              .getProfile(ProfileManager.ID_PREFIX + mNewProfileName);

      if (myProfile == null) {
        // take current settings and create new profile
        Profile current = manager.getSelected();
        myProfile = new CustomProfile(mNewProfileName, current.getSettings(),
                versioner.getCurrentVersion(), versioner.getProfileKind());
        manager.addProfile(myProfile);
      }

      Map<String, String> joinedSettings = myProfile.getSettings();
      joinedSettings.putAll(settings);

      myProfile.setSettings(joinedSettings);
      manager.setSelected(myProfile);

      // writes profiles to the workspace profile store
      profilesStore.writeProfiles(manager.getSortedProfiles(), instanceScope);

      // commits changes to the project profile settings
      manager.commitChanges(scope);

      scope.getNode(JDT_UI_PLUGINID).flush();
      scope.getNode(JavaCore.PLUGIN_ID).flush();
      if (scope != instanceScope) {
        instanceScope.getNode(JDT_UI_PLUGINID).flush();
        instanceScope.getNode(JavaCore.PLUGIN_ID).flush();
      }

    } catch (CoreException e) {
      CheckstyleLog.log(e, "Error storing cleanup profile");
    } catch (BackingStoreException e) {
      CheckstyleLog.log(e, "Error storing cleanup profile");
    }
  }

  /**
   * Method for writing all formatter-settings to disc.
   *
   * @param settings
   *          All the settings.
   */
  private void writeFormatterSettings(final Map<String, String> settings) {

    PreferencesAccess access = PreferencesAccess.getOriginalPreferences();

    IScopeContext instanceScope = access.getInstanceScope();
    IScopeContext scope = access.getProjectScope(mProject);

    IProfileVersioner versioner = new ProfileVersioner();
    ProfileStore profilesStore = new FormatterProfileStore(versioner);
    try {

      List<Profile> profiles = profilesStore.readProfiles(instanceScope);

      if (profiles == null) {
        profiles = new ArrayList<>();
      }

      ProfileManager manager = new FormatterProfileManager(profiles, scope, access, versioner);

      CustomProfile myProfile = (CustomProfile) manager
              .getProfile(ProfileManager.ID_PREFIX + mNewProfileName);

      if (myProfile == null) {
        // take current settings and create new profile
        Profile current = manager.getSelected();
        myProfile = new CustomProfile(mNewProfileName, current.getSettings(),
                versioner.getCurrentVersion(), versioner.getProfileKind());
        manager.addProfile(myProfile);
      }

      Map<String, String> joinedSettings = myProfile.getSettings();
      joinedSettings.putAll(settings);

      myProfile.setSettings(joinedSettings);
      manager.setSelected(myProfile);

      // writes profiles to the workspace profile store
      profilesStore.writeProfiles(manager.getSortedProfiles(), instanceScope);

      // commits changes to the project profile settings
      manager.commitChanges(scope);

      scope.getNode(JDT_UI_PLUGINID).flush();
      scope.getNode(JavaCore.PLUGIN_ID).flush();
      if (scope != instanceScope) {
        instanceScope.getNode(JDT_UI_PLUGINID).flush();
        instanceScope.getNode(JavaCore.PLUGIN_ID).flush();
      }

    } catch (CoreException e) {
      CheckstyleLog.log(e, "Error storing formatter profile");
    } catch (BackingStoreException e) {
      CheckstyleLog.log(e, "Error storing formatter profile");
    }
  }
}
