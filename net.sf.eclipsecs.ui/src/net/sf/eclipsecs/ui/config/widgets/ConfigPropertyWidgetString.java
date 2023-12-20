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
import org.eclipse.swt.widgets.Text;

import net.sf.eclipsecs.core.config.ConfigProperty;
import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;

/**
 * A string property configuration widget.
 */
public class ConfigPropertyWidgetString extends ConfigPropertyWidgetAbstractBase {

  private static final String APOSTROPHE_PLAIN = "'";
  private static final String APOSTROPHE_ESCAPED = "''";
  private Text mTextWidget;

  /**
   * Creates the widget.
   *
   * @param parent
   *          the parent composite
   * @param prop
   *          the property
   */
  public ConfigPropertyWidgetString(Composite parent, ConfigProperty prop) {
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

      String initValue = getInitValue();
      if (initValue != null) {
        mTextWidget.setText(unescape(initValue));
      }
    }

    return mTextWidget;
  }

  @Override
  public String getValue() {
    String result = mTextWidget.getText();
    if (result == null) {
      result = ""; //$NON-NLS-1$
    }
    return escape(result);
  }

  private String unescape(String text) {
    // custom messages use MessageFormat, single quote is escaped as 2 single quotes there
    if (XMLTags.MESSAGE_TAG.equals(getConfigProperty().getName())) {
      return text.replace(APOSTROPHE_ESCAPED, APOSTROPHE_PLAIN);
    }
    return text;
  }

  private String escape(String text) {
    if (XMLTags.MESSAGE_TAG.equals(getConfigProperty().getName())) {
      return text.replace(APOSTROPHE_PLAIN, APOSTROPHE_ESCAPED);
    }
    return text;
  }

  @Override
  public void restorePropertyDefault() {
    ConfigPropertyMetadata metadata = getConfigProperty().getMetaData();
    String defaultValue = metadata.getOverrideDefault() != null ? metadata.getOverrideDefault()
            : metadata.getDefaultValue();
    mTextWidget.setText(defaultValue != null ? defaultValue : ""); //$NON-NLS-1$
  }
}
