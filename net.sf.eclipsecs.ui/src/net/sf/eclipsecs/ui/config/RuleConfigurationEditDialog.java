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

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.osgi.service.prefs.BackingStoreException;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Edit dialog for property values.
 */
public class RuleConfigurationEditDialog extends TitleAreaDialog {

  private final Module mRule;
  private final String mTitle;
  private final boolean mReadonly;

  private RuleConfigurationEditDialogGeneralSettings generalSettings;
  private RuleConfigurationEditDialogAdvancedSettings advancedSettings;

  /**
   * Constructor.
   *
   * @param parent
   *          Parent shell.
   * @param rule
   *          Rule being edited.
   */
  public RuleConfigurationEditDialog(Shell parent, Module rule, boolean readonly, String title) {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
    mRule = rule;
    mReadonly = readonly;
    mTitle = title;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);

    TabFolder mMainTab = new TabFolder(composite, SWT.NULL);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(mMainTab);

    generalSettings = new RuleConfigurationEditDialogGeneralSettings(mMainTab, SWT.NULL, mRule, mReadonly);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(generalSettings);

    advancedSettings = new RuleConfigurationEditDialogAdvancedSettings(mMainTab, SWT.NULL, mRule, mReadonly);
    GridDataFactory.create(GridData.FILL_BOTH).applyTo(advancedSettings);

    TabItem mainItem = new TabItem(mMainTab, SWT.NULL);
    mainItem.setControl(generalSettings);
    mainItem.setText(Messages.RuleConfigurationEditDialog_tabGeneral);

    TabItem advancedItem = new TabItem(mMainTab, SWT.NULL);
    advancedItem.setControl(advancedSettings);
    advancedItem.setText(Messages.RuleConfigurationEditDialog_tabAdvanced);

    initialize();
    return composite;
  }

  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).applyTo(composite);

    Button mBtnTranslate = new Button(composite, SWT.CHECK);
    mBtnTranslate.setText(Messages.RuleConfigurationEditDialog_btnTranslateTokens);
    GridDataFactory.swtDefaults().indent(5, 0).applyTo(mBtnTranslate);

    // Init the translate tokens preference
    mBtnTranslate.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_TRANSLATE_TOKENS));
    mBtnTranslate.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      // store translation preference
      try {
        CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_TRANSLATE_TOKENS,
                ((Button) event.widget).getSelection());
      } catch (BackingStoreException ex) {
        CheckstyleLog.log(ex);
      }
    }));

    Button mBtnSort = new Button(composite, SWT.CHECK);
    mBtnSort.setText(Messages.RuleConfigurationEditDialog_btnSortTokens);
    GridDataFactory.swtDefaults().indent(5, 0).applyTo(mBtnSort);

    // Init the sort tokens preference
    mBtnSort.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_SORT_TOKENS));
    mBtnSort.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      // store translation preference
      try {
        CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_SORT_TOKENS,
                ((Button) event.widget).getSelection());
      } catch (BackingStoreException ex) {
        CheckstyleLog.log(ex);
      }
    }));

    Control buttonBar = super.createButtonBar(composite);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).align(SWT.END, SWT.CENTER).applyTo(buttonBar);

    return composite;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {

    Button defautlt = createButton(parent, IDialogConstants.BACK_ID,
            Messages.RuleConfigurationEditDialog_btnDefaul, false);
    defautlt.setEnabled(!mReadonly);

    // create OK and Cancel buttons by default
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  private void initialize() {

    this.setTitle(
            NLS.bind(Messages.RuleConfigurationEditDialog_titleRuleConfigEditor, mRule.getName()));
    if (mReadonly) {
      this.setMessage(Messages.RuleConfigurationEditDialog_msgReadonlyModule);
    } else {
      this.setMessage(Messages.RuleConfigurationEditDialog_msgEditRuleConfig);
    }

    // set the logo
    this.setTitleImage(CheckstyleUIPluginImages.PLUGIN_LOGO.getImage());
  }

  @Override
  protected void buttonPressed(int buttonId) {
    if (IDialogConstants.BACK_ID == buttonId) {

      if (MessageDialog.openConfirm(getShell(),
              Messages.RuleConfigurationEditDialog_titleRestoreDefault,
              Messages.RuleConfigurationEditDialog_msgRestoreDefault)) {

        if (mRule.getMetaData().hasSeverity()) {
          generalSettings.setSeverity(mRule.getMetaData().defaultSeverity());
          advancedSettings.resetComment();
        }

        // restore the default value for the properties
        generalSettings.restoreProperties();
      }
    } else {
      super.buttonPressed(buttonId);
    }
  }

  /**
   * OK button was selected.
   */
  @Override
  protected void okPressed() {
    //
    // Get the selected severity level.
    //
    Severity severity = mRule.getSeverity();
    try {
      severity = generalSettings.getSeverity();
    } catch (IllegalArgumentException ex) {
      CheckstyleLog.log(ex);
    }

    // Get the comment.
    final String comment = StringUtils.trimToNull(advancedSettings.getComment());

    // Get the id
    final String id = StringUtils.trimToNull(advancedSettings.getId());

    // Get the custom message
    for (Map.Entry<String, String> entry : advancedSettings.getCustomMessages().entrySet()) {

      String msgKey = entry.getKey();

      String standardMessage = MetadataFactory.getStandardMessage(msgKey,
              mRule.getMetaData().identity().internalName());
      if (standardMessage == null) {
        standardMessage = ""; //$NON-NLS-1$
      }

      String message = StringUtils.trimToNull(entry.getValue());
      if (message != null && !message.equals(standardMessage)) {
        mRule.getCustomMessages().put(msgKey, message);
      } else {
        mRule.getCustomMessages().remove(msgKey);
      }
    }

    //
    // Build a new collection of configuration properties.
    //
    // Note: if the rule does not have any configuration properties then
    // skip over the populating of the config property hash map.
    //
    Optional<String> widgetValiationError = generalSettings.validatePropertyWidgets();
    if (widgetValiationError.isPresent()) {
      setErrorMessage(widgetValiationError.get());
    } else {
      // If we made it this far then all of the user input validated and we
      // can
      // update the final rule with the values the user entered.
      mRule.setSeverity(severity);
      mRule.setComment(comment);
      mRule.setId(id);
      super.okPressed();
    }
  }

  @Override
  public void create() {
    super.create();

    // add resize support - for each different module the settings will be
    // stored separately
    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            RuleConfigurationEditDialog.class.getName() + "#" //$NON-NLS-1$
                    + mRule.getMetaData().identity().internalName());
  }

  /**
   * Over-rides method from Window to configure the shell (e.g. the enclosing window).
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);

    shell.setText(mTitle);
  }
}
