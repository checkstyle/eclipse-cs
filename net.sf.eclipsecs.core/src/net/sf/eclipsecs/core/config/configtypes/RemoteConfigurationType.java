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

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.sf.eclipsecs.core.CheckstylePlugin;
import net.sf.eclipsecs.core.Messages;
import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstyleLog;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.osgi.util.NLS;

/**
 * Implementation of a check configuration that uses an exteral checkstyle configuration file.
 *
 * @author Lars Ködderitzsch
 */
public class RemoteConfigurationType extends ConfigurationType {

  /** Key to access the information if the configuration should be cached. */
  public static final String KEY_CACHE_CONFIG = "cache-file"; //$NON-NLS-1$

  /** Key to access the path of the cached configuration file. */
  public static final String KEY_CACHE_FILE_LOCATION = "cache-file-location"; //$NON-NLS-1$

  /** Key to access the path of the cached property file. */
  public static final String KEY_CACHE_PROPS_FILE_LOCATION = "cache-props-file-location"; //$NON-NLS-1$

  /** Key to access the username. */
  public static final String KEY_USERNAME = "username"; //$NON-NLS-1$

  /** Key to access the password. */
  public static final String KEY_PASSWORD = "password"; //$NON-NLS-1$

  private static Set<String> sFailedWith401URLs = new HashSet<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public CheckstyleConfigurationFile getCheckstyleConfiguration(
          ICheckConfiguration checkConfiguration) throws CheckstylePluginException {

    boolean useCacheFile = Boolean
            .valueOf(checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG)).booleanValue();

    CheckstyleConfigurationFile data = new CheckstyleConfigurationFile();

