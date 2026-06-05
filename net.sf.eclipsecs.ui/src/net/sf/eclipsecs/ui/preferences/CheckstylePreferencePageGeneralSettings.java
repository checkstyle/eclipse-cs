//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.builder.CheckerFactory;
import net.sf.eclipsecs.ui.CheckstyleUiPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUiPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.SwtUtil;

public final class CheckstylePreferencePageGeneralSettings extends Composite {

  private static final String DEFAULT_LANGUAGE = "default";

  private static final List<String> SUPPORTED_LANGUAGES = List.of(DEFAULT_LANGUAGE, "de", "en", "es",
          "fi", "fr", "ja", "pt", "tr", "zh");
  private static final String[] LANGUAGE_ITEMS = SUPPORTED_LANGUAGES.stream().map(code -> {
    String displayLang;
    if (code == DEFAULT_LANGUAGE) {
      displayLang = code;
    } else {
      var loc = Locale.forLanguageTag(code);
      displayLang = code + " - " + loc.getDisplayLanguage(loc);
    }
    return displayLang;
  }).toArray(String[]::new);

  private final Combo languageIf;
  private final Combo mRebuildIfNeeded;
  private final Button mWarnBeforeLosingFilesets;
  private final Button mIncludeRuleNamesButton;
  private final Button mIncludeModuleIdButton;
  private final Button mLimitCheckstyleMarkers;
  private final Text mTxtMarkerLimit;
  private final Button mBackgroundFullBuild;

  public CheckstylePreferencePageGeneralSettings(Composite parent, int style, Runnable setRebuildAll) {
    super(parent, style);
    setLayout(new FillLayout());

    Group group = new Group(this, style);

    group.setText(Messages.CheckstylePreferencePage_lblGeneralSettings);
    GridLayoutFactory.swtDefaults().applyTo(group);

    languageIf = createLanguageSetting(group);
    mRebuildIfNeeded = createRebuildSection(group, setRebuildAll);

    //
    // Create the "Fileset warning" check box.
    //
    mWarnBeforeLosingFilesets = makeButton(group, SWT.CHECK,
            Messages.CheckstylePreferencePage_lblWarnFilesets,
            CheckstyleUiPluginPrefs.getBoolean(CheckstyleUiPluginPrefs.PREF_FILESET_WARNING));

    //
    // Create the "Include rule name" check box.
    //
    mIncludeRuleNamesButton = makeCheckboxWithRebuildNoteLabel(group,
            Messages.CheckstylePreferencePage_lblIncludeRulenames,
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_RULE_NAMES));

