package net.sf.eclipsecs.checkstyle.utils;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.utils.ModuleReflectionUtil;

import java.util.HashSet;
import java.util.Set;

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
