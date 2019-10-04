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

import com.google.common.io.Closeables;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.configtypes.IContextAware;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.projectconfig.IProjectConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationFactory;
import net.sf.eclipsecs.core.transformer.CheckstyleTransformer;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xml.sax.InputSource;

/**
 * Job which starts transforming the checkstyle-rules to eclipse-formatter-settings.
 *
 * @author Lukas Frena
 *
 */
public class TransformCheckstyleRulesJob extends WorkspaceJob {

  /** Selected project in workspace. */
  private IProject mProject;

  /**
   * Job for transforming checkstyle to formatter-rules.
   *
   * @param project
   *          The current selected project in the workspace.
   */
  public TransformCheckstyleRulesJob(final IProject project) {
    super("transformCheckstyle");

    this.mProject = project;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IStatus runInWorkspace(final IProgressMonitor arg0) throws CoreException {

    try {
      final IProjectConfiguration conf = ProjectConfigurationFactory.getConfiguration(mProject);

      final List<Configuration> rules = new ArrayList<>();

      // collect rules from all configured filesets
      for (FileSet fs : conf.getFileSets()) {

        ICheckConfiguration checkConfig = fs.getCheckConfig();

        CheckstyleConfigurationFile configFile = checkConfig.getCheckstyleConfiguration();

        PropertyResolver resolver = configFile.getPropertyResolver();

        // set the project context if the property resolver needs the
        // context
        if (resolver instanceof IContextAware) {
          ((IContextAware) resolver).setProjectContext(mProject);
        }

        InputSource in = null;
        try {
          in = configFile.getCheckConfigFileInputSource();

          Configuration configuration = ConfigurationLoader.loadConfiguration(in, resolver,
                  IgnoredModulesOptions.OMIT);

          // flatten the nested configuration tree into a list
          recurseConfiguration(configuration, rules);
        } finally {
          Closeables.closeQuietly(in.getByteStream());
        }
      }

      if (rules.isEmpty()) {
        return Status.CANCEL_STATUS;
      }

      final CheckstyleTransformer transformer = new CheckstyleTransformer(mProject, rules);
      transformer.transformRules();
    } catch (CheckstyleException e) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              e.getMessage(), e);
      throw new CoreException(status);
    } catch (CheckstylePluginException e) {
      Status status = new Status(IStatus.ERROR, CheckstylePlugin.PLUGIN_ID, IStatus.ERROR,
              e.getMessage(), e);
      throw new CoreException(status);
    }

    return Status.OK_STATUS;
  }

  private static void recurseConfiguration(Configuration module, List<Configuration> flatModules) {

    flatModules.add(module);

    Configuration[] childs = module.getChildren();
    if (childs != null && childs.length > 0) {

      for (Configuration child : childs) {
        recurseConfiguration(child, flatModules);
      }
    }
  }
}
