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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleBuilder;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.ConfigurationTypes;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IConfigurationType;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.InternalCheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

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
    //=================================================
    // Public static final variables.
    //=================================================

    //=================================================
    // Static class variables.
    //=================================================

    //=================================================
    // Instance member variables.
    //=================================================

    private TableViewer mViewer;

    private Button mAddButton;

    private Button mEditButton;

    private Button mConfigureButton;

    private Button mCopyButton;

    private Button mRemoveButton;

    private Button mExportButton;

    private Text mConfigurationDescription;

    private Button mWarnBeforeLosingFilesets;

    private Button mIncludeRuleNamesButton;

    private List mCheckConfigurations;

    private boolean mNeedRebuild = false;

    private PageController mController = new PageController();

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
        //setDescription("Checkstyle Settings:");
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

        //TODO externalize message strings

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
        Composite parentComposite = new Composite(ancestor, SWT.NULL);
        FormLayout layout = new FormLayout();
        parentComposite.setLayout(layout);

        //
        //  Create the general section of the screen.
        //
        Composite generalComposite = createGeneralContents(parentComposite);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        generalComposite.setLayoutData(fd);

        //
        //  Create the check configuration section of the screen.
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
        //  Build the composite for the general settings.
        //
        Group generalComposite = new Group(parent, SWT.NULL);
        generalComposite.setText(" General Settings ");
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        generalComposite.setLayout(layout);

        //
        //  Get the preferences.
        //
        Preferences prefs = CheckstylePlugin.getDefault().getPluginPreferences();

        //
        //  Create the "Fileset warning" check box.
        //
        mWarnBeforeLosingFilesets = new Button(generalComposite, SWT.CHECK);
        mWarnBeforeLosingFilesets.setText("Warn before losing configured file sets");
        mWarnBeforeLosingFilesets.setSelection(prefs
                .getBoolean(CheckstylePlugin.PREF_FILESET_WARNING));

        //
        //  Create the "Include rule name" check box.
        //
        mIncludeRuleNamesButton = new Button(generalComposite, SWT.CHECK);
        mIncludeRuleNamesButton.setText("Include rule names in violation messages");
        mIncludeRuleNamesButton.setSelection(prefs
                .getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES));

        new Label(generalComposite, SWT.NULL)
                .setText("Note: Changes to this option only become visible after a full rebuild of your projects.");

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
        //  Create the composite for configuring check configurations.
        //
        Group configComposite = new Group(parent, SWT.NULL);
        configComposite.setText(" Check Configurations ");
        configComposite.setLayout(new FormLayout());

        Control rightButtons = createButtonBar(configComposite);
        FormData fd = new FormData();
        fd.top = new FormAttachment(0, 3);
        fd.right = new FormAttachment(100, -3);
        fd.bottom = new FormAttachment(100, -3);
        rightButtons.setLayoutData(fd);

        Composite tableAndDesc = new Composite(configComposite, SWT.NULL);
        tableAndDesc.setLayout(new FormLayout());
        fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(0, 3);
        fd.right = new FormAttachment(rightButtons, -3, SWT.LEFT);
        fd.bottom = new FormAttachment(100, -3);
        tableAndDesc.setLayoutData(fd);

        Control table = createConfigTable(tableAndDesc);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(70);
        table.setLayoutData(fd);

        Label lblDescription = new Label(tableAndDesc, SWT.NULL);
        lblDescription.setText("Description:");
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(table, 3);
        fd.right = new FormAttachment(100);
        lblDescription.setLayoutData(fd);

        mConfigurationDescription = new Text(tableAndDesc, SWT.LEFT | SWT.WRAP | SWT.MULTI
                | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(lblDescription);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        mConfigurationDescription.setLayoutData(fd);

        return configComposite;
    }

    /**
     * Creates the table viewer to show the existing check configurations.
     * 
     * @param parent the parent composite
     * @return the table control
     */
    private Control createConfigTable(Composite parent)
    {
        Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column1 = new TableColumn(table, SWT.NULL);
        column1.setText("Check Configuration");
        tableLayout.addColumnData(new ColumnWeightData(40));

        TableColumn column2 = new TableColumn(table, SWT.NULL);
        column2.setText("Location");
        tableLayout.addColumnData(new ColumnWeightData(30));

        TableColumn column3 = new TableColumn(table, SWT.NULL);
        column3.setText("Type");
        tableLayout.addColumnData(new ColumnWeightData(30));

        mViewer = new TableViewer(table);
        mViewer.setLabelProvider(new CheckConfigurationLabelProvider());
        mViewer.setContentProvider(new ArrayContentProvider());
        mViewer.setSorter(new CheckConfigurationViewerSorter());
        mViewer.setInput(mCheckConfigurations);
        mViewer.addDoubleClickListener(mController);
        mViewer.addSelectionChangedListener(mController);

        return table;
    }

    /**
     * Creates the button bar.
     * 
     * @param parent the parent composite
     * @return the button bar composite
     */
    private Control createButtonBar(Composite parent)
    {

        Composite rightButtons = new Composite(parent, SWT.NULL);
        rightButtons.setLayout(new FormLayout());

        mAddButton = new Button(rightButtons, SWT.PUSH);
        mAddButton.setText("New...");
        mAddButton.addSelectionListener(mController);
        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        mAddButton.setLayoutData(fd);

        mEditButton = new Button(rightButtons, SWT.PUSH);
        mEditButton.setText("Properties...");
        mEditButton.addSelectionListener(mController);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(mAddButton, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100);
        mEditButton.setLayoutData(fd);

        mConfigureButton = new Button(rightButtons, SWT.PUSH);
        mConfigureButton.setText("Configure...");
        mConfigureButton.addSelectionListener(mController);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(mEditButton, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100);
        mConfigureButton.setLayoutData(fd);

        mCopyButton = new Button(rightButtons, SWT.PUSH);
        mCopyButton.setText("Copy...");
        mCopyButton.addSelectionListener(mController);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(mConfigureButton, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100);
        mCopyButton.setLayoutData(fd);

        mRemoveButton = new Button(rightButtons, SWT.PUSH);
        mRemoveButton.setText("Remove");
        mRemoveButton.addSelectionListener(mController);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(mCopyButton, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100);
        mRemoveButton.setLayoutData(fd);

        mExportButton = new Button(rightButtons, SWT.PUSH);
        mExportButton.setText("Export...");
        mExportButton.addSelectionListener(mController);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        mExportButton.setLayoutData(fd);

        return rightButtons;
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
        // fileset warning preference
        //
        boolean warnFileSetsNow = mWarnBeforeLosingFilesets.getSelection();
        boolean iwarnFileSetsNowOriginal = prefs.getBoolean(CheckstylePlugin.PREF_FILESET_WARNING);
        prefs.setValue(CheckstylePlugin.PREF_FILESET_WARNING, warnFileSetsNow);

        //
        //  Include rule names preference.
        //
        boolean includeRuleNamesNow = mIncludeRuleNamesButton.getSelection();
        boolean includeRuleNamesOriginal = prefs
                .getBoolean(CheckstylePlugin.PREF_INCLUDE_RULE_NAMES);
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
     * Controller for this page.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController implements SelectionListener, IDoubleClickListener,
            ISelectionChangedListener
    {

        /**
         * @see SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e)
        {

            if (mAddButton == e.widget)
            {
                addCheckConfig();
            }
            if (mEditButton == e.widget && mViewer.getSelection() instanceof IStructuredSelection)
            {
                editCheckConfig();
            }
            if (mConfigureButton == e.widget
                    && mViewer.getSelection() instanceof IStructuredSelection)
            {
                configureCheckConfig();
            }
            if (mCopyButton == e.widget && mViewer.getSelection() instanceof IStructuredSelection)
            {
                copyCheckConfig();
            }
            if (mRemoveButton == e.widget && mViewer.getSelection() instanceof IStructuredSelection)
            {
                removeCheckConfig();
            }
            if (mExportButton == e.widget && mViewer.getSelection() instanceof IStructuredSelection)
            {
                exportCheckstyleCheckConfig();
            }
        }

        /**
         * @see sSelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetDefaultSelected(SelectionEvent e)
        {
        // NOOP
        }

        /**
         * @see IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
         */
        public void doubleClick(DoubleClickEvent event)
        {
            configureCheckConfig();
        }

        /**
         * @see ISelectionChangedListener#selectionChanged(
         *      org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event)
        {
            if (event.getSource() == mViewer
                    && event.getSelection() instanceof IStructuredSelection)
            {
                ICheckConfiguration config = (ICheckConfiguration) ((IStructuredSelection) event
                        .getSelection()).getFirstElement();
                if (config != null && config.getDescription() != null)
                {
                    mConfigurationDescription.setText(config.getDescription());
                }
                else
                {
                    mConfigurationDescription.setText("");
                }
            }
        }

    }

    /**
     * Create a new Check configuration.
     */
    private void addCheckConfig()
    {
        CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(
                getShell(), null);
        dialog.setBlockOnOpen(true);
        if (CheckConfigurationPropertiesDialog.OK == dialog.open())
        {
            try
            {
                ICheckConfiguration newConfig = dialog.getCheckConfiguration();
                mCheckConfigurations.add(newConfig);
                mViewer.refresh(true);
            }
            catch (CheckstylePluginException ex)
            {
                CheckstyleLog.error(ex.getLocalizedMessage(), ex);
                CheckstyleLog.errorDialog(getShell(), ex.getLocalizedMessage(), ex);
            }

        }
    }

    /**
     * Edit the properties of a check configuration.
     */
    private void editCheckConfig()
    {
        ICheckConfiguration config = (ICheckConfiguration) ((IStructuredSelection) mViewer
                .getSelection()).getFirstElement();

        if (config != null)
        {
            CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(
                    getShell(), config);
            dialog.setBlockOnOpen(true);
            if (CheckConfigurationPropertiesDialog.OK == dialog.open())
            {
                mViewer.refresh(true);
            }
        }
    }

    private void configureCheckConfig()
    {
        ICheckConfiguration config = (ICheckConfiguration) ((IStructuredSelection) mViewer
                .getSelection()).getFirstElement();

        if (config != null)
        {

            if (config.isContextNeeded())
            {

                IProject context = getProjectContext();
                if (context != null)
                {
                    config.setContext(context);
                }
                else
                {
                    //cant go further without context
                    return;
                }
            }

            try
            {
                //test if file exists
                config.getCheckstyleConfigurationURL();

                CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(
                        getShell(), config);
                dialog.setBlockOnOpen(true);
                if (CheckConfigurationConfigureDialog.OK == dialog.open()
                        && config.isConfigurable())
                {
                    mNeedRebuild = true;
                }

                config.setContext(null);
            }
            catch (CheckstylePluginException e)
            {
                CheckstyleLog.error(e.getLocalizedMessage(), e);
                CheckstyleLog.errorDialog(getShell(), e.getLocalizedMessage(), e);
                return;
            }
        }
    }

    /**
     * Lets the user choose a project context.
     * 
     * @return the project or <code>null</code>
     */
    private IProject getProjectContext()
    {

        IProject context = null;

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                new WorkbenchLabelProvider(), new WorkbenchContentProvider());
        dialog.setBlockOnOpen(true);
        dialog.setTitle("Select project context");
        dialog.setMessage("Select project context for check configuration.");
        dialog.setAllowMultiple(false);
        dialog.setInput(CheckstylePlugin.getWorkspace().getRoot());

        //filter all but projects
        dialog.addFilter(new ViewerFilter()
        {
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                return element instanceof IProject;
            }
        });
        dialog.setValidator(new ISelectionStatusValidator()
        {
            public IStatus validate(Object[] selection)
            {
                if (selection.length == 1 && selection[0] instanceof IProject)
                {
                    return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, IStatus.ERROR, "", null);
                }
                else
                {
                    return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, "", null);
                }
            }
        });

        if (ElementTreeSelectionDialog.OK == dialog.open())
        {
            Object[] result = dialog.getResult();
            context = (IProject) result[0];
        }

        return context;
    }

    /**
     * Copy an existing config.
     */
    private void copyCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        ICheckConfiguration sourceConfig = (ICheckConfiguration) selection.getFirstElement();
        if (sourceConfig == null)
        {
            //
            // Nothing is selected.
            //
            return;
        }

        try
        {

            //create a new internal check configuration
            ICheckConfiguration newConfig = new InternalCheckConfiguration();
            IConfigurationType internalType = ConfigurationTypes.getByInternalName("internal");
            newConfig.initialize("Copy of " + sourceConfig.getName(), null, internalType,
                    sourceConfig.getDescription());

            //Open the properties dialog to change default name and description
            CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(
                    getShell(), newConfig);
            dialog.setBlockOnOpen(true);
            if (CheckConfigurationPropertiesDialog.OK == dialog.open())
            {

                //Copy the source configuration into the new internal config
                CheckConfigurationFactory.copyConfiguration(sourceConfig, newConfig);

                mCheckConfigurations.add(newConfig);
                mViewer.refresh();
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error(e.getLocalizedMessage(), e);
            CheckstyleLog.internalErrorDialog();
            return;
        }
    }

    /**
     * Remove a config.
     */
    private void removeCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        ICheckConfiguration checkConfig = (ICheckConfiguration) selection.getFirstElement();
        if (checkConfig == null || !checkConfig.isEditable())
        {
            //
            // Nothing is selected.
            //
            return;
        }

        //
        // Make sure the check config is not in use. Don't let it be
        // deleted if it is.
        //
        try
        {
            if (ProjectConfigurationFactory.isCheckConfigInUse(checkConfig.getName()))
            {
                MessageDialog.openInformation(getShell(), "Can't Delete",
                        "The Check Configuration '" + checkConfig.getName()
                                + "' is currently in use by a project."
                                + " It must be removed from all project "
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

        boolean confirm = MessageDialog.openQuestion(getShell(), "Confirm Delete",
                "Remove check configuration '" + checkConfig.getName() + "'?");
        if (confirm)
        {
            mCheckConfigurations.remove(checkConfig);
            mViewer.refresh();
        }
    }

    /**
     * Export a configuration.
     */
    private void exportCheckstyleCheckConfig()
    {
        IStructuredSelection selection = (IStructuredSelection) mViewer.getSelection();
        ICheckConfiguration config = (ICheckConfiguration) selection.getFirstElement();
        if (config == null)
        {
            //
            // Nothing is selected.
            //
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
            CheckConfigurationFactory.exportConfiguration(file, config);
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.error("Failed to export CheckConfigurations to external file", e);
            CheckstyleLog.errorDialog(getShell(),
                    "Failed to export CheckConfigurations to external file", e);
        }
    }

    private void importCheckstyleCheckConfig()
    {
    //        //
    //        // Get the full path to the file to be imported.
    //        //
    //        FileDialog fileDialog = new FileDialog(getShell());
    //        fileDialog.setText("Import Checkstyle Check Configuration");
    //        String path = fileDialog.open();
    //        if (path == null)
    //        {
    //            return;
    //        }
    //
    //        try
    //        {
    //            //
    //            // Load the config file.
    //            //
    //            CheckConfigConverter converter = new CheckConfigConverter();
    //            converter.loadConfig(path);
    //
    //            //
    //            // Resolve property values.
    //            //
    //            List resolveProps = converter.getPropsToResolve();
    //            if (resolveProps.size() > 0)
    //            {
    //                ResolvePropertyValuesDialog resolveDialog = new
    // ResolvePropertyValuesDialog(
    //                        getShell(), resolveProps);
    //                resolveDialog.open();
    //            }
    //
    //            //
    //            // Get a CheckConfiguration from the converter.
    //            //
    //            CheckConfiguration config = converter.getCheckConfiguration();
    //
    //            //
    //            // Add the config using the add dialog so the user can see what it
    //            // looks like,
    //            // make changes, and it will be validated.
    //            //
    //            addCheckConfig(config);
    //        }
    //        catch (CheckstylePluginException e)
    //        {
    //            CheckstyleLog.error("Failed to import CheckConfigurations from external
    // file", e);
    //            CheckstyleLog.internalErrorDialog();
    //        }
    }

    /**
     * Make a local working copy of the Check Configurations in case the user
     * exits the preferences window with the cancel button. In this case all
     * edits will be discarded, otherwise they are saved when the user presses
     * the OK button.
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
                ICheckConfiguration cfg = (ICheckConfiguration) iter.next();
                mCheckConfigurations.add(cfg.clone());
            }
        }
        catch (CloneNotSupportedException e)
        {
            CheckstyleLog.error("Failed to clone CheckConfiguration", e);
            throw new CheckstylePluginException("Failed to clone CheckConfiguration, "
                    + e.getMessage());
        }
    }

}