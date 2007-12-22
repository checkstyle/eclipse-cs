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

package com.atlassw.tools.eclipse.checkstyle.config.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleGroupMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginImages;
import com.atlassw.tools.eclipse.checkstyle.util.SWTUtil;
import com.atlassw.tools.eclipse.checkstyle.util.table.EnhancedCheckBoxTableViewer;
import com.atlassw.tools.eclipse.checkstyle.util.table.ITableComparableProvider;
import com.atlassw.tools.eclipse.checkstyle.util.table.ITableSettingsProvider;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Enhanced checkstyle configuration editor.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckConfigurationConfigureDialog extends TitleAreaDialog
{

    //
    // attributes
    //

    /** The current check configuration. */
    private CheckConfigurationWorkingCopy mConfiguration;

    /** Flags if the Check configuration can be modified. */
    private boolean mConfigurable;

    /** Text field used to filter the module tree. */
    private Text mTxtTreeFilter;

    /** TreeViewer showing the known modules from the meta data. */
    private TreeViewer mTreeViewer;

    /** Button to add a module. */
    private Button mAddButton;

    /** The table viewer showing the configured modules. */
    private EnhancedCheckBoxTableViewer mTableViewer;

    /** Button to remove a module. */
    private Button mRemoveButton;

    /** Button to remove a module. */
    private Button mEditButton;

    /** Group containing the table viewer. */
    private Group mConfiguredModulesGroup;

    private Browser mBrowserDescription;

    /** Checkbox handling if the module editor is opened on add action. */
    private Button mBtnOpenModuleOnAdd;

    /** Filter for the table viewer to show only element of the selected group. */
    private RuleGroupModuleFilter mGroupFilter = new RuleGroupModuleFilter();

    /** Controller for this Dialog. */
    private PageController mController = new PageController();

    /** the list of modules. */
    private List mModules;

    /** Flags if the check configuration was changed. */
    private boolean mIsDirty;

    /** The default text for the filter text field. */
    private String mDefaultFilterText = Messages.CheckConfigurationConfigureDialog_defaultFilterText;

    /** The tree filter. */
    private TreeFilter mTreeFilter = new TreeFilter();

    //
    // constructors
    //

    /**
     * Creates the configuration dialog.
     * 
     * @param parentShell the parent shell
     * @param config the check configuration
     */
    public CheckConfigurationConfigureDialog(Shell parentShell, CheckConfigurationWorkingCopy config)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        mConfiguration = config;
    }

    //
    // methods
    //

    /**
     * Creates the dialogs main contents.
     * 
     * @param parent the parent composite
     */
    protected Control createDialogArea(Composite parent)
    {

        Composite composite = (Composite) super.createDialogArea(parent);

        Composite contents = new Composite(composite, SWT.NULL);
        contents.setLayoutData(new GridData(GridData.FILL_BOTH));
        contents.setLayout(new GridLayout());

        SashForm sashForm = new SashForm(contents, SWT.NULL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 700;
        gd.heightHint = 400;
        sashForm.setLayoutData(gd);
        sashForm.setLayout(new GridLayout());

        Control treeControl = createTreeViewer(sashForm);
        treeControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control tableControl = createTableViewer(sashForm);
        tableControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        sashForm.setWeights(new int[] { 30, 70 });

        Label lblDescription = new Label(contents, SWT.NULL);
        lblDescription.setText(Messages.CheckConfigurationConfigureDialog_lblDescription);
        lblDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mBrowserDescription = new Browser(contents, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        mBrowserDescription.setLayoutData(gd);

        // initialize the data
        initialize();

        return contents;
    }

    /**
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create()
    {
        super.create();

        SWTUtil.addResizeSupport(this, CheckstylePlugin.getDefault().getDialogSettings(),
                CheckConfigurationConfigureDialog.class.getName());
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(Messages.CheckConfigurationConfigureDialog_titleCheckConfigurationDialog);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed()
    {

        try
        {
            // only write the modules back if the config is configurable
            // and was actually changed
            if (mConfigurable && mIsDirty)
            {
                mConfiguration.setModules(mModules);
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.errorDialog(getShell(), e, true);
        }

        super.okPressed();
    }

    private Control createTreeViewer(Composite parent)
    {

        Group knownModules = new Group(parent, SWT.NULL);
        knownModules.setLayout(new GridLayout());
        knownModules.setText(Messages.CheckConfigurationConfigureDialog_lblKnownModules);

        mTxtTreeFilter = new Text(knownModules, SWT.SINGLE | SWT.BORDER);
        mTxtTreeFilter.setText(mDefaultFilterText);
        mTxtTreeFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTxtTreeFilter.addModifyListener(mController);

        // select all of the default text on focus gain
        mTxtTreeFilter.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                if (mDefaultFilterText.equals(mTxtTreeFilter.getText()))
                {
                    getShell().getDisplay().asyncExec(new Runnable()
                    {

                        public void run()
                        {
                            mTxtTreeFilter.selectAll();
                        }
                    });
                }
            }

            public void focusLost(FocusEvent e)
            {}
        });

        mTreeViewer = new TreeViewer(knownModules, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        mTreeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        mTreeViewer.setContentProvider(new MetaDataContentProvider());
        mTreeViewer.setLabelProvider(new MetaDataLabelProvider());
        mTreeViewer.addSelectionChangedListener(mController);
        mTreeViewer.addDoubleClickListener(mController);
        mTreeViewer.getTree().addKeyListener(mController);

        // filter hidden elements
        mTreeViewer.addFilter(new ViewerFilter()
        {

            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                boolean passes = true;
                if (element instanceof RuleGroupMetadata)
                {
                    passes = !((RuleGroupMetadata) element).isHidden();
                }
                else if (element instanceof RuleMetadata)
                {
                    passes = !((RuleMetadata) element).isHidden();
                }
                return passes;
            }
        });

        mAddButton = new Button(knownModules, SWT.PUSH);
        mAddButton.setText((Messages.CheckConfigurationConfigureDialog_btnAdd));
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        mAddButton.setLayoutData(gd);
        mAddButton.addSelectionListener(mController);

        return knownModules;
    }

    private Control createTableViewer(Composite parent)
    {

        mConfiguredModulesGroup = new Group(parent, SWT.NULL);
        mConfiguredModulesGroup.setLayout(new GridLayout());
        mConfiguredModulesGroup.setText("\0"); //$NON-NLS-1$

        Table table = new Table(mConfiguredModulesGroup, SWT.CHECK | SWT.BORDER | SWT.MULTI
                | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        TableColumn column1 = new TableColumn(table, SWT.NULL);
        column1.setAlignment(SWT.CENTER);
        column1.setText(Messages.CheckConfigurationConfigureDialog_colEnabled);
        tableLayout.addColumnData(new ColumnWeightData(15));

        TableColumn column2 = new TableColumn(table, SWT.NULL);
        column2.setText(Messages.CheckConfigurationConfigureDialog_colModule);
        tableLayout.addColumnData(new ColumnWeightData(30));

        TableColumn column3 = new TableColumn(table, SWT.NULL);
        column3.setText(Messages.CheckConfigurationConfigureDialog_colSeverity);
        tableLayout.addColumnData(new ColumnWeightData(20));

        TableColumn column4 = new TableColumn(table, SWT.NULL);
        column4.setText(Messages.CheckConfigurationConfigureDialog_colComment);
        tableLayout.addColumnData(new ColumnWeightData(35));

        mTableViewer = new EnhancedCheckBoxTableViewer(table);
        ModuleLabelProvider multiProvider = new ModuleLabelProvider();
        mTableViewer.setLabelProvider(multiProvider);
        mTableViewer.setTableComparableProvider(multiProvider);
        mTableViewer.setTableSettingsProvider(multiProvider);
        mTableViewer.setContentProvider(new ArrayContentProvider());
        mTableViewer.addFilter(mGroupFilter);
        mTableViewer.installEnhancements();

        mTableViewer.addDoubleClickListener(mController);
        mTableViewer.addSelectionChangedListener(mController);
        mTableViewer.addCheckStateListener(mController);
        mTableViewer.getTable().addKeyListener(mController);

        Composite buttons = new Composite(mConfiguredModulesGroup, SWT.NULL);
        GridLayout layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData());

        mRemoveButton = new Button(buttons, SWT.PUSH);
        mRemoveButton.setText((Messages.CheckConfigurationConfigureDialog_btnRemove));
        mRemoveButton.setLayoutData(new GridData());
        mRemoveButton.addSelectionListener(mController);

        mEditButton = new Button(buttons, SWT.PUSH);
        mEditButton.setText((Messages.CheckConfigurationConfigureDialog_btnOpen));
        mEditButton.setLayoutData(new GridData());
        mEditButton.addSelectionListener(mController);

        return mConfiguredModulesGroup;
    }

    protected Control createButtonBar(Composite parent)
    {

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mBtnOpenModuleOnAdd = new Button(composite, SWT.CHECK);
        mBtnOpenModuleOnAdd.setText(Messages.CheckConfigurationConfigureDialog_btnOpenModuleOnAdd);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalIndent = 5;
        mBtnOpenModuleOnAdd.setLayoutData(gd);

        // Init the translate tokens preference
        IPreferencesService prefStore = Platform.getPreferencesService();
        mBtnOpenModuleOnAdd.setSelection(prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_OPEN_MODULE_EDITOR, true, null));
        mBtnOpenModuleOnAdd.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                // store translation preference
                IEclipsePreferences prefStore = new InstanceScope()
                        .getNode(CheckstylePlugin.PLUGIN_ID);
                prefStore.putBoolean(CheckstylePlugin.PREF_OPEN_MODULE_EDITOR, ((Button) e.widget)
                        .getSelection());
                try
                {
                    prefStore.flush();
                }
                catch (BackingStoreException e1)
                {
                    CheckstyleLog.log(e1);
                }
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            // NOOP
            }
        });

        Control buttonBar = super.createButtonBar(composite);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttonBar.setLayoutData(gd);

        return composite;
    }

    /**
     * Initialize the dialogs controls with the data.
     */
    private void initialize()
    {

        mConfigurable = mConfiguration.isConfigurable();

        try
        {
            mModules = mConfiguration.getModules();
        }
        catch (CheckstylePluginException e)
        {
            mModules = new ArrayList();
            CheckstyleLog.errorDialog(getShell(), e, true);
        }
        mTableViewer.setInput(mModules);

        this.setTitle(NLS.bind(Messages.CheckConfigurationConfigureDialog_titleMessageArea,
                mConfiguration.getType().getName(), mConfiguration.getName()));

        if (mConfigurable)
        {
            this.setMessage(Messages.CheckConfigurationConfigureDialog_msgEditConfig);
        }
        else
        {
            this.setMessage(Messages.CheckConfigurationConfigureDialog_msgReadonlyConfig);
        }

        // set the logo
        this.setTitleImage(CheckstylePluginImages.getImage(CheckstylePluginImages.PLUGIN_LOGO));

        mAddButton.setEnabled(mConfigurable);
        mRemoveButton.setEnabled(mConfigurable);

        mTreeViewer.setInput(MetadataFactory.getRuleGroupMetadata());
        ISelection initialSelection = new StructuredSelection(MetadataFactory
                .getRuleGroupMetadata().get(0));
        mTreeViewer.setSelection(initialSelection);
    }

    /**
     * Controller for this page.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController implements ISelectionChangedListener, ICheckStateListener,
            IDoubleClickListener, SelectionListener, KeyListener, ModifyListener
    {

        /**
         * @see IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
         */
        public void doubleClick(DoubleClickEvent event)
        {
            if (event.getViewer() == mTableViewer)
            {
                openModule(event.getSelection());
            }
            else if (event.getViewer() == mTreeViewer)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object element = selection.getFirstElement();

                if (element instanceof RuleGroupMetadata)
                {
                    mTreeViewer.setExpandedState(element, !mTreeViewer.getExpandedState(element));
                }
                else
                {
                    newModule(event.getSelection());
                }
            }
        }

        /**
         * @see SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e)
        {

            if (mEditButton == e.widget)
            {
                openModule(mTableViewer.getSelection());
            }
            else if (mAddButton == e.widget)
            {
                newModule(mTreeViewer.getSelection());
            }
            else if (mRemoveButton == e.widget)
            {
                removeModule(mTableViewer.getSelection());
            }
        }

        /**
         * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
         */
        public void keyReleased(KeyEvent e)
        {
            if (e.widget == mTableViewer.getTable())
            {
                if (e.character == SWT.DEL || e.keyCode == SWT.ARROW_LEFT)
                {
                    removeModule(mTableViewer.getSelection());
                }
            }
            if (e.widget == mTreeViewer.getTree())
            {
                if (e.keyCode == SWT.ARROW_RIGHT || e.character == ' ')
                {
                    newModule(mTreeViewer.getSelection());
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public void modifyText(ModifyEvent e)
        {
            if (!mDefaultFilterText.equals(mTxtTreeFilter.getText())
                    && StringUtils.trimToNull(mTxtTreeFilter.getText()) != null)
            {
                mTreeViewer.removeFilter(mTreeFilter);
                mTreeViewer.addFilter(mTreeFilter);
                mTreeViewer.refresh();
                // mTreeViewer.expandAll();
            }
            else
            {
                mTreeViewer.removeFilter(mTreeFilter);
                mTreeViewer.refresh();
            }
        }

        /**
         * @see SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetDefaultSelected(SelectionEvent e)
        {
        // NOOP
        }

        /**
         * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        public void keyPressed(KeyEvent e)
        {
        // NOOP
        }

        /**
         * @see ICheckStateListener#checkStateChanged(CheckStateChangedEvent)
         */
        public void checkStateChanged(CheckStateChangedEvent event)
        {
            if (mConfigurable)
            {
                Module module = (Module) event.getElement();

                if (event.getChecked())
                {
                    // restore last severity before setting to ignore
                    SeverityLevel lastEnabled = module.getLastEnabledSeverity();
                    if (lastEnabled != null)
                    {
                        module.setSeverity(lastEnabled);
                    }
                    else
                    {
                        module.setSeverity(module.getMetaData().getDefaultSeverityLevel());
                    }
                }
                else
                {
                    module.setSeverity(SeverityLevel.IGNORE);
                }
                mIsDirty = true;
                mTableViewer.refresh(module, true);
            }
            refreshTableViewerState();
        }

        /**
         * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event)
        {

            IStructuredSelection selection = (IStructuredSelection) event.getSelection();

            Object element = selection.getFirstElement();
            String description = null;

            if (element instanceof RuleGroupMetadata)
            {
                mGroupFilter.setCurrentGroup((RuleGroupMetadata) element);
                mConfiguredModulesGroup.setText(Messages.bind(
                        Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        ((RuleGroupMetadata) element).getGroupName()));
                mTableViewer.refresh();

                refreshTableViewerState();
            }
            else if (element instanceof RuleMetadata)
            {

                description = ((RuleMetadata) element).getDescription();
                mGroupFilter.setCurrentGroup(((RuleMetadata) element).getGroup());
                mConfiguredModulesGroup.setText(Messages.bind(
                        Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        ((RuleMetadata) element).getGroup().getGroupName()));
                mTableViewer.refresh();
                refreshTableViewerState();

            }
            else if (element instanceof Module)
            {
                RuleMetadata meta = ((Module) element).getMetaData();
                if (meta != null)
                {
                    description = meta.getDescription();
                }
            }

            StringBuffer buf = new StringBuffer();
            buf.append("<html><body style=\"margin: 3px; font-size: 11px; ");
            buf.append("font-family: verdana, 'trebuchet MS', helvetica, sans-serif;\">");
            buf.append(description != null ? description
                    : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
            buf.append("</body></html>");
            mBrowserDescription.setText(buf.toString());
        }

        /**
         * Opens the module editor for the current selection.
         * 
         * @param selection the selection
         */
        private void openModule(ISelection selection)
        {

            Module m = (Module) ((IStructuredSelection) selection).getFirstElement();
            if (m != null)
            {
                try
                {

                    Module workingCopy = (Module) m.clone();

                    RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(
                            getShell(), workingCopy, !mConfigurable,
                            Messages.CheckConfigurationConfigureDialog_titleModuleConfigEditor);
                    if (RuleConfigurationEditDialog.OK == dialog.open() && mConfigurable)
                    {
                        mModules.set(mModules.indexOf(m), workingCopy);
                        mIsDirty = true;
                        mTableViewer.refresh(true);
                        refreshTableViewerState();
                    }
                }
                catch (CheckstylePluginException e)
                {
                    CheckstyleLog.errorDialog(getShell(), e, true);
                }
            }
        }

        /**
         * Creates a module editor for the current selection.
         * 
         * @param selection the selection
         */
        private void newModule(ISelection selection)
        {
            if (mConfigurable)
            {
                IPreferencesService prefStore = Platform.getPreferencesService();
                boolean openOnAdd = prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                        CheckstylePlugin.PREF_OPEN_MODULE_EDITOR, true, null);

                Iterator it = ((IStructuredSelection) selection).iterator();
                while (it.hasNext())
                {
                    Object selectedElement = it.next();
                    if (selectedElement instanceof RuleGroupMetadata)
                    {
                        // if group is selected add all modules from this group
                        List rules = ((RuleGroupMetadata) selectedElement).getRuleMetadata();

                        IStructuredSelection allRulesOfGroupSelection = new StructuredSelection(
                                rules);
                        newModule(allRulesOfGroupSelection);
                    }
                    else if (selectedElement instanceof RuleMetadata)
                    {

                        try
                        {

                            RuleMetadata metadata = (RuleMetadata) selectedElement;

                            // check if the module is a singleton and already
                            // configured
                            if (metadata.isSingleton() && isAlreadyConfigured(metadata))
                            {
                                return;
                            }

                            Module workingCopy = new Module(metadata, false);

                            if (openOnAdd)
                            {

                                RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(
                                        getShell(), workingCopy, !mConfigurable,
                                        Messages.CheckConfigurationConfigureDialog_titleNewModule);
                                if (RuleConfigurationEditDialog.OK == dialog.open()
                                        && mConfigurable)
                                {
                                    mModules.add(workingCopy);
                                    mIsDirty = true;
                                    mTableViewer.refresh(true);
                                    refreshTableViewerState();
                                    mTreeViewer.refresh();
                                }
                            }
                            else
                            {
                                mModules.add(workingCopy);
                                mIsDirty = true;
                                mTableViewer.refresh(true);
                                refreshTableViewerState();
                                mTreeViewer.refresh();
                            }
                        }
                        catch (CheckstylePluginException e)
                        {
                            CheckstyleLog.errorDialog(getShell(), e, true);
                        }
                    }
                }
            }

        }

        /**
         * Creates a module editor for the current selection.
         * 
         * @param selection the selection
         */
        private void removeModule(ISelection selection)
        {

            if (!selection.isEmpty() && mConfigurable)
            {

                if (MessageDialog.openConfirm(getShell(),
                        Messages.CheckConfigurationConfigureDialog_titleRemoveModules,
                        Messages.CheckConfigurationConfigureDialog_msgRemoveModules))
                {

                    Iterator it = ((IStructuredSelection) selection).iterator();
                    while (it.hasNext())
                    {
                        Module m = (Module) it.next();
                        if (m.getMetaData().isDeletable())
                        {
                            mModules.remove(m);
                            mIsDirty = true;
                            mTableViewer.refresh(true);
                            refreshTableViewerState();
                            mTreeViewer.refresh();
                        }
                    }
                }
            }
        }

        /**
         * Restores the checked state of the table items.
         */
        private void refreshTableViewerState()
        {

            // set selected modules (Modules where severity is not Ignore).
            int size = mModules != null ? mModules.size() : 0;
            for (int i = 0; i < size; i++)
            {
                Module module = (Module) mModules.get(i);
                if (mConfigurable)
                {
                    mTableViewer.setChecked(module, !SeverityLevel.IGNORE.equals(module
                            .getSeverity())
                            || !module.getMetaData().hasSeverity());
                }
                else
                {
                    mTableViewer.setChecked(module, !SeverityLevel.IGNORE.equals(module
                            .getSeverity())
                            || !module.getMetaData().hasSeverity());
                    mTableViewer.setGrayed(module, !SeverityLevel.IGNORE.equals(module
                            .getSeverity()));
                }
            }
        }

        /**
         * Checks if a certain module is already contained in the configuration.
         */
        private boolean isAlreadyConfigured(RuleMetadata metadata)
        {
            String internalName = metadata.getInternalName();
            boolean containsModule = false;
            for (int i = 0, size = mModules.size(); i < size; i++)
            {

                Module module = (Module) mModules.get(i);

                if (internalName.equals(module.getMetaData().getInternalName()))
                {
                    containsModule = true;
                    break;
                }

            }
            return containsModule;
        }

    }

    /**
     * TreeContentProvider that provides the structure of the rule metadata.
     * 
     * @author Lars Ködderitzsch
     */
    private class MetaDataContentProvider implements ITreeContentProvider
    {

        /**
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement)
        {
            Object[] ruleGroups = null;
            if (inputElement instanceof List)
            {
                ruleGroups = ((List) inputElement).toArray();
            }
            return ruleGroups;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement)
        {
            Object[] children = null;
            if (parentElement instanceof List)
            {
                children = getElements(parentElement);
            }
            else if (parentElement instanceof RuleGroupMetadata)
            {
                children = ((RuleGroupMetadata) parentElement).getRuleMetadata().toArray();
            }

            return children;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element)
        {
            Object parent = null;
            if (element instanceof RuleMetadata)
            {
                parent = ((RuleMetadata) element).getGroup();
            }
            return parent;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element)
        {
            boolean hasChildren = false;

            if (element instanceof RuleGroupMetadata)
            {
                hasChildren = ((RuleGroupMetadata) element).getRuleMetadata().size() > 0;
            }
            else if (element instanceof RuleMetadata)
            {
                hasChildren = false;
            }
            return hasChildren;
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose()
        {
        // NOOP
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(
         *      org.eclipse.jface.viewers.Viewer, java.lang.Object,
         *      java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        // NOOP
        }
    }

    /**
     * Label-provider for meta data information.
     * 
     * @author Lars Ködderitzsch
     */
    private class MetaDataLabelProvider extends LabelProvider
    {

        private Image mModuleGroupImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/modulegroup.gif").createImage(); //$NON-NLS-1$

        private Image mModuleGroupUsedImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/modulegroup_used.gif").createImage(); //$NON-NLS-1$

        private Image mModuleImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/module.gif").createImage(); //$NON-NLS-1$

        private Image mModuleUsedImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/module_used.gif").createImage(); //$NON-NLS-1$

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element)
        {
            String text = null;
            if (element instanceof RuleGroupMetadata)
            {
                text = ((RuleGroupMetadata) element).getGroupName();
            }
            else if (element instanceof RuleMetadata)
            {
                text = ((RuleMetadata) element).getRuleName();
            }
            return text;
        }

        /**
         * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
         */
        public Image getImage(Object element)
        {
            Image image = null;

            if (element instanceof RuleGroupMetadata)
            {
                image = isGroupUsed((RuleGroupMetadata) element) ? mModuleGroupUsedImage
                        : mModuleGroupImage;
            }
            else if (element instanceof RuleMetadata)
            {

                image = isMetadataUsed((RuleMetadata) element) ? mModuleUsedImage : mModuleImage;
            }
            return image;
        }

        private boolean isGroupUsed(RuleGroupMetadata group)
        {
            boolean used = true;

            Iterator it = group.getRuleMetadata().iterator();
            while (it.hasNext())
            {

                RuleMetadata metadata = (RuleMetadata) it.next();

                if (!isMetadataUsed(metadata))
                {
                    used = false;
                    break;
                }
            }
            return used;
        }

        private boolean isMetadataUsed(RuleMetadata metadata)
        {
            boolean used = false;
            if (mModules != null)
            {
                Iterator it = mModules.iterator();
                while (it.hasNext())
                {
                    Module module = (Module) it.next();

                    if (metadata.equals(module.getMetaData()))
                    {
                        used = true;
                        break;
                    }
                }
            }

            return used;
        }
    }

    /**
     * Label provider for the table showing the configured modules.
     * 
     * @author Lars Ködderitzsch
     */
    private class ModuleLabelProvider extends LabelProvider implements ITableLabelProvider,
            ITableComparableProvider, ITableSettingsProvider
    {

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object element, int columnIndex)
        {
            String text = null;

            if (element instanceof Module)
            {

                Module module = (Module) element;
                switch (columnIndex)
                {

                    case 0:
                        text = new String();
                        break;
                    case 1:
                        text = module.getName() != null ? module.getName() : new String();
                        break;
                    case 2:
                        text = module.getSeverity() != null ? module.getSeverity().getName()
                                : new String();
                        break;
                    case 3:
                        text = module.getComment() != null ? module.getComment() : new String();
                        break;
                    default:
                        text = new String();
                        break;
                }
            }
            return text;
        }

        /**
         * {@inheritDoc}
         */
        public Comparable getComparableValue(Object element, int col)
        {
            if (element instanceof Module && col == 0)
            {
                return SeverityLevel.IGNORE.equals(((Module) element).getSeverity()) ? new Integer(
                        0) : new Integer(1);
            }

            return getColumnText(element, col);
        }

        /**
         * {@inheritDoc}
         */
        public IDialogSettings getTableSettings()
        {
            String concreteViewId = CheckConfigurationConfigureDialog.class.getName();

            IDialogSettings workbenchSettings = CheckstylePlugin.getDefault().getDialogSettings();
            IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

            if (settings == null)
            {
                settings = workbenchSettings.addNewSection(concreteViewId);
            }

            return settings;
        }
    }

    /**
     * Viewer filter that includes all modules that belong to the currently
     * selected group.
     * 
     * @author Lars Ködderitzsch
     */
    private class RuleGroupModuleFilter extends ViewerFilter
    {

        /** the current rule group. */
        private RuleGroupMetadata mCurrentGroup;

        /**
         * Sets the current rule group.
         * 
         * @param groupMetaData
         */
        public void setCurrentGroup(RuleGroupMetadata groupMetaData)
        {
            mCurrentGroup = groupMetaData;
        }

        /**
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public boolean select(Viewer viewer, Object parentElement, Object element)
        {
            boolean result = false;

            Module module = (Module) element;
            RuleMetadata metaData = module.getMetaData();

            if (metaData == null)
            {
                return true;
            }

            RuleGroupMetadata moduleGroup = metaData.getGroup();

            if (mCurrentGroup == null || metaData.isHidden())
            {
                result = false;
            }
            else if (mCurrentGroup == moduleGroup)
            {
                result = true;
            }

            return result;
        }
    }

    /**
     * Filter implementation that filters the module tree with respect of a
     * filter text field to input a search word.
     * 
     * @author Lars Ködderitzsch
     */
    private class TreeFilter extends ViewerFilter
    {

        public boolean select(Viewer viewer, Object parentElement, Object element)
        {
            boolean result = true;

            String filterText = mTxtTreeFilter.getText();

            if (element instanceof RuleMetadata)
            {
                result = selectRule((RuleMetadata) element, filterText);
            }
            else if (element instanceof RuleGroupMetadata)
            {
                result = selectGroup((RuleGroupMetadata) element, filterText);
            }

            return result;
        }

        private boolean selectRule(RuleMetadata element, String filterText)
        {
            boolean passes = StringUtils.containsIgnoreCase(element.getRuleName(), filterText);

            if (!passes)
            {
                passes = StringUtils.containsIgnoreCase(element.getDescription(), filterText);
            }

            return passes;
        }

        private boolean selectGroup(RuleGroupMetadata group, String filterText)
        {
            boolean hasAtLeastOneMatchingChild = false;

            List rules = group.getRuleMetadata();
            for (int i = 0, size = rules.size(); i < size; i++)
            {
                RuleMetadata element = (RuleMetadata) rules.get(i);
                if (selectRule(element, filterText))
                {
                    hasAtLeastOneMatchingChild = true;
                    break;
                }
            }

            return hasAtLeastOneMatchingChild;
        }
    }
}