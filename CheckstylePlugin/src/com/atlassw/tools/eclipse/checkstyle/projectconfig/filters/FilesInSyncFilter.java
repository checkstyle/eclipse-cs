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

package com.atlassw.tools.eclipse.checkstyle.projectconfig.filters;

import org.eclipse.core.resources.IFile;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;

/**
 * Filters all files that are in sync with the repository.
 * 
 * @author Lars Ködderitzsch
 */
public class FilesInSyncFilter extends AbstractFilter
{

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object element)
    {
        boolean passes = true;

        if (element instanceof IFile)
        {
            ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor((IFile) element);

            try
            {
                if (cvsResource.isIgnored()
                        || (cvsResource.isManaged() && !cvsResource.isModified(null)))
                {
                    passes = false;
                }
            }
            catch (CVSException e)
            {
                CheckstyleLog.log(e);
            }
        }
        return passes;
    }

}