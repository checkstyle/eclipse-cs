///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.nature.CheckstyleNature;

/**
 * Operation which builds a project.
 *
 */
public class BuildProjectJob extends Job {

    /** The project to build. */
    private IProject[] projects;

    /** The build kind. */
    private int kind;

    /**
     * Creates an operation which builds a project.
     *
     * @param project
     *            the project to build
     * @param buildKind
     *            the kind of build to do
     */
    public BuildProjectJob(IProject project, int buildKind) {
        super(NLS.bind(Messages.BuildProjectJob_msgBuildProject, project.getName()));
        projects = new IProject[] {
            project,
        };
        kind = buildKind;
    }

    /**
     * Creates an operation which builds a set of project.
     *
     * @param projects
     *            the projects to build
     * @param buildKind
     *            the kind of build to do
     */
    public BuildProjectJob(IProject[] projects, int buildKind) {
        super(Messages.BuildProjectJob_msgBuildAllProjects);

        this.projects = projects;
        kind = buildKind;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {

        IStatus status = null;

        try {

            for (IProject project : projects) {

                // build only if open and checkstyle active for the project
                if (project.isOpen() && project.hasNature(CheckstyleNature.NATURE_ID)) {
                    project.build(kind, monitor);
                }
            }
            status = Status.OK_STATUS;
        }
        catch (CoreException exception) {
            status = exception.getStatus();
        }
        finally {
            monitor.done();
        }

        return status;
    }

}
