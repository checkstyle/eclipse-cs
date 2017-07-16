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

package net.sf.eclipsecs.ui.config.widgets;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.CheckstyleUIPluginImages;
import net.sf.eclipsecs.ui.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Base class for all configuration property input widget classes.
 */
public abstract class ConfigPropertyWidgetAbstractBase implements IConfigPropertyWidget {

  private ConfigProperty mProp;

  private Control mValueWidget;

  private Composite mParent;

  protected ConfigPropertyWidgetAbstractBase(Composite parent, ConfigProperty prop) {
    mParent = parent;
    mProp = prop;
  }

  /**
   * @see net.sf.eclipsecs.ui.config.widgets.IConfigPropertyWidget#initialize()
   */
  @Override
  public void initialize() {

    //
    // Add the property's name.
    //
    Label label = new Label(mParent, SWT.NULL);
    label.setText(mProp.getName() + ":"); //$NON-NLS-1$
    GridData gd = new GridData();
    gd.verticalAlignment = SWT.BEGINNING;
    label.setLayoutData(gd);

    mValueWidget = getValueWidget(mParent);
    gd = (GridData) mValueWidget.getLayoutData();
    mValueWidget.setToolTipText(mProp.getMetaData().getDescription());

    // provide a label that shows a tooltip with the property description
    Label lblPropertyInfo = new Label(mParent, SWT.NULL);
    gd = new GridData();
    gd.verticalAlignment = SWT.BEGINNING;
    lblPropertyInfo.setLayoutData(gd);
    lblPropertyInfo.setImage(CheckstyleUIPluginImages.getImage(CheckstyleUIPluginImages.HELP_ICON));
    lblPropertyInfo.setToolTipText(mProp.getMetaData().getDescription());
    SWTUtil.addTooltipOnPressSupport(lblPropertyInfo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEnabled(boolean enabled) {
    mValueWidget.setEnabled(enabled);
  }

  /**
   * Returns the widget containing the values.
   * 
   * @return the widget containing the value
   */
  protected abstract Control getValueWidget(Composite parent);

  /**
   * @return The property's value.
   */
  @Override
  public abstract String getValue();

  protected String getInitValue() {
    //
    // Figure out an initial value for the property. This will be,
    // in order of precidents:
    //
    // 1) the existing value
    // 2) a default value overriding the checkstyle default
    // 3) the checkstyle default value, if specified
    // 4) blank
    //
    String initValue = null;
    if (mProp != null) {
      initValue = mProp.getValue();
    }
    if (initValue == null) {
      initValue = mProp.getMetaData().getOverrideDefault();
    }
    if (initValue == null) {
      initValue = mProp.getMetaData().getDefaultValue();
    }
    if (initValue == null) {
      initValue = ""; //$NON-NLS-1$
    }

    return initValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConfigProperty getConfigProperty() {
    return mProp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate() throws CheckstylePluginException {
    // Nothing to to for most properties
  }
}
