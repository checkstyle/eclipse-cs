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

package net.sf.eclipsecs.core.config.meta;

import java.util.List;

/**
 * Interface for an option provider. This is used to provide dynamic or massive amount of options
 * which would be too difficult to handle in metadata. For instance this is true for module metadata
 * that need all token types as options.
 * 
 * @author Lars Ködderitzsch
 */
public interface IOptionProvider {

  /**
   * Returns a list of options.
   * 
   * @return the options
   */
  List<String> getOptions();
}