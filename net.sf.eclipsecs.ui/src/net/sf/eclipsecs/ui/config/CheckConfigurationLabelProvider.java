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

package net.sf.eclipsecs.ui.config;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;

/**
 * Provides the labels for the audit configuration list display.
 */
public class CheckConfigurationLabelProvider extends LabelProvider {

  @Override
  public String getText(Object element) {
    if (element instanceof ICheckConfiguration checkConfig) {
      return checkConfig.getName() + " " //$NON-NLS-1$
              + (checkConfig.isGlobal() ? Messages.CheckConfigurationLabelProvider_suffixGlobal
                      : Messages.CheckConfigurationLabelProvider_suffixLocal);
    }

    return super.getText(element);
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof ICheckConfiguration checkConfig) {
      return ConfigurationTypesUI.getConfigurationTypeImage(checkConfig.getType());
    }
    return null;
  }

}