    synchronized (Authenticator.class) {

      String currentRedirects = System.getProperty("http.maxRedirects"); //$NON-NLS-1$

      Authenticator oldAuthenticator = RemoteConfigAuthenticator.getDefault();
      try {

        // resolve the true configuration file URL
        data.setResolvedConfigFileURL(resolveLocation(checkConfiguration));

        Authenticator.setDefault(new RemoteConfigAuthenticator(data.getResolvedConfigFileURL()));

        boolean originalFileSuccess = false;
        byte[] configurationFileData = null;

        try {

          System.setProperty("http.maxRedirects", "3"); //$NON-NLS-1$ //$NON-NLS-2$

          URLConnection connection = data.getResolvedConfigFileURL().openConnection();

          // get the configuration file data
          configurationFileData = getBytesFromURLConnection(connection);

          // get last modification timestamp
          data.setModificationStamp(connection.getLastModified());

          originalFileSuccess = true;
        } catch (IOException e) {
          if (useCacheFile) {
            configurationFileData = getBytesFromCacheFile(checkConfiguration);
          } else {
            throw e;
          }
        }

        data.setCheckConfigFileBytes(configurationFileData);

        // get the properties bundle
        byte[] additionalPropertiesBytes = null;
        if (originalFileSuccess) {
          additionalPropertiesBytes = getAdditionPropertiesBundleBytes(
                  data.getResolvedConfigFileURL());
        } else if (useCacheFile) {
          additionalPropertiesBytes = getBytesFromCacheBundleFile(checkConfiguration);
        }

        data.setAdditionalPropertyBundleBytes(additionalPropertiesBytes);

        // get the property resolver
        PropertyResolver resolver = getPropertyResolver(checkConfiguration, data);
        data.setPropertyResolver(resolver);

        // write to cache file
        if (originalFileSuccess && useCacheFile) {
          writeToCacheFile(checkConfiguration, configurationFileData, additionalPropertiesBytes);
        }

      } catch (UnknownHostException e) {
        CheckstylePluginException.rethrow(e,
                NLS.bind(Messages.RemoteConfigurationType_errorUnknownHost, e.getMessage()));
      } catch (FileNotFoundException e) {
        CheckstylePluginException.rethrow(e,
                NLS.bind(Messages.RemoteConfigurationType_errorFileNotFound, e.getMessage()));
      } catch (IOException | URISyntaxException e) {
        CheckstylePluginException.rethrow(e);
      } finally {
        Authenticator.setDefault(oldAuthenticator);

        if (currentRedirects != null) {
          System.setProperty("http.maxRedirects", currentRedirects); //$NON-NLS-1$
        } else {
          System.getProperties().remove("http.maxRedirects"); //$NON-NLS-1$
        }
      }

    }
    return data;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException {
    return new URL(checkConfiguration.getLocation());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException {
    super.notifyCheckConfigRemoved(checkConfiguration);

    // remove authentication info
    RemoteConfigAuthenticator
            .removeCachedAuthInfo(checkConfiguration.getResolvedConfigurationFileURL());

    boolean useCacheFile = Boolean
            .valueOf(checkConfiguration.getAdditionalData().get(KEY_CACHE_CONFIG)).booleanValue();

    if (useCacheFile) {
      // remove the cached configuration file from the workspace metadata
      String cacheFileLocation = checkConfiguration.getAdditionalData()
              .get(KEY_CACHE_FILE_LOCATION);

      IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
      cacheFilePath = cacheFilePath.append(cacheFileLocation);
      File cacheFile = cacheFilePath.toFile();
      cacheFile.delete();
    }
  }

  /**
   * Method to get an input stream to the cached configuration file.
   *
   * @param checkConfig
   *          the check configuration
   * @return the input stream
   * @throws IOException
   *           error getting the stream (file does not exist)
   */
  private byte[] getBytesFromCacheFile(ICheckConfiguration checkConfig) throws IOException {
    String cacheFileLocation = checkConfig.getAdditionalData().get(KEY_CACHE_FILE_LOCATION);

    IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
    cacheFilePath = cacheFilePath.append(cacheFileLocation);
    File cacheFile = cacheFilePath.toFile();

    URL configURL = cacheFile.toURI().toURL();
    URLConnection connection = configURL.openConnection();

    return getBytesFromURLConnection(connection);
  }

  /**
   * Method to get an input stream to the cached bundle file.
   *
   * @param checkConfig
   *          the check configuration
   * @return the input stream
   * @throws IOException
   *           error getting the stream (file does not exist)
   */
  private byte[] getBytesFromCacheBundleFile(ICheckConfiguration checkConfig) {

    String cacheFileLocation = checkConfig.getAdditionalData().get(KEY_CACHE_PROPS_FILE_LOCATION);

    // bug 1748626
    if (cacheFileLocation == null) {
      return null;
    }

    try {
      IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
      cacheFilePath = cacheFilePath.append(cacheFileLocation);
      File cacheFile = cacheFilePath.toFile();

      URL configURL = cacheFile.toURI().toURL();
      URLConnection connection = configURL.openConnection();

      return getBytesFromURLConnection(connection);
    } catch (IOException e) {
      // we won't load the bundle then
      // disabled logging bug #1647602
      // CheckstyleLog.log(ioe);
    }
    return null;
  }

  private void writeToCacheFile(ICheckConfiguration checkConfig, byte[] configFileBytes,
          byte[] bundleBytes) {

    String cacheFileLocation = checkConfig.getAdditionalData().get(KEY_CACHE_FILE_LOCATION);

    IPath cacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
    cacheFilePath = cacheFilePath.append(cacheFileLocation);
    File cacheFile = cacheFilePath.toFile();

    try {
      Files.write(configFileBytes, cacheFile);
    } catch (IOException e) {
      CheckstyleLog.log(e, NLS.bind(Messages.RemoteConfigurationType_msgRemoteCachingFailed,
              checkConfig.getName(), checkConfig.getLocation()));
    }

    if (bundleBytes != null) {

      String propsCacheFileLocation = checkConfig.getAdditionalData()
              .get(KEY_CACHE_PROPS_FILE_LOCATION);

      IPath propsCacheFilePath = CheckstylePlugin.getDefault().getStateLocation();
      propsCacheFilePath = propsCacheFilePath.append(propsCacheFileLocation);
      File propsCacheFile = propsCacheFilePath.toFile();

      try {
        Files.write(bundleBytes, propsCacheFile);
      } catch (IOException e) {
        // ignore this since there simply might be no properties file
      }
    }
  }

  @Override
  protected byte[] getBytesFromURLConnection(URLConnection connection) throws IOException {

    byte[] configurationFileData = null;

    // set timeouts - bug 2941010
    connection.setConnectTimeout(10000);
    connection.setReadTimeout(10000);

    if (connection instanceof HttpURLConnection) {

      if (!sFailedWith401URLs.contains(connection.getURL().toString())) {

        HttpURLConnection httpConn = (HttpURLConnection) connection;
        httpConn.setInstanceFollowRedirects(true);
        httpConn.connect();
        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
          try {
            RemoteConfigAuthenticator.removeCachedAuthInfo(connection.getURL());
          } catch (CheckstylePluginException e) {
            CheckstyleLog.log(e);
          }

          // add to 401ed URLs
          sFailedWith401URLs.add(connection.getURL().toString());
          throw new IOException(Messages.RemoteConfigurationType_msgUnAuthorized);
        }
      } else {
        // don't retry since we just get another 401
        throw new IOException(Messages.RemoteConfigurationType_msgUnAuthorized);
      }
    }

    try (InputStream in = connection.getInputStream()) {
      configurationFileData = ByteStreams.toByteArray(in);
    }

    return configurationFileData;
  }

