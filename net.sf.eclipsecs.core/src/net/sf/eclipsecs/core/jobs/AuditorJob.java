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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.osgi.util.NLS;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.builder.Auditor;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Job to de-couple an audit. Used for the "Run Checkstyle in background on full build"
 * functionality.
 *
 * @author Lars Ködderitzsch
 */
public class AuditorJob extends AbstractCheckJob {

  private IProject mProject;

  private Auditor mAuditor;

  /**
   * Creates an operation which runs a pre-configured auditor.
   *
   * @param project
   *          the project to build
   * @param auditor
   *          the auditor to run
   */
  public AuditorJob(IProject project, Auditor auditor) {
    super(NLS.bind(Messages.AuditorJob_msgBuildProject, project.getName()));

    this.mProject = project;
    this.mAuditor = auditor;
  }

  @Override
  public boolean contains(ISchedulingRule arg0) {
    return arg0 instanceof AuditorJob;
  }

  @Override
  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

    try {

      mAuditor.runAudit(mProject, monitor);
    } catch (CheckstylePluginException ex) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              ex.getLocalizedMessage(), ex);
      throw new CoreException(status);
    }
    return Status.OK_STATUS;
  }

}
