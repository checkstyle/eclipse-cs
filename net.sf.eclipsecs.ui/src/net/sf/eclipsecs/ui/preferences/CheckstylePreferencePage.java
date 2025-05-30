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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.puppycrawl.tools.checkstyle.Main;

import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.builder.CheckerFactory;
import net.sf.eclipsecs.core.builder.CheckstyleBuilder;
import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor;
import net.sf.eclipsecs.ui.util.InternalBrowser;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * This class represents a preference page that is contributed to the Preferences dialog.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * </p>
 */
public class CheckstylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private static final String DEFAULT_LANGUAGE = "default";

  private static final List<String> SUPPORTED_LANGUAGES = Stream.concat(
          Stream.of(DEFAULT_LANGUAGE),
          Arrays.stream("de,en,es,fi,fr,ja,pt,tr,zh"
          .split(","))
          .sorted())
          .collect(Collectors.toList());

  private final PageController mController = new PageController();

  private final ICheckConfigurationWorkingSet mWorkingSet;

  private Combo mRebuildIfNeeded;

  private Button mPurgeCacheButton;

  private Button mWarnBeforeLosingFilesets;

  private Button mIncludeRuleNamesButton;

  private Button mIncludeModuleIdButton;

  private Button mLimitCheckstyleMarkers;

  private Combo mLanguageIf;

  private Text mTxtMarkerLimit;

  private Button mBackgroundFullBuild;

  private CheckConfigurationWorkingSetEditor mWorkingSetEditor;

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
    final Composite generalComposite = createGeneralContents(parentComposite);
    FormData formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    generalComposite.setLayoutData(formData);

    //
    // Create the check configuration section of the screen.
    //
    final Composite configComposite = createCheckConfigContents(parentComposite);
    formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(generalComposite, 3, SWT.BOTTOM);
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
   * Create the area with the general preference settings.
   *
   * @param parent
   *          the parent composite
   * @return the general area
   */
  private Composite createGeneralContents(Composite parent) {
    //
    // Build the composite for the general settings.
    //
    Group generalComposite = new Group(parent, SWT.NULL);
    generalComposite.setText(Messages.CheckstylePreferencePage_lblGeneralSettings);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    generalComposite.setLayout(gridLayout);

    final Composite langComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    langComposite.setLayout(gridLayout);
    langComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    final Label lblLanguage = new Label(langComposite, SWT.NULL);
    lblLanguage.setText(Messages.CheckstylePreferencePage_lblLocaleLanguage);
    mLanguageIf = new Combo(langComposite, SWT.READ_ONLY);
    mLanguageIf.setItems(SUPPORTED_LANGUAGES.stream().map(code -> {
      if (code == DEFAULT_LANGUAGE) {
        return code;
      }
      var loc = Locale.forLanguageTag(code);
      return code + " - " + loc.getDisplayLanguage(loc);
    }).toArray(String[]::new));
    final String lang = CheckstylePluginPrefs.getString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE);
    final int selectedLang = SUPPORTED_LANGUAGES.indexOf(lang == null || lang.isEmpty() ? DEFAULT_LANGUAGE : lang);
    if (selectedLang != -1) {
      mLanguageIf.select(selectedLang);
    }

    //
    // Create a combo with the rebuild options
    //
    final Composite rebuildComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    rebuildComposite.setLayout(gridLayout);
    rebuildComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label lblRebuild = new Label(rebuildComposite, SWT.NULL);
    lblRebuild.setText(Messages.CheckstylePreferencePage_lblRebuild);

    mRebuildIfNeeded = new Combo(rebuildComposite, SWT.READ_ONLY);
    mRebuildIfNeeded.setItems(new String[] { MessageDialogWithToggle.PROMPT,
        MessageDialogWithToggle.ALWAYS, MessageDialogWithToggle.NEVER });
    mRebuildIfNeeded.select(mRebuildIfNeeded.indexOf(
            CheckstyleUIPluginPrefs.getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD)));

    //
    // Create button to purge the checker cache
    //

    mPurgeCacheButton = new Button(rebuildComposite, SWT.FLAT);
    mPurgeCacheButton
            .setImage(CheckstyleUIPluginImages.REFRESH_ICON.getImage());
    mPurgeCacheButton.setToolTipText(Messages.CheckstylePreferencePage_btnRefreshCheckerCache);
    mPurgeCacheButton.addSelectionListener(mController);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.END;
    gridData.grabExcessHorizontalSpace = true;
    gridData.heightHint = 20;
    gridData.widthHint = 20;
    mPurgeCacheButton.setLayoutData(gridData);

    //
    // Create the "Fileset warning" check box.
    //
    mWarnBeforeLosingFilesets = new Button(generalComposite, SWT.CHECK);
    mWarnBeforeLosingFilesets.setText(Messages.CheckstylePreferencePage_lblWarnFilesets);
    mWarnBeforeLosingFilesets.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING));

    //
    // Create the "Include rule name" check box.
    //
    final Composite includeRuleNamesComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    includeRuleNamesComposite.setLayout(gridLayout);

    mIncludeRuleNamesButton = new Button(includeRuleNamesComposite, SWT.CHECK);
    mIncludeRuleNamesButton.setText(Messages.CheckstylePreferencePage_lblIncludeRulenames);
    mIncludeRuleNamesButton.setSelection(
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES));

    Label lblRebuildNote = new Label(includeRuleNamesComposite, SWT.NULL);
    lblRebuildNote.setImage(CheckstyleUIPluginImages.HELP_ICON.getImage());
    lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
    SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

    //
    // Create the "Include rule name" check box.
    //
    final Composite includeModuleIdComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    includeModuleIdComposite.setLayout(gridLayout);

    mIncludeModuleIdButton = new Button(includeModuleIdComposite, SWT.CHECK);
    mIncludeModuleIdButton.setText(Messages.CheckstylePreferencePage_lblIncludeModuleIds);
    mIncludeModuleIdButton.setSelection(
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS));

    lblRebuildNote = new Label(includeModuleIdComposite, SWT.NULL);
    lblRebuildNote.setImage(CheckstyleUIPluginImages.HELP_ICON.getImage());
    lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
    SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

    //
    // Create the "limit markers" check box and text field combination
    //
    final  Composite limitMarkersComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    limitMarkersComposite.setLayout(gridLayout);

    mLimitCheckstyleMarkers = new Button(limitMarkersComposite, SWT.CHECK);
    mLimitCheckstyleMarkers.setText(Messages.CheckstylePreferencePage_lblLimitMarker);
    mLimitCheckstyleMarkers.setSelection(CheckstylePluginPrefs
            .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE));

    mTxtMarkerLimit = new Text(limitMarkersComposite, SWT.SINGLE | SWT.BORDER);
    mTxtMarkerLimit.setTextLimit(5);
    SWTUtil.addOnlyDigitInputSupport(mTxtMarkerLimit);

    mTxtMarkerLimit.setText(Integer.toString(
            CheckstylePluginPrefs.getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT)));
    gridData = new GridData();
    gridData.widthHint = 30;
    mTxtMarkerLimit.setLayoutData(gridData);

    lblRebuildNote = new Label(limitMarkersComposite, SWT.NULL);
    lblRebuildNote.setImage(CheckstyleUIPluginImages.HELP_ICON.getImage());
    lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
    SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

    //
    // Create the "Run Checkstyle in background on full builds" check box.
    //
    final Composite backgroundFullBuildComposite = new Composite(generalComposite, SWT.NULL);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    backgroundFullBuildComposite.setLayout(gridLayout);

    mBackgroundFullBuild = new Button(backgroundFullBuildComposite, SWT.CHECK);
    mBackgroundFullBuild.setText(Messages.CheckstylePreferencePage_txtBackgroundFullBuild0);
    mBackgroundFullBuild.setSelection(
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD));
    return generalComposite;
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

    mWorkingSetEditor = new CheckConfigurationWorkingSetEditor(mWorkingSet, true);
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
              SUPPORTED_LANGUAGES.get(mLanguageIf.getSelectionIndex()));

      //
      // Save the general preferences.
      //
      CheckstyleUIPluginPrefs.setString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD,
              mRebuildIfNeeded.getItem(mRebuildIfNeeded.getSelectionIndex()));

      //
      // fileset warning preference
      //
      boolean warnFileSetsNow = mWarnBeforeLosingFilesets.getSelection();
      CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING,
              warnFileSetsNow);

      //
      // Include rule names preference.
      //
      boolean includeRuleNamesNow = mIncludeRuleNamesButton.getSelection();
      boolean includeRuleNamesOriginal = CheckstylePluginPrefs
              .getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES);
      CheckstylePluginPrefs.setBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES,
              includeRuleNamesNow);

      //
      // Include module id preference.
      //
      boolean includeModuleIdNow = mIncludeModuleIdButton.getSelection();
      boolean includeModuleIdOriginal = CheckstylePluginPrefs
              .getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS);
      CheckstylePluginPrefs.setBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS,
              includeModuleIdNow);

      //
      // Limit markers preference
      //

      boolean limitMarkersNow = mLimitCheckstyleMarkers.getSelection();
      boolean limitMarkersOriginal = CheckstylePluginPrefs
              .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE);
      CheckstylePluginPrefs.setBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE,
              limitMarkersNow);

      int markerLimitNow = Integer.parseInt(mTxtMarkerLimit.getText());
      int markerLimitOriginal = CheckstylePluginPrefs
              .getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT);
      CheckstylePluginPrefs.setInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT, markerLimitNow);

      //
      // Include background build preference.
      //
      boolean runInBackgroundNow = mBackgroundFullBuild.getSelection();
      CheckstylePluginPrefs.setBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD,
              runInBackgroundNow);

      // See if all projects need rebuild
      boolean needRebuildAllProjects = (includeRuleNamesNow != includeRuleNamesOriginal)
              || (includeModuleIdNow != includeModuleIdOriginal)
              || (limitMarkersNow != limitMarkersOriginal)
              || (markerLimitNow != markerLimitOriginal) || mRebuildAll;

      // Get projects that need rebuild considering the changes
      Collection<IProject> projectsToBuild = mWorkingSet.getAffectedProjects();

      String promptRebuildPref = CheckstyleUIPluginPrefs
              .getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

      boolean rebuild = MessageDialogWithToggle.ALWAYS.equals(promptRebuildPref)
              && (needRebuildAllProjects || !projectsToBuild.isEmpty());

      //
      // Prompt for rebuild
      //
      if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref)
              && (needRebuildAllProjects || !projectsToBuild.isEmpty())) {

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
    } catch (CheckstylePluginException | BackingStoreException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(),
              NLS.bind(Messages.errorFailedSavePreferences, ex.getLocalizedMessage()), ex, true);
    }

    return true;
  }

  /**
   * Controller for this page.
   *
   */
  private class PageController extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (mPurgeCacheButton == e.widget) {
        CheckerFactory.cleanup();
        mRebuildAll = true;
      }
    }
  }
}
