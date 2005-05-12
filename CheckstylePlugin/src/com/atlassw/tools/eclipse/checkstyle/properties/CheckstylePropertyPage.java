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

package com.atlassw.tools.eclipse.checkstyle.properties;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.ErrorMessages;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.builder.BuildProjectJob;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.nature.CheckstyleNature;
import com.atlassw.tools.eclipse.checkstyle.nature.ConfigureDeconfigureNatureJob;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.FileSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilter;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.filters.IFilterEditor;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Property page for projects to enable checkstyle audit.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstylePropertyPage extends PropertyPage
{

    //
    // controls
    //

    /** button to enable checkstyle for the project. */
    private Button mChkEnable;

    /** button to enable/disable the simple configuration. */
    private Button mChkSimpleConfig;

    /** the container holding the file sets editor. */
    private Composite mFileSetsContainer;

    /** the editor for the file sets. */
    private IFileSetsEditor mFileSetsEditor;

    /** viewer to display the known checkstyle filters. */
    private CheckboxTableViewer mFilterList;

    /** button to open a filter editor. */
    private Button mBtnEditFilter;

    /** used to display the filter description. */
    private Text mTxtFilterDescription;

    //
    // other members
    //

    /** controller of this page. */
    private PageController mPageController;

    /** the the original project configuration. */
    private ProjectConfiguration mProjectConfigOrig;

    /** the actual working data for this form. */
    private ProjectConfiguration mProjectConfig;

    /** the project. */
    private IProject mProject;

    private boolean mCheckstyleActivated;

    //
    // methods
    //

    /**
     * Create the contents of this page.
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(
     *      org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent)
    {

        Composite container = null;

        try
        {
            // initialize the forms data
            initialize();

            this.mPageController = new PageController();

            // suppress default- & apply-buttons
            noDefaultAndApplyButton();

            // create the main container
            container = new Composite(parent, SWT.NULL);
            container.setLayout(new FormLayout());
            container.setLayoutData(new GridData(GridData.FILL_BOTH));

            // create the checkbox to enable/diable the simple configuration
            this.mChkSimpleConfig = new Button(container, SWT.CHECK);
            this.mChkSimpleConfig.setText(Messages.CheckstylePropertyPage_btnUseSimpleConfig);
            this.mChkSimpleConfig.addSelectionListener(this.mPageController);
            this.mChkSimpleConfig.setSelection(mProjectConfig.isUseSimpleConfig());

            FormData fd = new FormData();
            // fd.left = new FormAttachment(this.mChkEnable, 0, SWT.RIGHT);
            fd.top = new FormAttachment(0);
            fd.right = new FormAttachment(100);
            this.mChkSimpleConfig.setLayoutData(fd);

            // create the checkbox to enabel/disable checkstyle
            this.mChkEnable = new Button(container, SWT.CHECK);
            this.mChkEnable.setText(Messages.CheckstylePropertyPage_btnActivateCheckstyle);
            this.mChkEnable.addSelectionListener(this.mPageController);
            this.mChkEnable.setSelection(mCheckstyleActivated);

            fd = new FormData();
            fd.left = new FormAttachment(0);
            fd.top = new FormAttachment(0);
            fd.right = new FormAttachment(this.mChkSimpleConfig, 0, SWT.LEFT);
            this.mChkEnable.setLayoutData(fd);

            // create the configuration area
            mFileSetsContainer = new Composite(container, SWT.NULL);
            Control configArea = createFileSetsArea(mFileSetsContainer);
            fd = new FormData();
            fd.left = new FormAttachment(0);
            fd.top = new FormAttachment(this.mChkEnable, 6, SWT.BOTTOM);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(50);
            configArea.setLayoutData(fd);

            // create the filter area
            Control filterArea = createFilterArea(container);
            fd = new FormData();
            fd.left = new FormAttachment(0);
            fd.top = new FormAttachment(configArea, 3, SWT.BOTTOM);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);
            filterArea.setLayoutData(fd);

        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog
                    .errorDialog(getShell(), ErrorMessages.errorOpeningPropertiesPage, e, true);
        }

        return container;
    }

    /**
     * Creates the file sets area.
     * 
     * @param fileSetsContainer the container to add the file sets area to
     */
    private Control createFileSetsArea(Composite fileSetsContainer)
        throws CheckstylePluginException
    {

        Control[] controls = fileSetsContainer.getChildren();
        for (int i = 0; i < controls.length; i++)
        {
            controls[i].dispose();
        }

        if (mProjectConfig.isUseSimpleConfig())
        {
            mFileSetsEditor = new SimpleFileSetsEditor(this);
        }
        else
        {
            mFileSetsEditor = new ComplexFileSetsEditor(this);
        }

        mFileSetsEditor.setFileSets(mProjectConfig.getFileSets());

        Control editor = mFileSetsEditor.createContents(mFileSetsContainer);

        fileSetsContainer.setLayout(new FormLayout());
        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        editor.setLayoutData(fd);

        return fileSetsContainer;
    }

    /**
     * Creates the filter area.
     * 
     * @param container the container to add the filter area
     */
    private Control createFilterArea(Composite container)
    {

        FormData fd = new FormData();

        // group composite containing the filter settings
        Group filterArea = new Group(container, SWT.NULL);
        filterArea.setText(Messages.CheckstylePropertyPage_titleFilterGroup);

        filterArea.setLayout(new FormLayout());

        this.mFilterList = CheckboxTableViewer.newCheckList(filterArea, SWT.BORDER);
        this.mBtnEditFilter = new Button(filterArea, SWT.PUSH);

        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(0, 3);
        fd.right = new FormAttachment(this.mBtnEditFilter, -3, SWT.LEFT);
        fd.bottom = new FormAttachment(70, -3);
        this.mFilterList.getTable().setLayoutData(fd);

        this.mFilterList.setLabelProvider(new LabelProvider()
        {

            public String getText(Object element)
            {

                StringBuffer buf = new StringBuffer();

                if (element instanceof IFilter)
                {

                    IFilter filter = (IFilter) element;

                    buf.append(filter.getName());
                    if (filter.getPresentableFilterData() != null)
                    {
                        buf.append(": ").append(filter.getPresentableFilterData()); //$NON-NLS-1$
                    }
                }
                else
                {
                    buf.append(super.getText(element));
                }

                return buf.toString();
            }
        });
        this.mFilterList.setContentProvider(new ArrayContentProvider());
        this.mFilterList.addSelectionChangedListener(this.mPageController);
        this.mFilterList.addDoubleClickListener(this.mPageController);
        this.mFilterList.addCheckStateListener(this.mPageController);

        this.mBtnEditFilter.setText(Messages.CheckstylePropertyPage_btnChangeFilter);
        this.mBtnEditFilter.addSelectionListener(this.mPageController);

        // don't show readonly filters
        mFilterList.addFilter(new ViewerFilter()
        {
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                return !((IFilter) element).isReadonly();
            }
        });

        fd = new FormData();
        fd.top = new FormAttachment(0, 3);
        fd.right = new FormAttachment(100, -3);
        this.mBtnEditFilter.setLayoutData(fd);

        // Description
        Label lblDesc = new Label(filterArea, SWT.LEFT);
        lblDesc.setText(Messages.CheckstylePropertyPage_lblDescription);
        fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(this.mFilterList.getTable(), 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100, -3);
        lblDesc.setLayoutData(fd);

        this.mTxtFilterDescription = new Text(filterArea, SWT.LEFT | SWT.WRAP | SWT.MULTI
                | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
        fd = new FormData();
        fd.left = new FormAttachment(0, 3);
        fd.top = new FormAttachment(lblDesc, 3, SWT.BOTTOM);
        fd.right = new FormAttachment(100, -3);
        fd.bottom = new FormAttachment(100, -3);
        this.mTxtFilterDescription.setLayoutData(fd);

        // intialize filter list
        IFilter[] filterDefs = mProjectConfig.getFilters();
        this.mFilterList.setInput(filterDefs);

        // set the checked state
        for (int i = 0; i < filterDefs.length; i++)
        {
            this.mFilterList.setChecked(filterDefs[i], filterDefs[i].isEnabled());
        }

        // set the readonly state
        for (int i = 0; i < filterDefs.length; i++)
        {
            this.mFilterList.setGrayed(filterDefs[i], filterDefs[i].isReadonly());
        }

        this.mBtnEditFilter.setEnabled(false);

        return filterArea;
    }

    private void initialize() throws CheckstylePluginException
    {

        //
        // Get the project.
        //
        IResource resource = (IResource) getElement();
        if (resource.getType() == IResource.PROJECT)
        {
            mProject = (IProject) resource;
        }

        mProjectConfigOrig = ProjectConfigurationFactory.getConfiguration(mProject);
        mProjectConfig = (ProjectConfiguration) mProjectConfigOrig.clone();

        try
        {
            mCheckstyleActivated = mProject.hasNature(CheckstyleNature.NATURE_ID);
        }
        catch (CoreException e1)
        {
            CheckstylePluginException.rethrow(e1);
        }
    }

    /**
     * @see org.eclipse.jface.preference.IPreferencePage#isValid()()
     */
    public boolean isValid()
    {
        // check if all check configurations resolve
        List fileSets = mProjectConfig.getFileSets();
        Iterator it = fileSets.iterator();
        while (it.hasNext())
        {
            FileSet fileset = (FileSet) it.next();
            ICheckConfiguration checkConfig = fileset.getCheckConfig();
            if (checkConfig != null)
            {
                if (checkConfig.isContextNeeded())
                {
                    checkConfig.setContext(mProject);
                }

                try
                {
                    checkConfig.getCheckstyleConfigurationURL();
                }
                catch (CheckstylePluginException e)
                {

                    CheckstyleLog.warningDialog(getShell(), NLS.bind(
                            ErrorMessages.errorCannotResolveCheckLocation, checkConfig
                                    .getLocation(), checkConfig.getName()), e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @return the result of the ok action
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {

        try
        {

            // save the edited form data
            ProjectConfigurationFactory.setConfiguration(mProjectConfig, mProject);

            boolean checkstyleEnabled = mChkEnable.getSelection();
            boolean needRebuild = !this.mProjectConfigOrig.equals(this.mProjectConfig);

            // check if checkstyle nature has to be configured/deconfigured
            if (checkstyleEnabled != mCheckstyleActivated)
            {

                ConfigureDeconfigureNatureJob configOperation = new ConfigureDeconfigureNatureJob(
                        mProject, CheckstyleNature.NATURE_ID);
                configOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
                configOperation.schedule();
                needRebuild = true;
            }

            // check if a rebuild is necessary
            if (checkstyleEnabled && needRebuild)
            {

                BuildProjectJob rebuildOperation = new BuildProjectJob(mProject,
                        IncrementalProjectBuilder.FULL_BUILD);
                rebuildOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
                rebuildOperation.schedule();
            }
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.errorDialog(getShell(), e, true);
        }
        return true;
    }

    /**
     * This class works as controller for the page. It listenes for events to
     * occur and handles the pages context.
     * 
     * @author Lars Ködderitzsch
     */
    private class PageController extends SelectionAdapter implements ISelectionChangedListener,
            ICheckStateListener, IDoubleClickListener
    {

        /**
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(
         *      org.eclipse.swt.events.SelectionEvent)
         */
        public void widgetSelected(SelectionEvent e)
        {

            Object source = e.getSource();
            // edit filter
            if (source == mBtnEditFilter)
            {

                ISelection selection = mFilterList.getSelection();
                openFilterEditor(selection);
            }
            else if (source == mChkSimpleConfig)
            {
                try
                {

                    mProjectConfig.setUseSimpleConfig(mChkSimpleConfig.getSelection());

                    IPreferenceStore prefStore = CheckstylePlugin.getDefault().getPreferenceStore();
                    boolean showWarning = prefStore
                            .getBoolean(CheckstylePlugin.PREF_FILESET_WARNING);
                    if (mProjectConfig.isUseSimpleConfig() && showWarning)
                    {
                        MessageDialogWithToggle dialog = new MessageDialogWithToggle(getShell(),
                                Messages.CheckstylePropertyPage_titleWarnFilesets, null,
                                Messages.CheckstylePropertyPage_msgWarnFilesets,
                                MessageDialogWithToggle.WARNING,
                                new String[] { IDialogConstants.OK_LABEL }, 0,
                                Messages.CheckstylePropertyPage_mgsWarnFileSetNagOption,
                                showWarning)
                        {
                            /**
                             * Overwritten because we don't want to store which
                             * button the user pressed but the state of the
                             * toggle.
                             * 
                             * @see MessageDialogWithToggle#buttonPressed(int)
                             */
                            protected void buttonPressed(int buttonId)
                            {
                                getPrefStore().setValue(getPrefKey(), getToggleState());
                                setReturnCode(buttonId);
                                close();
                            }

                        };
                        dialog.setPrefStore(prefStore);
                        dialog.setPrefKey(CheckstylePlugin.PREF_FILESET_WARNING);
                        dialog.open();

                    }

                    createFileSetsArea(mFileSetsContainer);
                    mFileSetsContainer.redraw();
                    mFileSetsContainer.update();
                    mFileSetsContainer.layout();
                }
                catch (CheckstylePluginException ex)
                {
                    CheckstyleLog.errorDialog(getShell(), ErrorMessages.errorChangingFilesetEditor,
                            ex, true);
                }
            }

        }

        /**
         * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged
         *      (org.eclipse.jface.viewers.SelectionChangedEvent)
         */
        public void selectionChanged(SelectionChangedEvent event)
        {

            Object source = event.getSource();
            if (source == mFilterList)
            {

                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection)
                {
                    Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

                    if (selectedElement instanceof IFilter)
                    {

                        IFilter filterDef = (IFilter) selectedElement;

                        mTxtFilterDescription.setText(filterDef.getDescription());

                        // activate edit button
                        mBtnEditFilter.setEnabled(filterDef.isEditable());
                    }
                }
            }
        }

        /**
         * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged
         *      (org.eclipse.jface.viewers.CheckStateChangedEvent)
         */
        public void checkStateChanged(CheckStateChangedEvent event)
        {

            Object element = event.getElement();
            if (element instanceof IFilter)
            {
                ((IFilter) element).setEnabled(event.getChecked());
            }
        }

        /**
         * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(
         *      org.eclipse.jface.viewers.DoubleClickEvent)
         */
        public void doubleClick(DoubleClickEvent event)
        {

            openFilterEditor(event.getSelection());
        }

        /**
         * Open the filter editor on a given selection of the list.
         * 
         * @param selection the selection
         */
        private void openFilterEditor(ISelection selection)
        {

            if (selection instanceof IStructuredSelection)
            {
                Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

                if (selectedElement instanceof IFilter)
                {

                    try
                    {

                        IFilter aFilterDef = (IFilter) selectedElement;

                        if (!aFilterDef.isEditable())
                        {
                            return;
                        }

                        Class editorClass = aFilterDef.getEditorClass();

                        IFilterEditor editableFilter = (IFilterEditor) editorClass.newInstance();
                        editableFilter.setInputProject(mProject);
                        editableFilter.setFilterData(aFilterDef.getFilterData());

                        if (Window.OK == editableFilter.openEditor(getShell()))
                        {

                            aFilterDef.setFilterData(editableFilter.getFilterData());
                            mFilterList.refresh();
                        }
                    }
                    catch (IllegalAccessException ex)
                    {
                        CheckstyleLog.errorDialog(getShell(), ex, true);
                    }
                    catch (InstantiationException ex)
                    {
                        CheckstyleLog.errorDialog(getShell(), ex, true);
                    }
                }
            }
        }
    }

}