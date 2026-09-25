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
    private static final Cache<String, Checker> CHECKERS;

    /** Map containing the modification times of configs. */
    private static final Map<String, Long> MODIFIED_TIME;

    /*
     * Initialize the cache.
     */
    static {
        CHECKERS = CacheBuilder.newBuilder().softValues().build();
        MODIFIED_TIME = new ConcurrentHashMap<>();
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
     *            the check configuration data
     * @param project
     *            the project to create the checker for
     * @return the checker for the given configuration file
     * @throws CheckstyleException
     *             the configuration file had errors
     * @throws CheckstylePluginException
     *             the configuration could not be read
     */
    public static Checker createChecker(ICheckConfiguration config, IProject project)
            throws CheckstyleException, CheckstylePluginException {

        final String cacheKey = getCacheKey(config, project);

        final CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();
        Checker checker = tryCheckerCache(cacheKey, configFileData.getModificationStamp());

        // clear Checkstyle internal caches upon checker reuse
        if (checker != null) {
            checker.clearCache();
        }

        // no cache hit
        if (checker == null) {
            final PropertyResolver resolver = configFileData.getPropertyResolver();

            // set the project context if the property resolver needs the
            // context
            if (resolver instanceof IContextAware) {
                ((IContextAware) resolver).setProjectContext(project);
            }

            InputSource input = null;
            try {
                input = configFileData.getCheckConfigFileInputSource();
                checker = createCheckerInternal(input, resolver, project);
            }
            finally {
                Closeables.closeQuietly(input.getByteStream());
            }

            // store checker in cache
            final Long modified = Long.valueOf(configFileData.getModificationStamp());
            CHECKERS.put(cacheKey, checker);
            MODIFIED_TIME.put(cacheKey, modified);
        }
        else {
            setLocaleIfChanged(checker);
        }

        return checker;
    }

    /**
     * Cleans up the checker cache.
     */
    public static void cleanup() {
        CHECKERS.invalidateAll();
        MODIFIED_TIME.clear();
    }

    /**
     * Build a unique cache key for the check configuration.
     *
     * @param config
     *            the check configuration
     * @param project
     *            the project being checked
     * @return the unique cache key
     * @throws CheckstylePluginException
     *             error getting configuration file data
     */
    private static String getCacheKey(ICheckConfiguration config, IProject project)
            throws CheckstylePluginException {
        final CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();

        final String globalVsLocal;
        if (config.isGlobal()) {
            globalVsLocal = "Global";
        }
        else {
            globalVsLocal = "Local";
        }
        final URL configLocation = configFileData.getResolvedConfigFileURL();
        return String.join("#", project.getName(), configLocation.toString(), config.getName(),
            globalVsLocal);
    }

    /**
     * Tries to reuse an already configured checker for this configuration.
     *
     * @param cacheKey
     *            the key for cache access
     * @param modificationStamp
     *            the last modification timestamp of the configuration file
     * @return the cached checker or null
     */
    private static Checker tryCheckerCache(String cacheKey, long modificationStamp) {

        // try the cache
        Checker checker = CHECKERS.getIfPresent(cacheKey);

        // if cache hit
        if (checker != null) {

            // compare modification times of the configs
            final Long oldTime = MODIFIED_TIME.get(cacheKey);
            final Long newTime = Long.valueOf(modificationStamp);

            // no match - remove checker from cache
            if (oldTime == null || oldTime.compareTo(newTime) != 0) {
                checker = null;
                CHECKERS.invalidate(cacheKey);
                MODIFIED_TIME.remove(cacheKey);
            }
        }
        return checker;
    }

    /**
     * Creates a new checker and configures it with the given configuration file.
     *
     * @param input
     *            the input source for the configuration file
     * @param propResolver
     *            a property resolver null
     * @param project
     *            the project
     * @return the newly created Checker
     * @throws CheckstyleException
     *             an exception during the creation of the checker occured
     * @throws CheckstylePluginException
     *             an unexpected exception occurred
     */
    private static Checker createCheckerInternal(InputSource input, PropertyResolver propResolver,
        IProject project) throws CheckstyleException, CheckstylePluginException {

        // load configuration
        final Configuration configuration =
            ConfigurationLoader.loadConfiguration(input, propResolver, IgnoredModulesOptions.OMIT);

        final ClassLoader moduleClassLoader =
            CheckstylePlugin.getDefault().getAddonExtensionClassLoader();
        final Set<String> packageNames = PackageNamesLoader.getPackageNames(moduleClassLoader);

        // create and configure checker
        final Checker checker = new Checker();
        checker.setModuleFactory(new PackageObjectFactory(packageNames, moduleClassLoader,
            ModuleLoadOption.TRY_IN_ALL_REGISTERED_PACKAGES));
        try {
            checker.setCharset(project.getDefaultCharset());
        }
        catch (UnsupportedEncodingException | CoreException ex) {
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

    /**
     * Sets the locale on the given checker if it changed.
     *
     * @param checker
     *            the checker to update
     */
    private static void setLocaleIfChanged(final Checker checker) {
        final String lc = getLocale();
        if (lc != null && !lc.equals(CheckstylePlugin.getPlatformLocale().getLanguage())) {
            setLocale(checker, lc);
        }
    }

    /**
     * Sets the locale on the given checker.
     *
     * @param checker
     *            the checker to update
     * @param lang
     *            the language to apply
     */
    private static void setLocale(final Checker checker, final String lang) {
        final String lastLocale;
        if (lang != null) {
            lastLocale = lang;
            checker.setLocaleLanguage(lang);
            checker.setLocaleCountry("");
            final Locale locale = new Locale(lang);
            LocalizedMessage.setLocale(locale);
            CheckstylePlugin.setPlatformLocale(locale);
        }
        else {
            // set the eclipse platform locale
            final Locale platformLocale = CheckstylePlugin.getPlatformLocale();
            lastLocale = platformLocale.getLanguage();
            checker.setLocaleLanguage(lastLocale);
            checker.setLocaleCountry(platformLocale.getCountry());
            LocalizedMessage.setLocale(platformLocale);
        }
    }

    /**
     * Returns the configured locale language.
     *
     * @return the configured language or <code>null</code> if the default should be used
     */
    private static String getLocale() {
        String lang = CheckstylePluginPrefs.getString(CheckstylePluginPrefs.PREF_LOCALE_LANGUAGE);
        if (lang != null && (lang.isEmpty() || "default".equals(lang))) {
            lang = null;
        }
        return lang;
    }
}
