//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

import java.io.FileNotFoundException;

import net.sf.eclipsecs.core.CheckstylePlugin;
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
    super("transformFormatter");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IStatus runInWorkspace(final IProgressMonitor arg0) throws CoreException {

    // TODO this way of loading formatter profiles is very dubious, to say
    // the least, refer to FormatterConfigWriter for a better API
    final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

    final String configLocation = workspace
            + "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs";

    FormatterConfigParser parser = null;

    try {
      parser = new FormatterConfigParser(configLocation);
    } catch (final FileNotFoundException e) {
      return Status.CANCEL_STATUS;
    }
    final FormatterConfiguration rules = parser.parseRules();

    if (rules == null) {
      return Status.CANCEL_STATUS;
    }

    try {
      FormatterTransformer transformer = new FormatterTransformer(rules);
      transformer.transformRules(workspace + "/test-checkstyle.xml");
    } catch (CheckstylePluginException e) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              e.getMessage(), e);
      throw new CoreException(status);
    }

    return Status.OK_STATUS;
  }
}
