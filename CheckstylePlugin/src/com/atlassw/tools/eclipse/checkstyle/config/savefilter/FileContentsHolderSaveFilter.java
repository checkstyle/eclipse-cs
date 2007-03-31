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

package com.atlassw.tools.eclipse.checkstyle.config.savefilter;

import java.util.List;

import com.atlassw.tools.eclipse.checkstyle.config.Module;
import com.atlassw.tools.eclipse.checkstyle.config.XMLTags;
import com.atlassw.tools.eclipse.checkstyle.config.meta.MetadataFactory;

/**
 * Special module logic for the FileContentsHolder module.
 * 
 * @author Lars Ködderitzsch
 */
public class FileContentsHolderSaveFilter implements ISaveFilter
{

    /**
     * {@inheritDoc}
     */
    public void postProcessConfiguredModules(List configuredModules)
    {
        // the FileContentsHolder module is needed if it is not configured and
        // the SuppressionCommentFilter module is configured
        boolean containsFileContentsHolderModule = false;
        boolean containsSuppressionCommentFilterModule = false;
        Module configuredFileContentsHolder = null;

        for (int i = 0, size = configuredModules.size(); i < size; i++)
        {

            Module module = (Module) configuredModules.get(i);

            if (XMLTags.FILECONTENTSHOLDER_MODULE.equals(module.getMetaData().getInternalName()))
            {
                containsFileContentsHolderModule = true;
                configuredFileContentsHolder = module;
            }

            if (XMLTags.SUPRESSIONCOMMENTFILTER_MODULE.equals(module.getMetaData()
                    .getInternalName()))
            {
                containsSuppressionCommentFilterModule = true;
            }

            if (containsFileContentsHolderModule && containsSuppressionCommentFilterModule)
            {
                break;
            }
        }

        // add the TreeWalker if needed
        if (!containsFileContentsHolderModule && containsSuppressionCommentFilterModule)
        {
            Module fileContentsHolder = new Module(MetadataFactory
                    .getRuleMetadata(XMLTags.FILECONTENTSHOLDER_MODULE), false);
            configuredModules.add(0, fileContentsHolder);
        }

        // remove the TreeWalker if not needed
        else if (containsFileContentsHolderModule && !containsSuppressionCommentFilterModule)
        {
            configuredModules.remove(configuredFileContentsHolder);
        }
    }
}
