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

public class ConfiguredModulesButtons extends Composite {

  public ConfiguredModulesButtons(Composite parent, int style, boolean configurable,
          Runnable removeModule, Runnable editModule) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).margins(0, 0).applyTo(this);

    Button mRemoveButton = new Button(this, SWT.PUSH);
    mRemoveButton.setText(Messages.CheckConfigurationConfigureDialog_btnRemove);
    GridDataFactory.swtDefaults().applyTo(mRemoveButton);
    if (configurable) {
      mRemoveButton.addSelectionListener(
              SelectionListener.widgetSelectedAdapter(event -> removeModule.run()));
    }
    mRemoveButton.setEnabled(configurable);

    Button mEditButton = new Button(this, SWT.PUSH);
    mEditButton.setText(Messages.CheckConfigurationConfigureDialog_btnOpen);
    GridDataFactory.swtDefaults().applyTo(mEditButton);
    mEditButton.addSelectionListener(
            SelectionListener.widgetSelectedAdapter(event -> editModule.run()));
  }

}
