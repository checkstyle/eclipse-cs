//============================================================================
//
// Copyright (C) 2002-2004  David Schneider
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

//=================================================
// Imports from java namespace
//=================================================
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigConverter;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.FileSetFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class CheckstylePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private Composite mParentComposite;

    private TableViewer mViewer;

    private Button mAddButton;

    private Button mEditButton;

    private Button mCopyButton;

    private Button mRemoveButton;

    private Button mImportPluginButton;

    private Button mExportPluginButton;

    private Button mImportCheckstyleButton;

    private Button mExportCheckstyleButton;

    private Button mIncludeRuleNamesButton;

    private List mCheckConfigurations;

    private boolean mNeedRebuild = false;

    //=================================================
    // Constructors & finalizer.
    //=================================================
    
    /**
     * Constructor.
     */
    public CheckstylePreferencePage()
    {
        super();
        setPreferenceStore(CheckstylePlugin.getDefault().getPreferenceStore());
        setDescription("Checkstyle Settings:");
        initializeDefaults();
    }

    //=================================================
    // Methods.
    //=================================================

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
        //  Initialize the check configurations.
        //
        try
        {
            initializeCheckConfigs();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to create preferences window, " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return ancestor;
        }

        //
        //  Build the top level composite with one colume.
        //
        mParentComposite = new Composite(ancestor, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        mParentComposite.setLayout(layout);
        
        //
        //  Create the general section of the screen.
        //
        createGeneralContents(mParentComposite);
        
        //
        //  Create the check configuration section of the screen.
        //
        createCheckConfigContents(mParentComposite);

        return mParentComposite;
    }
    
    private void createGeneralContents(Composite parent)
    {
        //
        //  Build the composite for the general settings.
        //
        Group generalComposite = new Group(parent, SWT.NULL);
        generalComposite.setText(" General Settings ");
        generalComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        generalComposite.setLayout(layout);

        //
        //  Get the preferences.
        //
        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();

        //
        //  Create the "Include rule name" check box.
        //
        mIncludeRuleNamesButton = new Button(generalComposite, SWT.CHECK);
        mIncludeRuleNamesButton.setText("Include rule names in violation messages");
        mIncludeRuleNamesButton.setSelection(
            prefs.getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES));
    }
    
    private void createCheckConfigContents(Composite parent)
    { 
        //
        //  Create the composite for configuring check configurations.
        //
        Group configComposite = new Group(parent, SWT.NULL);
        configComposite.setText(" Check Configurations ");
        configComposite.setLayoutData(
            new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        configComposite.setLayout(layout);

        Table table = new Table(configComposite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = convertWidthInCharsToPixels(80);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column1 = new TableColumn(table, SWT.NULL);
        column1.setText("Check Configuration");

        tableLayout.addColumnData(new ColumnWeightData(40));

        mViewer = new TableViewer(table);
        mViewer.setLabelProvider(new CheckConfigurationLabelProvider());
        mViewer.setContentProvider(new CheckConfigurationProvider());
        mViewer.setSorter(new CheckConfigurationViewerSorter());
        mViewer.setInput(mCheckConfigurations);
        mViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent e)
            {
                editCheckConfig();
            }
        });

        Composite rightButtons = new Composite(configComposite, SWT.NULL);
        rightButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        rightButtons.setLayout(layout);

        mAddButton = createPushButton(rightButtons, "Add...");
        mAddButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                addCheckConfig(null);
            }
        });

        mEditButton = createPushButton(rightButtons, "Edit...");
        mEditButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                editCheckConfig();
            }
        });

        mCopyButton = createPushButton(rightButtons, "Copy...");
        mCopyButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                copyCheckConfig();
            }
        });

        mRemoveButton = createPushButton(rightButtons, "Remove");
        mRemoveButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                removeCheckConfig();
            }
        });

        Composite bottomButtons = new Composite(configComposite, SWT.NULL);
        bottomButtons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        bottomButtons.setLayout(layout);

        mImportPluginButton = createPushButton(bottomButtons, "Import Plugin Config ...");
        mImportPluginButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                importPluginCheckConfig();
            }
        });

        mExportPluginButton = createPushButton(bottomButtons, "Export Plugin Config ...");
        mExportPluginButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                exportPluginCheckConfig();
            }
        });

        mImportCheckstyleButton = createPushButton(bottomButtons, "Import Checkstyle Config ...");
        mImportCheckstyleButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                importCheckstyleCheckConfig();
            }
        });

        mExportCheckstyleButton = createPushButton(bottomButtons, "Export Checkstyle Config ...");
        mExportCheckstyleButton.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event evt)
            {
                exportCheckstyleCheckConfig();
            }
        });
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
        //
        //  Save the check configurations.
        //
        try
        {
            CheckConfigurationFactory.setCheckConfigurations(mCheckConfigurations);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to save CheckConfigurations, " + e.getMessage(), e);
            CheckstyleLog.internalErrorDialog();
        }

        //
        //  Save the general preferences.
        //
        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();

        //
        //  Include rule names preference.
        //
        boolean includeRuleNamesNow = mIncludeRuleNamesButton.getSelection();
        boolean includeRuleNamesOriginal =
            prefs.getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES);
        prefs.setValue(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES, includeRuleNamesNow);
        mNeedRebuild = mNeedRebuild | (includeRuleNamesNow ^ includeRuleNamesOriginal);

        //
        //  Do a rebuild if one is needed.
        //
        if (mNeedRebuild)
        {
            try
            {
                CheckstyleBuilder.buildAllProjects(getShell());
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error("Failed to rebuild projects, " + e.getMessage(), e);
                CheckstyleLog.internalErrorDialog();
            }
        }

        return true;
    }

    /**
     * Utility method that creates a push button instance
     * and sets the default layout data.
     *
     * @param parent  the parent for the new button
     * @param label  the label for the new button
     * @return the newly-created button
     */
    private Button createPushButton(Composite parent, String label)
    {
        Button button = new Button(parent, SWT.PUSH | SWT.WRAP);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        button.setLayoutData(data);
        return button;
    }

    private void addCheckConfig(CheckConfiguration configToAdd)
    {
        CheckConfigurationEditDialog dialog = null;
        try
        {
            dialog =
                new CheckConfigurationEditDialog(
                    mParentComposite.getShell(),
                    configToAdd,
                    mCheckConfigurations);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error(
                "Failed to open CheckConfigurationEditDialog, " + e.getMessage(),
                e);
            CheckstyleLog.internalErrorDialog();
            return;
        }

        if (dialog.okWasPressed())
        {
            CheckConfiguration checkConfig = dialog.getFinalConfiguration();
            if (checkConfig != null)
            {
                //
                // ad, 7.Jan.2004, Bug #872279 
                // If the config that is to be imported already exists, ask the
                // user whether to replace it or not.
                // When the user chooses not to replace it, the import action
                // is aborted for the single config, but the import process
                // should be continued for the other configs (if multiple configs
                // were selected.)
                // 
                for (Iterator iter = mCheckConfigurations.iterator(); iter.hasNext();)
                {
                    CheckConfiguration c = (CheckConfiguration)iter.next();
                    if (c.getConfigName().equals(checkConfig.getConfigName()))
                    {
                        //
                        // The newly imported config exists already.  Ask the user if
                        // the existing one should be replaced.
                        //
                        boolean replaceConfiguration =
                            CheckstyleLog.questionDialog(
                                getShell(),
                                "The configuration '"
                                    + c.getConfigName()
                                    + "' already exists. Do you want to replace it?");

                        if (replaceConfiguration)
                        {
                            //
                            // The user chose to replace the config, so
                            // delete the existing one here
                            //
                            iter.remove();
                        }
                        else
                        {
                            //
                            // The user chose NOT to replace the exising config:
                            // Do nothing.
                            //
                            return;
                        }
                    }
                }

                mCheckConfigurations.add(checkConfig);
                mViewer.refresh();

                //
                // Since the config may have potentially been replaced by a 
                // new one, we need to do a rebuild, if it is in used.
                //
                try
                {
                    if (FileSetFactory.isCheckConfigInUse(checkConfig.getConfigName()))
                    {
                        mNeedRebuild = true;
                    }
                }
                catch (CheckstylePluginException e)
                {
                    //
                    //  Assume its in use.
                    //
                    mNeedRebuild = true;
                    CheckstyleLog.warning("Exception while checking for check config use", e);
                }
                // ad, 7.Jan.2004, Bug #872279 
                // end change

            }
            else
            {
                CheckstyleLog.error("New check configuration is null");
                CheckstyleLog.internalErrorDialog();
            }
        }
    }

    private void editCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration checkConfig = (CheckConfiguration)selection.getFirstElement();
        if (checkConfig == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        CheckConfigurationEditDialog dialog = null;
        try
        {
            dialog =
                new CheckConfigurationEditDialog(
                    mParentComposite.getShell(),
                    checkConfig,
                    mCheckConfigurations);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error(
                "Failed to open CheckConfigurationEditDialog, " + e.getMessage(),
                e);
            CheckstyleLog.internalErrorDialog();
            return;
        }

        if (dialog.okWasPressed())
        {
            CheckConfiguration editedConfig = dialog.getFinalConfiguration();
            if (editedConfig != null)
            {
                mCheckConfigurations.remove(checkConfig);
                mCheckConfigurations.add(editedConfig);
                try
                {
                    if (FileSetFactory.isCheckConfigInUse(editedConfig.getConfigName()))
                    {
                        mNeedRebuild = true;
                    }
                }
                catch (CheckstylePluginException e)
                {
                    //
                    //  Assume its in use.
                    //
                    mNeedRebuild = true;
                    CheckstyleLog.warning("Exception while checking for check config use", e);
                }
                mViewer.refresh();
            }
            else
            {
                CheckstyleLog.error("Edited check configuration is null");
                CheckstyleLog.internalErrorDialog();
            }
        }
    }

    private void copyCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration checkConfig = (CheckConfiguration)selection.getFirstElement();
        if (checkConfig == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        //
        //  Make a clone of the selected configuration.
        //
        try
        {
            checkConfig = (CheckConfiguration)checkConfig.clone();
            String name = "Copy of " + checkConfig.getConfigName();
            checkConfig.setName(name);
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone CheckConfiguration");
            CheckstyleLog.internalErrorDialog();
            return;
        }

        CheckConfigurationEditDialog dialog = null;
        try
        {
            dialog =
                new CheckConfigurationEditDialog(
                    mParentComposite.getShell(),
                    checkConfig,
                    mCheckConfigurations);
            dialog.open();
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error(
                "Failed to open CheckConfigurationEditDialog, " + e.getMessage(),
                e);
            CheckstyleLog.internalErrorDialog();
            return;
        }

        if (dialog.okWasPressed())
        {
            CheckConfiguration copiedConfig = dialog.getFinalConfiguration();
            if (copiedConfig != null)
            {
                mCheckConfigurations.add(copiedConfig);
                mViewer.refresh();
            }
            else
            {
                CheckstyleLog.error("Copied check configuration is null");
                CheckstyleLog.internalErrorDialog();
            }
        }
    }

    private void removeCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration checkConfig = (CheckConfiguration)selection.getFirstElement();
        if (checkConfig == null)
        {
            //
            //  Nothing is selected.
            //
            return;
        }

        //
        //  Make sure the check config is not in use.  Don't let it be
        //  deleted if it is.
        //
        try
        {
            if (FileSetFactory.isCheckConfigInUse(checkConfig.getConfigName()))
            {
                MessageDialog.openInformation(
                    mParentComposite.getShell(),
                    "Can't Delete",
                    "The Check Configuration '"
                        + checkConfig.getConfigName()
                        + "' is currently in use by a project."
                        + "  It must be removed from all project "
                        + "configurations before it can be deleted.");
                return;
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.warning("Exception while checking for check config use", e);
            CheckstyleLog.internalErrorDialog();
            return;
        }

        boolean confirm =
            MessageDialog.openQuestion(
                mParentComposite.getShell(),
                "Confirm Delete",
                "Remove check configuration '" + checkConfig.getConfigName() + "'?");
        if (confirm)
        {
            mCheckConfigurations.remove(checkConfig);
            mViewer.refresh();
        }
    }

    private void importPluginCheckConfig()
    {
        FileDialog dialog = new FileDialog(getShell());
        dialog.setText("Import Plug-in Check Configuration");
        String path = dialog.open();

        if (path == null)
        {
            return;
        }

        File checkConfigFile = new File(path);
        try
        {
            List newConfigs =
                CheckConfigurationFactory.importPluginCheckConfigurations(checkConfigFile);
            Iterator iter = newConfigs.iterator();
            while (iter.hasNext())
            {
                addCheckConfig((CheckConfiguration)iter.next());
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to import CheckConfigurations from external file", e);
            CheckstyleLog.internalErrorDialog();
        }
    }

    private void exportPluginCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration config = (CheckConfiguration)selection.getFirstElement();
        if (config == null)
        {
            MessageDialog.openInformation(
                mParentComposite.getShell(),
                "No Selection",
                "No Check Configuration Selected");
            return;
        }

        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText("Export Plug-in Check Configuration");
        String path = dialog.open();
        if (path == null)
        {
            return;
        }
        File file = new File(path);

        try
        {
            CheckConfigurationFactory.exportPluginCheckConfigurations(file, config);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to export CheckConfigurations to external file", e);
            MessageDialog.openError(
                mParentComposite.getShell(),
                "Checkstyle Error",
                "Failed to export CheckConfigurations to external file");
        }
    }

    private void exportCheckstyleCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
        CheckConfiguration config = (CheckConfiguration)selection.getFirstElement();
        if (config == null)
        {
            MessageDialog.openInformation(
                mParentComposite.getShell(),
                "No Selection",
                "No Check Configuration Selected");
            return;
        }

        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText("Export Checkstyle Check Configuration");
        String path = dialog.open();
        if (path == null)
        {
            return;
        }
        File file = new File(path);

        try
        {
            CheckConfigurationFactory.exportCheckstyleCheckConfigurations(file, config);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to export CheckConfigurations to external file", e);
            MessageDialog.openError(
                mParentComposite.getShell(),
                "Checkstyle Error",
                "Failed to export CheckConfigurations to external file");
        }
    }

    private void importCheckstyleCheckConfig()
    {
        //
        //  Get the full path to the file to be imported.
        //
        FileDialog fileDialog = new FileDialog(getShell());
        fileDialog.setText("Import Checkstyle Check Configuration");
        String path = fileDialog.open();
        if (path == null)
        {
            return;
        }

        try
        {
            //
            //  Load the config file.
            //
            CheckConfigConverter converter = new CheckConfigConverter();
            converter.loadConfig(path);

            //
            //  Resolve property values.
            //
            List resolveProps = converter.getPropsToResolve();
            if (resolveProps.size() > 0)
            {
                ResolvePropertyValuesDialog resolveDialog =
                    new ResolvePropertyValuesDialog(getShell(), resolveProps);
                resolveDialog.open();
            }

            //
            //  Get a CheckConfiguration from the converter.
            //
            CheckConfiguration config = converter.getCheckConfiguration();

            //
            //  Add the config using the add dialog so the user can see what it looks like,
            //  make changes, and it will be validated.
            //
            addCheckConfig(config);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to import CheckConfigurations from external file", e);
            CheckstyleLog.internalErrorDialog();
        }
    }

    /**
     *  Make a local working copy of the Check Configurations in case
     *  the user exits the preferences window with the cancel button.
     *  In this case all edits will be discarded, otherwise they are
     *  saved when the user presses the OK button.
     */
    private void initializeCheckConfigs() throws CheckstylePluginException
    {
        mCheckConfigurations = new LinkedList();
        try
        {
            List configs = CheckConfigurationFactory.getCheckConfigurations();
            Iterator iter = configs.iterator();
            while (iter.hasNext())
            {
                CheckConfiguration cfg = (CheckConfiguration)iter.next();
                CheckConfiguration clone = (CheckConfiguration)cfg.clone();
                mCheckConfigurations.add(clone);
            }
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone CheckConfiguration", e);
            throw new CheckstylePluginException(
                "Failed to clone CheckConfiguration, " + e.getMessage());
        }
    }
}