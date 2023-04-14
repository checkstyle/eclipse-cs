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

package net.sf.eclipsecs.core.jobs;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.nature.CheckstyleNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/**
 * Operation which builds a project.
 *
 * @author Lars Ködderitzsch
 */
public class BuildProjectJob extends Job {

  /** the project to build. */
  private IProject[] mProjects;

  /** the build kind. */
  private int mKind;

  /**
   * Creates an operation which builds a project.
   *
   * @param project
   *          the project to build
   * @param buildKind
   *          the kind of build to do
   */
  public BuildProjectJob(IProject project, int buildKind) {
    super(NLS.bind(Messages.BuildProjectJob_msgBuildProject, project.getName()));
    mProjects = new IProject[] { project };
    mKind = buildKind;
  }

  /**
   * Creates an operation which builds a set of project.
   *
   * @param projects
   *          the projects to build
   * @param buildKind
   *          the kind of build to do
   */
  public BuildProjectJob(IProject[] projects, int buildKind) {
    super(Messages.BuildProjectJob_msgBuildAllProjects);

    mProjects = projects;
    mKind = buildKind;
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {

    IStatus status = null;

    try {

      for (int i = 0; i < mProjects.length; i++) {

        // build only if open and checkstyle active for the project
        if (mProjects[i].isOpen() && mProjects[i].hasNature(CheckstyleNature.NATURE_ID)) {
          mProjects[i].build(mKind, monitor);
        }
      }
      status = Status.OK_STATUS;
    } catch (CoreException ex) {
      status = ex.getStatus();
    } finally {
      monitor.done();
    }

    return status;
  }
}
