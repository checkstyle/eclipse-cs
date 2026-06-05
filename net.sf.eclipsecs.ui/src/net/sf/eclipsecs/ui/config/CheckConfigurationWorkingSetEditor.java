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

package net.sf.eclipsecs.ui.config;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import net.sf.eclipsecs.core.config.CheckConfiguration;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.CheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.config.GlobalCheckConfigurationWorkingSet;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.CheckstyleUiPluginImages;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationWorkingSetEditorButtonBar.ButtonBarActions;
import net.sf.eclipsecs.ui.util.table.TableComparableProvider;
import net.sf.eclipsecs.ui.util.table.TableSettingsProvider;

/**
 * This class provides the editor GUI for a check configuration working set.
 *
 */
public final class CheckConfigurationWorkingSetEditor extends Composite {

  //
  // attributes
  //

  private final CheckConfigurationWorkingSet mWorkingSet;
  private final CheckConfigurationWorkingSetEditorView editorView;

  //
  // constructors
  //

  /**
   * Creates the configuration working set editor.
   *
   * @param workingSet
   *          the configuration working set to edit
   * @param showUsage
   *          determines if the usage area should be shown
   */
  public CheckConfigurationWorkingSetEditor(Composite parent, int style,
          CheckConfigurationWorkingSet workingSet) {
    super(parent, style);

    mWorkingSet = workingSet;

    GridLayoutFactory.fillDefaults().applyTo(this);

    boolean global = mWorkingSet instanceof GlobalCheckConfigurationWorkingSet;

    editorView = new CheckConfigurationWorkingSetEditorView(this, SWT.NONE, mWorkingSet.getWorkingCopies(), global,
            new ButtonBarActions(this::addCheckConfig, this::editCheckConfig,
                    this::configureCheckConfig, this::copyCheckConfig, this::removeCheckConfig,
                    this::setDefaultCheckConfig, this::exportCheckstyleCheckConfig),
            this::isDefaultConfig, new ConfigurationLabelProvider(workingSet));
    GridDataFactory.fillDefaults().grab(true, true).applyTo(editorView);
  }

