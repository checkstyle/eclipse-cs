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

package net.sf.eclipsecs.core.config.configtypes;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Property resolver that resolves properties from a resource bundle.
 *
 * @author Lars Ködderitzsch
 */
class ResourceBundlePropertyResolver implements PropertyResolver {

  /** the resource bundle. */
  private ResourceBundle mBundle;

  /**
   * Creates the property resolver.
   *
   * @param bundle
   *          the resource bundle
   */
  public ResourceBundlePropertyResolver(ResourceBundle bundle) {
    mBundle = bundle;
  }

  @Override
  public String resolve(String property) {

    String value = null;

    if (mBundle != null) {
      try {
        value = mBundle.getString(property);
      } catch (MissingResourceException e) {
        // ignore
      }
    }

    return value;
  }
}
