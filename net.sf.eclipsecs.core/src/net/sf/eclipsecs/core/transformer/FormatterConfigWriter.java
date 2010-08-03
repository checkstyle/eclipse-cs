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
import org.eclipse.jdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.jdt.ui.JavaUI;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Class for writing a new eclipse-configuration-file. Gets used by class
 * Transformer. A new eclipse-formatter-profile gets added.
 * 
 * @author Lukas Frena
 * @author Lars Koedderitzsch
 */
@SuppressWarnings("restriction")
public class FormatterConfigWriter {
    /** A eclipse-configuration. */
    private final FormatterConfiguration mConfiguration;

    /** Name of new createt profile. */
    private final String mNewProfileName;

    private IProject mProject;

    /**
     * Constructor to create a new instance of class FormatterConfigWriter.
     * 
     * @param settings
     *            A eclipse-configuration.
     */
    public FormatterConfigWriter(IProject project,
        final FormatterConfiguration settings) {
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

        writeLocalSettings(mConfiguration.getLocalSettings());
    }

    /**
     * Method for writing all formatter-settings to disc.
     * 
     * @param settings
     *            All the settings.
     */
    private void writeLocalSettings(final Map<String, String> settings) {

        PreferencesAccess access = PreferencesAccess.getOriginalPreferences();

        IScopeContext instanceScope = access.getInstanceScope();
        IScopeContext scope = access.getProjectScope(mProject);

        ProfileVersioner versioner = new ProfileVersioner();

        FormatterProfileStore profilesStore = new FormatterProfileStore(
            versioner);
        try {

            @SuppressWarnings("unchecked")
            List<Profile> profiles = profilesStore.readProfiles(instanceScope);

            if (profiles == null) {
                profiles = new ArrayList<Profile>();
            }

            ProfileManager manager = new FormatterProfileManager(profiles,
                scope, access, versioner);

            CustomProfile myProfile = (CustomProfile) manager
                .getProfile(ProfileManager.ID_PREFIX + mNewProfileName);

            if (myProfile == null) {
                // take current settings and create new profile
                Profile current = manager.getSelected();
                myProfile = new CustomProfile(mNewProfileName, current
                    .getSettings(), versioner.getCurrentVersion(), versioner
                    .getProfileKind());
                manager.addProfile(myProfile);
            }

            @SuppressWarnings("unchecked")
            Map<String, String> joinedSettings = myProfile.getSettings();
            joinedSettings.putAll(settings);

            myProfile.setSettings(joinedSettings);
            manager.setSelected(myProfile);

            // writes profiles to the workspace profile store
            profilesStore.writeProfiles(manager.getSortedProfiles(),
                instanceScope);

            // commits changes to the project profile settings
            manager.commitChanges(scope);

            scope.getNode(JavaUI.ID_PLUGIN).flush();
            scope.getNode(JavaCore.PLUGIN_ID).flush();
            if (scope != instanceScope) {
                instanceScope.getNode(JavaUI.ID_PLUGIN).flush();
                instanceScope.getNode(JavaCore.PLUGIN_ID).flush();
            }

        }
        catch (CoreException e) {
            CheckstyleLog.log(e, "Error storing formatter profile");
        }
        catch (BackingStoreException e) {
            CheckstyleLog.log(e, "Error storing formatter profile");
        }
    }
}
