//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
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

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckerFactory;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.builder.PackageNamesLoader;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.config.gui.CheckConfigurationWorkingSetEditor;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;
import com.atlassw.tools.eclipse.checkstyle.util.CustomLibrariesClassLoader;
import com.atlassw.tools.eclipse.checkstyle.util.SWTUtil;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class CheckstylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // =================================================
    // Public static final variables.
    // =================================================

    // =================================================
    // Static class variables.
    // =================================================

    // =================================================
    // Instance member variables.
    // =================================================

    private Combo mRebuildIfNeeded;

    private Button mPurgeCacheButton;

    private Button mWarnBeforeLosingFilesets;

    private Button mIncludeRuleNamesButton;

    private Button mIncludeModuleIdButton;

    private Button mLimitCheckstyleMarkers;

    private Button mDisableClassloader;

    private Text mTxtMarkerLimit;

    private CheckConfigurationWorkingSetEditor mWorkingSetEditor;

    private PageController mController = new PageController();

    private boolean mRebuildAll = false;

    private ICheckConfigurationWorkingSet mWorkingSet;

    // =================================================
    // Constructors & finalizer.
    // =================================================

    /**
     * Constructor.
     */
    public CheckstylePreferencePage()
    {
        super();
        setPreferenceStore(CheckstylePlugin.getDefault().getPreferenceStore());

        mWorkingSet = CheckConfigurationFactory.newWorkingSet();
        initializeDefaults();
    }

    // =================================================
    // Methods.
    // =================================================

    /**
     * Sets the default values of the preferences.
     */
    private void initializeDefaults()
    {}

    /**
     * {@inheritDoc}
     */
    public Control createContents(Composite ancestor)
    {

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
        Composite generalComposite = createGeneralContents(parentComposite);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        generalComposite.setLayoutData(fd);

        //
        // Create the check configuration section of the screen.
        //
        Composite configComposite = createCheckConfigContents(parentComposite);
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
     * @param parent the parent composite
     * @return the general area
     */
    private Composite createGeneralContents(Composite parent)
    {
        //
        // Build the composite for the general settings.
        //
        Group generalComposite = new Group(parent, SWT.NULL);
        generalComposite.setText(Messages.CheckstylePreferencePage_lblGeneralSettings);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        generalComposite.setLayout(gridLayout);

        //
        // Get the preferences.
        //
        IPreferencesService prefs = Platform.getPreferencesService();

        //
        // Create a combo with the rebuild options
        //
        Composite rebuildComposite = new Composite(generalComposite, SWT.NULL);
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
        mRebuildIfNeeded.select(mRebuildIfNeeded.indexOf(prefs.getString(
                CheckstylePlugin.PLUGIN_ID, CheckstylePlugin.PREF_ASK_BEFORE_REBUILD,
                MessageDialogWithToggle.PROMPT, null)));

        //
        // Create button to purge the checker cache
        //

        mPurgeCacheButton = new Button(rebuildComposite, SWT.FLAT);
        ImageDescriptor descriptor = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/refresh.gif"); //$NON-NLS-1$
        mPurgeCacheButton.setImage(descriptor.createImage());
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
        mWarnBeforeLosingFilesets.setSelection(prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_FILESET_WARNING, true, null));

        //
        // Create the "Include rule name" check box.
        //
        Composite includeRuleNamesComposite = new Composite(generalComposite, SWT.NULL);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        includeRuleNamesComposite.setLayout(gridLayout);

        mIncludeRuleNamesButton = new Button(includeRuleNamesComposite, SWT.CHECK);
        mIncludeRuleNamesButton.setText(Messages.CheckstylePreferencePage_lblIncludeRulenames);
        mIncludeRuleNamesButton.setSelection(prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, false, null));

        Label lblRebuildNote = new Label(includeRuleNamesComposite, SWT.NULL);
        lblRebuildNote.setImage(CheckstylePluginImages.getImage(CheckstylePluginImages.HELP_ICON));
        lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
        SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

        //
        // Create the "Include rule name" check box.
        //
        Composite includeModuleIdComposite = new Composite(generalComposite, SWT.NULL);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        includeModuleIdComposite.setLayout(gridLayout);

        mIncludeModuleIdButton = new Button(includeModuleIdComposite, SWT.CHECK);
        mIncludeModuleIdButton.setText(Messages.CheckstylePreferencePage_lblIncludeModuleIds);
        mIncludeModuleIdButton.setSelection(prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_INCLUDE_MODULE_IDS, false, null));

        lblRebuildNote = new Label(includeModuleIdComposite, SWT.NULL);
        lblRebuildNote.setImage(CheckstylePluginImages.getImage(CheckstylePluginImages.HELP_ICON));
        lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
        SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

        //
        // Create the "limit markers" check box and text field combination
        //
        Composite limitMarkersComposite = new Composite(generalComposite, SWT.NULL);
        gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        limitMarkersComposite.setLayout(gridLayout);

        mLimitCheckstyleMarkers = new Button(limitMarkersComposite, SWT.CHECK);
        mLimitCheckstyleMarkers.setText(Messages.CheckstylePreferencePage_lblLimitMarker);
        mLimitCheckstyleMarkers.setSelection(prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_LIMIT_MARKERS_PER_RESOURCE, false, null));

        mTxtMarkerLimit = new Text(limitMarkersComposite, SWT.SINGLE | SWT.BORDER);
        mTxtMarkerLimit.setTextLimit(5);
        SWTUtil.addOnlyDigitInputSupport(mTxtMarkerLimit);

        mTxtMarkerLimit.setText(Integer.toString(prefs.getInt(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_MARKER_AMOUNT_LIMIT, CheckstylePlugin.MARKER_LIMIT, null)));
        gd = new GridData();
        gd.widthHint = 30;
        mTxtMarkerLimit.setLayoutData(gd);

        lblRebuildNote = new Label(limitMarkersComposite, SWT.NULL);
        lblRebuildNote.setImage(CheckstylePluginImages.getImage(CheckstylePluginImages.HELP_ICON));
        lblRebuildNote.setToolTipText(Messages.CheckstylePreferencePage_txtSuggestRebuild);
        SWTUtil.addTooltipOnPressSupport(lblRebuildNote);

        //
        // Create the "disable classloader" check box
        //

        Composite disableClassloaderComposite = new Composite(generalComposite, SWT.NULL);
        gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        disableClassloaderComposite.setLayout(gridLayout);

        mDisableClassloader = new Button(disableClassloaderComposite, SWT.CHECK);
        mDisableClassloader.setText(Messages.CheckstylePreferencePage_lblDisableClassloader);
        mDisableClassloader.setSelection(prefs.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_DISABLE_PROJ_CLASSLOADER, false, null));
        mDisableClassloader
                .setToolTipText(Messages.CheckstylePreferencePage_lblDisableClassloaderNote);

        Label lblDisableClassloader = new Label(disableClassloaderComposite, SWT.NULL);
        lblDisableClassloader.setImage(CheckstylePluginImages
                .getImage(CheckstylePluginImages.HELP_ICON));
        lblDisableClassloader
                .setToolTipText(Messages.CheckstylePreferencePage_lblDisableClassloaderNote);
        SWTUtil.addTooltipOnPressSupport(lblDisableClassloader);

        return generalComposite;
    }

    /**
     * Creates the content regarding the management of check configurations.
     * 
     * @param parent the parent composite
     * @return the configuration area
     */
    private Composite createCheckConfigContents(Composite parent)
    {
        //
        // Create the composite for configuring check configurations.
        //
        Group configComposite = new Group(parent, SWT.NULL);
        configComposite.setText(Messages.CheckstylePreferencePage_titleCheckConfigs);
        configComposite.setLayout(new FormLayout());

        mWorkingSetEditor = new CheckConfigurationWorkingSetEditor(mWorkingSet, true);
        Control editorControl = mWorkingSetEditor.createContents(configComposite);
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
    public void init(IWorkbench workbench)
    {}

    /**
     * {@inheritDoc}
     */
    public boolean performOk()
    {

        try
        {

            //
            // Save the check configurations.
            //
            mWorkingSet.store();

            //
            // Save the general preferences.
            //
            IPreferencesService prefService = Platform.getPreferencesService();
            IEclipsePreferences prefs = new InstanceScope().getNode(CheckstylePlugin.PLUGIN_ID);
            prefs.put(CheckstylePlugin.PREF_ASK_BEFORE_REBUILD, mRebuildIfNeeded
                    .getItem(mRebuildIfNeeded.getSelectionIndex()));

            //
            // Save the classloader preferences.
            //
            prefs.putBoolean(CheckstylePlugin.PREF_DISABLE_PROJ_CLASSLOADER, mDisableClassloader
                    .getSelection());

            //
            // fileset warning preference
            //
            boolean warnFileSetsNow = mWarnBeforeLosingFilesets.getSelection();
            prefs.putBoolean(CheckstylePlugin.PREF_FILESET_WARNING, warnFileSetsNow);

            //
            // Include rule names preference.
            //
            boolean includeRuleNamesNow = mIncludeRuleNamesButton.getSelection();
            boolean includeRuleNamesOriginal = prefService.getBoolean(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, false, null);
            prefs.putBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, includeRuleNamesNow);

            //
            // Include module id preference.
            //
            boolean includeModuleIdNow = mIncludeModuleIdButton.getSelection();
            boolean includeModuleIdOriginal = prefService.getBoolean(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_INCLUDE_MODULE_IDS, false, null);
            prefs.putBoolean(CheckstylePlugin.PREF_INCLUDE_MODULE_IDS, includeModuleIdNow);

            //
            // Limit markers preference
            //

            boolean limitMarkersNow = mLimitCheckstyleMarkers.getSelection();
            boolean limitMarkersOriginal = prefService.getBoolean(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_LIMIT_MARKERS_PER_RESOURCE, false, null);
            prefs.putBoolean(CheckstylePlugin.PREF_LIMIT_MARKERS_PER_RESOURCE, limitMarkersNow);

            int markerLimitNow = Integer.parseInt(mTxtMarkerLimit.getText());
            int markerLimitOriginal = prefService.getInt(CheckstylePlugin.PLUGIN_ID,
                    CheckstylePlugin.PREF_MARKER_AMOUNT_LIMIT, CheckstylePlugin.MARKER_LIMIT, null);
            prefs.putInt(CheckstylePlugin.PREF_MARKER_AMOUNT_LIMIT, markerLimitNow);

            // save the preferences
            prefs.flush();

            // See if all projects need rebuild
            boolean needRebuildAllProjects = (includeRuleNamesNow != includeRuleNamesOriginal)
                    || (includeModuleIdNow != includeModuleIdOriginal)
                    || (limitMarkersNow != limitMarkersOriginal)
                    || (markerLimitNow != markerLimitOriginal) || mRebuildAll;

            // Get projects that need rebuild considering the changes
            Collection projectsToBuild = mWorkingSet.getAffectedProjects();

            IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
            String promptRebuildPref = prefStore
                    .getString(CheckstylePlugin.PREF_ASK_BEFORE_REBUILD);

            boolean rebuild = MessageDialogWithToggle.ALWAYS.equals(promptRebuildPref)
                    && (needRebuildAllProjects || projectsToBuild.size() > 0);

            //
            // Prompt for rebuild
            //
            if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref)
                    && (needRebuildAllProjects || projectsToBuild.size() > 0))
            {

                MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
                        getShell(), Messages.CheckstylePreferencePage_titleRebuild,
                        Messages.CheckstylePreferencePage_msgRebuild,
                        Messages.CheckstylePreferencePage_nagRebuild, false, prefStore,
                        CheckstylePlugin.PREF_ASK_BEFORE_REBUILD);

                rebuild = dialog.getReturnCode() == IDialogConstants.YES_ID;
            }

            if (rebuild)
            {
                try
                {
                    if (needRebuildAllProjects)
                    {
                        CheckstyleBuilder.buildAllProjects();
                    }
                    else
                    {
                        CheckstyleBuilder.buildProjects(projectsToBuild);
                    }

                }
                catch (CheckstylePluginException e)
                {
                    CheckstyleLog.errorDialog(getShell(), NLS.bind(
                            ErrorMessages.errorFailedRebuild, e.getMessage()), e, true);
                }
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.errorDialog(getShell(), NLS.bind(
                    ErrorMessages.errorFailedSavePreferences, e.getLocalizedMessage()), e, true);
        }
        catch (BackingStoreException e)
        {
            CheckstyleLog.errorDialog(getShell(), NLS.bind(
                    ErrorMessages.errorFailedSavePreferences, e.getLocalizedMessage()), e, true);
        }

        return true;
    }

    /**
     * Controller for this page.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController extends SelectionAdapter
    {

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e)
        {
            if (mPurgeCacheButton == e.widget)
            {
                CheckerFactory.cleanup();
                MetadataFactory.refresh();
                CustomLibrariesClassLoader.invalidate();
                PackageNamesLoader.refresh();
                mRebuildAll = true;
            }
        }
    }
}