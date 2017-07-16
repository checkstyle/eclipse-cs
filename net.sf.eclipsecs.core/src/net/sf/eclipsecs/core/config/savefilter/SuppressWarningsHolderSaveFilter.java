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
 * Special module logic for the SuppressWarningsHolder module.
 *
 * @author Lars Ködderitzsch
 */
public class SuppressWarningsHolderSaveFilter implements ISaveFilter {

  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessConfiguredModules(List<Module> configuredModules) {

    // the SuppressWarningsHolder module is needed if it is not configured and
    // the SuppressWarningsFilter module is configured
    boolean containsSuppressWarningsHolderModule = false;
    boolean needsSuppressWarningsHolderModule = false;
    Module configuredSuppressWarningsHolder = null;

    for (int i = 0, size = configuredModules.size(); i < size; i++) {

      Module module = configuredModules.get(i);
      String internalName = module.getMetaData().getInternalName();

      if (XMLTags.SUPPRESSWARNINGSHOLDER_MODULE.equals(internalName)) {
        containsSuppressWarningsHolderModule = true;
        configuredSuppressWarningsHolder = module;
      } else if (XMLTags.SUPPRESSWARNINGSFILTER_MODULE.equals(internalName)) {
        needsSuppressWarningsHolderModule = true;
      }

      if (containsSuppressWarningsHolderModule && needsSuppressWarningsHolderModule) {
        break;
      }
    }

    // add the SuppressWarningsHolder if needed
    if (!containsSuppressWarningsHolderModule && needsSuppressWarningsHolderModule) {
      Module fileContentsHolder = new Module(
              MetadataFactory.getRuleMetadata(XMLTags.SUPPRESSWARNINGSHOLDER_MODULE), false);
      configuredModules.add(0, fileContentsHolder);
    } else if (containsSuppressWarningsHolderModule && !needsSuppressWarningsHolderModule) {
      // remove the SuppressWarningsHolder if not needed
      configuredModules.remove(configuredSuppressWarningsHolder);
    }
  }
}
