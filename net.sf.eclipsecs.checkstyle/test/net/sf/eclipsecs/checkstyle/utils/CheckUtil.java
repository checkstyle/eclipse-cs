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

package net.sf.eclipsecs.checkstyle.utils;

import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.utils.ModuleReflectionUtil;

public final class CheckUtil {
  private CheckUtil() {
  }

  public static Set<Class<?>> getCheckstyleModules() throws Exception {
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return ModuleReflectionUtil.getCheckstyleModules(PackageNamesLoader.getPackageNames(loader),
            loader);
  }

  public static Set<String> getPackages(Set<Class<?>> modules) {
    final Set<String> result = new HashSet<>();

    for (Class<?> module : modules) {
      result.add(module.getPackage().getName());
    }

    return result;
  }

  public static Set<Class<?>> getModulesInPackage(Set<Class<?>> modules, String packge) {
    final Set<Class<?>> result = new HashSet<>();

    for (Class<?> module : modules) {
      if (module.getPackage().getName().endsWith(packge)) {
        result.add(module);
      }
    }

    return result;
  }
}
