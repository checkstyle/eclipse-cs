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

package net.sf.eclipsecs.core.config.savefilter;

import java.util.List;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.XMLTags;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;

/**
 * Special module logic for the FileContentsHolder module.
 * 
 * @author Lars Ködderitzsch
 */
public class FileContentsHolderSaveFilter implements ISaveFilter {

    /**
     * {@inheritDoc}
     */
    public void postProcessConfiguredModules(List<Module> configuredModules) {

        // the FileContentsHolder module is needed if it is not configured and
        // the SuppressionCommentFilter module is configured
        boolean containsFileContentsHolderModule = false;
        boolean needsFileContentsHolderModule = false;
        Module configuredFileContentsHolder = null;

        for (int i = 0, size = configuredModules.size(); i < size; i++) {

            Module module = configuredModules.get(i);
            String internalName = module.getMetaData().getInternalName();

            if (XMLTags.FILECONTENTSHOLDER_MODULE.equals(internalName)) {
                containsFileContentsHolderModule = true;
                configuredFileContentsHolder = module;
            }
            else if (XMLTags.SUPRESSIONCOMMENTFILTER_MODULE.equals(internalName)) {
                needsFileContentsHolderModule = true;
            }
            else if (XMLTags.SUPRESSWITHNEARBYCOMMENTFILTER_MODULE.equals(internalName)) {
                needsFileContentsHolderModule = true;
            }

            if (containsFileContentsHolderModule && needsFileContentsHolderModule) {
                break;
            }
        }

        // add the FileContentsHolder if needed
        if (!containsFileContentsHolderModule && needsFileContentsHolderModule) {
            Module fileContentsHolder = new Module(MetadataFactory.getRuleMetadata(XMLTags.FILECONTENTSHOLDER_MODULE),
                false);
            configuredModules.add(0, fileContentsHolder);
        }

        // remove the FileContentsHolder if not needed
        else if (containsFileContentsHolderModule && !needsFileContentsHolderModule) {
            configuredModules.remove(configuredFileContentsHolder);
        }
    }
}
