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
import org.eclipse.jface.layout.RowLayoutFactory;
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
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Composite containing the general settings section of the Checkstyle preference page,
 * e.g. language, rebuild behavior and marker limit.
 *
 */
public final class CheckstylePreferencePageGeneralSettings extends Composite {

    /** The default language code. */
    private static final String DEFAULT_LANGUAGE = "default";
    /** The list of supported language codes. */
    private static final List<String> SUPPORTED_LANGUAGES =
        List.of(DEFAULT_LANGUAGE, "de", "en", "es", "fi", "fr", "ja", "pt", "tr", "zh");
    /** The display items for the language combo. */
    private static final String[] LANGUAGE_ITEMS = SUPPORTED_LANGUAGES.stream().map(code -> {
        String displayLang = code;
        if (code != DEFAULT_LANGUAGE) {
            final var loc = Locale.forLanguageTag(code);
            displayLang += " - " + loc.getDisplayLanguage(loc);
        }
        return displayLang;
    }).toArray(String[]::new);
    /** Maximum number of digits for the marker limit setting. */
    private static final int MARKER_LIMIT_MAX_DIGITS = 5;
    /** Number of columns of the rebuild section. */
    private static final int REBUILD_SECTION_NUM_COLUMNS = 3;

    /** The language selection combo. */
    private final Combo languageIf;
    /** The rebuild preference combo. */
    private final Combo mRebuildIfNeeded;
    /** The checkbox for warning before losing filesets. */
    private final Button mWarnBeforeLosingFilesets;
    /** The checkbox to include rule names. */
    private final Button mIncludeRuleNamesButton;
    /** The checkbox to include module IDs. */
    private final Button mIncludeModuleIdButton;
    /** The checkbox to limit checkstyle markers. */
    private final Button mLimitCheckstyleMarkers;
    /** The text field for the marker limit. */
    private final Text mTxtMarkerLimit;
    /** The checkbox for running in background on full builds. */
    private final Button mBackgroundFullBuild;

    /**
     * Creates the general settings section of the Checkstyle preference page.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the widget style
     * @param setRebuildAll
     *            the runnable to trigger a full rebuild
     */
    public CheckstylePreferencePageGeneralSettings(Composite parent, int style,
        Runnable setRebuildAll) {
        super(parent, style);
        setLayout(new FillLayout());

        final Group group = new Group(this, style);

        group.setText(Messages.CheckstylePreferencePage_lblGeneralSettings);
        GridLayoutFactory.swtDefaults().applyTo(group);

        languageIf = createLanguageSetting(group);
        mRebuildIfNeeded = createRebuildSection(group, setRebuildAll);

        //
        // Create the "Fileset warning" check box.
        //
        mWarnBeforeLosingFilesets =
            makeButton(group, SWT.CHECK, Messages.CheckstylePreferencePage_lblWarnFilesets,
                CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING));

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
        RowLayoutFactory.fillDefaults().applyTo(limitMarkersComposite);

        mLimitCheckstyleMarkers = makeButton(limitMarkersComposite, SWT.CHECK,
            Messages.CheckstylePreferencePage_lblLimitMarker, CheckstylePluginPrefs
                .getBoolean(CheckstylePluginPrefs.PREF_LIMIT_MARKERS_PER_RESOURCE));

        mTxtMarkerLimit = new Text(limitMarkersComposite, SWT.SINGLE | SWT.BORDER);
        mTxtMarkerLimit.setTextLimit(MARKER_LIMIT_MAX_DIGITS);
        SWTUtil.addOnlyDigitInputSupport(mTxtMarkerLimit);

        mTxtMarkerLimit.setText(Integer.toString(
            CheckstylePluginPrefs.getInt(CheckstylePluginPrefs.PREF_MARKER_AMOUNT_LIMIT)));

        addRebuildNoteLabel(limitMarkersComposite);

        //
        // Create the "Run Checkstyle in background on full builds" check box.
        //
        final Composite backgroundFullBuildComposite = new Composite(group, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0)
            .applyTo(backgroundFullBuildComposite);

