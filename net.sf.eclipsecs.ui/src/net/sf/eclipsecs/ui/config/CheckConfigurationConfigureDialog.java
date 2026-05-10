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
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import net.sf.eclipsecs.core.config.CheckConfigurationWorkingCopy;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.RuleGroupMetadata;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.ConfiguredModulesTable.ConfiguredModulesTableCallbacks;
import net.sf.eclipsecs.ui.util.InternalBrowser;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Enhanced checkstyle configuration editor.
 *
 */
public class CheckConfigurationConfigureDialog extends TitleAreaDialog {

  private static final Pattern PATTERN_INLINE_CODE = Pattern.compile(Pattern.quote("{@code ") + "([^}]*?)" + Pattern.quote("}"));

  /** The current check configuration. */
  private final CheckConfigurationWorkingCopy mConfiguration;

  private AvailableModulesViewer availableModulesViewer;
  private ConfiguredModulesTable configuredModulesTable;

  private Browser mBrowserDescription;

  /** the list of modules. */
  private List<Module> mModules;

  /** Flags if the check configuration was changed. */
  private boolean mIsDirty;

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

    Composite contents = new Composite(composite, SWT.NULL);
    contents.setLayoutData(new GridData(GridData.FILL_BOTH));
    contents.setLayout(new GridLayout());

    SashForm sashForm = new SashForm(contents, SWT.NULL);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.widthHint = 700;
    gridData.heightHint = 400;
    sashForm.setLayoutData(gridData);
    sashForm.setLayout(new GridLayout());

    Label lblDescription = new Label(contents, SWT.NULL);
    lblDescription.setText(Messages.CheckConfigurationConfigureDialog_lblDescription);
    lblDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBrowserDescription = new Browser(contents, SWT.BORDER);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 100;
    mBrowserDescription.setLayoutData(gridData);
    mBrowserDescription.addLocationListener(LocationListener.changingAdapter(event -> {
      String url = event.location;
      if (url == null || !url.startsWith("http")) {
        return;
      }
      InternalBrowser.openLinkInExternalBrowser(url);
      event.doit = false;
    }));

    try {
      new ProgressMonitorDialog(getShell()).run(true, false, monitor -> {
        // this takes quite long the first time due to class loading etc. of Checkstyle
        monitor.beginTask("Loading Checkstyle metadata", IProgressMonitor.UNKNOWN);
        loadModules();
      });
    } catch (InvocationTargetException | InterruptedException ex) {
      CheckstyleLog.log(ex);
    }

    this.availableModulesViewer = new AvailableModulesViewer(sashForm, SWT.NULL, mModules,
            mConfiguration.isConfigurable(), this::newModule, this::selectionChanged);
    availableModulesViewer.setLayoutData(new GridData(GridData.FILL_BOTH));

    this.configuredModulesTable = new ConfiguredModulesTable(sashForm, SWT.NULL,
            mConfiguration.isConfigurable(), mModules,
            new ConfiguredModulesTableCallbacks(this::openModule, this::removeModule,
                    mBrowserDescription::setText, this::checkStateChanged));
    configuredModulesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

    sashForm.setWeights(new int[] { 30, 70 });

    // initialize the data
    initialize();

    return contents;
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
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Button mBtnOpenModuleOnAdd = new Button(composite, SWT.CHECK);
    mBtnOpenModuleOnAdd.setText(Messages.CheckConfigurationConfigureDialog_btnOpenModuleOnAdd);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.BEGINNING;
    gridData.horizontalIndent = 5;
    mBtnOpenModuleOnAdd.setLayoutData(gridData);

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
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalAlignment = GridData.END;
    buttonBar.setLayoutData(gridData);

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

    this.availableModulesViewer.selectFirstGroup();
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
            configuredModulesTable.refresh();
            availableModulesViewer.refresh();
            availableModulesViewer.focus();
          }
          if (Window.CANCEL == dialogResult) {
            // stop showing more dialogs and also don't add any further rules
            return;
          }
        }
      } else {
        mModules.add(workingCopy);
        mIsDirty = true;
        configuredModulesTable.refresh();
        availableModulesViewer.refresh();
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
        configuredModulesTable.refresh();
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
            configuredModulesTable.refresh();
            availableModulesViewer.refresh();
          }
        }
      }
    }
  }

  private void checkStateChanged(CheckStateChangedEvent event) {
    Module module = (Module) event.getElement();
    if (mConfiguration.isConfigurable()) {
      if (event.getChecked()) {
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
      configuredModulesTable.refresh();
    } else {
      configuredModulesTable.setChecked(module, !event.getChecked());
    }
  }

  /**
   * Convert a module description to HTML for use with a browser component.
   * @param description module description
   * @return HTML converted description
   */
  public static String getDescriptionHtml(String description) {
    StringBuilder buf = new StringBuilder();
    buf.append("<html><body style=\"margin: 3px; font-size: 11px; ");
    buf.append("font-family: verdana, 'trebuchet MS', helvetica, sans-serif;\">");
    buf.append(description != null ? convertInlineCodeTags(description)
            : Messages.CheckConfigurationConfigureDialog_txtNoDescription);
    buf.append("</body></html>");
    return buf.toString();
  }

  private static String convertInlineCodeTags(String html) {
    return PATTERN_INLINE_CODE.matcher(html).replaceAll("<code>$1</code>");
  }

  private void selectionChanged(Object selection) {
    String description = null;
    if (selection instanceof RuleGroupMetadata group) {
      description = group.getDescription();
      configuredModulesTable.setCurrentGroup(group);
      configuredModulesTable.setTextHeader(
              NLS.bind(Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                      group.getGroupName()));
      configuredModulesTable.refresh();
    } else if (selection instanceof RuleMetadata rule) {
      description = rule.identity().description();
      configuredModulesTable.setCurrentGroup(rule.identity().group());
      configuredModulesTable.setTextHeader(
              NLS.bind(Messages.CheckConfigurationConfigureDialog_lblConfiguredModules,
                      rule.identity().group().getGroupName()));
      configuredModulesTable.refresh();
    }

    String buf = getDescriptionHtml(description);
    mBrowserDescription.setText(buf);
  }

}
