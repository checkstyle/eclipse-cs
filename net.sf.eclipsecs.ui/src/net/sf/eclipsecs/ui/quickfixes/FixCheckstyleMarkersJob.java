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

package net.sf.eclipsecs.ui.quickfixes;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.Messages;

/**
 * Job implementation that tries to fix all Checkstyle markers in a file.
 *
 * @author Lars Ködderitzsch
 */
public class FixCheckstyleMarkersJob extends UIJob {

  private IFile mFile;

  /**
   * Creates the job.
   *
   * @param file
   *          the file to fix
   */
  public FixCheckstyleMarkersJob(IFile file) {
    super(Messages.FixCheckstyleMarkersJob_title);
    this.mFile = file;
  }

  @Override
  public IStatus runInUIThread(IProgressMonitor monitor) {

    try {

      CheckstyleMarkerResolutionGenerator generator = new CheckstyleMarkerResolutionGenerator();

      IMarker[] markers = mFile.findMarkers(CheckstyleMarker.MARKER_ID, true,
              IResource.DEPTH_INFINITE);

      for (int i = 0; i < markers.length; i++) {

        ICheckstyleMarkerResolution[] resolutions = (ICheckstyleMarkerResolution[]) generator
                .getResolutions(markers[i]);

        if (resolutions.length > 0) {
          // only run the first fix for this marker
          resolutions[0].run(markers[i]);
        }

      }
    } catch (CoreException ex) {
      return new Status(IStatus.ERROR, CheckstyleUIPlugin.PLUGIN_ID, IStatus.OK, ex.getMessage(), ex);
    }

    return Status.OK_STATUS;
  }
}
