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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.builder.CheckstyleBuilder;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.projectconfig.filters.IFilter;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Job that invokes Checkstyle on a list of workspace files.
 *
 */
public class RunCheckstyleOnFilesJob extends AbstractCheckJob {

  private List<IFile> mFilesToCheck;

  /**
   * Creates the job for a list of <code>IFile</code> objects.
   *
   * @param files
   *          the files to check
   */
  public RunCheckstyleOnFilesJob(final List<IFile> files) {
    super(Messages.RunCheckstyleOnFilesJob_title);
    mFilesToCheck = new ArrayList<>(files);

    setRule(this);
  }

  /**
   * Creates the job for a single file.
   *
   * @param file
   *          the file to check
   */
  public RunCheckstyleOnFilesJob(final IFile file) {
    this(Collections.singletonList(file));
  }

  @Override
  public boolean contains(ISchedulingRule arg0) {
    return arg0 instanceof RunCheckstyleOnFilesJob;
  }

  @Override
  public final IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

    try {

      Map<IProject, List<IFile>> projectFilesMap = getFilesSortedToProject(mFilesToCheck);

      for (Map.Entry<IProject, List<IFile>> entry : projectFilesMap.entrySet()) {

        IProject project = entry.getKey();
        List<IFile> files = entry.getValue();

        IProjectConfiguration checkConfig = ProjectConfigurationFactory.getConfiguration(project);

        filter(files, checkConfig);

        CheckstyleBuilder builder = new CheckstyleBuilder();
        builder.handleBuildSelection(files, checkConfig, monitor, project,
                IncrementalProjectBuilder.INCREMENTAL_BUILD);
      }
    } catch (CheckstylePluginException ex) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              ex.getLocalizedMessage(), ex);
      throw new CoreException(status);
    }
    return Status.OK_STATUS;
  }

  private static Map<IProject, List<IFile>> getFilesSortedToProject(
          final List<IFile> filesToCheck) {

    Map<IProject, List<IFile>> projectFilesMap = new HashMap<>();

    for (int i = 0, size = filesToCheck.size(); i < size; i++) {

      IFile file = filesToCheck.get(i);
      IProject project = file.getProject();

      List<IFile> projectFiles = projectFilesMap.get(project);
      if (projectFiles == null) {

        projectFiles = new ArrayList<>();
        projectFilesMap.put(project, projectFiles);
      }
      projectFiles.add(file);
    }

    return projectFilesMap;
  }

  private static void filter(final List<IFile> files, final IProjectConfiguration projectConfig) {

    List<IFilter> filters = projectConfig.getFilters();
    for (IFilter filter : filters) {

      Iterator<IFile> filesIt = files.iterator();
      while (filesIt.hasNext()) {

        IFile file = filesIt.next();

        if (filter.isEnabled() && !filter.accept(file)) {
          filesIt.remove();
        }
      }
    }
  }
}
