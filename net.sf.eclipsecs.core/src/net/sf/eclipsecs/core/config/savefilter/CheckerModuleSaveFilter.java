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
 * Special module logic for the Checker module.
 * 
 * @author Lars Ködderitzsch
 */
public class CheckerModuleSaveFilter implements ISaveFilter {

  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessConfiguredModules(List<Module> configuredModules) {

    boolean containsCheckerModule = false;

    for (int i = 0, size = configuredModules.size(); i < size; i++) {

      Module module = configuredModules.get(i);

      if (XMLTags.CHECKER_MODULE.equals(module.getMetaData().getInternalName())) {

        containsCheckerModule = true;
        break;
      }
    }

    // add checker module if it is not contained in the configured modules
    if (!containsCheckerModule) {
      Module checker = new Module(MetadataFactory.getRuleMetadata(XMLTags.CHECKER_MODULE), false);
      configuredModules.add(0, checker);
    }
  }
}