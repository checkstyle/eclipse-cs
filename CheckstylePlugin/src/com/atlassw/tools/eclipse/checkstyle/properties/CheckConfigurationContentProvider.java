//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.atlassw.tools.eclipse.checkstyle.config.CheckConfigurationFactory;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.projectconfig.ProjectConfigurationWorkingCopy;

/**
 * Content provider implementation that provides check configurations.
 * 
 * @author Lars Ködderitzsch
 */
class CheckConfigurationContentProvider implements IStructuredContentProvider
{

    //
    // methods
    //

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement)
    {

        List configurations = new ArrayList();

        if (inputElement != null && inputElement instanceof ProjectConfigurationWorkingCopy)
        {
            ICheckConfiguration[] localConfigs = ((ProjectConfigurationWorkingCopy) inputElement)
                    .getLocalCheckConfigWorkingSet().getWorkingCopies();

            ICheckConfiguration[] globalConfigs = ((ProjectConfigurationWorkingCopy) inputElement)
                    .getGlobalCheckConfigWorkingSet().getWorkingCopies();

            configurations.addAll(Arrays.asList(localConfigs));
            configurations.addAll(Arrays.asList(globalConfigs));
        }
        else
        {
            configurations.addAll(CheckConfigurationFactory.getCheckConfigurations());
        }

        return configurations.toArray();
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    // do nothing.
    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
    // do nothing.
    }
}
