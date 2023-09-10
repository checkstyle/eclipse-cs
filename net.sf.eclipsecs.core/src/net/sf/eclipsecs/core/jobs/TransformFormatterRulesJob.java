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

import java.io.FileNotFoundException;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.transformer.FormatterConfigParser;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;
import net.sf.eclipsecs.core.transformer.FormatterTransformer;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Job who starts transforming the formatter-rules to checkstyle-settings.
 *
 * @author Lukas Frena
 *
 */
public class TransformFormatterRulesJob extends WorkspaceJob {

  /**
   * Job for transforming formatter-rules to checkstyle-settings.
   */
  public TransformFormatterRulesJob() {
    super(Messages.TransformFormatterRulesJob_name);
  }

  @Override
  public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
    SubMonitor subMonitor = SubMonitor.convert(monitor);
    subMonitor.setWorkRemaining(IProgressMonitor.UNKNOWN);

    // TODO this way of loading formatter profiles is very dubious, to say
    // the least, refer to FormatterConfigWriter for a better API
    final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

    final String configLocation = workspace
            + "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs"; //$NON-NLS-1$

    FormatterConfigParser parser = null;

    try {
      parser = new FormatterConfigParser(configLocation);
    } catch (final FileNotFoundException ex) {
      return Status.CANCEL_STATUS;
    }
    final FormatterConfiguration rules = parser.parseRules();

    if (rules == null) {
      return Status.CANCEL_STATUS;
    }

    try {
      FormatterTransformer transformer = new FormatterTransformer(rules);
      transformer.transformRules(workspace + "/test-checkstyle.xml"); //$NON-NLS-1$
    } catch (CheckstylePluginException ex) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              ex.getMessage(), ex);
      throw new CoreException(status);
    }

    return Status.OK_STATUS;
  }

  @Override
  public boolean belongsTo(Object family) {
    return AbstractCheckJob.CHECKSTYLE_JOB_FAMILY.equals(family) || super.belongsTo(family);
  }

}
