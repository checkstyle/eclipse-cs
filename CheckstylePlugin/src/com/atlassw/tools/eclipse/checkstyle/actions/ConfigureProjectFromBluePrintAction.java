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

package com.atlassw.tools.eclipse.checkstyle.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.Messages;
import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfigurationWorkingSet;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.IProjectConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationWorkingCopy;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;

/**
 * Action to configure one ore more projects at once by using another project as
 * blueprint.
 * 
 * @author Lars Koedderitzsch
 */
public class ConfigureProjectFromBluePrintAction implements IObjectActionDelegate
{

    private IWorkbenchPart mPart;

    private Collection mSelectedProjects;

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart)
    {
        mPart = targetPart;
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection)
    {

        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection sel = (IStructuredSelection) selection;
            mSelectedProjects = sel.toList();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(IAction action)
    {

        IProject[] projects = CheckstylePlugin.getWorkspace().getRoot().getProjects();
        List filteredProjects = new ArrayList();
        for (int i = 0; i < projects.length; i++)
        {
            filteredProjects.add(projects[i]);
        }

        filteredProjects.removeAll(mSelectedProjects);

        ListDialog dialog = new ListDialog(mPart.getSite().getShell());
        dialog.setInput(filteredProjects);
        dialog.setContentProvider(new ArrayContentProvider());
        dialog.setLabelProvider(new WorkbenchLabelProvider());
        dialog
                .setMessage(Messages.ConfigureProjectFromBluePrintAction_msgSelectBlueprintProject);
        dialog.setTitle(Messages.ConfigureProjectFromBluePrintAction_titleSelectBlueprintProject);
        if (ListDialog.OK == dialog.open())
        {

            Object[] result = dialog.getResult();

            if (result.length > 0)
            {

                BulkConfigureJob job = new BulkConfigureJob((IProject) result[0], mSelectedProjects);
                job.schedule();
            }

        }
    }

    /**
     * Job implementation that configures several projects from a blueprint
     * project.
     * 
     * @author Lars Koedderitzsch
     */
    private class BulkConfigureJob extends WorkspaceJob
    {

        private IProject mBlueprint;

        private Collection mProjectsToConfigure;

        public BulkConfigureJob(IProject blueprint, Collection projectsToConfigure)
        {
            super(Messages.ConfigureProjectFromBluePrintAction_msgConfiguringFromBluePrint);
            this.mBlueprint = blueprint;
            this.mProjectsToConfigure = projectsToConfigure;
        }

        /**
         * {@inheritDoc}
         */
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
        {

            try
            {

                IProjectConfiguration bluePrintConfig = ProjectConfigurationFactory
                        .getConfiguration(mBlueprint);

                List bluePrintLocalConfigs = bluePrintConfig.getLocalCheckConfigurations();

                Iterator it = mProjectsToConfigure.iterator();
                while (it.hasNext())
                {

                    IProject configurationTarget = (IProject) it.next();

                    IProjectConfiguration config = ProjectConfigurationFactory
                            .getConfiguration(configurationTarget);
                    ProjectConfigurationWorkingCopy workingCopy = new ProjectConfigurationWorkingCopy(
                            config);

                    // clear filesets and filters
                    workingCopy.getFileSets().clear();
                    workingCopy.getFilters().clear();

                    // clear local configurations
                    ICheckConfigurationWorkingSet checkConfigsWorkingSet = workingCopy
                            .getLocalCheckConfigWorkingSet();
                    List localConfigs = workingCopy.getLocalCheckConfigurations();
                    Iterator it2 = localConfigs.iterator();
                    while (it2.hasNext())
                    {
                        checkConfigsWorkingSet
                                .removeCheckConfiguration((CheckConfigurationWorkingCopy) it2
                                        .next());
                    }

                    // TODO consider copying internal configurations

                    // add local configurations from blueprint
                    it2 = bluePrintLocalConfigs.iterator();
                    while (it2.hasNext())
                    {

                        CheckConfigurationWorkingCopy wk = checkConfigsWorkingSet
                                .newWorkingCopy((ICheckConfiguration) it2.next());
                        checkConfigsWorkingSet.addCheckConfiguration(wk);
                    }

                    // add filesets and filters
                    workingCopy.setUseSimpleConfig(bluePrintConfig.isUseSimpleConfig());
                    workingCopy.getFileSets().addAll(bluePrintConfig.getFileSets());
                    workingCopy.getFilters().addAll(bluePrintConfig.getFilters());

                    workingCopy.store();
                }
            }
            catch (CheckstylePluginException e)
            {
                return new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.OK, e
                        .getMessage(), e);
            }

            return Status.OK_STATUS;
        }
    }
}
