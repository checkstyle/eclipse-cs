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

package net.sf.eclipsecs.ui.config;

import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.ui.Messages;
import net.sf.eclipsecs.ui.config.configtypes.ConfigurationTypesUI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provides the labels for the audit configuration list display.
 */
public class CheckConfigurationLabelProvider extends LabelProvider {

  /**
   * {@inheritDoc}
   */
  @Override
  public String getText(Object element) {
    String text = super.getText(element);
    if (element instanceof ICheckConfiguration) {
      ICheckConfiguration checkConfig = (ICheckConfiguration) element;

      text = checkConfig.getName() + " " //$NON-NLS-1$
              + (checkConfig.isGlobal() ? Messages.CheckConfigurationLabelProvider_suffixGlobal
                      : Messages.CheckConfigurationLabelProvider_suffixLocal);
    }

    return text;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Image getImage(Object element) {
    Image image = null;
    if (element instanceof ICheckConfiguration) {
      image = ConfigurationTypesUI
              .getConfigurationTypeImage(((ICheckConfiguration) element).getType());
    }

    return image;
  }

}
