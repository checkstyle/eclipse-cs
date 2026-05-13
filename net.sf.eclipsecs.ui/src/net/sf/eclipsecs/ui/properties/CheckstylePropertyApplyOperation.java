//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.jobs.BuildProjectJob;
import net.sf.eclipsecs.core.jobs.ConfigureDeconfigureNatureJob;
import net.sf.eclipsecs.core.jobs.TransformCheckstyleRulesJob;
import net.sf.eclipsecs.core.nature.CheckstyleNature;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;

public class CheckstylePropertyApplyOperation {

  private CheckstylePropertyApplyOperation() {

  }

  public static boolean apply(Shell shell, ProjectConfigurationWorkingCopy projectConfig,
          boolean checkstyleEnabled, boolean checkstyleInitiallyEnabled) {
    try {

      IProject project = projectConfig.getProject();

      // save the edited project configuration
      if (projectConfig.isDirty()) {
        projectConfig.store();
      }

      // check if checkstyle nature has to be configured/deconfigured
      if (checkstyleEnabled != checkstyleInitiallyEnabled) {
        ConfigureDeconfigureNatureJob configOperation = new ConfigureDeconfigureNatureJob(project,
                CheckstyleNature.NATURE_ID);
        configOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
        configOperation.schedule();
      }

      if (checkstyleEnabled) {
        boolean needRebuild = !checkstyleInitiallyEnabled || projectConfig.isRebuildNeeded();

        if (projectConfig.isSyncFormatter()) {
          TransformCheckstyleRulesJob transFormJob = new TransformCheckstyleRulesJob(project);
          transFormJob.schedule();
        }

        // if a rebuild is advised, check/prompt if the rebuild should
        // really be done.
        if (needRebuild) {
          String promptRebuildPref = CheckstyleUIPluginPrefs
                  .getString(CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

          boolean doRebuild = MessageDialogWithToggle.ALWAYS.equals(promptRebuildPref);

          //
          // Prompt for rebuild
          //
          if (MessageDialogWithToggle.PROMPT.equals(promptRebuildPref)) {
            MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(shell,
                    Messages.CheckstylePropertyPage_titleRebuild,
                    Messages.CheckstylePropertyPage_msgRebuild,
                    Messages.CheckstylePropertyPage_nagRebuild, false,
                    CheckstyleUIPlugin.getDefault().getPreferenceStore(),
                    CheckstyleUIPluginPrefs.PREF_ASK_BEFORE_REBUILD);

            doRebuild = dialog.getReturnCode() == IDialogConstants.YES_ID;
          }

          // check if a rebuild is necessary
          if (doRebuild) {

            BuildProjectJob rebuildOperation = new BuildProjectJob(project,
                    IncrementalProjectBuilder.FULL_BUILD);
            rebuildOperation.setRule(ResourcesPlugin.getWorkspace().getRoot());
            rebuildOperation.schedule();
          }
        }
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(shell, ex, true);
    }
    return true;
  }
}
