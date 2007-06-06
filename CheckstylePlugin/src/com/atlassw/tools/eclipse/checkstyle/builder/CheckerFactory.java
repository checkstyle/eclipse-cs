//============================================================================
//
// Copyright (C) 2002-2007  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.builder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.ReferenceMap;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.CheckstyleConfigurationFile;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationReader;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.config.ConfigurationReader.AdditionalConfigData;
import com.atlassw.tools.eclipse.checkstyle.config.configtypes.IContextAware;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Factory class to create (and cache) checker objects.
 * 
 * @author Lars Ködderitzsch
 */
public final class CheckerFactory
{

    //
    // class attributes
    //

    /** Map containing the configured checkers. */
    private static Map sCheckerMap;

    /** Map containing the modification times of configs. */
    private static Map sModifiedMap;

    /** Map containing additional data about the check configurations. */
    private static Map sAdditionalDataMap;

    /** the shared classloader for the checkers. */
    private static ProjectClassLoader sSharedClassLoader;

    //
    // static initializer
    //

    /**
     * Initialize the cache.
     */
    static
    {

        // Use synchronized collections to avoid concurrent modification
        sCheckerMap = Collections.synchronizedMap(new ReferenceMap());
        sModifiedMap = Collections.synchronizedMap(new HashMap());
        sAdditionalDataMap = Collections.synchronizedMap(new HashMap());

        sSharedClassLoader = new ProjectClassLoader();
    }

    //
    // constructors
    //

    /**
     * Hidden utility class constructor.
     */
    private CheckerFactory()
    {
    // noop
    }

    //
    // methods
    //

    /**
     * Creates a checker for a given configuration file.
     * 
     * @param config the check configuration data
     * @param project the project to create the checker for
     * @return the checker for the given configuration file
     * @throws CheckstyleException the configuration file had errors
     * @throws IOException the config file could not be read
     * @throws CheckstylePluginException the configuration could not be read
     */
    public static Checker createChecker(ICheckConfiguration config, IProject project)
        throws CheckstyleException, IOException, CheckstylePluginException
    {

        String cacheKey = getCacheKey(config, project);

        CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();
        Checker checker = tryCheckerCache(cacheKey, configFileData.getModificationStamp());

        // no cache hit
        if (checker == null)
        {
            PropertyResolver resolver = configFileData.getPropertyResolver();

            // set the project context if the property resolver needs the
            // context
            if (resolver instanceof IContextAware)
            {
                ((IContextAware) resolver).setProjectContext(project);
            }

            InputStream in = null;
            try
            {
                in = configFileData.getCheckConfigFileStream();
                checker = createCheckerInternal(in, resolver);
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }

            // store checker in cache
            Long modified = new Long(configFileData.getModificationStamp());
            sCheckerMap.put(cacheKey, checker);
            sModifiedMap.put(cacheKey, modified);
        }

        return checker;
    }

    /**
     * Determines the additional data for a given configuration file.
     * 
     * @param config the check configuration
     * @param project the project to create the checker for
     * @return the checker for the given configuration file
     * @throws CheckstyleException the configuration file had errors
     * @throws IOException the config file could not be read
     * @throws CheckstylePluginException the configuration could not be read
     */
    public static ConfigurationReader.AdditionalConfigData getAdditionalData(
            ICheckConfiguration config, IProject project) throws CheckstyleException, IOException,
        CheckstylePluginException
    {

        String cacheKey = getCacheKey(config, project);

        ConfigurationReader.AdditionalConfigData additionalData = (AdditionalConfigData) sAdditionalDataMap
                .get(cacheKey);

        // no cache hit - create the additional data
        if (additionalData == null)
        {
            CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();
            additionalData = ConfigurationReader.getAdditionalConfigData(configFileData
                    .getCheckConfigFileStream());
            sAdditionalDataMap.put(cacheKey, additionalData);
        }

        return additionalData;
    }

    /**
     * Returns the shared classloader which is used by all checkers created by
     * this factory.
     * 
     * @return the shared classloader
     */
    public static ProjectClassLoader getSharedClassLoader()
    {
        return sSharedClassLoader;
    }

    /**
     * Cleans up the checker cache.
     */
    public static void cleanup()
    {
        sCheckerMap.clear();
        sModifiedMap.clear();
        sAdditionalDataMap.clear();
    }

    /**
     * Build a unique cache key for the check configuration.
     * 
     * @param config the check configuration
     * @param project the project being checked
     * @return the unique cache key
     * @throws CheckstylePluginException error getting configuration file data
     */
    private static String getCacheKey(ICheckConfiguration config, IProject project)
        throws CheckstylePluginException
    {
        CheckstyleConfigurationFile configFileData = config.getCheckstyleConfiguration();

        URL configLocation = configFileData.getResolvedConfigFileURL();
        String checkConfigName = config.getName() + "#" + (config.isGlobal() ? "Global" : "Local");

        String cacheKey = project.getName() + "#" + configLocation + "#" + checkConfigName; //$NON-NLS-1$

        return cacheKey;
    }

    /**
     * Tries to reuse an already configured checker for this configuration.
     * 
     * @param config the configuration file
     * @param cacheKey the key for cache access
     * @return the cached checker or null
     * @throws IOException the config file could not be read
     */
    private static Checker tryCheckerCache(String cacheKey, long modificationStamp)
        throws IOException
    {

        // try the cache
        Checker checker = (Checker) sCheckerMap.get(cacheKey);

        // if cache hit
        if (checker != null)
        {

            // compare modification times of the configs
            Long oldTime = (Long) sModifiedMap.get(cacheKey);
            Long newTime = new Long(modificationStamp);

            // no match - remove checker from cache
            if (oldTime == null || oldTime.compareTo(newTime) != 0)
            {
                checker = null;
                sCheckerMap.remove(cacheKey);
                sModifiedMap.remove(cacheKey);
                sAdditionalDataMap.remove(cacheKey);
            }
        }
        return checker;
    }

    /**
     * Creates a new checker and configures it with the given configuration
     * file.
     * 
     * @param inStream stream to the configuration file
     * @param propResolver a property resolver null
     * @return the newly created Checker
     * @throws CheckstyleException an exception during the creation of the
     *             checker occured
     * @throws CheckstylePluginException an exception during the creation of the
     *             checker occured
     */
    private static Checker createCheckerInternal(InputStream inStream, PropertyResolver propResolver)
        throws CheckstyleException, CheckstylePluginException
    {

        // load configuration
        Configuration configuration = ConfigurationLoader.loadConfiguration(inStream, propResolver,
                true);

        // create and configure checker
        Checker checker = new Checker();

        // load the package name files and create the module factory
        List packages = com.atlassw.tools.eclipse.checkstyle.builder.PackageNamesLoader
                .getPackageNames(Thread.currentThread().getContextClassLoader());
        checker.setModuleFactory(new PackageObjectFactory(packages));

        // set the eclipse platform locale
        Locale platformLocale = CheckstylePlugin.getPlatformLocale();
        checker.setLocaleLanguage(platformLocale.getLanguage());
        checker.setLocaleCountry(platformLocale.getCountry());

        IPreferencesService prefStore = Platform.getPreferencesService();
        if (!prefStore.getBoolean(CheckstylePlugin.PLUGIN_ID,
                CheckstylePlugin.PREF_DISABLE_PROJ_CLASSLOADER, false, null))
        {
            checker.setClassloader(sSharedClassLoader);
        }

        checker.configure(configuration);

        return checker;
    }
}