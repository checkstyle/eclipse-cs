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

import java.util.function.BiConsumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;
import net.sf.eclipsecs.ui.config.configtypes.ICheckConfigurationEditor;

public final class CheckConfigurationPropertiesDialogView extends Composite {

  private final ComboViewer mConfigType;
  private final Composite mEditorPlaceHolder;

  public CheckConfigurationPropertiesDialogView(Composite parent, int style,
          BiConsumer<IConfigurationType, Boolean> changeSelectedConfigurationType) {
    super(parent, style);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

    Label lblConfigType = new Label(this, SWT.NULL);
    lblConfigType.setText(Messages.CheckConfigurationPropertiesDialog_lblConfigType);

    // this is a weird hack to find the longest label
    // this is done to have a nice ordered appearance of the this label
    // and the labels below
    // this is very difficult to do, because they belong to different
    // layouts
    GC graphics = new GC(lblConfigType);
    int nameSize = graphics.textExtent(Messages.CheckConfigurationPropertiesDialog_lblName).x;
    int locationsSize = graphics.textExtent(Messages.CheckConfigurationPropertiesDialog_lblLocation).x;
    int max = Math.max(nameSize, locationsSize);
    graphics.dispose();

    GridDataFactory.swtDefaults().hint(max, SWT.DEFAULT).applyTo(lblConfigType);

    mConfigType = new ComboViewer(this);
    GridDataFactory.swtDefaults().applyTo(mConfigType.getCombo());
    mConfigType.setContentProvider(ArrayContentProvider.getInstance());
    mConfigType.setLabelProvider(LabelProvider.createTextImageProvider(
            element -> ((IConfigurationType) element).getName(), element -> ConfigurationTypesUI
                    .getConfigurationTypeImage((IConfigurationType) element)));
    mConfigType.addSelectionChangedListener(event -> changeSelectedConfigurationType.accept(
            (IConfigurationType) event.getStructuredSelection().getFirstElement(),
            mConfigType.getCombo().getEnabled()));

    mEditorPlaceHolder = new Composite(this, SWT.NULL);
    GridLayoutFactory.swtDefaults().equalWidth(true).margins(0, 0).applyTo(mEditorPlaceHolder);
    GridDataFactory.create(GridData.FILL_HORIZONTAL).span(2, 1).applyTo(mEditorPlaceHolder);
  }

  public void initConfigType(IConfigurationType[] types) {
    mConfigType.setInput(types);
    mConfigType.setSelection(new StructuredSelection(types[0]), true);
  }

  public void disable() {
    mConfigType.getCombo().setEnabled(false);
  }

  public void bindEditor(ICheckConfigurationEditor mConfigurationEditor) {
    // remove old editor
    Control[] controls = mEditorPlaceHolder.getChildren();
    for (int i = 0; i < controls.length; i++) {
      controls[i].dispose();
    }

    mConfigurationEditor.createEditorControl(mEditorPlaceHolder, getShell());

    mEditorPlaceHolder.redraw();
    mEditorPlaceHolder.update();
    mEditorPlaceHolder.layout();
  }
}
