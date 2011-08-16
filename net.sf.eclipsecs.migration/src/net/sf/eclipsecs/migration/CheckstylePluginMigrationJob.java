//============================================================================
//
// Copyright (C) 2002-2011  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.core.nature.CheckstyleNature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;

/**
 * Job running at workspace startup which automatically migrates projects from
 * Eclipse Checkstyle Plugin 4.x.x to 5.x.x.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstylePluginMigrationJob extends WorkspaceJob implements IStartup {

    private static final String OLD_NATURE_ID = "com.atlassw.tools.eclipse.checkstyle.CheckstyleNature";

    private static final String OLD_BUILDER_ID = "com.atlassw.tools.eclipse.checkstyle.CheckstyleBuilder";

    /**
     * Creates the job.
     */
    public CheckstylePluginMigrationJob() {
        super("Migrate Checkstyle enabled projects");
    }

    /**
     * Called on plugin startup, schedules the migration job.
     */
    public void earlyStartup() {
        this.schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

        monitor.beginTask("Migrate Checkstyle enabled projects", projects.length);

        try {

            for (IProject project : projects) {

                if (project.isAccessible() && project.hasNature(OLD_NATURE_ID)) {

                    IProjectDescription desc = project.getDescription();

                    // copy existing natures and add the nature
                    List<String> natures = new ArrayList<String>(Arrays.asList(desc.getNatureIds()));
                    List<ICommand> builders = new ArrayList<ICommand>(Arrays.asList(desc
                            .getBuildSpec()));

                    natures.remove(OLD_NATURE_ID);

                    if (!natures.contains(CheckstyleNature.NATURE_ID)) {
                        natures.add(CheckstyleNature.NATURE_ID);
                    }

                    Iterator<ICommand> it = builders.iterator();
                    while (it.hasNext()) {

                        ICommand builder = it.next();
                        if (OLD_BUILDER_ID.equals(builder.getBuilderName())) {
                            it.remove();
                        }
                    }

                    desc.setNatureIds(natures.toArray(new String[natures.size()]));
                    desc.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
                    project.setDescription(desc, monitor);
                }

                monitor.worked(1);
            }
        }
        finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

}
