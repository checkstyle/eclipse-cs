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

package net.sf.eclipsecs.core.jobs;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.core.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * This operation configures or deconfigures a project with a given nature. If the project is
 * already configured with this nature, the nature will be deconfigured. Otherwise if the project is
 * not configured with this nature, the nature will be configured for this project
 * 
 * @author Lars Ködderitzsch
 */
public class ConfigureDeconfigureNatureJob extends WorkspaceJob {

  /** the project to be configured/deconfigured. */
  private IProject mProject;

  /** the nature to be configured/deconfigured. */
  private String mNatureId;

  /** the monitor. */
  private IProgressMonitor mMonitor;

  /**
   * Construktor for this operation.
   * 
   * @param project
   *          the project to be configured/deconfiured
   * @param natureId
   *          the nature the project will be configured/deconfigured
   */
  public ConfigureDeconfigureNatureJob(IProject project, String natureId) {
    super(NLS.bind(Messages.ConfigureDeconfigureNatureJob_msgTaksAddingNature, natureId));
    mProject = project;
    mNatureId = natureId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

    IStatus status = null;

    mMonitor = monitor;

    try {
      if (mProject.hasNature(mNatureId)) {
        disableNature();
      } else {
        enableNature();
      }

      status = Status.OK_STATUS;
    } finally {
      monitor.done();
    }
    return status;
  }

  /**
   * Helper method to enable the given nature for the project.
   * 
   * @throws CoreException
   *           an error while setting the nature occured
   */
  private void enableNature() throws CoreException {

    // get the description
    IProjectDescription desc = mProject.getDescription();

    // copy existing natures and add the nature
    String[] natures = desc.getNatureIds();

    String[] newNatures = new String[natures.length + 1];
    System.arraycopy(natures, 0, newNatures, 0, natures.length);
    newNatures[natures.length] = mNatureId;

    // set natures
    desc.setNatureIds(newNatures);
    mProject.setDescription(desc, mMonitor);
  }

  /**
   * Helper method to disable the given nature for the project.
   * 
   * @throws CoreException
   *           an error while removing the nature occured
   */
  private void disableNature() throws CoreException {

    IProjectDescription desc = mProject.getDescription();
    String[] natures = desc.getNatureIds();

    // remove given nature from the array
    List<String> newNaturesList = new ArrayList<>();
    for (int i = 0; i < natures.length; i++) {
      if (!mNatureId.equals(natures[i])) {
        newNaturesList.add(natures[i]);
      }
    }

    String[] newNatures = newNaturesList.toArray(new String[newNaturesList.size()]);

    // set natures
    desc.setNatureIds(newNatures);
    mProject.setDescription(desc, mMonitor);
  }

}