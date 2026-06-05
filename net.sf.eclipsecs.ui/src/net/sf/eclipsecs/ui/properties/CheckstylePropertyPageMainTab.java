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

import java.util.function.Consumer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sf.eclipsecs.ui.CheckstyleUiPlugin;
import net.sf.eclipsecs.ui.CheckstyleUiPluginPrefs;
import net.sf.eclipsecs.ui.Messages;

public final class CheckstylePropertyPageMainTab extends Composite {

  private final PropertyPageContext propertyPageContext;
  private final Button mChkSimpleConfig;
  private final Button mChkEnable;
  private final Composite mFileSetsContainer;

  private FileSetsEditor mFileSetsEditor;

  public CheckstylePropertyPageMainTab(Composite parent, int style,
          PropertyPageContext propertyPageContext, boolean mCheckstyleInitiallyActivated) {
    super(parent, style);
    this.propertyPageContext = propertyPageContext;

    setLayout(new FormLayout());

    // create the checkbox to enable/disable the simple configuration
    this.mChkSimpleConfig = new Button(this, SWT.CHECK);
    this.mChkSimpleConfig.setText(Messages.CheckstylePropertyPage_btnUseSimpleConfig);
    this.mChkSimpleConfig.addSelectionListener(new ChkSimpleConfigController());
    this.mChkSimpleConfig.setSelection(propertyPageContext.configuration().isUseSimpleConfig());

    this.mChkSimpleConfig.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(100, -3);
    }));

    // create the checkbox to enable/disable checkstyle
    this.mChkEnable = new Button(this, SWT.CHECK);
    this.mChkEnable.setText(Messages.CheckstylePropertyPage_btnActivateCheckstyle);
    this.mChkEnable.setSelection(mCheckstyleInitiallyActivated);

    this.mChkEnable.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(0, 3);
      formData.right = new FormAttachment(this.mChkSimpleConfig, 3, SWT.LEFT);
    }));

    // create the checkbox for formatter syncing
    Button mChkSyncFormatter = new Button(this, SWT.CHECK);
    mChkSyncFormatter.setText(Messages.CheckstylePropertyPage_btnSyncFormatter);
    mChkSyncFormatter.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
      propertyPageContext.configuration().setSyncFormatter(mChkSyncFormatter.getSelection());
    }));
    mChkSyncFormatter.setSelection(propertyPageContext.configuration().isSyncFormatter());

    mChkSyncFormatter.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(this.mChkEnable, 3, SWT.BOTTOM);
    }));

    // create the configuration area
    mFileSetsContainer = new Composite(this, SWT.NULL);
    final Control configArea = createFileSetsArea(mFileSetsContainer);
    configArea.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(mChkSyncFormatter, 6, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
      formData.bottom = new FormAttachment(45);
    }));

    // create the filter area
    final Control filterArea = new FilterSettings(this, SWT.NONE,
            propertyPageContext.configuration().getProject(),
            propertyPageContext.configuration().getFilters(), propertyPageContext.updateButtons());
    filterArea.setLayoutData(formData(formData -> {
      formData.left = new FormAttachment(0, 3);
      formData.top = new FormAttachment(configArea, 3, SWT.BOTTOM);
      formData.right = new FormAttachment(100, -3);
      formData.bottom = new FormAttachment(100, -3);
      formData.width = 500;
    }));
  }

  private static FormData formData(Consumer<FormData> custom) {
    FormData formData = new FormData();
    custom.accept(formData);
    return formData;
  }

  public boolean isCheckstyleEnabled() {
    return mChkEnable.getSelection();
  }

  public void refreshFileSetEditor() {
    mFileSetsEditor.refresh();
  }

  /**
   * Creates the file sets area.
   *
   * @param fileSetsContainer
   *          the container to add the file sets area to
   */
  private Control createFileSetsArea(Composite fileSetsContainer) {
    Control[] controls = fileSetsContainer.getChildren();
    for (int i = 0; i < controls.length; i++) {
      controls[i].dispose();
    }

    this.mFileSetsEditor = FileSetsEditorFactory.createEditor(getShell(), propertyPageContext,
            propertyPageContext.configuration().isUseSimpleConfig());
    mFileSetsEditor.setFileSets(propertyPageContext.configuration().getFileSets());

    final Control editor = mFileSetsEditor.createContents(mFileSetsContainer);

    fileSetsContainer.setLayout(new FormLayout());
    FormData formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.top = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    formData.bottom = new FormAttachment(100);
    editor.setLayoutData(formData);

    return fileSetsContainer;
  }

  private final class ChkSimpleConfigController extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      propertyPageContext.configuration().setUseSimpleConfig(mChkSimpleConfig.getSelection());

      boolean showWarning = CheckstyleUiPluginPrefs
              .getBoolean(CheckstyleUiPluginPrefs.PREF_FILESET_WARNING);
      if (showWarning && propertyPageContext.configuration().isUseSimpleConfig()) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(getShell(),
                Messages.CheckstylePropertyPage_titleWarnFilesets, null,
                Messages.CheckstylePropertyPage_msgWarnFilesets, MessageDialog.WARNING,
                new String[] {
                    IDialogConstants.OK_LABEL,
                }, 0,
                Messages.CheckstylePropertyPage_mgsWarnFileSetNagOption, showWarning) {
          /**
           * Overwritten because we don't want to store which button the user pressed but the
           * state of the toggle.
           */
          @Override
          protected void buttonPressed(int buttonId) {
            getPrefStore().setValue(getPrefKey(), getToggleState());
            setReturnCode(buttonId);
            close();
          }

        };
        dialog.setPrefStore(CheckstyleUiPlugin.getDefault().getPreferenceStore());
        dialog.setPrefKey(CheckstyleUiPluginPrefs.PREF_FILESET_WARNING);
        dialog.open();

      }

      createFileSetsArea(mFileSetsContainer);
      mFileSetsContainer.redraw();
      mFileSetsContainer.update();
      mFileSetsContainer.layout();
    }
  }
}
