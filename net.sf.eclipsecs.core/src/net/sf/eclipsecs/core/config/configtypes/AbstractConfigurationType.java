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

package net.sf.eclipsecs.core.config.configtypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.URIUtil;

import com.google.common.io.ByteStreams;
import com.puppycrawl.tools.checkstyle.PropertyResolver;

import net.sf.eclipsecs.core.config.CheckstyleConfigurationFile;
import net.sf.eclipsecs.core.config.ICheckConfiguration;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Base implementation of <code>IConfigurationType</code>.
 *
 */
public abstract class AbstractConfigurationType implements IConfigurationType {

  /** The name. */
  private String mName;

  /** The internal name. */
  private String mInternalName;

  /** Flag if creatable. */
  private boolean mIsCreatable;

  /** Flag if properties are editable. */
  private boolean mIsEditable;

  /** Flag if configuration file is configurable. */
  private boolean mIsConfigurable;

  /**
   * Returns the URL of the checkstyle configuration file. Implementors are not expected to open any
   * connection to the URL.
   *
   * @param checkConfiguration
   *          the actual check configuration
   * @return the URL of the checkstyle configuration file
   * @throws IOException
   *           error while resolving the url
   */
  protected abstract URL resolveLocation(ICheckConfiguration checkConfiguration) throws IOException;

  @Override
  public void initialize(String name, String internalName, String definingPluginId,
          boolean creatable, boolean editable, boolean configurable) {
    mName = name;
    mInternalName = internalName;
    mIsCreatable = creatable;
    mIsEditable = editable;
    mIsConfigurable = configurable;
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public String getInternalName() {
    return mInternalName;
  }

  @Override
  public boolean isCreatable() {
    return mIsCreatable;
  }

  @Override
  public boolean isEditable() {
    return mIsEditable;
  }

  @Override
  public boolean isConfigurable(ICheckConfiguration checkConfiguration) {
    return mIsConfigurable;
  }

  @Override
  public URL getResolvedConfigurationFileURL(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException {
    URL url = null;

    try {
      url = resolveLocation(checkConfiguration);
    } catch (IOException ex) {
      CheckstylePluginException.rethrow(ex);
    }
    return url;
  }

  @Override
  public CheckstyleConfigurationFile getCheckstyleConfiguration(
          ICheckConfiguration checkConfiguration) throws CheckstylePluginException {

    CheckstyleConfigurationFile data = new CheckstyleConfigurationFile();

    try {

      // resolve the true configuration file URL
      data.setResolvedConfigFileURL(resolveLocation(checkConfiguration));

      URLConnection connection = data.getResolvedConfigFileURL().openConnection();
      connection.connect();

      // get last modification timestamp
      data.setModificationStamp(connection.getLastModified());

      // get the configuration file data
      byte[] configurationFileData = getBytesFromURLConnection(connection);
      data.setCheckConfigFileBytes(configurationFileData);

      // get the properties bundle
      getAdditionPropertiesBundleBytes(data.getResolvedConfigFileURL())
              .ifPresent(data::setAdditionalPropertyBundleBytes);

      // get the property resolver
      PropertyResolver resolver = getPropertyResolver(checkConfiguration, data);
      data.setPropertyResolver(resolver);

    } catch (IOException | URISyntaxException ex) {
      CheckstylePluginException.rethrow(ex);
    }

    return data;
  }

  /**
   * Retrieves the bytes of an additional properties bundle associated with the Checkstyle
   * configuration.
   * <p>
   * This method attempts to find a <code>.properties</code> file with the same base name as the
   * provided configuration URL.
   * </p>
   *
   * @param checkConfigURL
   *          the URL of the Checkstyle configuration file
   * @return an optional containing the bytes of the properties bundle, or empty if it cannot be
   *         loaded
   */
  protected Optional<byte[]> getAdditionPropertiesBundleBytes(URL checkConfigURL) {

    String location = checkConfigURL.toString();

    // Strip file extension
    String propsLocation = null;
    int lastPointIndex = location.lastIndexOf("."); //$NON-NLS-1$
    if (lastPointIndex > -1) {
      propsLocation = location.substring(0, lastPointIndex);
    } else {
      propsLocation = location;
    }

    propsLocation = propsLocation + ".properties"; //$NON-NLS-1$

    try {
      URL propertyFileURL = new URL(propsLocation);
      URLConnection connection = propertyFileURL.openConnection();

      return Optional.of(getBytesFromURLConnection(connection));
    } catch (IOException ex) {
      // we won't load the bundle then
      // disabled logging bug #1647602
      // CheckstyleLog.log(ioe);
    }
    return Optional.empty();
  }

  /**
   * Gets the property resolver for this configuration type used to expand property values within
   * the checkstyle configuration.
   *
   * @param config
   *          the actual check configuration
   * @param configFile
   *          the checkstyle configuration file
   * @return the property resolver
   * @throws IOException
   *           error creating the property resolver
   * @throws URISyntaxException
   *           if configuration file URL cannot be resolved
   */
  protected PropertyResolver getPropertyResolver(ICheckConfiguration config,
          CheckstyleConfigurationFile configFile) throws IOException, URISyntaxException {

    MultiPropertyResolver multiResolver = new MultiPropertyResolver();
    multiResolver.addPropertyResolver(new ResolvablePropertyResolver(config));

    File file = URIUtil.toFile(configFile.getResolvedConfigFileURL().toURI());
    if (file != null) {
      multiResolver.addPropertyResolver(new StandardPropertyResolver(file.toString()));
    } else {
      multiResolver.addPropertyResolver(
              new StandardPropertyResolver(configFile.getResolvedConfigFileURL().toString()));
    }

    multiResolver.addPropertyResolver(new ClasspathVariableResolver());
    multiResolver.addPropertyResolver(new SystemPropertyResolver());

    if (configFile.getAdditionalPropertiesBundleStream() != null) {
      ResourceBundle bundle = new PropertyResourceBundle(
              configFile.getAdditionalPropertiesBundleStream());
      multiResolver.addPropertyResolver(new ResourceBundlePropertyResolver(bundle));
    }

    return multiResolver;
  }

  /**
   * Reads and returns all bytes from the provided <code>URLConnection</code>.
   *
   * @param connection
   *          the connection to read from
   * @return the byte array containing the data from the connection
   * @throws IOException
   *           if an I/O error occurs while reading from the connection
   */
  protected byte[] getBytesFromURLConnection(URLConnection connection) throws IOException {
    try (InputStream in = connection.getInputStream()) {
      return ByteStreams.toByteArray(in);
    }
  }

  @Override
  public void notifyCheckConfigRemoved(ICheckConfiguration checkConfiguration)
          throws CheckstylePluginException {
    // standard is that nothing happens
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null || !(obj instanceof AbstractConfigurationType)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    AbstractConfigurationType rhs = (AbstractConfigurationType) obj;
    return mIsCreatable == rhs.mIsCreatable && mIsEditable == rhs.mIsEditable
            && mIsConfigurable == rhs.mIsConfigurable
            && Objects.equals(mName, rhs.mName)
            && Objects.equals(mInternalName, rhs.mInternalName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mName, mInternalName, mIsCreatable, mIsEditable, mIsConfigurable);
  }
}
