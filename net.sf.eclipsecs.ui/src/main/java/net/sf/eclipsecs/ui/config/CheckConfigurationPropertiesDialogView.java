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

/**
 * Composite building the upper part of the check configuration properties dialog, offering the
 * configuration type selector and the placeholder hosting the matching editor.
 *
 */
public final class CheckConfigurationPropertiesDialogView extends Composite {

    /** The combo viewer for selecting the configuration type. */
    private final ComboViewer mConfigType;
    /** The placeholder composite for the configuration editor. */
    private final Composite mEditorPlaceHolder;

    /**
     * Constructor building the configuration type selector and the placeholder for the matching
     * editor.
     *
     * @param parent
     *            the parent composite
     * @param style
     *            the SWT style bits to apply to this composite
     * @param changeSelectedConfigurationType
     *            the callback invoked when the selected configuration type changes
     */
    public CheckConfigurationPropertiesDialogView(Composite parent, int style,
        BiConsumer<IConfigurationType, Boolean> changeSelectedConfigurationType) {
        super(parent, style);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

        final Label lblConfigType = new Label(this, SWT.NULL);
        lblConfigType.setText(Messages.CheckConfigurationPropertiesDialog_lblConfigType);

        // this is a weird hack to find the longest label
        // this is done to have a nice ordered appearance of the this label
        // and the labels below
        // this is very difficult to do, because they belong to different
        // layouts
        final GC graphics = new GC(lblConfigType);
        final int nameSize =
            graphics.textExtent(Messages.CheckConfigurationPropertiesDialog_lblName).x;
        final int locationsSize =
            graphics.textExtent(Messages.CheckConfigurationPropertiesDialog_lblLocation).x;
        final int max = Math.max(nameSize, locationsSize);
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

    /**
     * Initializes the configuration type combo with the given types and selects the first one.
     *
     * @param types
     *            the configuration types to show
     */
    public void initConfigType(IConfigurationType[] types) {
        mConfigType.setInput(types);
        mConfigType.setSelection(new StructuredSelection(types[0]), true);
    }

    /**
     * Disables the configuration type combo box.
     */
    public void disable() {
        mConfigType.getCombo().setEnabled(false);
    }

    /**
     * Binds the given configuration editor into the editor placeholder, removing any previously
     * bound editor.
     *
     * @param mConfigurationEditor
     *            the configuration editor to bind
     */
    public void bindEditor(ICheckConfigurationEditor mConfigurationEditor) {
        // remove old editor
        final Control[] controls = mEditorPlaceHolder.getChildren();
        for (Control control : controls) {
            control.dispose();
        }

        mConfigurationEditor.createEditorControl(mEditorPlaceHolder, getShell());

        mEditorPlaceHolder.redraw();
        mEditorPlaceHolder.update();
        mEditorPlaceHolder.layout();
    }
}
