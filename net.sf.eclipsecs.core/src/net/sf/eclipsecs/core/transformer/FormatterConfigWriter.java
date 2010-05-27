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

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Class for writing a new eclipse-configuration-file. Gets used by class
 * Transformer. A new eclipse-formatter-profile gets added.
 * 
 * @author Lukas Frena
 */
public class FormatterConfigWriter {
    /** A eclipse-configuration. */
    private final FormatterConfiguration mConfiguration;

    /** XML-Tag for the new profile. */
    private final String mNewProfileTag;

    /** Name of new createt profile. */
    private final String mNewProfileName;

    /**
     * Constructor to create a new instance of class FormatterConfigWriter.
     * 
     * @param settings
     *            A eclipse-configuration.
     */
    public FormatterConfigWriter(final FormatterConfiguration settings) {
        mConfiguration = settings;

        final Date date = new Date();
        mNewProfileName = "Eclipse-cs " + date.toString();
        mNewProfileTag = "<profile kind=\"CodeFormatterProfile\" name=\""
            + mNewProfileName + "\" version=\"11\">\n";
        writeSettings();
    }

    /**
     * Method for writing all settings to disc. Also activates new profile.
     */
    private void writeSettings() {
        // read the Eclipse-Preferences for manipulation
        final Preferences uiPreferences = new InstanceScope()
            .getNode("org.eclipse.jdt.ui");

        writeLocalSettings(uiPreferences, mConfiguration.getLocalSettings());
        writeGlobalSettings(uiPreferences, mConfiguration.getGlobalSettings());

        try {
            uiPreferences.flush();
        }
        catch (final BackingStoreException e) {
            Logger.writeln("unable to store new profile: " + e);
        }
    }

    /**
     * Method for writing all formatter-settings to disc.
     * 
     * @param uiPreferences
     *            Link to java-Preferences.
     * @param settings
     *            All the settings.
     */
    private void writeLocalSettings(final Preferences uiPreferences,
        final Map<String, String> settings) {
        final Preferences corePreferences = new InstanceScope()
            .getNode("org.eclipse.jdt.core");

        final Iterator<String> setIt = settings.keySet().iterator();
        String setting;

        String profiles = "";
        final String[] profile = uiPreferences.get(
            "org.eclipse.jdt.ui.formatterprofiles", "default").split(
            "<profiles version=.*>");
        profiles = profile[0] + "<profiles version=\"11\">\n" + mNewProfileTag;

        while (setIt.hasNext()) {
            setting = setIt.next();
            profiles = profiles + "<setting id=\"" + setting + "\" value=\""
                + settings.get(setting) + "\"/>\n";
            corePreferences.put(setting, settings.get(setting));
        }
        profiles = profiles + "</profile>\n";
        if (profile[1].equals("\n")) {
            profiles = profiles + "</profiles>";
        }

        try {
            corePreferences.flush();
        }
        catch (final BackingStoreException e) {
            Logger.writeln("unable to store new profile: " + e);
        }

        for (int i = 1; i < profile.length; i++) {
            profiles = profiles + profile[i];
        }

        uiPreferences.put("org.eclipse.jdt.ui.formatterprofiles", profiles);
    }

    /**
     * Method for writing all editor-settings to disc.
     * 
     * @param uiPreferences
     *            Link to java-Preferences.
     * @param settings
     *            All the settings.
     */
    private void writeGlobalSettings(final Preferences uiPreferences,
        final Map<String, String> settings) {
        final Iterator<String> setIt = settings.keySet().iterator();
        String setting;

        while (setIt.hasNext()) {
            setting = setIt.next();
            uiPreferences.put(setting, settings.get(setting));
        }
        uiPreferences.put("formatter_profile", "_" + mNewProfileName);
        uiPreferences.put("formatter_settings_version", "11");
    }
}
