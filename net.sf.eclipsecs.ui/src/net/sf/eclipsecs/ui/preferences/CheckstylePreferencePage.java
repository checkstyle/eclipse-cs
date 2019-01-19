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

package net.sf.eclipsecs.ui.preferences;

import java.util.Collection;
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
import net.sf.eclipsecs.ui.util.SWTUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.puppycrawl.tools.checkstyle.Main;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage </samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself.
 *
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * </p>
 */
public class CheckstylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  private Combo mRebuildIfNeeded;

  private Button mPurgeCacheButton;

  private Button mWarnBeforeLosingFilesets;

  private Button mIncludeRuleNamesButton;

  private Button mIncludeModuleIdButton;

  private Button mLimitCheckstyleMarkers;

  private Text mTxtMarkerLimit;

  private Button mBackgroundFullBuild;

  private CheckConfigurationWorkingSetEditor mWorkingSetEditor;

  private final PageController mController = new PageController();

  private boolean mRebuildAll = false;

  private final ICheckConfigurationWorkingSet mWorkingSet;

  /**
   * Constructor.
   */
  public CheckstylePreferencePage() {
    super();
    setDescription(NLS.bind(Messages.CheckstylePreferencePage_version, getCheckstyleVersion()));
    setPreferenceStore(CheckstyleUIPlugin.getDefault().getPreferenceStore());

    mWorkingSet = CheckConfigurationFactory.newWorkingSet();
    initializeDefaults();
  }

  private String getCheckstyleVersion() {
    return Main.class.getPackage().getImplementationVersion();
  }

  /**
   * Sets the default values of the preferences.
   */
  private void initializeDefaults() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Control createContents(Composite ancestor) {

    noDefaultAndApplyButton();

    //
    // Build the top level composite with one colume.
    //
    Composite parentComposite = new Composite(ancestor, SWT.NULL);
    FormLayout layout = new FormLayout();
    parentComposite.setLayout(layout);

    //
    // Create the general section of the screen.
    //
    final Composite generalComposite = createGeneralContents(parentComposite);
    FormData fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(0);
    fd.right = new FormAttachment(100);
    generalComposite.setLayoutData(fd);

    //
    // Create the check configuration section of the screen.
    //
    final Composite configComposite = createCheckConfigContents(parentComposite);
    fd = new FormData();
    fd.left = new FormAttachment(0);
    fd.top = new FormAttachment(generalComposite, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100);
    fd.bottom = new FormAttachment(100);
    configComposite.setLayoutData(fd);

    return parentComposite;
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
            .setImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.REFRESH_ICON));
    mPurgeCacheButton.setToolTipText(Messages.CheckstylePreferencePage_btnRefreshCheckerCache);
    mPurgeCacheButton.addSelectionListener(mController);
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.END;
    gd.grabExcessHorizontalSpace = true;
    gd.heightHint = 20;
    gd.widthHint = 20;
    mPurgeCacheButton.setLayoutData(gd);

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
    lblRebuildNote.setImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.HELP_ICON));
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
    lblRebuildNote.setImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.HELP_ICON));
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
    gd = new GridData();
    gd.widthHint = 30;
    mTxtMarkerLimit.setLayoutData(gd);

    lblRebuildNote = new Label(limitMarkersComposite, SWT.NULL);
    lblRebuildNote.setImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.HELP_ICON));
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
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 3);
    fd.top = new FormAttachment(0, 3);
    fd.right = new FormAttachment(100, -3);
    fd.bottom = new FormAttachment(100, -3);
    editorControl.setLayoutData(fd);

    return configComposite;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(IWorkbench workbench) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean performOk() {

    try {

      //
      // Save the check configurations.
      //
      mWorkingSet.store();

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
              && (needRebuildAllProjects || projectsToBuild.size() > 0);

      //
      // Prompt for rebuild
      //
      if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref)
              && (needRebuildAllProjects || projectsToBuild.size() > 0)) {

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

        } catch (CheckstylePluginException e) {
          CheckstyleUIPlugin.errorDialog(getShell(),
                  NLS.bind(Messages.errorFailedRebuild, e.getMessage()), e, true);
        }
      }
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(getShell(),
              NLS.bind(Messages.errorFailedSavePreferences, e.getLocalizedMessage()), e, true);
    } catch (BackingStoreException e) {
      CheckstyleUIPlugin.errorDialog(getShell(),
              NLS.bind(Messages.errorFailedSavePreferences, e.getLocalizedMessage()), e, true);
    }

    return true;
  }

  /**
   * Controller for this page.
   *
   * @author Lars Ködderitzsch
   */
  private class PageController extends SelectionAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
      if (mPurgeCacheButton == e.widget) {
        CheckerFactory.cleanup();
        mRebuildAll = true;
      }
    }
  }
}
