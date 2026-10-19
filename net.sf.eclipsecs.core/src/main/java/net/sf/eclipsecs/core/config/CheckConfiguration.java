///////////////////////////////////////////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////

package net.sf.eclipsecs.core.config;

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 */
public class CheckConfiguration extends AbstractCheckConfiguration {

    /** The displayable name of the configuration. */
    private final String name;

    /** The location of the checkstyle configuration file. */
    private final String location;

    /** The description of the configuration. */
    private final String description;

    /** The configuration type. */
    private final IConfigurationType configType;

    /** Flags if the configuration is global. */
    private final boolean isGlobal;

    /** The list of resolvable properties. */
    private final List<ResolvableProperty> properties;

    /** Map containing additional data for this check configuration. */
    private Map<String, String> additionalData;

    /** Cached data of the Checkstyle configuration file. */
    private CheckstyleConfigurationFile checkstyleConfigurationFile;

    /** Time stamp when the cached configuration file data expires. */
    private Instant expirationTime;

    /**
     * Creates a check configuration instance.
     *
     * @param name
     *            the name of the check configuration
     * @param location
     *            the location of the check configuration
     * @param description
     *            the description of the check configuration
     * @param type
     *            the check configuration type
     * @param global
     *            determines if the check configuration is a global configuration
     * @param properties
     *            the list of properties configured for this check configuration
     * @param additionalData
     *            a map of additional data for this configuration
     */
    public CheckConfiguration(final String name, final String location, final String description,
        final IConfigurationType type, final boolean global,
        final List<ResolvableProperty> properties, final Map<String, String> additionalData) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.configType = type;
        this.isGlobal = global;

        if (additionalData != null) {
            this.additionalData = Collections.unmodifiableMap(additionalData);
        }
        else {
            this.additionalData = Collections.unmodifiableMap(new HashMap<String, String>());
        }

        if (properties != null) {
            this.properties = Collections.unmodifiableList(properties);
        }
        else {
            this.properties = Collections.unmodifiableList(new ArrayList<>());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IConfigurationType getType() {
        return configType;
    }

    @Override
    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    @Override
    public List<ResolvableProperty> getResolvableProperties() {
        return properties;
    }

    @Override
    public boolean isEditable() {
        return configType.isEditable();
    }

    @Override
    public boolean isConfigurable() {
        return configType.isConfigurable(this);
    }

    @Override
    public boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public URL getResolvedConfigurationFileURL() throws CheckstylePluginException {
        return getType().getResolvedConfigurationFileURL(this);
    }

    @Override
    public CheckstyleConfigurationFile getCheckstyleConfiguration()
            throws CheckstylePluginException {
        final Instant currentTime = Instant.now();

        if (checkstyleConfigurationFile == null || currentTime.isAfter(expirationTime)) {
            checkstyleConfigurationFile = getType().getCheckstyleConfiguration(this);
            // 1 hour
            expirationTime = currentTime.plus(1, ChronoUnit.HOURS);
        }

        return checkstyleConfigurationFile;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof ICheckConfiguration)) {
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
