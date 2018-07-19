package net.sf.eclipsecs.checkstyle.utils;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck;
import com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck;
import com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineJavaCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.ModuleReflectionUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;

public final class CheckUtil {
  private static final Set<String> CHECK_PROPERTIES = getProperties(AbstractCheck.class);
  private static final Set<String> JAVADOC_CHECK_PROPERTIES = getProperties(
          AbstractJavadocCheck.class);
  private static final Set<String> FILESET_PROPERTIES = getProperties(AbstractFileSetCheck.class);

  private static final List<String> UNDOCUMENTED_PROPERTIES = Arrays.asList("Checker.classLoader",
          "Checker.classloader", "Checker.moduleClassLoader", "Checker.moduleFactory",
          "TreeWalker.classLoader", "TreeWalker.moduleFactory", "TreeWalker.cacheFile",
          "TreeWalker.upChild", "SuppressWithNearbyCommentFilter.fileContents",
          "SuppressionCommentFilter.fileContents");

  private CheckUtil() {
  }

  public static String getSimpleCheckstyleModuleName(Class<?> module) {
    return module.getSimpleName().replaceAll("Check$", "");
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

  public static Set<String> getCheckProperties(Class<?> clss) throws Exception {
    final Set<String> properties = getProperties(clss);

    if (AbstractJavadocCheck.class.isAssignableFrom(clss)) {
      properties.removeAll(JAVADOC_CHECK_PROPERTIES);

      // override
      properties.add("violateExecutionOnNonTightHtml");
    } else if (AbstractCheck.class.isAssignableFrom(clss)) {
      properties.removeAll(CHECK_PROPERTIES);
    } else if (AbstractFileSetCheck.class.isAssignableFrom(clss)) {
      properties.removeAll(FILESET_PROPERTIES);

      // override
      properties.add("fileExtensions");
    } else if (Checker.class.equals(clss)) {
      properties.remove("severity");
    }

    // remove undocumented properties
    for (String prop : new HashSet<>(properties)) {
      if (UNDOCUMENTED_PROPERTIES.contains(clss.getSimpleName() + "." + prop)) {
        properties.remove(prop);
      }
    }

    if (AbstractCheck.class.isAssignableFrom(clss)) {
      final Object instance = clss.newInstance();
      final AbstractCheck check = (AbstractCheck) instance;

      final int[] acceptableTokens = check.getAcceptableTokens();
      Arrays.sort(acceptableTokens);
      final int[] defaultTokens = check.getDefaultTokens();
      Arrays.sort(defaultTokens);
      final int[] requiredTokens = check.getRequiredTokens();
      Arrays.sort(requiredTokens);

      if (!Arrays.equals(acceptableTokens, defaultTokens)
              || !Arrays.equals(acceptableTokens, requiredTokens)) {
        properties.add("tokens");
      }
    }

    if (AbstractJavadocCheck.class.isAssignableFrom(clss)) {
      final Object instance = clss.newInstance();
      final AbstractJavadocCheck check = (AbstractJavadocCheck) instance;

      final int[] acceptableJavadocTokens = check.getAcceptableJavadocTokens();
      Arrays.sort(acceptableJavadocTokens);
      final int[] defaultJavadocTokens = check.getDefaultJavadocTokens();
      Arrays.sort(defaultJavadocTokens);
      final int[] requiredJavadocTokens = check.getRequiredJavadocTokens();
      Arrays.sort(requiredJavadocTokens);

      if (!Arrays.equals(acceptableJavadocTokens, defaultJavadocTokens)
              || !Arrays.equals(acceptableJavadocTokens, requiredJavadocTokens)) {
        properties.add("javadocTokens");
      }
    }

    return properties;
  }

  private static Set<String> getProperties(Class<?> clss) {
    final Set<String> result = new TreeSet<>();
    final PropertyDescriptor[] map = PropertyUtils.getPropertyDescriptors(clss);

    for (PropertyDescriptor p : map) {
      if (p.getWriteMethod() != null) {
        result.add(p.getName());
      }
    }

    return result;
  }

  /**
   * Get's the check's messages.
   *
   * @param module
   *          class to examine.
   * @return a set of checkstyle's module message fields.
   * @throws ClassNotFoundException
   *           if the attempt to read a protected class fails.
   */
  public static Set<Field> getCheckMessages(Class<?> module) throws ClassNotFoundException {
    final Set<Field> checkstyleMessages = new HashSet<>();

    // get all fields from current class
    final Field[] fields = module.getDeclaredFields();

    for (Field field : fields) {
      if (field.getName().startsWith("MSG_")) {
        checkstyleMessages.add(field);
      }
    }

    // deep scan class through hierarchy
    final Class<?> superModule = module.getSuperclass();

    if (superModule != null) {
      checkstyleMessages.addAll(getCheckMessages(superModule));
    }

    // special cases that require additional classes
    if (module == RegexpMultilineCheck.class) {
      checkstyleMessages.addAll(getCheckMessages(
              Class.forName("com.puppycrawl.tools.checkstyle.checks.regexp.MultilineDetector")));
    } else if (module == RegexpSinglelineCheck.class || module == RegexpSinglelineJavaCheck.class) {
      checkstyleMessages.addAll(getCheckMessages(
              Class.forName("com.puppycrawl.tools.checkstyle.checks.regexp.SinglelineDetector")));
    }

    return checkstyleMessages;
  }

  public static String getTokenText(int[] tokens, int... subtractions) {
    String tokenText = null;
    if (subtractions.length == 0 && Arrays.equals(tokens, TokenUtil.getAllTokenIds())) {
      tokenText = "TokenTypes";
    } else {

      Arrays.sort(subtractions);

      tokenText = Arrays.stream(tokens)
              .filter(token -> Arrays.binarySearch(subtractions, token) < 0)
              .mapToObj(TokenUtil::getTokenName).collect(Collectors.joining(","));
    }
    return tokenText.length() == 0 ? null : tokenText;
  }

  public static String getJavadocTokenText(int[] tokens, int... subtractions) {

    Arrays.sort(subtractions);

    String tokenText = Arrays.stream(tokens)
            .filter(token -> Arrays.binarySearch(subtractions, token) < 0)
            .mapToObj(JavadocUtil::getTokenName).collect(Collectors.joining(","));

    if (tokenText.length() == 0) {
      tokenText = "empty";
    } else {
      tokenText = tokenText + ".";
    }

    return tokenText;
  }
}