  /**
   * Support for http authentication.
   *
   * @author Lars Ködderitzsch
   */
  public static class RemoteConfigAuthenticator extends Authenticator {

    /** The check configuration URL. */
    private final URL mResolvedCheckConfigurationURL;

    /**
     * Creates the authenticator.
     *
     * @param resolvedCheckConfigurationURL
     *          the check configuration URL
     */
    public RemoteConfigAuthenticator(URL resolvedCheckConfigurationURL) {
      mResolvedCheckConfigurationURL = resolvedCheckConfigurationURL;
    }

    /**
     * Hacked together a piece of code to get the current default authenticator. Can't believe the
     * API is that bad...
     *
     * @return the current Authenticator
     */
    public static Authenticator getDefault() {

      Authenticator currentDefault = null;

      try {

        // Hack to get the current authenticator
        Field[] fields = Authenticator.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
          if (Authenticator.class.equals(fields[i].getType())) {
            fields[i].setAccessible(true);
            currentDefault = (Authenticator) fields[i].get(Authenticator.class);
            break;
          }
        }
      } catch (IllegalArgumentException e) {
        CheckstyleLog.log(e);
      } catch (IllegalAccessException e) {
        CheckstyleLog.log(e);
      }
      return currentDefault;
    }

    /**
     * Stores the credentials to the key ring.
     *
     * @param resolvedCheckConfigurationURL
     *          the url
     * @param userName
     *          the user name
     * @param password
     *          the password
     */
    public static void storeCredentials(URL resolvedCheckConfigurationURL, String userName,
            String password) {

      try {

        // store authorization info to the internal key ring
        ISecurePreferences prefs = SecurePreferencesFactory.getDefault()
                .node(getSecureStoragePath(resolvedCheckConfigurationURL));

        prefs.put(KEY_USERNAME, userName, false);
        prefs.put(KEY_PASSWORD, password, true);

        sFailedWith401URLs.remove(resolvedCheckConfigurationURL.toString());
      } catch (CheckstylePluginException e) {
        CheckstyleLog.log(e);
      } catch (StorageException e) {
        CheckstyleLog.log(e);
      }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return getPasswordAuthentication(mResolvedCheckConfigurationURL);
    }

    /**
     * Returns the stored authentication for the given check configuration URL.
     *
     * @param resolvedCheckConfigurationURL
     *          the configuration URL
     * @return the authentication object or <code>null</code> if none is stored
     */
    public static PasswordAuthentication getPasswordAuthentication(
            URL resolvedCheckConfigurationURL) {

      PasswordAuthentication auth = null;

      try {

        ISecurePreferences prefs = SecurePreferencesFactory.getDefault()
                .node(getSecureStoragePath(resolvedCheckConfigurationURL));

        String userName = prefs.get(KEY_USERNAME, null);
        String password = prefs.get(KEY_PASSWORD, null);

        if (userName != null && password != null) {
          auth = new PasswordAuthentication(userName, password.toCharArray());
        }
      } catch (CheckstylePluginException e) {
        CheckstyleLog.log(e);
      } catch (StorageException e) {
        CheckstyleLog.log(e);
      }

      return auth;
    }

    /**
     * Removes the authentication info from the session cache.
     *
     * @param resolvedCheckConfigurationURL
     *          the check configuration URL
     * @throws CheckstylePluginException
     *           if the authentication could not be removed
     */
    public static void removeCachedAuthInfo(URL resolvedCheckConfigurationURL)
            throws CheckstylePluginException {
      sFailedWith401URLs.remove(resolvedCheckConfigurationURL.toString());

      String storagePath = getSecureStoragePath(resolvedCheckConfigurationURL);

      if (SecurePreferencesFactory.getDefault().nodeExists(storagePath)) {

        ISecurePreferences prefs = SecurePreferencesFactory.getDefault()
                .node(getSecureStoragePath(resolvedCheckConfigurationURL));
        prefs.removeNode();
      }
    }

    private static String getSecureStoragePath(URL resolvedCheckConfigurationURL)
            throws CheckstylePluginException {

      // convert the config url to a hash, because storage paths can only
      // be 128 chars long
      // and config URLs are very likely to be longer
      String urlHash = null;

      try {

        MessageDigest d = MessageDigest.getInstance("MD5");
        byte[] hash = d.digest(resolvedCheckConfigurationURL.toExternalForm().getBytes("UTF-8"));
        urlHash = EncodingUtils.encodeBase64(hash);

        urlHash = urlHash.replace('/', '_');
        urlHash = urlHash.replace('\\', '_');
      } catch (NoSuchAlgorithmException e) {
        CheckstylePluginException.rethrow(e);
      } catch (UnsupportedEncodingException e) {
        CheckstylePluginException.rethrow(e);
      }
      return "eclipse-cs/" + urlHash;
    }
  }
}
