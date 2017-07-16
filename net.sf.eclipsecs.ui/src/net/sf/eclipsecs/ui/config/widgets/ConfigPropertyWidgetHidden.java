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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Non-configuration property.
 */
public class ConfigPropertyWidgetHidden extends ConfigPropertyWidgetAbstractBase {

  private String mValue = ""; //$NON-NLS-1$

  /**
   * Creates the widget.
   *
   * @param parent
   *          the parent composite
   * @param prop
   *          the property
   */
  public ConfigPropertyWidgetHidden(Composite parent, ConfigProperty prop) {
    super(parent, prop);
  }

  @Override
  protected Control getValueWidget(Composite parent) {
    return null;
  }

  @Override
  public String getValue() {
    return mValue;
  }

  @Override
  public void restorePropertyDefault() {
    // NOOP
  }
}
