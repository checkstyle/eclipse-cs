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

package net.sf.eclipsecs.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.eclipsecs.core.config.CheckConfigurationFactory;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.projectconfig.ProjectConfigurationWorkingCopy;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider implementation that provides check configurations.
 * 
 * @author Lars Ködderitzsch
 */
class CheckConfigurationContentProvider implements IStructuredContentProvider {

  //
  // methods
  //

  /**
   * {@inheritDoc}
   */
  @Override
  public Object[] getElements(Object inputElement) {

    List<ICheckConfiguration> configurations = new ArrayList<>();

    if (inputElement != null && inputElement instanceof ProjectConfigurationWorkingCopy) {
      ICheckConfiguration[] localConfigs = ((ProjectConfigurationWorkingCopy) inputElement)
              .getLocalCheckConfigWorkingSet().getWorkingCopies();

      ICheckConfiguration[] globalConfigs = ((ProjectConfigurationWorkingCopy) inputElement)
              .getGlobalCheckConfigWorkingSet().getWorkingCopies();

      configurations.addAll(Arrays.asList(localConfigs));
      configurations.addAll(Arrays.asList(globalConfigs));
    } else {
      configurations.addAll(CheckConfigurationFactory.getCheckConfigurations());
    }

    return configurations.toArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // do nothing.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    // do nothing.
  }
}