    //
    // Create the "Include rule name" check box.
    //
    mIncludeModuleIdButton = makeCheckboxWithRebuildNoteLabel(group,
            Messages.CheckstylePreferencePage_lblIncludeModuleIds,
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_INCLUDE_MODULE_IDS));

    //
    // Create the "limit markers" check box and text field combination
    //
    final Composite limitMarkersComposite = new Composite(group, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(limitMarkersComposite);

    mLimitCheckstyleMarkers = makeButton(limitMarkersComposite, SWT.CHECK,
            Messages.CheckstylePreferencePage_lblLimitMarker, CheckstylePluginPrefs
                    .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE));

    mTxtMarkerLimit = new Text(limitMarkersComposite, SWT.SINGLE | SWT.BORDER);
    mTxtMarkerLimit.setTextLimit(5);
    SwtUtil.addOnlyDigitInputSupport(mTxtMarkerLimit);

    mTxtMarkerLimit.setText(Integer.toString(
            CheckstylePluginPrefs.getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT)));
    GridDataFactory.swtDefaults().hint(30, SWT.DEFAULT).applyTo(mTxtMarkerLimit);

    addRebuildNoteLabel(limitMarkersComposite);

    //
    // Create the "Run Checkstyle in background on full builds" check box.
    //
    final Composite backgroundFullBuildComposite = new Composite(group, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(backgroundFullBuildComposite);

    mBackgroundFullBuild = makeButton(backgroundFullBuildComposite, SWT.CHECK,
            Messages.CheckstylePreferencePage_txtBackgroundFullBuild0,
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD));
  }

  private static Combo createRebuildSection(Group group, Runnable setRebuildAll) {
    final Composite rebuildComposite = new Composite(group, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(rebuildComposite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(rebuildComposite);

    Label lblRebuild = new Label(rebuildComposite, SWT.NULL);
    lblRebuild.setText(Messages.CheckstylePreferencePage_lblRebuild);

    Combo mRebuildIfNeeded = new Combo(rebuildComposite, SWT.READ_ONLY);
    mRebuildIfNeeded.setItems(new String[] {
        MessageDialogWithToggle.PROMPT,
        MessageDialogWithToggle.ALWAYS,
        MessageDialogWithToggle.NEVER,
    });
    mRebuildIfNeeded.select(mRebuildIfNeeded.indexOf(
            CheckstyleUiPluginPrefs.getString(CheckstyleUiPluginPrefs.PREF_ASK_BEFORE_REBUILD)));

    //
    // Create button to purge the checker cache
    //

    Button mPurgeCacheButton = new Button(rebuildComposite, SWT.FLAT);
    mPurgeCacheButton
            .setImage(CheckstyleUiPluginImages.REFRESH_ICON.getImage());
    mPurgeCacheButton.setToolTipText(Messages.CheckstylePreferencePage_btnRefreshCheckerCache);
    mPurgeCacheButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      CheckerFactory.cleanup();
      setRebuildAll.run();
    }));
    GridDataFactory.swtDefaults().align(GridData.END, GridData.CENTER).grab(true, false)
            .hint(20, 20).applyTo(mPurgeCacheButton);

    return mRebuildIfNeeded;
  }

  private static Combo createLanguageSetting(Group group) {
    Composite langComposite = new Composite(group, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(langComposite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(langComposite);

    final Label lblLanguage = new Label(langComposite, SWT.NULL);
    lblLanguage.setText(Messages.CheckstylePreferencePage_lblLocaleLanguage);
    Combo languageIf = new Combo(langComposite, SWT.READ_ONLY);
    languageIf.setItems(LANGUAGE_ITEMS);
    final String lang = CheckstylePluginPrefs.getString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE);
    final int selectedLang = SUPPORTED_LANGUAGES.indexOf(lang == null || lang.isEmpty() ? DEFAULT_LANGUAGE : lang);
    if (selectedLang != -1) {
      languageIf.select(selectedLang);
    }
    return languageIf;
  }

  private static Button makeCheckboxWithRebuildNoteLabel(Group group, String text,
          boolean selection) {
    Composite composite = new Composite(group, SWT.NULL);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(composite);
    Button button = makeButton(composite, SWT.CHECK, text, selection);
    addRebuildNoteLabel(composite);
    return button;
  }

  private static Button makeButton(Composite parent, int style, String text, boolean selection) {
    Button button = new Button(parent, style);
    button.setText(text);
    button.setSelection(selection);
    return button;
  }

  private static void addRebuildNoteLabel(Composite parent) {
    Label lblRebuildNote = new Label(parent, SWT.NULL);
    lblRebuildNote.setImage(CheckstyleUiPluginImages.HELP_ICON.getImage());
    lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
    SwtUtil.addTooltipOnPressSupport(lblRebuildNote);
  }

  public String getLanguageIf() {
    return SUPPORTED_LANGUAGES.get(languageIf.getSelectionIndex());
  }

  public String getRebuildIfNeeded() {
    return mRebuildIfNeeded.getItem(mRebuildIfNeeded.getSelectionIndex());
  }

  public boolean getWarnBeforeLosingFilesets() {
    return mWarnBeforeLosingFilesets.getSelection();
  }

  public boolean getIncludeRuleNames() {
    return mIncludeRuleNamesButton.getSelection();
  }

  public boolean getIncludeModuleIdButton() {
    return mIncludeModuleIdButton.getSelection();
  }

  public boolean getLimitCheckstyleMarkers() {
    return mLimitCheckstyleMarkers.getSelection();
  }

  public String getTxtMarkerLimit() {
    return mTxtMarkerLimit.getText();
  }

  public boolean getBackgroundFullBuild() {
    return mBackgroundFullBuild.getSelection();
  }
}
