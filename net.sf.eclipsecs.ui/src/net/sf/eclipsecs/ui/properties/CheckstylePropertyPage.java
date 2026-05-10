//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor;

/**
 * Property page for projects to enable checkstyle audit.
 *
 */
public class CheckstylePropertyPage extends PropertyPage {

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

  //
  // other members
  //

  /** the actual working data for this form. */
  private ProjectConfigurationWorkingCopy mProjectConfig;

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
    } catch (CoreException | CheckstylePluginException ex) {
      handleConfigFileError(ex, project);
    }
  }

  private void handleConfigFileError(Exception error, IProject project) {

    CheckstyleLog.log(error, Messages.errorOpeningPropertiesPage);
    CheckstyleUIPlugin.warningDialog(null, Messages.errorOpeningPropertiesPage, error);

    IProjectConfiguration projectConfig = ProjectConfigurationFactory
            .createDefaultProjectConfiguration(project);
    mProjectConfig = new ProjectConfigurationWorkingCopy(projectConfig);
    try {
      mCheckstyleInitiallyActivated = project.hasNature(CheckstyleNature.NATURE_ID);
    } catch (CoreException nested) {
      CheckstyleUIPlugin.errorDialog(null, nested.getMessage(), nested, true);
    }
  }

  @Override
  public Control createContents(Composite parent) {

    Composite container = null;

    try {
      // suppress default- & apply-buttons
      noDefaultAndApplyButton();

      TabFolder mainTab = new TabFolder(parent, SWT.TOP);
      mainTab.setLayoutData(new GridData(GridData.FILL_BOTH));
      mainTab.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        mFileSetsEditor.refresh();
        getContainer().updateButtons();
      }));

      // create the main container
      container = new Composite(mainTab, SWT.NULL);
      container.setLayout(new FormLayout());
      container.setLayoutData(new GridData(GridData.FILL_BOTH));

      // create the checkbox to enable/disable the simple configuration
      this.mChkSimpleConfig = new Button(container, SWT.CHECK);
      this.mChkSimpleConfig.setText(Messages.CheckstylePropertyPage_btnUseSimpleConfig);
      this.mChkSimpleConfig.addSelectionListener(new ChkSimpleConfigController());
      this.mChkSimpleConfig.setSelection(mProjectConfig.isUseSimpleConfig());

      FormData formData = new FormData();
      // fd.left = new FormAttachment(this.mChkEnable, 0, SWT.RIGHT);
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(100, -3);
      this.mChkSimpleConfig.setLayoutData(formData);

      // create the checkbox to enable/disable checkstyle
      this.mChkEnable = new Button(container, SWT.CHECK);
      this.mChkEnable.setText(Messages.CheckstylePropertyPage_btnActivateCheckstyle);
      this.mChkEnable.setSelection(mCheckstyleInitiallyActivated);

      formData = new FormData();
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(this.mChkSimpleConfig, 3, SWT.LEFT);
      this.mChkEnable.setLayoutData(formData);

      // create the checkbox for formatter syncing
      Button mChkSyncFormatter = new Button(container, SWT.CHECK);
      mChkSyncFormatter.setText(Messages.CheckstylePropertyPage_btnSyncFormatter);
      mChkSyncFormatter.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
        mProjectConfig.setSyncFormatter(mChkSyncFormatter.getSelection());
      }));
      mChkSyncFormatter.setSelection(mProjectConfig.isSyncFormatter());

      formData = new FormData();
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(this.mChkEnable, 3, SWT.BOTTOM);
      mChkSyncFormatter.setLayoutData(formData);

      // create the configuration area
      mFileSetsContainer = new Composite(container, SWT.NULL);
      final Control configArea = createFileSetsArea(mFileSetsContainer);
      formData = new FormData();
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(mChkSyncFormatter, 6, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
      formData.bottom = new FormAttachment(45);
      configArea.setLayoutData(formData);

      // create the filter area
      final Control filterArea = new FilterSettings(container, SWT.NONE,
              mProjectConfig.getProject(), mProjectConfig.getFilters(), getContainer()::updateButtons);
      formData = new FormData();
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(configArea, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
      formData.bottom = new FormAttachment(100, -3);
      formData.width = 500;
      filterArea.setLayoutData(formData);

      // create the local configurations area
      Control localConfigArea = new LocalConfig(mainTab, SWT.NONE,
              mProjectConfig.getLocalCheckConfigWorkingSet());

      TabItem mainItem = new TabItem(mainTab, SWT.NULL);
      mainItem.setControl(container);
      mainItem.setText(Messages.CheckstylePropertyPage_tabMain);

      TabItem localItem = new TabItem(mainTab, SWT.NULL);
      localItem.setControl(localConfigArea);
      localItem.setText(Messages.CheckstylePropertyPage_tabCheckConfigs);

    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(), Messages.errorOpeningPropertiesPage, ex, true);
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

    PropertyPageContext propertyPageContext = new PropertyPageContext((IProject) getElement(),
            getProjectConfigurationWorkingCopy(), getContainer()::updateButtons);
    mFileSetsEditor = FileSetsEditorFactory.createEditor(getShell(), propertyPageContext,
            mProjectConfig.isUseSimpleConfig());
    mFileSetsEditor.setFileSets(mProjectConfig.getFileSets());

    final Control editor = mFileSetsEditor.createContents(mFileSetsContainer);

    fileSetsContainer.setLayout(new FormLayout());
    FormData formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(100);
    editor.setLayoutData(formData);

    return fileSetsContainer;
  }

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
          } catch (CheckstylePluginException ex) {
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
    return CheckstylePropertyApplyOperation.apply(getShell(), mProjectConfig,
            mChkEnable.getSelection(), mCheckstyleInitiallyActivated);
  }

  private static class LocalConfig extends Composite {

    private LocalConfig(Composite parent, int style, ICheckConfigurationWorkingSet workingSet) {
      super(parent, style);
      setLayout(new FillLayout());

      Composite noteAndEditor = new Composite(this, SWT.NULL);
      noteAndEditor.setLayout(new GridLayout(1, false));
      noteAndEditor.setLayoutData(new GridData(GridData.FILL_BOTH));

      Label lblHint = new Label(noteAndEditor, SWT.WRAP);
      lblHint.setText(Messages.CheckstylePropertyPage_msgLocalConfigs);
      GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.widthHint = 200;
      lblHint.setLayoutData(gridData);

      CheckConfigurationWorkingSetEditor workingSetEditor = new CheckConfigurationWorkingSetEditor(
              workingSet, false);
      Control editorControl = workingSetEditor.createContents(noteAndEditor);
      editorControl.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

  }

  private class ChkSimpleConfigController extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      try {
        mProjectConfig.setUseSimpleConfig(mChkSimpleConfig.getSelection());

        boolean showWarning = CheckstyleUIPluginPrefs
                .getBoolean(CheckstyleUIPluginPrefs.PREF_FILESET_WARNING);
        if (showWarning && mProjectConfig.isUseSimpleConfig()) {
          MessageDialogWithToggle dialog = new MessageDialogWithToggle(getShell(),
                  Messages.CheckstylePropertyPage_titleWarnFilesets, null,
                  Messages.CheckstylePropertyPage_msgWarnFilesets, MessageDialog.WARNING,
                  new String[] { IDialogConstants.OK_LABEL }, 0,
                  Messages.CheckstylePropertyPage_mgsWarnFileSetNagOption, showWarning) {
            /**
             * Overwritten because we don't want to store which button the user pressed but the
             * state of the toggle.
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
}
