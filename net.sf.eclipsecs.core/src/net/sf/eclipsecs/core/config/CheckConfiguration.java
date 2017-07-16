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

package net.sf.eclipsecs.core.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.sf.eclipsecs.core.config.configtypes.IConfigurationType;
import net.sf.eclipsecs.core.util.CheckstylePluginException;

/**
 * Base implementation of a check configuration. Leaves the specific tasks to the concrete
 * subclasses.
 *
 * @author Lars Ködderitzsch
 */
public class CheckConfiguration implements ICheckConfiguration {

  /** the displayable name of the configuration. */
  private final String mName;

  /** the location of the checkstyle configuration file. */
  private final String mLocation;

  /** the description of the configuration. */
  private final String mDescription;

  /** the configuration type. */
  private final IConfigurationType mConfigType;

  /** flags if the configuration is global. */
  private final boolean mIsGlobal;

  /** The list of resolvable properties. */
  private final List<ResolvableProperty> mProperties;

  /** Map containing additional data for this check configuration. */
  private Map<String, String> mAdditionalData;

  /** Cached data of the Checkstyle configuration file. */
  private CheckstyleConfigurationFile mCheckstyleConfigurationFile;

  /** Time stamp when the cached configuration file data expires. */
  private long mExpirationTime = 0;

  /**
   * Creates a check configuration instance.
   *
   * @param name
   *          the name of the check configuration
   * @param location
   *          the location of the check configuration
   * @param description
   *          the description of the check configuration
   * @param type
   *          the check configuration type
   * @param global
   *          determines if the check configuration is a global configuration
   * @param properties
   *          the list of properties configured for this check configuration
   * @param additionalData
   *          a map of additional data for this configuration
   */
  public CheckConfiguration(final String name, final String location, final String description,
          final IConfigurationType type, final boolean global,
          final List<ResolvableProperty> properties, final Map<String, String> additionalData) {
    mName = name;
    mLocation = location;
    mDescription = description;
    mConfigType = type;
    mIsGlobal = global;

    if (additionalData != null) {
      mAdditionalData = Collections.unmodifiableMap(additionalData);
    } else {
      mAdditionalData = Collections.unmodifiableMap(new HashMap<String, String>());
    }

    mProperties = properties != null ? Collections.unmodifiableList(properties)
            : Collections.unmodifiableList(new ArrayList<ResolvableProperty>());
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public String getLocation() {
    return mLocation;
  }

  @Override
  public String getDescription() {
    return mDescription;
  }

  @Override
  public IConfigurationType getType() {
    return mConfigType;
  }

  @Override
  public Map<String, String> getAdditionalData() {
    return mAdditionalData;
  }

  @Override
  public List<ResolvableProperty> getResolvableProperties() {
    return mProperties;
  }

  @Override
  public boolean isEditable() {
    return mConfigType.isEditable();
  }

  @Override
  public boolean isConfigurable() {
    return mConfigType.isConfigurable(this);
  }

  @Override
  public boolean isGlobal() {
    return mIsGlobal;
  }

  @Override
  public URL getResolvedConfigurationFileURL() throws CheckstylePluginException {
    return getType().getResolvedConfigurationFileURL(this);
  }

  @Override
  public CheckstyleConfigurationFile getCheckstyleConfiguration() throws CheckstylePluginException {
    final long currentTime = System.currentTimeMillis();

    if ((mCheckstyleConfigurationFile == null) || (currentTime > mExpirationTime)) {
      mCheckstyleConfigurationFile = getType().getCheckstyleConfiguration(this);
      mExpirationTime = currentTime + 1000 * 60 * 60; // 1 hour
    }

    return mCheckstyleConfigurationFile;
  }

  @Override
  public boolean equals(final Object obj) {
    if ((obj == null) || !(obj instanceof ICheckConfiguration)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    final ICheckConfiguration rhs = (ICheckConfiguration) obj;
    return Objects.equals(getName(), rhs.getName())
            && Objects.equals(getLocation(), rhs.getLocation())
            && Objects.equals(getDescription(), rhs.getDescription())
            && Objects.equals(getType(), rhs.getType()) && isGlobal() == rhs.isGlobal()
            && Objects.equals(getResolvableProperties(), rhs.getResolvableProperties())
            && Objects.equals(getAdditionalData(), rhs.getAdditionalData());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getLocation(), getDescription(), getType(), isGlobal(),
            getResolvableProperties(), getAdditionalData());
  }
}
