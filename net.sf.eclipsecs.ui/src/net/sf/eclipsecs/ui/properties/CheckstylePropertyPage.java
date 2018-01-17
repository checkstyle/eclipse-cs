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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.jobs.BuildProjectJob;
import net.sf.eclipsecs.core.jobs.ConfigureDeconfigureNatureJob;
import net.sf.eclipsecs.core.jobs.TransformCheckstyleRulesJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor;
import net.sf.eclipsecs.ui.properties.filter.IFilterEditor;
import net.sf.eclipsecs.ui.properties.filter.PluginFilterEditors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for projects to enable checkstyle audit.
 *
 * @author Lars Ködderitzsch
 */
public class CheckstylePropertyPage extends PropertyPage {

  //
  // controls
  //

  /** The tab folder. */
  private TabFolder mMainTab = null;

  /** button to enable checkstyle for the project. */
  private Button mChkEnable;

  /** button to enable/disable the simple configuration. */
  private Button mChkSimpleConfig;

  /**
   * button to enable/disable synchronizing the checkstyle configuration with the formatter
   * configuration.
   */
  private Button mChkSyncFormatter;

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

  /** the actual working data for this form. */
  private ProjectConfigurationWorkingCopy mProjectConfig;

  /** the local configurations working set editor. */
  private CheckConfigurationWorkingSetEditor mWorkingSetEditor;

  private boolean mCheckstyleInitiallyActivated;

  //
  // methods
  //

