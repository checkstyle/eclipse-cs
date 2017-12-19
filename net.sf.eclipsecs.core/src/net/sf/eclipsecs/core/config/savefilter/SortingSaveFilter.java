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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.eclipsecs.core.config.Module;
import net.sf.eclipsecs.core.config.XMLTags;

/**
 * Save filter that sorts modules in a certain order.
 *
 * @author Lars Ködderitzsch
 */
public class SortingSaveFilter implements ISaveFilter {

  /**
   * {@inheritDoc}
   */
  @Override
  public void postProcessConfiguredModules(List<Module> configuredModules) {
    // Sort modules because of
    // Checkstyle bug #1183749
    Collections.sort(configuredModules, new ModuleComparator());
  }

  /**
   * Comparator to sort modules so that Checker and TreeWalker come first. This is done because of a
   * bug in SuppressionCommentFilter.
   *
   * @author Lars Ködderitzsch
   */
  private static class ModuleComparator implements Comparator<Module> {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Module o1, Module o2) {

      String internalName1 = o1.getMetaData().getInternalName();
      String internalName2 = o2.getMetaData().getInternalName();

      if (XMLTags.CHECKER_MODULE.equals(internalName1)
              || XMLTags.TREEWALKER_MODULE.equals(internalName1)) {
        return -1;
      } else if (XMLTags.CHECKER_MODULE.equals(internalName2)
              || XMLTags.TREEWALKER_MODULE.equals(internalName2)) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
