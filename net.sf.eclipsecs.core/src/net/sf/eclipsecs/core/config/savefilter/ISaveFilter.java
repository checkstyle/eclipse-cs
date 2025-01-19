//============================================================================
//
// Copyright (C) 2003-2023  David Schneider, Lars Ködderitzsch
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
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//
//============================================================================

package net.sf.eclipsecs.core.config.savefilter;

import java.util.List;

import net.sf.eclipsecs.core.config.Module;

/**
 * Interface to implement special module logic.
 *
 */
public interface ISaveFilter {

  /**
   * Post processes the selection of configurad modules. This can be used to add/remove certain
   * 'special' modules. Implementations are encouraged to manipulate the list of configured modules.
   *
   * @param configuredModules
   *          the configured modules in from this configuration
   */
  void postProcessConfiguredModules(List<Module> configuredModules);
}