  /**
   * Returns the project configuration.
   *
   * @return the project configuration
   */
  public ProjectConfigurationWorkingCopy getProjectConfigurationWorkingCopy() {
    return mProjectConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setElement(IAdaptable element) {
    super.setElement(element);

    IProject project = null;

    try {

      //
      // Get the project.
      //

      IResource resource = (IResource) element;
      if (resource.getType() == IResource.PROJECT) {
        project = (IProject) resource;
      }

      IProjectConfiguration projectConfig = ProjectConfigurationFactory.getConfiguration(project);
      mProjectConfig = new ProjectConfigurationWorkingCopy(projectConfig);

      mCheckstyleInitiallyActivated = project.hasNature(CheckstyleNature.NATURE_ID);
    } catch (CoreException e) {
      handleConfigFileError(e, project);
    } catch (CheckstylePluginException e) {
      handleConfigFileError(e, project);
    }
  }

  private void handleConfigFileError(Exception e, IProject project) {

    CheckstyleLog.log(e, Messages.errorOpeningPropertiesPage);
    CheckstyleUIPlugin.warningDialog(null, Messages.errorOpeningPropertiesPage, e);

    IProjectConfiguration projectConfig = ProjectConfigurationFactory
            .createDefaultProjectConfiguration(project);
    mProjectConfig = new ProjectConfigurationWorkingCopy(projectConfig);
    try {
      mCheckstyleInitiallyActivated = project.hasNature(CheckstyleNature.NATURE_ID);
    } catch (CoreException e1) {
      CheckstyleUIPlugin.errorDialog(null, e1.getMessage(), e1, true);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Control createContents(Composite parent) {

    Composite container = null;

    try {

      this.mPageController = new PageController();

      // suppress default- & apply-buttons
      noDefaultAndApplyButton();

      mMainTab = new TabFolder(parent, SWT.TOP);
      mMainTab.setLayoutData(new GridData(GridData.FILL_BOTH));
      mMainTab.addSelectionListener(mPageController);

      // create the main container
      container = new Composite(mMainTab, SWT.NULL);
      container.setLayout(new FormLayout());
      container.setLayoutData(new GridData(GridData.FILL_BOTH));

      // create the checkbox to enable/disable the simple configuration
      this.mChkSimpleConfig = new Button(container, SWT.CHECK);
      this.mChkSimpleConfig.setText(Messages.CheckstylePropertyPage_btnUseSimpleConfig);
      this.mChkSimpleConfig.addSelectionListener(this.mPageController);
      this.mChkSimpleConfig.setSelection(mProjectConfig.isUseSimpleConfig());

      FormData fd = new FormData();
      // fd.left = new FormAttachment(this.mChkEnable, 0, SWT.RIGHT);
      fd.top = new FormAttachment(0, 3);
      fd.right = new FormAttachment(100, -3);
      this.mChkSimpleConfig.setLayoutData(fd);

      // create the checkbox to enable/disable checkstyle
      this.mChkEnable = new Button(container, SWT.CHECK);
      this.mChkEnable.setText(Messages.CheckstylePropertyPage_btnActivateCheckstyle);
      this.mChkEnable.addSelectionListener(this.mPageController);
      this.mChkEnable.setSelection(mCheckstyleInitiallyActivated);

      fd = new FormData();
      fd.left = new FormAttachment(0, 3);
      fd.top = new FormAttachment(0, 3);
      fd.right = new FormAttachment(this.mChkSimpleConfig, 3, SWT.LEFT);
      this.mChkEnable.setLayoutData(fd);

      // create the checkbox for formatter syncing
      this.mChkSyncFormatter = new Button(container, SWT.CHECK);
      this.mChkSyncFormatter.setText(Messages.CheckstylePropertyPage_btnSyncFormatter);
      this.mChkSyncFormatter.addSelectionListener(this.mPageController);
      this.mChkSyncFormatter.setSelection(mProjectConfig.isSyncFormatter());

      fd = new FormData();
      fd.left = new FormAttachment(0, 3);
      fd.top = new FormAttachment(this.mChkEnable, 3, SWT.BOTTOM);
      this.mChkSyncFormatter.setLayoutData(fd);

      // create the configuration area
      mFileSetsContainer = new Composite(container, SWT.NULL);
      final Control configArea = createFileSetsArea(mFileSetsContainer);
      fd = new FormData();
      fd.left = new FormAttachment(0, 3);
      fd.top = new FormAttachment(this.mChkSyncFormatter, 6, SWT.BOTTOM);
      fd.right = new FormAttachment(100, -3);
      fd.bottom = new FormAttachment(45);
      configArea.setLayoutData(fd);

      // create the filter area
      final Control filterArea = createFilterArea(container);
      fd = new FormData();
      fd.left = new FormAttachment(0, 3);
      fd.top = new FormAttachment(configArea, 3, SWT.BOTTOM);
      fd.right = new FormAttachment(100, -3);
      fd.bottom = new FormAttachment(100, -3);
      fd.width = 500;
      filterArea.setLayoutData(fd);

      // create the local configurations area
      Control localConfigArea = createLocalConfigArea(mMainTab);

      TabItem mainItem = new TabItem(mMainTab, SWT.NULL);
      mainItem.setControl(container);
      mainItem.setText(Messages.CheckstylePropertyPage_tabMain);

      TabItem localItem = new TabItem(mMainTab, SWT.NULL);
      localItem.setControl(localConfigArea);
      localItem.setText(Messages.CheckstylePropertyPage_tabCheckConfigs);

    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(getShell(), Messages.errorOpeningPropertiesPage, e, true);
    }

    return container;
  }

  /**
   * Creates the file sets area.
   *
   * @param fileSetsContainer
   *          the container to add the file sets area to
   */
  private Control createFileSetsArea(Composite fileSetsContainer) throws CheckstylePluginException {

    Control[] controls = fileSetsContainer.getChildren();
    for (int i = 0; i < controls.length; i++) {
      controls[i].dispose();
    }

    if (mProjectConfig.isUseSimpleConfig()) {
      mFileSetsEditor = new SimpleFileSetsEditor(this);
    } else {
      mFileSetsEditor = new ComplexFileSetsEditor(this);
    }

    mFileSetsEditor.setFileSets(mProjectConfig.getFileSets());

    final Control editor = mFileSetsEditor.createContents(mFileSetsContainer);

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
   * @param container
   *          the container to add the filter area
   */
  private Control createFilterArea(Composite container) {

    // group composite containing the filter settings
    Group filterArea = new Group(container, SWT.NULL);
    filterArea.setText(Messages.CheckstylePropertyPage_titleFilterGroup);

    filterArea.setLayout(new FormLayout());

    this.mFilterList = CheckboxTableViewer.newCheckList(filterArea, SWT.BORDER);
    this.mBtnEditFilter = new Button(filterArea, SWT.PUSH);

    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 3);
    fd.top = new FormAttachment(0, 3);
    fd.right = new FormAttachment(this.mBtnEditFilter, -3, SWT.LEFT);
    fd.bottom = new FormAttachment(60, -3);
    this.mFilterList.getTable().setLayoutData(fd);

    this.mFilterList.setLabelProvider(new LabelProvider() {

      @Override
      public String getText(Object element) {

        StringBuffer buf = new StringBuffer();

        if (element instanceof IFilter) {

          IFilter filter = (IFilter) element;

          buf.append(filter.getName());
          if (filter.getPresentableFilterData() != null) {
            buf.append(": ").append(filter.getPresentableFilterData()); //$NON-NLS-1$
          }
        } else {
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

    this.mTxtFilterDescription = new Text(filterArea,
            SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL);
    fd = new FormData();
    fd.left = new FormAttachment(0, 3);
    fd.top = new FormAttachment(lblDesc, 3, SWT.BOTTOM);
    fd.right = new FormAttachment(100, -3);
    fd.bottom = new FormAttachment(100, -3);
    this.mTxtFilterDescription.setLayoutData(fd);

    // intialize filter list
    List<IFilter> filterDefs = mProjectConfig.getFilters();
    this.mFilterList.setInput(filterDefs);

    // set the checked state
    for (int i = 0; i < filterDefs.size(); i++) {
      IFilter filter = filterDefs.get(i);
      this.mFilterList.setChecked(filter, filter.isEnabled());
    }

    // set the readonly state
    for (int i = 0; i < filterDefs.size(); i++) {
      IFilter filter = filterDefs.get(i);
      this.mFilterList.setGrayed(filter, filter.isReadonly());
    }

    this.mBtnEditFilter.setEnabled(false);

    return filterArea;
  }

  private Control createLocalConfigArea(Composite parent) {

    Composite noteAndEditor = new Composite(parent, SWT.NULL);
    noteAndEditor.setLayout(new GridLayout(1, false));
    noteAndEditor.setLayoutData(new GridData(GridData.FILL_BOTH));

    Label lblHint = new Label(noteAndEditor, SWT.WRAP);
    lblHint.setText(Messages.CheckstylePropertyPage_msgLocalConfigs);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = 200;
    lblHint.setLayoutData(gd);

    mWorkingSetEditor = new CheckConfigurationWorkingSetEditor(
            mProjectConfig.getLocalCheckConfigWorkingSet(), false);
    Control editorControl = mWorkingSetEditor.createContents(noteAndEditor);
    editorControl.setLayoutData(new GridData(GridData.FILL_BOTH));

    return noteAndEditor;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid() {

    if (mProjectConfig != null) {
      // check if all check configurations resolve
      List<FileSet> fileSets = mProjectConfig.getFileSets();
      for (FileSet fileset : fileSets) {
        ICheckConfiguration checkConfig = fileset.getCheckConfig();
        if (checkConfig != null) {
          try {
            checkConfig.getCheckstyleConfiguration();
          } catch (CheckstylePluginException e) {
            setErrorMessage(NLS.bind(Messages.errorCannotResolveCheckLocation,
                    checkConfig.getLocation(), checkConfig.getName()));
            return false;
          }
        }
      }
    }

    setErrorMessage(null);
    return true;
  }

  @Override
  public boolean performOk() {

    try {

      IProject project = mProjectConfig.getProject();

      // save the edited project configuration
      if (mProjectConfig.isDirty()) {
        mProjectConfig.store();
      }

      boolean checkstyleEnabled = mChkEnable.getSelection();
      boolean needRebuild = mProjectConfig.isRebuildNeeded();

      // check if checkstyle nature has to be configured/deconfigured
      if (checkstyleEnabled != mCheckstyleInitiallyActivated) {

        ConfigureDeconfigureNatureJob configOperation = new ConfigureDeconfigureNatureJob(project,
                CheckstyleNature.NATURE_ID);
        configOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
        configOperation.schedule();

        needRebuild = needRebuild || !mCheckstyleInitiallyActivated;
      }

      if (checkstyleEnabled && mProjectConfig.isSyncFormatter()) {

        TransformCheckstyleRulesJob transFormJob = new TransformCheckstyleRulesJob(project);
        transFormJob.schedule();
      }

      // if a rebuild is advised, check/prompt if the rebuild should
      // really be done.
      if (checkstyleEnabled && needRebuild) {

        String promptRebuildPref = CheckstyleUIPluginPrefs
                .getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

        boolean doRebuild = MessageDialogWithToggle.ALWAYS.equals(promptRebuildPref) && needRebuild;

        //
        // Prompt for rebuild
        //
        if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref) && needRebuild) {
          MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getShell(),
                  Messages.CheckstylePropertyPage_titleRebuild,
                  Messages.CheckstylePropertyPage_msgRebuild,
                  Messages.CheckstylePropertyPage_nagRebuild, false,
                  CheckstyleUIPlugin.getDefault().getPreferenceStore(),
                  CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

          doRebuild = dialog.getReturnCode() == IDialogConstants.YES_ID;
        }

        // check if a rebuild is necessary
        if (checkstyleEnabled && doRebuild) {

          BuildProjectJob rebuildOperation = new BuildProjectJob(project,
                  IncrementalProjectBuilder.FULL_BUILD);
          rebuildOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
          rebuildOperation.schedule();
        }
      }
    } catch (CheckstylePluginException e) {
      CheckstyleUIPlugin.errorDialog(getShell(), e, true);
    }
    return true;
  }

  /**
   * This class works as controller for the page. It listenes for events to occur and handles the
   * pages context.
   *
   * @author Lars Ködderitzsch
   */
  private class PageController extends SelectionAdapter
          implements ISelectionChangedListener, ICheckStateListener, IDoubleClickListener {

    @Override
    public void widgetSelected(SelectionEvent e) {

      Object source = e.getSource();
      // edit filter
      if (source == mBtnEditFilter) {

        ISelection selection = mFilterList.getSelection();
        openFilterEditor(selection);
        getContainer().updateButtons();
      }
      if (source == mMainTab) {
        mFileSetsEditor.refresh();
        getContainer().updateButtons();

      } else if (source == mChkSyncFormatter) {
        mProjectConfig.setSyncFormatter(mChkSyncFormatter.getSelection());
      } else if (source == mChkSimpleConfig) {
        try {

          mProjectConfig.setUseSimpleConfig(mChkSimpleConfig.getSelection());

          boolean showWarning = CheckstyleUIPluginPrefs
                  .getBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING);
          if (mProjectConfig.isUseSimpleConfig() && showWarning) {
            MessageDialogWithToggle dialog = new MessageDialogWithToggle(getShell(),
                    Messages.CheckstylePropertyPage_titleWarnFilesets, null,
                    Messages.CheckstylePropertyPage_msgWarnFilesets, MessageDialog.WARNING,
                    new String[] { IDialogConstants.OK_LABEL }, 0,
                    Messages.CheckstylePropertyPage_mgsWarnFileSetNagOption, showWarning) {
              /**
               * Overwritten because we don't want to store which button the user pressed but the
               * state of the toggle.
               *
               * @see MessageDialogWithToggle#buttonPressed(int)
               */
              @Override
              protected void buttonPressed(int buttonId) {
                getPrefStore().setValue(getPrefKey(), getToggleState());
                setReturnCode(buttonId);
                close();
              }

            };
            dialog.setPrefStore(CheckstyleUIPlugin.getDefault().getPreferenceStore());
            dialog.setPrefKey(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING);
            dialog.open();

          }

          createFileSetsArea(mFileSetsContainer);
          mFileSetsContainer.redraw();
          mFileSetsContainer.update();
          mFileSetsContainer.layout();
        } catch (CheckstylePluginException ex) {
          CheckstyleUIPlugin.errorDialog(getShell(), Messages.errorChangingFilesetEditor, ex, true);
        }
      }

    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {

      Object source = event.getSource();
      if (source == mFilterList) {

        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection) {
          Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

          if (selectedElement instanceof IFilter) {

            IFilter filterDef = (IFilter) selectedElement;

            mTxtFilterDescription.setText(filterDef.getDescription());

            // activate edit button
            mBtnEditFilter.setEnabled(PluginFilterEditors.hasEditor(filterDef));
          }
        }
      }
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {

      Object element = event.getElement();
      if (element instanceof IFilter) {
        IFilter filter = (IFilter) element;
        if (!filter.isReadonly()) {
          filter.setEnabled(event.getChecked());
        } else {
          event.getCheckable().setChecked(event.getElement(), true);
        }
      }
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {

      openFilterEditor(event.getSelection());
    }

    /**
     * Open the filter editor on a given selection of the list.
     *
     * @param selection
     *          the selection
     */
    private void openFilterEditor(ISelection selection) {

      if (selection instanceof IStructuredSelection) {
        Object selectedElement = ((IStructuredSelection) selection).getFirstElement();

        if (selectedElement instanceof IFilter) {

          try {

            IFilter aFilterDef = (IFilter) selectedElement;

            if (!PluginFilterEditors.hasEditor(aFilterDef)) {
              return;
            }

            IFilterEditor editableFilter = PluginFilterEditors.getNewEditor(aFilterDef);
            editableFilter.setInputProject(mProjectConfig.getProject());
            editableFilter.setFilterData(aFilterDef.getFilterData());

            if (Window.OK == editableFilter.openEditor(getShell())) {

              aFilterDef.setFilterData(editableFilter.getFilterData());
              mFilterList.refresh();
            }
          } catch (CheckstylePluginException ex) {
            CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
          }
        }
      }
    }
  }

}
