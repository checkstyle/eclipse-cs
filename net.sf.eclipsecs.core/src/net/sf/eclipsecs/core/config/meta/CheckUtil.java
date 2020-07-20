package net.sf.eclipsecs.core.config.meta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import net.sf.eclipsecs.core.CheckstylePlugin;

public final class CheckUtil {
    private CheckUtil() {
    }

    public static String getModifiableTokens(String checkName) {
      final Object checkResult = getCheck(checkName);
      String result = null;
      if (AbstractJavadocCheck.class.isAssignableFrom(checkResult.getClass())) {
          final AbstractJavadocCheck javadocCheck = (AbstractJavadocCheck) checkResult;
          final List<Integer> modifiableJavadocTokens = subtractTokens(javadocCheck.getAcceptableJavadocTokens(),
                  javadocCheck.getRequiredJavadocTokens());
          result = getTokens(JavadocUtil::getTokenName, modifiableJavadocTokens);
      }
      else if (AbstractCheck.class.isAssignableFrom(checkResult.getClass())) {
          final AbstractCheck check = (AbstractCheck) checkResult;
          final List<Integer> modifiableTokens = subtractTokens(check.getAcceptableTokens(),
                  check.getRequiredTokens());
          result = getTokens(TokenUtil::getTokenName, modifiableTokens);
      }
      else {
        throw new IllegalStateException("Exception caused in CheckUtil.getCheck, "
                + "method executed in wrong context, heirarchy of check class missing");
      }
      return result;
   }

    private static AbstractCheck getCheck(String checkName) {
        final ClassLoader classLoader = CheckstylePlugin.getDefault()
                .getAddonExtensionClassLoader();
        try {
            final Set<String> packageNames = PackageNamesLoader.getPackageNames(classLoader);
            return (AbstractCheck) new PackageObjectFactory(packageNames, classLoader)
                    .createModule(checkName);
        }
        catch (CheckstyleException ex) {
            throw new IllegalStateException("exception occured during load of " + checkName, ex);
        }
    }

    private static List<Integer> subtractTokens(int[] tokens, int... requiredTokens) {
      Set<Integer> requiredTokensSet = new HashSet<>(Arrays.stream(requiredTokens)
              .boxed()
              .collect(Collectors.toList()));
      return Arrays.stream(tokens)
              .boxed()
              .filter(token -> !requiredTokensSet.contains(token))
              .collect(Collectors.toList());
    }

    private static String getTokens(Function<Integer, String> function, List<Integer> modifiableTokens) {
      return modifiableTokens.stream()
            .map(tokenInteger -> function.apply(tokenInteger))
            .collect(Collectors.joining(","));
    }
}

