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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.sf.eclipsecs.ui.Messages;

public final class CheckConfigurationWorkingSetEditorButtonBar extends Composite {

    /** Whether the default button should be shown. */
    private final boolean useDefaultButton;
    /** The edit button. */
    private final Button mEditButton;
    /** The configure button. */
    private final Button mConfigureButton;
    /** The copy button. */
    private final Button mCopyButton;
    /** The remove button. */
    private final Button mRemoveButton;
    /** The default button. */
    private final Button mDefaultButton;
    /** The export button. */
    private final Button mExportButton;

    public CheckConfigurationWorkingSetEditorButtonBar(Composite parent, int style,
        boolean useDefaultButton, ButtonBarActions actions) {
        super(parent, style);

        this.useDefaultButton = useDefaultButton;

        GridLayoutFactory.fillDefaults().applyTo(this);

        final Button addButton =
            createButton(this, Messages.CheckstylePreferencePage_btnNew, actions.addCheckConfig);
        GridDataFactory.fillDefaults().applyTo(addButton);

        mEditButton = createButton(this, Messages.CheckstylePreferencePage_btnProperties,
            actions.editCheckConfig);
        GridDataFactory.fillDefaults().applyTo(mEditButton);

        mConfigureButton = createButton(this, Messages.CheckstylePreferencePage_btnConfigure,
            actions.configureCheckConfig);
        GridDataFactory.fillDefaults().applyTo(mConfigureButton);

        mCopyButton =
            createButton(this, Messages.CheckstylePreferencePage_btnCopy, actions.copyCheckConfig);
        GridDataFactory.fillDefaults().applyTo(mCopyButton);

        mRemoveButton = createButton(this, Messages.CheckstylePreferencePage_btnRemove,
            actions.removeCheckConfig);
        GridDataFactory.fillDefaults().applyTo(mRemoveButton);

        if (useDefaultButton) {
            mDefaultButton = createButton(this, Messages.CheckstylePreferencePage_btnDefault,
                actions.setDefaultCheckConfig());
            mDefaultButton.setToolTipText(Messages.CheckstylePreferencePage_txtDefault);
            GridDataFactory.fillDefaults().applyTo(mDefaultButton);
        } else {
            mDefaultButton = null;
        }

        mExportButton = createButton(this, Messages.CheckstylePreferencePage_btnExport,
            actions.exportCheckstyleCheckConfig);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.END)
            .applyTo(mExportButton);
    }

    private Button createButton(Composite parent, String text, Runnable action) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> action.run()));
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
        Runnable configureCheckConfig, Runnable copyCheckConfig, Runnable removeCheckConfig,
        Runnable setDefaultCheckConfig, Runnable exportCheckstyleCheckConfig) {

    }

}
