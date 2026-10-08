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

package net.sf.eclipsecs.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;
import net.sf.eclipsecs.core.config.meta.MetadataFactory;
import net.sf.eclipsecs.core.config.meta.RuleMetadata;

/**
 * Object representing a module from a checkstyle configuration. Can be augmented with meta data.
 *
 */
public class Module {

    //
    // attributes
    //

    /** The custom messages for this module. */
    private final Map<String, String> customMessages = new HashMap<>();

    /** Map containing unknown custom metadata of the module. */
    private final Map<String, String> customMetaData = new HashMap<>();

    /** The name of the module. */
    private String name;

    /** The meta data associated with the module. */
    private RuleMetadata metaData;

    /** The properties of the modules. */
    private List<ConfigProperty> properties = new ArrayList<>();

    /** The comment of the module. */
    private String comment;

    /** The id of the module. */
    private String id;

    /** The severity level. */
    private Severity severityLevel = Severity.INHERIT;

    /** The last severity level before setting to ignored. */
    private Severity lastEnabledSeverity;

    /**
     * Copy constructor.
     *
     * @param other
     *            the module to copy
     */
    public Module(Module other) {
        name = other.name;
        metaData = other.metaData;
        properties = new ArrayList<>();
        for (ConfigProperty prop : other.properties) {
            properties.add(new ConfigProperty(prop));
        }
        comment = other.comment;
        id = other.id;
        severityLevel = other.severityLevel;
        lastEnabledSeverity = other.lastEnabledSeverity;
        customMessages.putAll(other.customMessages);
        customMetaData.putAll(other.customMetaData);
    }

    //
    // constructors
    //

    /**
     * Create a module without meta data.
     *
     * @param name
     *            the name of the module
     */
    public Module(String name) {
        this.name = name;
    }

    /**
     * Creates a module with the according meta data.
     *
     * @param metaData
     *            the meta data
     * @param withDefaults
     *            determines if the properties should be initialized with the Checkstyle default
     *            values
     */
    public Module(RuleMetadata metaData, boolean withDefaults) {
        this.metaData = metaData;

        if (metaData != null) {

            // create the properties according to the meta data
            final List<ConfigPropertyMetadata> propMetas = metaData.configPropMetadata();
            for (ConfigPropertyMetadata propMeta : propMetas) {
                final ConfigProperty property = new ConfigProperty(propMeta);
                getProperties().add(property);

                if (withDefaults) {
                    property.setValue(propMeta.getDefaultValue());
                }
            }

            if (metaData.defaultSeverity() != null) {
                severityLevel = metaData.defaultSeverity();
            }
        }
    }

    /**
     * Returns the name of the module.
     *
     * @return the name of the module
     */
    public String getName() {
        final String moduleName;
        if (metaData != null) {
            moduleName = metaData.identity().ruleName();
        }
        else {
            moduleName = name;
        }
        return moduleName;
    }

    /**
     * Returns the unique id of the module.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id of the module.
     *
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the custom messages to display instead of Checkstyle's default message.
     *
     * @return the customMessage
     */
    public Map<String, String> getCustomMessages() {
        return customMessages;
    }

    /**
     * Returns the meta data associated with the module.
     *
     * @return the meta data of the module
     */
    public RuleMetadata getMetaData() {
        return metaData;
    }

    /**
     * Sets the meta data of the module.
     *
     * @param metaData
     *            the meta data
     */
    public void setMetaData(RuleMetadata metaData) {
        this.metaData = metaData;
    }

    /**
     * Returns all properties of this module.
     *
     * @return the properties
     */
    public List<ConfigProperty> getProperties() {
        return properties;
    }

    /**
     * Returns the property data for a given property name.
     *
     * @param property
     *            the property name
     * @return the coresponding property or <code>null</code>
     */
    public ConfigProperty getProperty(String property) {
        ConfigProperty propertyObj = null;
        for (ConfigProperty tmp : properties) {
            if (tmp.getName().equals(property)) {
                propertyObj = tmp;
                break;
            }
        }
        return propertyObj;
    }

    /**
     * Returns the user comment for this module.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the user comment for this module.
     *
     * @param comment
     *            the comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the severity level of this module.
     *
     * @return the severity level
     */
    public Severity getSeverity() {
        Severity severity = null;
        if (metaData != null && metaData.hasSeverity()) {
            if (severityLevel != null) {
                severity = severityLevel;
            }
            else {
                severity = getMetaData().defaultSeverity();
            }
        }
        return severity;
    }

    /**
     * Returns the last severity level before the module was set to ignore.
     *
     * @return the last severity level
     */
    public Severity getLastEnabledSeverity() {
        return lastEnabledSeverity;
    }

    /**
     * Sets the last enabled severity. This is used to restore the original severity setting after
     * the module has been set to ignore.
     *
     * @param severity
     *            the severity
     */
    public void setLastEnabledSeverity(Severity severity) {
        lastEnabledSeverity = severity;
    }

    /**
     * Sets the severity level.
     *
     * @param severity
     *            the severity level to set
     */
    public void setSeverity(Severity severity) {

        Severity defaultLevel = null;

        if (metaData != null && metaData.hasSeverity()) {
            defaultLevel = getMetaData().defaultSeverity();
        }
        else if (metaData == null) {
            defaultLevel = MetadataFactory.getDefaultSeverity();
        }

        if (defaultLevel != null) {
            if (severity == defaultLevel) {
                severityLevel = null;
                setLastEnabledSeverity(null);
            }
            else if (Severity.IGNORE == severity) {
                if (severityLevel != null && Severity.IGNORE != severityLevel) {
                    setLastEnabledSeverity(severityLevel);
                }

                severityLevel = severity;
            }
            else {
                severityLevel = severity;
            }
        }
    }

    /**
     * Return the map containing custom metadata for the module.
     *
     * @return the custom metadata
     */
    public Map<String, String> getCustomMetaData() {
        return customMetaData;
    }

}
