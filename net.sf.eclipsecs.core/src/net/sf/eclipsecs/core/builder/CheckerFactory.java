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

package net.sf.eclipsecs.core.builder;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.InputSource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Closeables;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.LocalizedMessage;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory.ModuleLoadOption;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.CheckstylePluginPrefs;
import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.config.configtypes.IContextAware;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Factory class to create (and cache) checker objects.
 *
 */
public final class CheckerFactory {

  /** Map containing the configured checkers. */
  private static Cache<String, Checker> sCheckerMap;

  /** Map containing the modification times of configs. */
  private static Map<String, Long> sModifiedMap;

  /*
   * Initialize the cache.
   */
  static {

    sCheckerMap = CacheBuilder.newBuilder().softValues().build();

    sModifiedMap = new ConcurrentHashMap<>();
  }

  /**
   * Hidden utility class constructor.
   */
  private CheckerFactory() {
    // noop
  }

  /**
   * Creates a checker for a given configuration file.
   *
   * @param config
   *          the check configuration data
   * @param project
   *          the project to create the checker for
   * @return the checker for the given configuration file
   * @throws CheckstyleException
   *           the configuration file had errors
   * @throws CheckstylePluginException
   *           the configuration could not be read
   */
  public static Checker createChecker(ICheckConfiguration config, IProject project)
          throws CheckstyleException, CheckstylePluginException {

    String cacheKey = getCacheKey(config, project);

    CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();
    Checker checker = tryCheckerCache(cacheKey, configFileData.getModificationStamp());

    // clear Checkstyle internal caches upon checker reuse
    if (checker != null) {
      checker.clearCache();
    }

    // no cache hit
    if (checker == null) {
      PropertyResolver resolver = configFileData.getPropertyResolver();

      // set the project context if the property resolver needs the
      // context
      if (resolver instanceof IContextAware) {
        ((IContextAware) resolver).setProjectContext(project);
      }

      InputSource input = null;
      try {
        input = configFileData.getCheckConfigFileInputSource();
        checker = createCheckerInternal(input, resolver, project);
      } finally {
        Closeables.closeQuietly(input.getByteStream());
      }

      // store checker in cache
      Long modified = Long.valueOf(configFileData.getModificationStamp());
      sCheckerMap.put(cacheKey, checker);
      sModifiedMap.put(cacheKey, modified);
    } else {
      setLocaleIfChanged(checker);
    }

    return checker;
  }

  /**
   * Cleans up the checker cache.
   */
  public static void cleanup() {
    sCheckerMap.invalidateAll();
    sModifiedMap.clear();
  }

  /**
   * Build a unique cache key for the check configuration.
   *
   * @param config
   *          the check configuration
   * @param project
   *          the project being checked
   * @return the unique cache key
   * @throws CheckstylePluginException
   *           error getting configuration file data
   */
  private static String getCacheKey(ICheckConfiguration config, IProject project)
          throws CheckstylePluginException {
    CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();

    URL configLocation = configFileData.getResolvedConfigFileURL();
    String checkConfigName = config.getName() + "#" + (config.isGlobal() ? "Global" : "Local");

    return project.getName() + "#" + configLocation + "#" + checkConfigName;
  }

  /**
   * Tries to reuse an already configured checker for this configuration.
   *
   * @param config
   *          the configuration file
   * @param cacheKey
   *          the key for cache access
   * @return the cached checker or null
   */
  private static Checker tryCheckerCache(String cacheKey, long modificationStamp) {

    // try the cache
    Checker checker = sCheckerMap.getIfPresent(cacheKey);

    // if cache hit
    if (checker != null) {

      // compare modification times of the configs
      Long oldTime = sModifiedMap.get(cacheKey);
      Long newTime = Long.valueOf(modificationStamp);

      // no match - remove checker from cache
      if (oldTime == null || oldTime.compareTo(newTime) != 0) {
        checker = null;
        sCheckerMap.invalidate(cacheKey);
        sModifiedMap.remove(cacheKey);
      }
    }
    return checker;
  }

  /**
   * Creates a new checker and configures it with the given configuration file.
   *
   * @param input
   *          the input source for the configuration file
   * @param configFileUri
   *          the URI of the configuration file, or <code>null</code> if it could not be determined
   * @param propResolver
   *          a property resolver null
   * @param project
   *          the project
   * @return the newly created Checker
   * @throws CheckstyleException
   *           an exception during the creation of the checker occured
   */
  private static Checker createCheckerInternal(InputSource input, PropertyResolver propResolver,
          IProject project) throws CheckstyleException, CheckstylePluginException {

    // load configuration
    final Configuration configuration = ConfigurationLoader.loadConfiguration(input, propResolver,
            IgnoredModulesOptions.OMIT);

    ClassLoader moduleClassLoader = CheckstylePlugin.getDefault().getAddonExtensionClassLoader();
    Set<String> packageNames = PackageNamesLoader.getPackageNames(moduleClassLoader);

    // create and configure checker
    Checker checker = new Checker();
    checker.setModuleFactory(new PackageObjectFactory(packageNames, moduleClassLoader,
            ModuleLoadOption.TRY_IN_ALL_REGISTERED_PACKAGES));
    try {
      checker.setCharset(project.getDefaultCharset());
    } catch (UnsupportedEncodingException | CoreException ex) {
      CheckstylePluginException.rethrow(ex);
    }

    setLocale(checker, getLocale());
    checker.configure(configuration);

    // reset the basedir if it is set so it won't get into the plugins way
    // of determining workspace resources from checkstyle reported file
    // names, see
    // https://sourceforge.net/tracker/?func=detail&aid=2880044&group_id=80344&atid=559497
    checker.setBasedir(null);

    return checker;
  }

  private static void setLocaleIfChanged(final Checker checker) {
    final String lc = getLocale();
    if (lc != null && !lc.equals(CheckstylePlugin.getPlatformLocale().getLanguage())) {
      setLocale(checker, lc);
    }
  }

  private static void setLocale(final Checker checker, final String lang) {
    final String lastLocale;
    if (lang != null) {
      lastLocale = lang;
      checker.setLocaleLanguage(lang);
      checker.setLocaleCountry("");
      final Locale locale = new Locale(lang);
      LocalizedMessage.setLocale(locale);
      CheckstylePlugin.setPlatformLocale(locale);
    } else {
      // set the eclipse platform locale
      final Locale platformLocale = CheckstylePlugin.getPlatformLocale();
      lastLocale = platformLocale.getLanguage();
      checker.setLocaleLanguage(lastLocale);
      checker.setLocaleCountry(platformLocale.getCountry());
      LocalizedMessage.setLocale(platformLocale);
    }
  }

  private static String getLocale() {
    String lang = CheckstylePluginPrefs.getString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE);
    if (lang != null && (lang.isEmpty() || "default".equals(lang))) {
      lang = null;
    }
    return lang;
  }
}
