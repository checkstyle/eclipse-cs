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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditor;

/**
 * Property page for projects to enable checkstyle audit.
 *
 */
public class CheckstylePropertyPage extends PropertyPage {

  private CheckstylePropertyPageMainTab mainTab;

  //
  // other members
  //

  /** the actual working data for this form. */
  private ProjectConfigurationWorkingCopy mProjectConfig;

  private boolean mCheckstyleInitiallyActivated;

  //
  // methods
  //

  @Override
  public void setElement(IAdaptable element) {
    super.setElement(element);

    //
    // Get the project.
    //
    IProject project = null;

    IResource resource = (IResource) element;
    if (resource.getType() == IResource.PROJECT) {
      project = (IProject) resource;
    }

    try {
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
    // suppress default- & apply-buttons
    noDefaultAndApplyButton();

    TabFolder tabFolder = new TabFolder(parent, SWT.TOP);
    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
    tabFolder.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      mainTab.refreshFileSetEditor();
      getContainer().updateButtons();
    }));
    this.mainTab = new CheckstylePropertyPageMainTab(tabFolder, SWT.NONE,
            new PropertyPageContext((IProject) getElement(), mProjectConfig,
                    getContainer()::updateButtons),
            mCheckstyleInitiallyActivated);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(mainTab);

    // create the local configurations area
    Control localConfigArea = new LocalConfig(tabFolder, SWT.NONE,
            mProjectConfig.getLocalCheckConfigWorkingSet());

    TabItem mainItem = new TabItem(tabFolder, SWT.NULL);
    mainItem.setControl(mainTab);
    mainItem.setText(Messages.CheckstylePropertyPage_tabMain);

    TabItem localItem = new TabItem(tabFolder, SWT.NULL);
    localItem.setControl(localConfigArea);
    localItem.setText(Messages.CheckstylePropertyPage_tabCheckConfigs);

    return mainTab;
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
            mainTab.isCheckstyleEnabled(), mCheckstyleInitiallyActivated);
  }

  private static final class LocalConfig extends Composite {

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

      Control workingSetEditor = new CheckConfigurationWorkingSetEditor(noteAndEditor, SWT.NONE,
              workingSet);
      workingSetEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

  }
}
