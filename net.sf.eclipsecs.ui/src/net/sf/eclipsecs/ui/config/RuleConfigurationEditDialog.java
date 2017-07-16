//============================================================================
//
// Copyright (C) 2002-2016  David Schneider, Lars Ködderitzsch
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

package net.sf.eclipsecs.ui.config;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.Severity;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPlugin;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.CheckstyleUIPluginPrefs;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.widgets.ConfigPropertyWidgetFactory;
import net.sf.eclipsecs.ui.config.widgets.IConfigPropertyWidget;
import net.sf.eclipsecs.ui.util.SWTUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Edit dialog for property values.
 */
public class RuleConfigurationEditDialog extends TitleAreaDialog {

  private final Module mRule;

  private TabFolder mMainTab;

  private Text mCommentText;

  private Text mIdText;

  private ComboViewer mSeverityCombo;

  private IConfigPropertyWidget[] mConfigPropertyWidgets;

  private Button mBtnTranslate;

  private Button mBtnSort;

  private Map<String, Text> mCustomMessages;

  private boolean mReadonly = false;

  private final String mTitle;

  /**
   * Constructor.
   *
   * @param parent
   *          Parent shell.
   * @param rule
   *          Rule being edited.
   */
  RuleConfigurationEditDialog(Shell parent, Module rule, boolean readonly, String title) {
    super(parent);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    mRule = rule;
    mReadonly = readonly;
    mTitle = title;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);

    mMainTab = new TabFolder(composite, SWT.NULL);
    mMainTab.setLayoutData(new GridData(GridData.FILL_BOTH));

    Composite generalSettings = createGeneralSection();
    Composite advancedSettings = createAdvancedSection();

    TabItem mainItem = new TabItem(mMainTab, SWT.NULL);
    mainItem.setControl(generalSettings);
    mainItem.setText(Messages.RuleConfigurationEditDialog_tabGeneral);

    TabItem advancedItem = new TabItem(mMainTab, SWT.NULL);
    advancedItem.setControl(advancedSettings);
    advancedItem.setText(Messages.RuleConfigurationEditDialog_tabAdvanced);

