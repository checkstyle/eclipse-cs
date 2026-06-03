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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.CheckConfigurationConfigureDialogView.CheckConfigurationConfigureDialogViewCallbacks;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Enhanced checkstyle configuration editor.
 *
 */
public class CheckConfigurationConfigureDialog extends TitleAreaDialog {

  /** The current check configuration. */
  private final CheckConfigurationWorkingCopy mConfiguration;

  /** the list of modules. */
  private List<Module> mModules;

  /** Flags if the check configuration was changed. */
  private boolean mIsDirty;

  private CheckConfigurationConfigureDialogView dialogView;

  //
  // constructors
  //

  /**
   * Creates the configuration dialog.
   *
   * @param parentShell
   *          the parent shell
   * @param config
   *          the check configuration
   */
  public CheckConfigurationConfigureDialog(Shell parentShell,
          CheckConfigurationWorkingCopy config) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    setHelpAvailable(false);
    mConfiguration = config;
  }

  //
  // methods
  //

  /**
   * Creates the dialogs main contents.
   *
   * @param parent
   *          the parent composite
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);

    try {
      new ProgressMonitorDialog(getShell()).run(true, false, monitor -> {
        // this takes quite long the first time due to class loading etc. of Checkstyle
        monitor.beginTask("Loading Checkstyle metadata", IProgressMonitor.UNKNOWN);
        loadModules();
      });
    } catch (InvocationTargetException | InterruptedException ex) {
      CheckstyleLog.log(ex);
    }

    dialogView = new CheckConfigurationConfigureDialogView(composite, SWT.NULL,
            new CheckConfigurationConfigureDialogViewCallbacks(this::newModule, this::openModule,
                    this::removeModule, this::checkStateChanged),
            mModules, mConfiguration.isConfigurable());
    GridDataFactory.fillDefaults().applyTo(dialogView);

    // initialize the data
    initialize();

    return dialogView;
  }

  @Override
  public void create() {
    super.create();

    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            CheckConfigurationConfigureDialog.class.getName());
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.CheckConfigurationConfigureDialog_titleCheckConfigurationDialog);
  }

  @Override
  protected void okPressed() {

    try {
      // only write the modules back if the config is configurable
      // and was actually changed
      if (mConfiguration.isConfigurable() && mIsDirty) {
        mConfiguration.setModules(mModules);
      }
    } catch (CheckstylePluginException ex) {
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }

    super.okPressed();
  }

  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(composite);

    Button mBtnOpenModuleOnAdd = new Button(composite, SWT.CHECK);
    mBtnOpenModuleOnAdd.setText(Messages.CheckConfigurationConfigureDialog_btnOpenModuleOnAdd);
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(5, 0).applyTo(mBtnOpenModuleOnAdd);

    // Init the translate tokens preference
    mBtnOpenModuleOnAdd.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR));
    mBtnOpenModuleOnAdd.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      // store translation preference
      try {
        CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR,
                ((Button) event.widget).getSelection());
      } catch (BackingStoreException ex) {
        CheckstyleLog.log(ex);
      }
    }));

    Control buttonBar = super.createButtonBar(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).align(SWT.END, SWT.CENTER).applyTo(buttonBar);

    return composite;
  }

  /**
   * Initialize the dialogs controls with the data.
   */
  private void initialize() {
    this.setTitle(NLS.bind(Messages.CheckConfigurationConfigureDialog_titleMessageArea,
            mConfiguration.getType().getName(), mConfiguration.getName()));

    if (mConfiguration.isConfigurable()) {
      this.setMessage(Messages.CheckConfigurationConfigureDialog_msgEditConfig);
    } else {
      this.setMessage(Messages.CheckConfigurationConfigureDialog_msgReadonlyConfig);
    }

    // set the logo
    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());

    this.dialogView.selectFirstAvailableGroup();
  }

  private void loadModules() {
    try {
      mModules = mConfiguration.getModules();
    } catch (CheckstylePluginException ex) {
      mModules = new ArrayList<>();
      CheckstyleUIPlugin.errorDialog(getShell(), ex, true);
    }
  }

  /**
   * Creates a module editor for the current selection.
   *
   * @param selection
   *          the selection
   * @return whether configuration was successful
   */
  private void newModule(List<RuleMetadata> rules) {
    if (!mConfiguration.isConfigurable()) {
      return;
    }
    boolean openOnAdd = CheckstyleUIPluginPrefs
            .getBoolean(CheckstyleUIPluginPrefs.PREF_OPEN_MODULE_EDITOR);

    for (RuleMetadata rule : rules) {
      // check if the module is a singleton and already
      // configured
      if (rule.isSingleton() && isAlreadyConfigured(rule)) {
        return;
      }

      Module workingCopy = new Module(rule, false);

      if (openOnAdd) {

        RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(getShell(),
                workingCopy, !mConfiguration.isConfigurable(),
                Messages.CheckConfigurationConfigureDialog_titleNewModule);
        if (mConfiguration.isConfigurable()) {
          int dialogResult = dialog.open();
          if (Window.OK == dialogResult) {
            mModules.add(workingCopy);
            mIsDirty = true;
            dialogView.refreshConfiguredModules();
            dialogView.refreshAvailableModules();
            dialogView.focusAvailableModules();
          }
          if (Window.CANCEL == dialogResult) {
            // stop showing more dialogs and also don't add any further rules
            return;
          }
        }
      } else {
        mModules.add(workingCopy);
        mIsDirty = true;
        dialogView.refreshConfiguredModules();
        dialogView.refreshAvailableModules();
      }
    }
  }

  /**
   * Checks if a certain module is already contained in the configuration.
   */
  private boolean isAlreadyConfigured(RuleMetadata metadata) {
    String internalName = metadata.identity().internalName();
    boolean containsModule = false;
    for (int i = 0, size = mModules.size(); i < size; i++) {

      Module module = mModules.get(i);

      if (internalName.equals(module.getMetaData().identity().internalName())) {
        containsModule = true;
        break;
      }

    }
    return containsModule;
  }

  /**
   * Opens the module editor for the current selection.
   *
   * @param selection
   *          the selection
   */
  private void openModule(Module module) {
    if (module != null) {
      Module workingCopy = module.clone();
      RuleConfigurationEditDialog dialog = new RuleConfigurationEditDialog(getShell(), workingCopy,
              !mConfiguration.isConfigurable(),
              Messages.CheckConfigurationConfigureDialog_titleModuleConfigEditor);
      if (Window.OK == dialog.open() && mConfiguration.isConfigurable()) {
        mModules.set(mModules.indexOf(module), workingCopy);
        mIsDirty = true;
        dialogView.refreshConfiguredModules();
      }
    }
  }

  /**
   * Creates a module editor for the current selection.
   *
   * @param selection
   *          the selection
   */
  private void removeModule(List<Module> modules) {
    if (mConfiguration.isConfigurable() && !modules.isEmpty()) {
      if (MessageDialog.openConfirm(getShell(),
              Messages.CheckConfigurationConfigureDialog_titleRemoveModules,
              Messages.CheckConfigurationConfigureDialog_msgRemoveModules)) {
        for (Module module : modules) {
          if (module.getMetaData().deletable()) {
            mModules.remove(module);
            mIsDirty = true;
            dialogView.refreshConfiguredModules();
            dialogView.refreshAvailableModules();
          }
        }
      }
    }
  }

  private void checkStateChanged(Module module, boolean checked) {
    if (checked) {
      // restore last severity before setting to ignore
      Severity lastEnabled = module.getLastEnabledSeverity();
      if (lastEnabled != null) {
        module.setSeverity(lastEnabled);
      } else {
        module.setSeverity(module.getMetaData().defaultSeverity());
      }
    } else {
      module.setSeverity(Severity.IGNORE);
    }
    mIsDirty = true;
    dialogView.refreshConfiguredModules();
  }

}