  private boolean isDefaultConfig(CheckConfigurationWorkingCopy config) {
    boolean configDefault = false;
    if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet globalWorkingSet) {
      CheckConfigurationWorkingCopy defaultConfig = globalWorkingSet.getDefaultCheckConfig();
      configDefault = defaultConfig != null && defaultConfig.equals(config);
    }
    return configDefault;
  }

  /**
   * Create a new Check configuration.
   */
  private void addCheckConfig() {
    CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(), null,
            mWorkingSet);
    dialog.setBlockOnOpen(true);
    if (Window.OK == dialog.open()) {

      CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();
      mWorkingSet.addCheckConfiguration(newConfig);

      editorView.setConfigs(mWorkingSet.getWorkingCopies());
      editorView.setSelection(newConfig);
    }
  }

  /**
   * Edit the properties of a check configuration.
   */
  private void editCheckConfig() {
    CheckConfigurationWorkingCopy config = editorView.getSelectedConfig();
    if (config != null) {
      CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(),
              config, mWorkingSet);
      dialog.setBlockOnOpen(true);
      if (Window.OK == dialog.open()) {
        editorView.refresh();
      }
    }
  }

  private void configureCheckConfig() {
    CheckConfigurationWorkingCopy config = editorView.getSelectedConfig();

    if (config != null) {

      try {
        // test if file exists
        config.getCheckstyleConfiguration();

        CheckConfigurationConfigureDialog dialog = new CheckConfigurationConfigureDialog(getShell(),
                config);
        dialog.setBlockOnOpen(true);
        dialog.open();
      } catch (CheckstylePluginException ex) {
        CheckstyleUiPlugin.warningDialog(getShell(), NLS.bind(Messages.errorCannotResolveCheckLocation,
                config.getLocation(), config.getName()), ex);
      }
    }
  }

  /**
   * Copy an existing config.
   */
  private void copyCheckConfig() {
    CheckConfiguration sourceConfig = editorView.getSelectedConfig();
    if (sourceConfig != null) {
      try {

        // Open the properties dialog to change default name and description
        CheckConfigurationPropertiesDialog dialog = new CheckConfigurationPropertiesDialog(getShell(),
                null, mWorkingSet);
        dialog.setTemplateConfiguration(sourceConfig);

        dialog.setBlockOnOpen(true);
        if (Window.OK == dialog.open()) {

          CheckConfigurationWorkingCopy newConfig = dialog.getCheckConfiguration();

          // Copy the source configuration into the new internal config
          sourceConfig.copyConfiguration(newConfig);

          mWorkingSet.addCheckConfiguration(newConfig);

          editorView.setConfigs(mWorkingSet.getWorkingCopies());
        }
      } catch (CheckstylePluginException ex) {
        CheckstyleUiPlugin.errorDialog(getShell(), ex, true);
      }
    }
  }

  /**
   * Remove a config.
   */
  private void removeCheckConfig() {
    CheckConfigurationWorkingCopy checkConfig = editorView.getSelectedConfig();
    if (checkConfig != null && checkConfig.isEditable()) {
      boolean confirm = MessageDialog.openQuestion(getShell(),
              Messages.CheckstylePreferencePage_titleDelete,
              NLS.bind(Messages.CheckstylePreferencePage_msgDelete, checkConfig.getName()));
      if (confirm) {

        //
        // Make sure the check config is not in use. Don't let it be
        // deleted if it is.
        //
        if (mWorkingSet.removeCheckConfiguration(checkConfig)) {

          editorView.setConfigs(mWorkingSet.getWorkingCopies());
        } else {
          MessageDialog.openInformation(getShell(), Messages.CheckstylePreferencePage_titleCantDelete,
                  NLS.bind(Messages.CheckstylePreferencePage_msgCantDelete, checkConfig.getName()));
        }
      }
    }
  }

  private void setDefaultCheckConfig() {
    CheckConfigurationWorkingCopy checkConfig = editorView.getSelectedConfig();
    if (checkConfig != null) {
      if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet) {
        ((GlobalCheckConfigurationWorkingSet) mWorkingSet).setDefaultCheckConfig(checkConfig);
      }

      editorView.refresh();
    }
  }

  /**
   * Export a configuration.
   */
  private void exportCheckstyleCheckConfig() {
    CheckConfiguration config = editorView.getSelectedConfig();
    if (config != null) {
      FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
      dialog.setText(Messages.CheckstylePreferencePage_titleExportConfig);
      String path = dialog.open();
      if (path != null) {
        File file = new File(path);

        try {
          config.exportConfiguration(file);
        } catch (CheckstylePluginException ex) {
          CheckstyleUiPlugin.errorDialog(getShell(), Messages.msgErrorFailedExportConfig, ex, true);
        }
      }
    }
  }

  /**
   * Label provider for the check configuration table. Implements also support for table sorting and
   * storing of the table settings.
   *
   */
  public static final class ConfigurationLabelProvider extends CheckConfigurationLabelProvider
          implements ITableLabelProvider, TableComparableProvider, TableSettingsProvider {

    private final CheckConfigurationWorkingSet mWorkingSet;

    private ConfigurationLabelProvider(CheckConfigurationWorkingSet mWorkingSet) {
      this.mWorkingSet = mWorkingSet;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      String result = element.toString();
      if (element instanceof CheckConfiguration cfg) {
        if (columnIndex == 0) {
          result = cfg.getName();
        }
        if (columnIndex == 1) {
          result = cfg.getLocation();
        }
        if (columnIndex == 2) {
          result = cfg.getType().getName();
        }
        if (columnIndex == 3) {
          result = "";
        }
      }
      return result;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return switch (columnIndex) {
        case 0 -> getImage(element);
        case 3 -> {
          CheckConfiguration cfg = (CheckConfiguration) element;
          if (mWorkingSet instanceof GlobalCheckConfigurationWorkingSet globalWorkingSet
                  && globalWorkingSet.getDefaultCheckConfig() == cfg) {
            yield CheckstyleUiPluginImages.TICK_ICON.getImage();
          }
          yield null;
        }
        default -> null;
      };
    }

    @Override
    public Comparable<String> getComparableValue(Object element, int col) {
      return getColumnText(element, col);
    }

    @Override
    public IDialogSettings getTableSettings() {
      String concreteViewId = mWorkingSet.getClass().getName();

      IDialogSettings workbenchSettings = CheckstyleUiPlugin.getDefault().getDialogSettings();
      IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

      if (settings == null) {
        settings = workbenchSettings.addNewSection(concreteViewId);
      }

      return settings;
    }
  }
}
