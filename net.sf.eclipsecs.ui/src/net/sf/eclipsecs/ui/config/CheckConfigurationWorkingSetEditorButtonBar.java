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

package net.sf.eclipsecs.ui.config;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsecs.ui.Messages;

public final class CheckConfigurationWorkingSetEditorButtonBar extends Composite {

  private final boolean useDefaultButton;
  private final Button mEditButton;
  private final Button mConfigureButton;
  private final Button mCopyButton;
  private final Button mRemoveButton;
  private final Button mDefaultButton;
  private final Button mExportButton;

  public CheckConfigurationWorkingSetEditorButtonBar(Composite parent, int style,
          boolean useDefaultButton, ButtonBarActions actions) {
    super(parent, style);

    this.useDefaultButton = useDefaultButton;

    setLayout(new FormLayout());

    Button mAddButton = createButton(this, Messages.CheckstylePreferencePage_btnNew,
            actions.addCheckConfig);
    mAddButton.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(0);
    }));

    mEditButton = createButton(this, Messages.CheckstylePreferencePage_btnProperties,
            actions.editCheckConfig);
    mEditButton.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(mAddButton, 3, SWT.BOTTOM);
    }));

    mConfigureButton = createButton(this, Messages.CheckstylePreferencePage_btnConfigure,
            actions.configureCheckConfig);
    mConfigureButton.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(mEditButton, 3, SWT.BOTTOM);
    }));

    mCopyButton = createButton(this, Messages.CheckstylePreferencePage_btnCopy,
            actions.copyCheckConfig);
    mCopyButton.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(mConfigureButton, 3, SWT.BOTTOM);
    }));

    mRemoveButton = createButton(this, Messages.CheckstylePreferencePage_btnRemove,
            actions.removeCheckConfig);
    mRemoveButton.setLayoutData(formData(formData -> {
      formData.top = new FormAttachment(mCopyButton, 3, SWT.BOTTOM);
    }));

    if (useDefaultButton) {
      mDefaultButton = createButton(this, Messages.CheckstylePreferencePage_btnDefault,
              actions.setDefaultCheckConfig());
      mDefaultButton.setToolTipText(Messages.CheckstylePreferencePage_txtDefault);
      mDefaultButton.setLayoutData(formData(formData -> {
        formData.top = new FormAttachment(mRemoveButton, 3, SWT.BOTTOM);
      }));
    } else {
      mDefaultButton = null;
    }

    mExportButton = createButton(this, Messages.CheckstylePreferencePage_btnExport,
            actions.exportCheckstyleCheckConfig);
    mExportButton.setLayoutData(formData(formData -> {
      formData.bottom = new FormAttachment(100);
    }));
  }

  private static FormData formData(Consumer<FormData> custom) {
    FormData formData = new FormData();
    formData.left = new FormAttachment(0);
    formData.right = new FormAttachment(100);
    custom.accept(formData);
    return formData;
  }

  private Button createButton(Composite parent, String text, Runnable action) {
    Button button = new Button(parent, SWT.PUSH);
    button.setText(text);
    button.addSelectionListener(SelectionListener.widgetSelectedAdapter(
            event -> action.run()));
    return button;
  }

  public void setSelectionState(boolean configSelected, boolean configEditable,
          boolean configDefault) {
    mEditButton.setEnabled(configSelected);
    mConfigureButton.setEnabled(configSelected);
    mCopyButton.setEnabled(configSelected);
    mExportButton.setEnabled(configSelected);
    mRemoveButton.setEnabled(configSelected && configEditable);
    if (useDefaultButton) {
      mDefaultButton.setEnabled(configSelected && !configDefault);
    }
  }

  public record ButtonBarActions(Runnable addCheckConfig, Runnable editCheckConfig,
          Runnable configureCheckConfig, Runnable copyCheckConfig,
          Runnable removeCheckConfig, Runnable setDefaultCheckConfig,
          Runnable exportCheckstyleCheckConfig) {

  }

}
