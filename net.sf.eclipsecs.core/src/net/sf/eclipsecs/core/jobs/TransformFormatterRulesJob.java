//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.transformer.FormatterTransformer;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Job who starts transforming the formatter-rules to checkstyle-settings.
 *
 *
 */
public class TransformFormatterRulesJob extends WorkspaceJob {

  /** Selected project in workspace. */
  private final IProject mProject;

  /**
   * Job for transforming formatter-rules to checkstyle-settings.
   *
   * @param project
   *          The current selected project in the workspace.
   */
  public TransformFormatterRulesJob(final IProject project) {
    super(Messages.TransformFormatterRulesJob_name);

    this.mProject = project;
  }

  @Override
  public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
    IStatus status = Status.CANCEL_STATUS;
    SubMonitor subMonitor = SubMonitor.convert(monitor);
    subMonitor.setWorkRemaining(IProgressMonitor.UNKNOWN);

    IJavaProject javaProject = JavaCore.create(mProject);
    if (javaProject != null) {
      final String projectPath = mProject.getLocation().toString();

      Map<String, String> formatterSettings = javaProject.getOptions(true).entrySet().stream()
              .filter(entry -> entry.getKey().startsWith("org.eclipse.jdt.core.formatter."))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      if (!formatterSettings.isEmpty()) {
        try {
          FormatterTransformer transformer = new FormatterTransformer();
          transformer.transformRules(projectPath + "/test-checkstyle.xml", formatterSettings);
        } catch (CheckstylePluginException ex) {
          throw new CoreException(new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
                  ex.getMessage(), ex));
        }
        status = Status.OK_STATUS;
      }
    }
    return status;
  }

  @Override
  public boolean belongsTo(Object family) {
    return AbstractCheckJob.CHECKSTYLE_JOB_FAMILY.equals(family) || super.belongsTo(family);
  }

}
