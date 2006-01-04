//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars Ködderitzsch
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

package com.atlassw.tools.eclipse.checkstyle.config.configtypes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;

import com.atlassw.tools.eclipse.checkstyle.CheckstylePlugin;
import com.atlassw.tools.eclipse.checkstyle.config.ICheckConfiguration;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstyleLog;
import com.atlassw.tools.eclipse.checkstyle.util.CheckstylePluginException;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

/**
 * Implementation of a check configuration that uses an exteral checkstyle
 * configuration file.
 * 
 * @author Lars Ködderitzsch
 */
public class RemoteConfigurationType extends ConfigurationType
{

    /** Key to access the information if the configuration should be cached. */
    public static final String KEY_CACHE_CONFIG = "cache-file";

    /** Key to access the path of the cached configuration file. */
    public static final String KEY_CACHE_FILE_LOCATION = "cache-file-location";

    /** Key to access if http-basic authentication should be used. */
    public static final String KEY_USE_BASIC_AUTH = "use-http-basic-auth";

    /** Key to access the username. */
    public static final String KEY_USERNAME = "username";

    /** Key to access the password. */
    public static final String KEY_PASSWORD = "password";

    /** A static key to encrypt username/password values. */
    public static final String SIMPLE_ECRYPTION_KEY = "jldf20984029kdj204308bfjks98";

    /**
     * {@inheritDoc}
     */
    public InputStream openConfigurationFileStream(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        InputStream inStream = null;

        boolean useCacheFile = Boolean.valueOf(
                (String) checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG))
                .booleanValue();

        if (useCacheFile)
        {
            synchronizeCacheFile(checkConfiguration);
        }

        try
        {
            inStream = getStreamFromOriginalLocation(checkConfiguration);
        }
        catch (CheckstylePluginException e)
        {
            if (useCacheFile)
            {
                inStream = getStreamFromCacheFile(checkConfiguration);
            }
            else
            {
                throw e;
            }
        }

        return inStream;
    }

    /**
     * {@inheritDoc}
     */
    public URL resolveLocation(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        try
        {
            return new URL(checkConfiguration.getLocation());
        }
        catch (MalformedURLException e)
        {
            CheckstylePluginException.rethrow(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {
        super.notifyCheckConfigRemoved(checkConfiguration);

        boolean useCacheFile = Boolean.valueOf(
                (String) checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG))
                .booleanValue();

        if (useCacheFile)
        {
            // remove the cahced configuration file from the workspace metadata
            String cacheFileLocation = (String) checkConfiguration.getAdditionalData().get(
                    KEY_CACHE_FILE_LOCATION);

            IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
            cacheFilePath = cacheFilePath.append(cacheFileLocation);
            File cacheFile = cacheFilePath.toFile();
            cacheFile.delete();
        }
    }

    /**
     * {@inheritDoc}
     */
    public PropertyResolver getPropertyResolver(ICheckConfiguration checkConfiguration)
        throws CheckstylePluginException
    {

        String location = checkConfiguration.getLocation();

        MultiPropertyResolver multiResolver = new MultiPropertyResolver();
        multiResolver.addPropertyResolver(new StandardPropertyResolver(location));

        ResourceBundle bundle = getBundle(location);
        if (bundle != null)
        {
            multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
        }

        return multiResolver;
    }

    /**
     * Method to get the input stream for the remote location config file.
     * 
     * @param checkConfig the check configuration
     * @return the input stream
     * @throws CheckstylePluginException error getting the stream (remote
     *             location not reachable)
     */
    private InputStream getStreamFromOriginalLocation(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {

        // TODO http authentication
        boolean useHttpAuth = Boolean.valueOf(
                (String) checkConfig.getAdditionalData().get(KEY_USE_BASIC_AUTH)).booleanValue();

        InputStream inStream = null;

        try
        {
            URL configURL = resolveLocation(checkConfig);
            inStream = new BufferedInputStream(configURL.openStream());
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return inStream;
    }

    /**
     * Method to get an input stream to the cached configuration file.
     * 
     * @param checkConfig the check configuration
     * @return the input stream
     * @throws CheckstylePluginException error getting the stream (file does not
     *             exist)
     */
    private InputStream getStreamFromCacheFile(ICheckConfiguration checkConfig)
        throws CheckstylePluginException
    {
        String cacheFileLocation = (String) checkConfig.getAdditionalData().get(
                KEY_CACHE_FILE_LOCATION);

        IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
        cacheFilePath = cacheFilePath.append(cacheFileLocation);
        File cacheFile = cacheFilePath.toFile();

        InputStream inStream = null;

        try
        {
            URL configURL = cacheFile.toURL();
            inStream = new BufferedInputStream(configURL.openStream());
        }
        catch (IOException e)
        {
            CheckstylePluginException.rethrow(e);
        }

        return inStream;
    }

    /**
     * Method to synchronize the cached configuration file from the remote
     * location.
     * 
     * @param checkConfig the check configuration
     * @return <code>true</code> if the synchronization was successful,
     *         <code>false</code> otherwise
     */
    private boolean synchronizeCacheFile(ICheckConfiguration checkConfig)
    {

        String cacheFileLocation = (String) checkConfig.getAdditionalData().get(
                KEY_CACHE_FILE_LOCATION);

        IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
        cacheFilePath = cacheFilePath.append(cacheFileLocation);
        File cacheFile = cacheFilePath.toFile();

        OutputStream outStream = null;
        InputStream inStream = null;

        try
        {

            // fetch the original configuration file and store into the cache
            // file
            inStream = getStreamFromOriginalLocation(checkConfig);

            // temporary output stream to ensure the input is fully fetched
            // before writing to the actual cache file
            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();

            byte[] buf = new byte[512];
            int len = 0;
            while ((len = inStream.read(buf)) > -1)
            {
                tmpOut.write(buf, 0, len);
            }

            // finally write to the cache file
            outStream = new BufferedOutputStream(new FileOutputStream(cacheFile));
            outStream.write(tmpOut.toByteArray());
            outStream.flush();
        }
        catch (IOException e)
        {
            CheckstyleLog.log(e, NLS.bind("Could not cache remote configuration {0} ({1})",
                    checkConfig.getName(), checkConfig.getLocation()));
            return false;
        }
        catch (CheckstylePluginException e)
        {
            CheckstyleLog.log(e, NLS.bind("Could not cache remote configuration {0} ({1})",
                    checkConfig.getName(), checkConfig.getLocation()));
            return false;
        }
        finally
        {
            try
            {
                inStream.close();
            }
            catch (Exception e)
            {
                // we tried to be nice
            }
            try
            {
                outStream.close();
            }
            catch (Exception e)
            {
                // we tried to be nice
            }
        }
        return true;
    }

    /**
     * Helper method to get the resource bundle for this configuration.
     * 
     * @param location the configuration file location
     * @return the resource bundle or <code>null</code> if no bundle exists
     */
    private static ResourceBundle getBundle(String location)
    {

        ResourceBundle bundle = null;

        try
        {

            // Strip file extension
            String propsLocation = null;

            int lastPointIndex = location.lastIndexOf("."); //$NON-NLS-1$
            if (lastPointIndex > -1)
            {
                propsLocation = location.substring(0, lastPointIndex);
            }
            else
            {
                propsLocation = location;
            }

            URL propUrl = new URL(propsLocation + ".properties"); //$NON-NLS-1$

            bundle = new PropertyResourceBundle(new BufferedInputStream(propUrl.openStream()));
        }
        catch (IOException ioe)
        {
            // we won't load the bundle then
        }

        return bundle;
    }

}