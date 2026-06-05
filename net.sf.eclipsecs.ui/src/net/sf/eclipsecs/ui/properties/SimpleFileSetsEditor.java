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

package net.sf.eclipsecs.ui.properties;

import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.projectconfig.FileMatchPattern;
import net.sf.eclipsecs.core.projectconfig.FileSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialog;

/**
 * Simple file sets editor producing only one file set that contains all files. Only the check
 * configuration can be chosen.
 *
 */
public class SimpleFileSetsEditor implements FileSetsEditor {

  private final PropertyPageContext propertyPageContext;
  private final Shell shell;

  private SimpleFileSetsEditorView editorView;

  private List<FileSet> mFileSets;

  private FileSet mDefaultFileSet;

  /**
   * Creates the SimpleFileSetsEditor.
   *
   * @param propsPage
   *          the property page
   */
  public SimpleFileSetsEditor(Shell shell, PropertyPageContext propertyPageContext) {
    this.shell = shell;
    this.propertyPageContext = propertyPageContext;
  }

  @Override
  public void setFileSets(List<FileSet> fileSets) {
    mFileSets = fileSets;

    CheckConfiguration config = null;
    if (!mFileSets.isEmpty()) {
      config = (mFileSets.get(0)).getCheckConfig();
    }

    if (config == null) {
      CheckConfigurationWorkingCopy[] allConfigs = propertyPageContext.configuration()
              .getGlobalCheckConfigWorkingSet().getWorkingCopies();
      if (allConfigs.length > 0) {
        config = allConfigs[0];
      }
    }

    mDefaultFileSet = new FileSet(Messages.SimpleFileSetsEditor_nameAllFileset, config);
    try {
      mDefaultFileSet.getFileMatchPatterns().add(new FileMatchPattern("."));
    } catch (CheckstylePluginException ex) {
      // impossible
    }
    mFileSets.clear();
    mFileSets.add(mDefaultFileSet);
  }

  @Override
  public List<FileSet> getFileSets() {
    return mFileSets;
  }

  @Override
  public Control createContents(Composite parent) {
    this.editorView = new SimpleFileSetsEditorView(parent, SWT.NONE, this::manageConfig,
            mDefaultFileSet, propertyPageContext);
    return editorView;
  }

  private void manageConfig() {
    CheckConfiguration config = mDefaultFileSet.getCheckConfig();
    if (config != null) {
      try {
        config.getCheckstyleConfiguration();
        CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(shell,
                (CheckConfigurationWorkingCopy) config);
        dialog.setBlockOnOpen(true);
        dialog.open();
      } catch (CheckstylePluginException ex) {
        CheckstyleUiPlugin.warningDialog(shell,
                NLS.bind(Messages.CheckstylePreferencePage_msgProjectRelativeConfigNoFound,
                        propertyPageContext.project(), config.getLocation()), ex);
      }
    }
  }

  @Override
  public void refresh() {
    editorView.refresh();
  }

}
