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

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.core.util.CheckstylePluginException;
import net.sf.eclipsecs.ui.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A string property configuration widget.
 */
public class ConfigPropertyWidgetInteger extends ConfigPropertyWidgetAbstractBase {

  private Text mTextWidget;

  /**
   * Creates the widget.
   *
   * @param parent
   *          the parent composite
   * @param prop
   *          the property
   */
  public ConfigPropertyWidgetInteger(Composite parent, ConfigProperty prop) {
    super(parent, prop);
  }

  @Override
  protected Control getValueWidget(Composite parent) {

    if (mTextWidget == null) {

      //
      // Create a text entry field.
      //
      mTextWidget = new Text(parent, SWT.SINGLE | SWT.BORDER);
      mTextWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      SWTUtil.addOnlyDigitInputSupport(mTextWidget);

      String initValue = getInitValue();
      if (initValue != null) {
        mTextWidget.setText(initValue);
      }
    }

    return mTextWidget;
  }

  @Override
  public String getValue() {
    String result = mTextWidget.getText();
    if (result == null) {
      result = new String();
    }
    return result;
  }

  @Override
  public void restorePropertyDefault() {
    ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
    String defaultValue = metadata.getOverrideDefault() != null ? metadata.getOverrideDefault()
            : metadata.getDefaultValue();
    mTextWidget.setText(defaultValue != null ? defaultValue : new String());
  }

  @Override
  public void validate() throws CheckstylePluginException {
    try {
      //
      // Parse the value to see if an exception gets thrown.
      //
      Integer.parseInt(mTextWidget.getText());
    } catch (NumberFormatException ex) {
      CheckstylePluginException.rethrow(ex, ex.getLocalizedMessage());
    }
  }
}
