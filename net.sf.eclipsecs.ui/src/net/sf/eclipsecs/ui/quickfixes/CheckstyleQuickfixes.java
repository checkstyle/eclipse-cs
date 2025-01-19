//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.quickfixes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import net.sf.eclipsecs.core.util.CheckstyleLog;

/**
 * List of all registered quickfixes.
 *
 * @implNote Cached over the runtime of the application. Will not react to plugins loaded
 *           dynamically.
 */
public final class CheckstyleQuickfixes {

  /**
   * ID of the quickfix extension point
   */
  private static final String QUICKFIX_EXTENSION_POINT = "net.sf.eclipsecs.ui.quickfix";
  /**
   * attribute under which the fully qualified class name of the quick fix is registered
   */
  private static final String EXTENSION_CLASS_ATTRIBUTE = "class";
  /**
   * attribute under which the module id is registered
   */
  private static final String EXTENSION_MODULE_ATTRIBUTE = "module";

  private final Collection<ICheckstyleMarkerResolution> quickfixes;

  private CheckstyleQuickfixes() {
    quickfixes = readRegistry();
  }

  public static CheckstyleQuickfixes getInstance() {
    return LazyHolder.INSTANCE;
  }

  /**
   * @return the quickfixes
   */
  public Collection<ICheckstyleMarkerResolution> getQuickfixes() {
    return quickfixes;
  }

  /**
   * @return all registered quickfixes
   */
  private Collection<ICheckstyleMarkerResolution> readRegistry() {
    List<ICheckstyleMarkerResolution> result = new ArrayList<>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] elements = registry.getConfigurationElementsFor(QUICKFIX_EXTENSION_POINT);
    for (IConfigurationElement element : elements) {
      var module = element.getAttribute(EXTENSION_MODULE_ATTRIBUTE);
      if (StringUtils.isNotBlank(module)) {
        var resolution = toClass(element);
        if (resolution != null) {
          resolution.setModule(module);
          result.add(resolution);
        }
      }
    }

    return result;
  }

  private ICheckstyleMarkerResolution toClass(IConfigurationElement element) {
    try {
      var resolution = element.createExecutableExtension(EXTENSION_CLASS_ATTRIBUTE);
      if (resolution instanceof ICheckstyleMarkerResolution) {
        return (ICheckstyleMarkerResolution) resolution;
      }
    } catch (CoreException ex) {
      CheckstyleLog.log(ex, "cannot create quickfix for " + element.getAttribute(EXTENSION_CLASS_ATTRIBUTE));
    }
    return null;
  }

  /**
   * initialization-on-demand-holder
   */
  private static class LazyHolder {
    static final CheckstyleQuickfixes INSTANCE = new CheckstyleQuickfixes();
  }

}
