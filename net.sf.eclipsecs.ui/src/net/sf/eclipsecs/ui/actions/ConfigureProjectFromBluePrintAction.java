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

package net.sf.eclipsecs.ui.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.ICheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

/**
 * Action to configure one ore more projects at once by using another project as
 * blueprint.
 *
 * @author Lars Ködderitzsch
 */
public class ConfigureProjectFromBluePrintAction implements IObjectActionDelegate {

  private IWorkbenchPart mPart;

  private Collection<IProject> mSelectedProjects;

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    mPart = targetPart;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void selectionChanged(IAction action, ISelection selection) {

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection sel = (IStructuredSelection) selection;
      mSelectedProjects = sel.toList();
    }
  }

  @Override
  public void run(IAction action) {
    List<IProject> filteredProjects = Arrays
            .stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
            .filter(IProject::isAccessible)
            .collect(Collectors.toList());

    filteredProjects.removeAll(mSelectedProjects);

    ElementListSelectionDialog dialog = new ElementListSelectionDialog(mPart.getSite().getShell(),
            new WorkbenchLabelProvider());
    dialog.setElements(filteredProjects.toArray(new IProject[0]));
    dialog.setHelpAvailable(false);
    dialog.setMessage(Messages.ConfigureProjectFromBluePrintAction_msgSelectBlueprintProject);
    dialog.setTitle(Messages.ConfigureProjectFromBluePrintAction_titleSelectBlueprintProject);
    if (Window.OK == dialog.open()) {

      Object[] result = dialog.getResult();

      if (result.length > 0) {

        BulkConfigureJob job = new BulkConfigureJob((IProject) result[0], mSelectedProjects);
        job.schedule();
      }

    }
  }

  /**
   * Job implementation that configures several projects from a blueprint
   * project.
   *
   * @author Lars Ködderitzsch
   */
  private static class BulkConfigureJob extends WorkspaceJob {

    private final IProject mBlueprint;

    private final Collection<IProject> mProjectsToConfigure;

    public BulkConfigureJob(IProject blueprint, Collection<IProject> projectsToConfigure) {
      super(Messages.ConfigureProjectFromBluePrintAction_msgConfiguringFromBluePrint);
      this.mBlueprint = blueprint;
      this.mProjectsToConfigure = projectsToConfigure;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {

      try {

        IProjectConfiguration bluePrintConfig = ProjectConfigurationFactory
                .getConfiguration(mBlueprint);

        List<ICheckConfiguration> bluePrintLocalConfigs = bluePrintConfig
                .getLocalCheckConfigurations();

        for (IProject configurationTarget : mProjectsToConfigure) {
          IProjectConfiguration config = ProjectConfigurationFactory
                  .getConfiguration(configurationTarget);
          ProjectConfigurationWorkingCopy workingCopy = new ProjectConfigurationWorkingCopy(config);

          // clear filesets and filters
          workingCopy.getFileSets().clear();
          workingCopy.getFilters().clear();

          // clear local configurations
          ICheckConfigurationWorkingSet checkConfigsWorkingSet = workingCopy
                  .getLocalCheckConfigWorkingSet();

          for (ICheckConfiguration localConfig : workingCopy.getLocalCheckConfigurations()) {

            if (localConfig instanceof CheckConfigurationWorkingCopy) {
              checkConfigsWorkingSet
                      .removeCheckConfiguration((CheckConfigurationWorkingCopy) localConfig);
            }
          }

          // TODO consider copying internal configurations

          // add local configurations from blueprint
          for (ICheckConfiguration localConfig : bluePrintLocalConfigs) {
            CheckConfigurationWorkingCopy newCopy = checkConfigsWorkingSet.newWorkingCopy(localConfig);
            checkConfigsWorkingSet.addCheckConfiguration(newCopy);
          }

          // add filesets and filters
          workingCopy.setUseSimpleConfig(bluePrintConfig.isUseSimpleConfig());
          workingCopy.getFileSets().addAll(bluePrintConfig.getFileSets());
          workingCopy.getFilters().addAll(bluePrintConfig.getFilters());

          workingCopy.store();
        }
      } catch (CheckstylePluginException ex) {
        return new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK, ex.getMessage(),
                ex);
      }

      return Status.OK_STATUS;
    }
  }
}
