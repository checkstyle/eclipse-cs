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

package net.sf.eclipsecs.ui.config.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.util.SWTUtil;

/**
 * Base class for all configuration property input widget classes.
 */
public abstract class AbstractConfigPropertyWidget implements IConfigPropertyWidget {

    /** The config property. */
    private ConfigProperty prop;

    /** The value widget control. */
    private Control valueWidget;

    /** The parent composite. */
    private Composite parent;

    /**
     * Creates the widget for the given property.
     *
     * @param parent
     *            the parent composite
     * @param prop
     *            the config property
     */
    protected AbstractConfigPropertyWidget(Composite parent, ConfigProperty prop) {
        this.parent = parent;
        this.prop = prop;
    }

    /**
     * Returns the widget containing the values.
     *
     * @param parentComposite
     *            the parent composite
     * @return the widget containing the value
     */
    protected abstract Control getValueWidget(Composite parentComposite);

    /**
     * Gets the property's value.
     *
     * @return The property's value.
     */
    @Override
    public abstract String getValue();

    @Override
    public void initialize() {

        //
        // Add the property's name.
        //
        final Label label = new Label(parent, SWT.NULL);
        label.setText(prop.getName() + ":");
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.BEGINNING;
        label.setLayoutData(gridData);

        valueWidget = getValueWidget(parent);
        gridData = (GridData) valueWidget.getLayoutData();
        valueWidget.setToolTipText(prop.getMetaData().getDescription());

        // provide a label that shows a tooltip with the property description
        final Label lblPropertyInfo = new Label(parent, SWT.NULL);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.BEGINNING;
        lblPropertyInfo.setLayoutData(gridData);
        lblPropertyInfo.setImage(CheckstyleUIPluginImages.HELP_ICON.getImage());
        lblPropertyInfo.setToolTipText(prop.getMetaData().getDescription());
        SWTUtil.addTooltipOnPressSupport(lblPropertyInfo);
    }

    @Override
    public void setEnabled(boolean enabled) {
        valueWidget.setEnabled(enabled);
    }

    /**
     * Figure out an initial value for the property. This will be, in order of precedence: 1) the
     * existing value 2) a default value overriding the checkstyle default 3) the checkstyle default
     * value, if specified 4) blank
     *
     * @return the initial value
     */
    protected String getInitValue() {
        String initValue = null;
        if (prop != null) {
            initValue = prop.getValue();
        }
        if (initValue == null) {
            initValue = prop.getMetaData().getOverrideDefault();
        }
        if (initValue == null) {
            initValue = prop.getMetaData().getDefaultValue();
        }
        if (initValue == null) {
            initValue = "";
        }

        return initValue;
    }

    @Override
    public ConfigProperty getConfigProperty() {
        return prop;
    }

    @Override
    public void validate() throws CheckstylePluginException {
        // Nothing to to for most properties
    }

}
