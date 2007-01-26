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

package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleMarker;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;
import com.atlassw.tools.eclipse.checkstyle.config.meta.RuleMetadata;

/**
 * Profides marker resolutions (quickfixes) for Checkstyle markers.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckstyleMarkerResolutionGenerator implements IMarkerResolutionGenerator2
{

    /**
     * {@inheritDoc}
     */
    public IMarkerResolution[] getResolutions(IMarker marker)
    {

        Collection fixes = new ArrayList();

        // get all fixes that apply to this marker instance
        String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);

        RuleMetadata metadata = MetadataFactory.getRuleMetadata(moduleName);
        Collection quickfixes = metadata.getQuickfixes();

        Iterator it = quickfixes.iterator();
        while (it.hasNext())
        {

            ICheckstyleMarkerResolution fix = (ICheckstyleMarkerResolution) it.next();

            if (fix.canFix(marker))
            {
                fixes.add(fix);
            }
        }

        return (ICheckstyleMarkerResolution[]) fixes.toArray(new ICheckstyleMarkerResolution[fixes
                .size()]);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasResolutions(IMarker marker)
    {

        boolean hasAtLeastOneFix = false;

        // check if there is at least one fix that really applies to the module
        String moduleName = marker.getAttribute(CheckstyleMarker.MODULE_NAME, null);

        RuleMetadata metadata = MetadataFactory.getRuleMetadata(moduleName);

        if (metadata != null)
        {

            Collection quickfixes = metadata.getQuickfixes();

            Iterator it = quickfixes.iterator();
            while (it.hasNext())
            {

                ICheckstyleMarkerResolution fix = (ICheckstyleMarkerResolution) it.next();

                if (fix.canFix(marker))
                {
                    hasAtLeastOneFix = true;
                    break;
                }
            }
        }
        return hasAtLeastOneFix;
    }
}
