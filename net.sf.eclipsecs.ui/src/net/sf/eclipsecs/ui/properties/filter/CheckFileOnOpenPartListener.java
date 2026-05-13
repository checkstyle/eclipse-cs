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

package net.sf.eclipsecs.ui.properties.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.builder.CheckstyleMarker;
import net.sf.eclipsecs.core.jobs.AbstractCheckJob;
import net.sf.eclipsecs.core.jobs.RunCheckstyleOnFilesJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.projectconfig.filters.UnOpenedFilesFilter;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * PartListener implementation that listens for opening editor parts and runs Checkstyle on the
 * opened file if the UnOpenedFileFilter is active.
 *
 * @see <a href="https://sourceforge.net/p/eclipse-cs/feature-requests/93/">feature request</a>
 */
public class CheckFileOnOpenPartListener implements IPartListener2 {

  /**
   * Register multiple parts as opened at once. Used during workspace startup.
   *
   * @param parts
   *          the opened parts
   */
  public void partsOpened(Collection<IWorkbenchPartReference> parts) {

    new PartsOpenedJob(parts).schedule();
  }

  @Override
  public void partOpened(IWorkbenchPartReference partRef) {

    partsOpened(Collections.singleton(partRef));
  }

  @Override
  public void partClosed(IWorkbenchPartReference partRef) {

    IFile editorFile = getEditorFile(partRef);
    if (editorFile != null) {
      UnOpenedFilesFilter.removeOpenedFile(editorFile);
    }

    // if the UnOpenedFilesFilter is active and the editor closes
    // the markers of the current file need to be removed
    if (editorFile != null && isFileAffected(editorFile)) {
      try {
        editorFile.deleteMarkers(CheckstyleMarker.MARKER_ID, true, IResource.DEPTH_INFINITE);
      } catch (CoreException ex) {
        CheckstyleLog.log(ex);
      }
    }
  }

  @Override
  public void partActivated(IWorkbenchPartReference partRef) {
    // NOOP
  }

  @Override
  public void partBroughtToTop(IWorkbenchPartReference partRef) {
    // NOOP
  }

  @Override
  public void partDeactivated(IWorkbenchPartReference partRef) {
    // NOOP
  }

  @Override
  public void partHidden(IWorkbenchPartReference partRef) {
    // NOOP
  }

  @Override
  public void partInputChanged(IWorkbenchPartReference partRef) {
    // NOOP
  }

  @Override
  public void partVisible(IWorkbenchPartReference partRef) {
    // NOOP
  }

  /**
   * Returns the file behind the referenced workbench part.
   *
   * @param partRef
   *          the workbench part in question
   * @return the editors file or <code>null</code> if the workbench part is no file based editor
   */
  private IFile getEditorFile(IWorkbenchPartReference partRef) {
    try {
      if (partRef instanceof IEditorReference editorRef
              && editorRef.getEditorInput() instanceof FileEditorInput input) {
        return input.getFile();
      }
    } catch (PartInitException ex) {
      throw new RuntimeException(ex);
    }
    return null;
  }

  /**
   * Checks if the given file is affected by the UnOpenedFilesFilter and needs to be handled on
   * editor open/close.
   *
   * @param file
   *          the file to check
   * @return <code>true</code> if the file is affected, <code>false</code> otherwise
   */
  private boolean isFileAffected(IFile file) {
    IProject project = file.getProject();

    try {
      // check if checkstyle is enabled on the project
      if (project.isAccessible() && project.hasNature(CheckstyleNature.NATURE_ID)) {

        IProjectConfiguration config = ProjectConfigurationFactory.getConfiguration(project);

        // now check if the UnOpenedFilesFilter is active
        boolean unOpenedFilesFilterActive = false;
        boolean filtered = false;
        List<IFilter> filters = config.getFilters();
        for (IFilter filter : filters) {
          if (filter.isEnabled()) {
            if (filter instanceof UnOpenedFilesFilter) {
              unOpenedFilesFilterActive = true;
            } else {
              filtered = filtered || !filter.accept(file);
            }
          }
        }

        return unOpenedFilesFilterActive && !filtered;
      }
    } catch (CoreException | CheckstylePluginException ex) {
      CheckstyleLog.log(ex);
    }

    return false;
  }

  private class PartsOpenedJob extends AbstractCheckJob {

    private Collection<IWorkbenchPartReference> mParts;

    public PartsOpenedJob(Collection<IWorkbenchPartReference> parts) {
      super(Messages.PartsOpenedJob_title);
      this.mParts = parts;
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
      return false;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
      SubMonitor subMonitor = SubMonitor.convert(monitor, mParts.size());
      List<IFile> filesToCheck = new ArrayList<>();

      for (IWorkbenchPartReference partRef : mParts) {
        subMonitor.worked(1);
        IFile editorFile = getEditorFile(partRef);
        if (editorFile != null) {
          UnOpenedFilesFilter.addOpenedFile(editorFile);
        }

        // check if the opened part is a editor
        // and the editors file need to be checked
        if (editorFile != null && isFileAffected(editorFile)) {
          filesToCheck.add(editorFile);
        }
      }

      RunCheckstyleOnFilesJob job = new RunCheckstyleOnFilesJob(filesToCheck);
      job.schedule();

      return Status.OK_STATUS;
    }
  }

}
