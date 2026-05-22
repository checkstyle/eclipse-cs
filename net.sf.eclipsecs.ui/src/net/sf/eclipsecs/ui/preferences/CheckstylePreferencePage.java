//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.ui.preferences;

import java.util.Collection;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.puppycrawl.tools.checkstyle.Main;

import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.builder.CheckstyleBuilder;
import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor;
import net.sf.eclipsecs.ui.util.InternalBrowser;

/**
 * This class represents a preference page that is contributed to the Preferences dialog.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * </p>
 */
public class CheckstylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private final ICheckConfigurationWorkingSet mWorkingSet;
  private CheckstylePreferencePageGeneralSettings generalSettings;
  private boolean mRebuildAll;

  /**
   * Constructor.
   */
  public CheckstylePreferencePage() {
    setPreferenceStore(CheckstyleUIPlugin.getDefault().getPreferenceStore());

    mWorkingSet = CheckConfigurationFactory.newWorkingSet();
    noDefaultAndApplyButton();
  }

  private String getCheckstyleVersion() {
    return Main.class.getPackage().getImplementationVersion();
  }

  @Override
  public Control createContents(Composite ancestor) {
    // the description with hyperlinks at the top
    Composite oneRowComposite = new Composite(ancestor, SWT.NULL);
    var oneRowLayout = RowLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).type(SWT.VERTICAL).create();
    oneRowComposite.setLayout(oneRowLayout);
    createTitleWithLink(oneRowComposite);

    //
    // Build the top level composite with one column.
    //
    Composite parentComposite = new Composite(ancestor, SWT.NULL);
    FormLayout layout = new FormLayout();
    parentComposite.setLayout(layout);

    //
    // Create the general section of the screen.
    //
    this.generalSettings = new CheckstylePreferencePageGeneralSettings(parentComposite, SWT.NONE, () -> mRebuildAll = true);
    FormData formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    generalSettings.setLayoutData(formData);

    //
    // Create the check configuration section of the screen.
    //
    final Composite configComposite = createCheckConfigContents(parentComposite);
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(generalSettings, 3, SWT.BOTTOM);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(100);
    configComposite.setLayoutData(formData);

    return parentComposite;
  }

  private void createTitleWithLink(Composite parent) {
    Link link = new Link(parent, SWT.NONE);
    var text = NLS.bind(Messages.CheckstylePreferencePage_version, "<a>" + getCheckstyleVersion() + "</a>");
    text = text.replace("Checkstyle", "<a>Checkstyle</a>");
    link.setText(text);
    link.addListener(SWT.Selection, this::linkClicked);
  }

  private void linkClicked(Event event) {
    String url = "https://checkstyle.org";
    if (Character.isDigit(event.text.charAt(0))) {
      url = url + "/releasenotes.html#Release_" + getCheckstyleVersion();
    }
    InternalBrowser.openLinkInExternalBrowser(url);
  }

  /**
   * Creates the content regarding the management of check configurations.
   *
   * @param parent
   *          the parent composite
   * @return the configuration area
   */
  private Composite createCheckConfigContents(Composite parent) {
    //
    // Create the composite for configuring check configurations.
    //
    Group configComposite = new Group(parent, SWT.NULL);
    configComposite.setText(Messages.CheckstylePreferencePage_titleCheckConfigs);
    configComposite.setLayout(new FormLayout());

    CheckConfigurationWorkingSetEditor mWorkingSetEditor = new CheckConfigurationWorkingSetEditor(
            mWorkingSet, true);
    final Control editorControl = mWorkingSetEditor.createContents(configComposite);
    FormData formData = new FormData();
    formData.left = new FormAttachment(0, 3);
    formData.top = new FormAttachment(0, 3);
    formData.right = new FormAttachment(100, -3);
    formData.bottom = new FormAttachment(100, -3);
    editorControl.setLayoutData(formData);

    return configComposite;
  }

  @Override
  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    try {
      //
      // Save the check configurations.
      //
      mWorkingSet.store();

      CheckstylePluginPrefs.setString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE,
              generalSettings.getLanguageIf());

      //
      // Save the general preferences.
      //
      CheckstyleUIPluginPrefs.setString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD,
              generalSettings.getRebuildIfNeeded());

      //
      // fileset warning preference
      //
      boolean warnFileSetsNow = generalSettings.getWarnBeforeLosingFilesets();
      CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING,
              warnFileSetsNow);

      //
      // Include rule names preference.
      //
      boolean includeRuleNamesHasChanged = updateBooleanPreference(
              generalSettings.getIncludeRuleNames(), CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES);

      //
      // Include module id preference.
      //
      boolean includeModuleIdHasChanged = updateBooleanPreference(
              generalSettings.getIncludeModuleIdButton(),
              CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS);

      //
      // Limit markers preference
      //

      boolean limitMarkersHasChanged = updateBooleanPreference(
              generalSettings.getLimitCheckstyleMarkers(),
              CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE);

      int markerLimitNow = Integer.parseInt(generalSettings.getTxtMarkerLimit());
      int markerLimitOriginal = CheckstylePluginPrefs
              .getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT);
      CheckstylePluginPrefs.setInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT, markerLimitNow);
      boolean markerLimitHasChanged = markerLimitNow != markerLimitOriginal;

      //
      // Include background build preference.
      //
      boolean runInBackgroundNow = generalSettings.getBackgroundFullBuild();
      CheckstylePluginPrefs.setBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD,
              runInBackgroundNow);

      // See if all projects need rebuild
      boolean needRebuildAllProjects = needRebuildAllProjects(includeRuleNamesHasChanged,
              includeModuleIdHasChanged, limitMarkersHasChanged, markerLimitHasChanged);

      // Get projects that need rebuild considering the changes
      Collection<IProject> projectsToBuild = mWorkingSet.getAffectedProjects();

      if (needRebuildAllProjects || !projectsToBuild.isEmpty()) {
        String promptRebuildPref = CheckstyleUIPluginPrefs
                .getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

        boolean rebuild = MessageDialogWithToggle.ALWAYS.equals(promptRebuildPref);

        //
        // Prompt for rebuild
        //
        if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref)) {

          MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getShell(),
                  Messages.CheckstylePreferencePage_titleRebuild,
                  Messages.CheckstylePreferencePage_msgRebuild,
                  Messages.CheckstylePreferencePage_nagRebuild, false,
                  CheckstyleUIPlugin.getDefault().getPreferenceStore(),
                  CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

          rebuild = dialog.getReturnCode() == IDialogConstants.YES_ID;
        }

        if (rebuild) {
          try {
            if (needRebuildAllProjects) {
              CheckstyleBuilder.buildAllProjects();
            } else {
              CheckstyleBuilder.buildProjects(projectsToBuild);
            }

          } catch (CheckstylePluginException ex) {
            CheckstyleUIPlugin.errorDialog(getShell(),
                    NLS.bind(Messages.errorFailedRebuild, ex.getMessage()), ex, true);
          }
        }
      }
    } catch (CheckstylePluginException | BackingStoreException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(),
              NLS.bind(Messages.errorFailedSavePreferences, ex.getLocalizedMessage()), ex, true);
    }

    return true;
  }

  private static final boolean updateBooleanPreference(boolean selection, String preference)
          throws BackingStoreException {
    boolean original = CheckstylePluginPrefs.getBoolean(preference);
    CheckstylePluginPrefs.setBoolean(preference, selection);
    return selection != original;
  }

  private boolean needRebuildAllProjects(boolean includeRuleNamesHasChanged,
          boolean includeModuleIdHasChanged, boolean limitMarkersHasChanged,
          boolean markerLimitHasChanged) {
    return includeRuleNamesHasChanged || includeModuleIdHasChanged || limitMarkersHasChanged
            || markerLimitHasChanged || mRebuildAll;
  }
}