        mBackgroundFullBuild = makeButton(backgroundFullBuildComposite, SWT.CHECK,
            Messages.CheckstylePreferencePage_txtBackgroundFullBuild0,
            CheckstylePluginPrefs.getBoolean(CheckstylePluginPrefs.PREF_BACKGROUND_FULL_BUILD));
    }

    /**
     * Creates the rebuild section with the rebuild selection combo and the cache purge button.
     *
     * @param group
     *            the parent group
     * @param setRebuildAll
     *            the runnable to trigger a full rebuild
     * @return the rebuild preference combo
     */
    private static Combo createRebuildSection(Group group, Runnable setRebuildAll) {
        final Composite rebuildComposite = new Composite(group, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(REBUILD_SECTION_NUM_COLUMNS).margins(0, 0)
            .applyTo(rebuildComposite);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(rebuildComposite);

        final Label lblRebuild = new Label(rebuildComposite, SWT.NULL);
        lblRebuild.setText(Messages.CheckstylePreferencePage_lblRebuild);

        final Combo mRebuildIfNeeded = new Combo(rebuildComposite, SWT.READ_ONLY);
        mRebuildIfNeeded.setItems(new String[] {
            MessageDialogWithToggle.PROMPT, MessageDialogWithToggle.ALWAYS,
            MessageDialogWithToggle.NEVER,
        });
        mRebuildIfNeeded.select(mRebuildIfNeeded.indexOf(
            CheckstyleUIPluginPrefs.getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD)));

        //
        // Create button to purge the checker cache
        //

        final Button mPurgeCacheButton = new Button(rebuildComposite, SWT.FLAT);
        mPurgeCacheButton.setImage(CheckstyleUIPluginImages.REFRESH_ICON.getImage());
        mPurgeCacheButton.setToolTipText(Messages.CheckstylePreferencePage_btnRefreshCheckerCache);
        mPurgeCacheButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
            CheckerFactory.cleanup();
            setRebuildAll.run();
        }));
        GridDataFactory.swtDefaults().align(GridData.END, GridData.CENTER)
            .applyTo(mPurgeCacheButton);

        return mRebuildIfNeeded;
    }

    /**
     * Creates the language selection combo.
     *
     * @param group
     *            the parent group
     * @return the language selection combo
     */
    private static Combo createLanguageSetting(Group group) {
        final Composite langComposite = new Composite(group, SWT.NULL);
        RowLayoutFactory.fillDefaults().applyTo(langComposite);
        GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(langComposite);

        final Label lblLanguage = new Label(langComposite, SWT.NULL);
        lblLanguage.setText(Messages.CheckstylePreferencePage_lblLocaleLanguage);
        final Combo languageIf = new Combo(langComposite, SWT.READ_ONLY);
        languageIf.setItems(LANGUAGE_ITEMS);
        final String lang =
            CheckstylePluginPrefs.getString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE);
        final String effectiveLang;
        if (lang == null || lang.isEmpty()) {
            effectiveLang = DEFAULT_LANGUAGE;
        }
        else {
            effectiveLang = lang;
        }
        final int selectedLang = SUPPORTED_LANGUAGES.indexOf(effectiveLang);
        if (selectedLang != -1) {
            languageIf.select(selectedLang);
        }
        return languageIf;
    }

    /**
     * Creates a checkbox with a rebuild note label.
     *
     * @param group
     *            the parent group
     * @param text
     *            the checkbox label text
     * @param selection
     *            the initial selection state
     * @return the created checkbox button
     */
    private static Button makeCheckboxWithRebuildNoteLabel(Group group, String text,
        boolean selection) {
        final Composite composite = new Composite(group, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(composite);
        final Button button = makeButton(composite, SWT.CHECK, text, selection);
        addRebuildNoteLabel(composite);
        return button;
    }

    /**
     * Creates a button with the given label and selection state.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the button style
     * @param text
     *            the button label text
     * @param selection
     *            the initial selection state
     * @return the created button
     */
    private static Button makeButton(Composite parent, int style, String text, boolean selection) {
        final Button button = new Button(parent, style);
        button.setText(text);
        button.setSelection(selection);
        return button;
    }

    /**
     * Adds a label with a rebuild hint tooltip to the given parent.
     *
     * @param parent
     *            the parent composite
     */
    private static void addRebuildNoteLabel(Composite parent) {
        final Label lblRebuildNote = new Label(parent, SWT.NULL);
        lblRebuildNote.setImage(CheckstyleUIPluginImages.HELP_ICON.getImage());
        lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
        SWTUtil.addTooltipOnPressSupport(lblRebuildNote);
    }

    /**
     * Returns the selected language.
     *
     * @return the selected language
     */
    public String getLanguageIf() {
        return SUPPORTED_LANGUAGES.get(languageIf.getSelectionIndex());
    }

    /**
     * Returns the selected rebuild preference.
     *
     * @return the selected rebuild preference
     */
    public String getRebuildIfNeeded() {
        return mRebuildIfNeeded.getItem(mRebuildIfNeeded.getSelectionIndex());
    }

    /**
     * Returns whether to warn before losing filesets.
     *
     * @return true if the warning is enabled, false otherwise
     */
    public boolean getWarnBeforeLosingFilesets() {
        return mWarnBeforeLosingFilesets.getSelection();
    }

    /**
     * Returns whether rule names are included in the report.
     *
     * @return true if rule names are included, false otherwise
     */
    public boolean getIncludeRuleNames() {
        return mIncludeRuleNamesButton.getSelection();
    }

    /**
     * Returns whether module IDs are included in the report.
     *
     * @return true if module IDs are included, false otherwise
     */
    public boolean getIncludeModuleIdButton() {
        return mIncludeModuleIdButton.getSelection();
    }

    /**
     * Returns whether the number of Checkstyle markers is limited.
     *
     * @return true if markers are limited, false otherwise
     */
    public boolean getLimitCheckstyleMarkers() {
        return mLimitCheckstyleMarkers.getSelection();
    }

    /**
     * Returns the configured marker limit.
     *
     * @return the marker limit as text
     */
    public String getTxtMarkerLimit() {
        return mTxtMarkerLimit.getText();
    }

    /**
     * Returns whether Checkstyle runs in the background on full builds.
     *
     * @return true if Checkstyle runs in the background, false otherwise
     */
    public boolean getBackgroundFullBuild() {
        return mBackgroundFullBuild.getSelection();
    }
}