    initialize();
    return composite;
  }

  private Composite createGeneralSection() {
    Composite generalSettings = new Composite(mMainTab, SWT.NULL);
    generalSettings.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    generalSettings.setLayout(layout);

    // Build severity
    Label lblSeverity = new Label(generalSettings, SWT.NULL);
    lblSeverity.setText(Messages.RuleConfigurationEditDialog_lblSeverity);
    lblSeverity.setLayoutData(new GridData());

    mSeverityCombo = new ComboViewer(generalSettings);
    mSeverityCombo.setContentProvider(new ArrayContentProvider());
    mSeverityCombo.setLabelProvider(new LabelProvider() {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText(Object element) {
        return ((Severity) element).name();
      }
    });
    mSeverityCombo.getControl().setLayoutData(new GridData());

    Group properties = new Group(generalSettings, SWT.NULL);
    properties.setLayout(new GridLayout(3, false));
    properties.setText(Messages.RuleConfigurationEditDialog_lblProperties);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 2;
    properties.setLayoutData(gd);

    createConfigPropertyEntries(properties);

    if (mConfigPropertyWidgets == null || mConfigPropertyWidgets.length == 0) {

      properties.dispose();
    }
    return generalSettings;
  }

  private Composite createAdvancedSection() {

    Composite advancedSettings = new Composite(mMainTab, SWT.NULL);
    advancedSettings.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    advancedSettings.setLayout(layout);

    // Build comment
    Label commentLabel = new Label(advancedSettings, SWT.NULL);
    commentLabel.setText(Messages.RuleConfigurationEditDialog_lblComment);
    commentLabel.setLayoutData(new GridData());

    mCommentText = new Text(advancedSettings, SWT.SINGLE | SWT.BORDER);
    mCommentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label idLabel = new Label(advancedSettings, SWT.NULL);
    idLabel.setText(Messages.RuleConfigurationEditDialog_lblId);
    idLabel.setLayoutData(new GridData());

    mIdText = new Text(advancedSettings, SWT.SINGLE | SWT.BORDER);
    mIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Group messagesGroup = new Group(advancedSettings, SWT.NULL);
    messagesGroup.setText(Messages.RuleConfigurationEditDialog_titleCustMsg);
    messagesGroup.setLayout(new GridLayout(2, false));
    GridData d = new GridData(GridData.FILL_HORIZONTAL);
    d.horizontalSpan = 2;
    messagesGroup.setLayoutData(d);

    mCustomMessages = new HashMap<>();

    // take keys from metadata as well as predefined from the
    // configuration. This way we don't lose keys not defined in metadata.
    Set<String> msgKeys = new TreeSet<>();
    msgKeys.addAll(mRule.getMetaData().getMessageKeys());
    msgKeys.addAll(mRule.getCustomMessages().keySet());

    for (String msgKey : msgKeys) {

      Label msgLabel = new Label(messagesGroup, SWT.NULL);
      msgLabel.setText(msgKey);
      msgLabel.setLayoutData(new GridData());

      final Text msgText = new Text(messagesGroup, SWT.SINGLE | SWT.BORDER);
      // |SWT.SEARCH see below
      msgText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      final String standardMessage = MetadataFactory.getStandardMessage(msgKey,
              mRule.getMetaData().getInternalName());

      // msgText.setMessage(standardMessage); //a nice solution, sadly only for
      // Eclipse 3.3+

      // alternative for above
      if (standardMessage != null) {
        msgText.setText(standardMessage);
      }
      msgText.addFocusListener(new FocusListener() {

        @Override
        public void focusGained(FocusEvent e) {
          Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
              if (msgText.getText().equals(standardMessage)) {
                msgText.selectAll();
              }
            }
          });
        }

        @Override
        public void focusLost(FocusEvent e) {
          // NOOP
        }
      });

      String message = mRule.getCustomMessages().get(msgKey);
      if (Strings.emptyToNull(message) != null) {
        msgText.setText(message);
      }
      msgText.setEnabled(!mReadonly);

      mCustomMessages.put(msgKey, msgText);
    }

    return advancedSettings;
  }

  @Override
  protected Control createButtonBar(Composite parent) {

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    mBtnTranslate = new Button(composite, SWT.CHECK);
    mBtnTranslate.setText(Messages.RuleConfigurationEditDialog_btnTranslateTokens);
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.horizontalIndent = 5;
    mBtnTranslate.setLayoutData(gd);

    // Init the translate tokens preference
    mBtnTranslate.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_TRANSLATE_TOKENS));
    mBtnTranslate.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        // store translation preference
        try {
          CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_TRANSLATE_TOKENS,
                  ((Button) e.widget).getSelection());
        } catch (BackingStoreException e1) {
          CheckstyleLog.log(e1);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        // NOOP
      }
    });

    mBtnSort = new Button(composite, SWT.CHECK);
    mBtnSort.setText(Messages.RuleConfigurationEditDialog_btnSortTokens);
    gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.horizontalIndent = 5;
    mBtnSort.setLayoutData(gd);

    // Init the sort tokens preference
    mBtnSort.setSelection(
            CheckstyleUIPluginPrefs.getBoolean(CheckstyleUIPluginPrefs.PREF_SORT_TOKENS));
    mBtnSort.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {

        // store translation preference
        try {
          CheckstyleUIPluginPrefs.setBoolean(CheckstyleUIPluginPrefs.PREF_SORT_TOKENS,
                  ((Button) e.widget).getSelection());
        } catch (BackingStoreException e1) {
          CheckstyleLog.log(e1);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        // NOOP
      }
    });

    Control buttonBar = super.createButtonBar(composite);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalAlignment = GridData.END;
    buttonBar.setLayoutData(gd);

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
    if (!mReadonly) {
      this.setMessage(Messages.RuleConfigurationEditDialog_msgEditRuleConfig);
    } else {
      this.setMessage(Messages.RuleConfigurationEditDialog_msgReadonlyModule);
    }

    String comment = mRule.getComment();
    if (comment != null) {
      mCommentText.setText(comment);
    }

    String id = mRule.getId();
    if (id != null) {
      mIdText.setText(id);
    }

    mIdText.setEnabled(!mReadonly);
    // mCustomMessageText.setEditable(!mReadonly);
    mCommentText.setEnabled(!mReadonly);

    mSeverityCombo.setInput(Severity.values());
    mSeverityCombo.getCombo().setEnabled(!mReadonly);
    if (mRule.getMetaData().hasSeverity()) {
      mSeverityCombo.setSelection(new StructuredSelection(mRule.getSeverity()));
    } else {
      mSeverityCombo.getCombo().setEnabled(false);
    }

    // set the logo
    this.setTitleImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.PLUGIN_LOGO));

  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {
    if (IDialogConstants.BACK_ID == buttonId) {

      if (MessageDialog.openConfirm(getShell(),
              Messages.RuleConfigurationEditDialog_titleRestoreDefault,
              Messages.RuleConfigurationEditDialog_msgRestoreDefault)) {

        if (mRule.getMetaData().hasSeverity()) {
          mSeverityCombo.setSelection(
                  new StructuredSelection(mRule.getMetaData().getDefaultSeverityLevel()));
          mCommentText.setText(new String());
        }

        // restore the default value for the properties
        int size = mConfigPropertyWidgets != null ? mConfigPropertyWidgets.length : 0;
        for (int i = 0; i < size; i++) {
          mConfigPropertyWidgets[i].restorePropertyDefault();
        }
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
      severity = (Severity) ((IStructuredSelection) mSeverityCombo.getSelection())
              .getFirstElement();
    } catch (IllegalArgumentException e) {
      CheckstyleLog.log(e);
    }

    // Get the comment.
    final  String comment = Strings.emptyToNull(mCommentText.getText());

    // Get the id
    final String id = Strings.emptyToNull(mIdText.getText());

    // Get the custom message
    for (Map.Entry<String, Text> entry : mCustomMessages.entrySet()) {

      String msgKey = entry.getKey();

      String standardMessage = MetadataFactory.getStandardMessage(msgKey,
              mRule.getMetaData().getInternalName());
      if (standardMessage == null) {
        standardMessage = ""; //$NON-NLS-1$
      }

      String message = Strings.emptyToNull(entry.getValue().getText());
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
    if (mConfigPropertyWidgets != null) {
      for (int i = 0; i < mConfigPropertyWidgets.length; i++) {
        IConfigPropertyWidget widget = mConfigPropertyWidgets[i];
        ConfigProperty property = widget.getConfigProperty();

        try {
          widget.validate();
        } catch (CheckstylePluginException e) {
          String message = NLS.bind(Messages.RuleConfigurationEditDialog_msgInvalidPropertyValue,
                  property.getMetaData().getName());
          this.setErrorMessage(message);
          return;
        }
        property.setValue(widget.getValue());
      }
    }

    //
    // If we made it this far then all of the user input validated and we
    // can
    // update the final rule with the values the user entered.
    //
    mRule.setSeverity(severity);
    mRule.setComment(comment);
    mRule.setId(id);

    super.okPressed();

  }

  private void createConfigPropertyEntries(Composite parent) {

    List<ConfigProperty> configItemMetadata = mRule.getProperties();
    if (configItemMetadata.size() <= 0) {
      return;
    }

    mConfigPropertyWidgets = new IConfigPropertyWidget[configItemMetadata.size()];
    Iterator<ConfigProperty> iter = configItemMetadata.iterator();
    for (int i = 0; iter.hasNext(); i++) {
      ConfigProperty prop = iter.next();

      //
      // Add an input widget for the properties value.
      //
      mConfigPropertyWidgets[i] = ConfigPropertyWidgetFactory.createWidget(parent, prop,
              getShell());
      mConfigPropertyWidgets[i].setEnabled(!mReadonly);
    }
  }

  /**
   * @see org.eclipse.jface.window.Window#create()
   */
  @Override
  public void create() {
    super.create();

    // add resize support - for each different module the settings will be
    // stored separately
    SWTUtil.addResizeSupport(this, CheckstyleUIPlugin.getDefault().getDialogSettings(),
            RuleConfigurationEditDialog.class.getName() + "#" //$NON-NLS-1$
                    + mRule.getMetaData().getInternalName());
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
