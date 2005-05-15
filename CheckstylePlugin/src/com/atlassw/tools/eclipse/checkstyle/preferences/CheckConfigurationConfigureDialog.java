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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
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
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleGroupMetadata;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
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
    private ICheckConfiguration mConfiguration;

    /** TreeViewer showing the known modules from the meta data. */
    private TreeViewer mTreeViewer;

    /** Button to add a module. */
    private Button mAddButton;

    /** The table viewer showing the configured modules. */
    private CheckboxTableViewer mTableViewer;

    /** Button to remove a module. */
    private Button mRemoveButton;

    /** Button to remove a module. */
    private Button mEditButton;

    /** Group containing the table viewer. */
    private Group mConfiguredModulesGroup;

    /** Textarea showing the description of a module. */
    private Text mTxtDescription;

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

    //
    // constructors
    //

    /**
     * Creates the configuration dialog.
     * 
     * @param parentShell the parent shell
     * @param config the check configuration
     */
    public CheckConfigurationConfigureDialog(Shell parentShell, ICheckConfiguration config)
    {
        super(parentShell);
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

        mTxtDescription = new Text(contents, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY
                | SWT.BORDER | SWT.VERTICAL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        mTxtDescription.setLayoutData(gd);

        // initialize the data
        initialize();

        return contents;
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
            //only write the modules back if the config is configurable
            //and was actually changed
            if (mConfiguration.isConfigurable() && mIsDirty)
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

        mTableViewer = new CheckboxTableViewer(table);
        mTableViewer.setLabelProvider(new ModuleLabelProvider());
        mTableViewer.setContentProvider(new ArrayContentProvider());
        mTableViewer.addFilter(mGroupFilter);

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
        IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
        mBtnOpenModuleOnAdd.setSelection(prefStore
                .getBoolean(CheckstylePlugin.PREF_OPEN_MODULE_EDITOR));
        mBtnOpenModuleOnAdd.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                // store translation preference
                IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
                prefStore.setValue(CheckstylePlugin.PREF_OPEN_MODULE_EDITOR, ((Button) e.widget)
                        .getSelection());
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

        mTreeViewer.setInput(MetadataFactory.getRuleGroupMetadata());

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

        if (mConfiguration.isConfigurable())
        {
            this.setMessage(Messages.CheckConfigurationConfigureDialog_msgEditConfig);
        }
        else
        {
            this.setMessage(Messages.CheckConfigurationConfigureDialog_msgReadonlyConfig);
        }

        // set the logo
        this.setTitleImage(CheckstylePlugin.getLogo());

        mAddButton.setEnabled(mConfiguration.isConfigurable());
        mRemoveButton.setEnabled(mConfiguration.isConfigurable());
    }

    /**
     * Controller for this page.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController implements ISelectionChangedListener, ICheckStateListener,
            IDoubleClickListener, SelectionListener, KeyListener
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
                newModule(event.getSelection());
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
        //NOOP
        }

        /**
         * @see ICheckStateListener#checkStateChanged(CheckStateChangedEvent)
         */
        public void checkStateChanged(CheckStateChangedEvent event)
        {
            if (mConfiguration.isConfigurable())
            {
                Module module = (Module) event.getElement();

                if (event.getChecked())
                {
                    module.setSeverity(module.getMetaData().getDefaultSeverityLevel());
                }
                else
                {
                    module.setSeverity(SeverityLevel.IGNORE);
                }
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
                mConfiguredModulesGroup.setText(NLS.bind(
                        Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        ((RuleGroupMetadata) element).getGroupName()));
                mTableViewer.refresh();

                refreshTableViewerState();
            }
            else if (element instanceof RuleMetadata)
            {

                description = ((RuleMetadata) element).getDescription();
                mGroupFilter.setCurrentGroup(((RuleMetadata) element).getGroup());
                mConfiguredModulesGroup.setText(NLS.bind(
                        Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                        ((RuleMetadata) element).getGroup().getGroupName()));
                mTableViewer.refresh();
                refreshTableViewerState();

            }
            else if (element instanceof Module)
            {
                description = ((Module) element).getMetaData().getDescription();
            }

            mTxtDescription.setText(description != null ? description
                    : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
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
                            getShell(), workingCopy, !mConfiguration.isConfigurable(),
                            Messages.CheckConfigurationConfigureDialog_titleModuleConfigEditor);
                    if (RuleConfigurationEditDialog.OK == dialog.open()
                            && mConfiguration.isConfigurable())
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
            if (mConfiguration.isConfigurable())
            {
                IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
                boolean openOnAdd = prefStore.getBoolean(CheckstylePlugin.PREF_OPEN_MODULE_EDITOR);

                Iterator it = ((IStructuredSelection) selection).iterator();
                while (it.hasNext())
                {
                    Object selectedElement = it.next();
                    if (selectedElement instanceof RuleMetadata)
                    {

                        try
                        {

                            Module workingCopy = new Module((RuleMetadata) selectedElement);

                            if (openOnAdd)
                            {

                                RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(
                                        getShell(), workingCopy, !mConfiguration.isConfigurable(),
                                        Messages.CheckConfigurationConfigureDialog_titleNewModule);
                                if (RuleConfigurationEditDialog.OK == dialog.open()
                                        && mConfiguration.isConfigurable())
                                {
                                    mModules.add(workingCopy);
                                    mIsDirty = true;
                                    mTableViewer.refresh(true);
                                    refreshTableViewerState();
                                }
                            }
                            else
                            {
                                mModules.add(workingCopy);
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
            }
        }

        /**
         * Creates a module editor for the current selection.
         * 
         * @param selection the selection
         */
        private void removeModule(ISelection selection)
        {

            if (!selection.isEmpty() && mConfiguration.isConfigurable())
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
                if (mConfiguration.isConfigurable())
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
    private static class MetaDataLabelProvider extends LabelProvider
    {

        private static Image sModuleGroupImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/modulegroup.gif").createImage(); //$NON-NLS-1$

        private static Image sModuleImage = CheckstylePlugin.imageDescriptorFromPlugin(
                CheckstylePlugin.PLUGIN_ID, "icons/module.gif").createImage(); //$NON-NLS-1$

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
                image = sModuleGroupImage;
            }
            else if (element instanceof RuleMetadata)
            {
                image = sModuleImage;
            }
            return image;
        }
    }

    /**
     * Label provider for the table showing the configured modules.
     * 
     * @author Lars Ködderitzsch
     */
    private class ModuleLabelProvider extends LabelProvider implements ITableLabelProvider
    {

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
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

            if (mCurrentGroup == null)
            {
                result = false;
            }
            else if (metaData.isHidden())
            {
                result = false;
            }
            else if (mCurrentGroup == moduleGroup)
            {
                result = true;
            }
            else if (moduleGroup == null && mCurrentGroup.getGroupName().equals("Other")) //$NON-NLS-1$
            {
                result = true;
            }
            // TODO Thats not too nice - make better
            else if (moduleGroup != null && moduleGroup.getGroupName().equals("Internal") //$NON-NLS-1$
                    && mCurrentGroup.getGroupName().equals("Other")) //$NON-NLS-1$
            {
                result = true;
            }

            return result;
        }

    }
}