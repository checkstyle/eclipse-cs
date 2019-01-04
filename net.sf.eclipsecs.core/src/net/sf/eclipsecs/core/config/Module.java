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
 * @author Lars Ködderitzsch
 */
public class Module implements Cloneable {

  //
  // attributes
  //

  /** the name of the module. */
  private String mName;

  /** the meta data associated with the module. */
  private RuleMetadata mMetaData;

  /** the properties of the modules. */
  private List<ConfigProperty> mProperties = new ArrayList<>();

  /** the comment of the module. */
  private String mComment;

  /** the id of the module. */
  private String mId;

  /** the custom messages for this module. */
  private final Map<String, String> mCustomMessages = new HashMap<>();

  /** the severity level. */
  private Severity mSeverityLevel = Severity.inherit;

  /** the last severity level before setting to ignored. */
  private Severity mLastEnabledSeverity;

  /** map containing unknown custom metadata of the module. */
  private final Map<String, String> mCustomMetaData = new HashMap<>();

  //
  // constructors
  //

  /**
   * Creates a module with the according meta data.
   * 
   * @param metaData
   *          the meta data
   * @param withDefaults
   *          determines if the properties should be initialized with the Checkstyle default values
   */
  public Module(RuleMetadata metaData, boolean withDefaults) {
    mMetaData = metaData;

    if (metaData != null) {

      // create the properties according to the meta data
      List<ConfigPropertyMetadata> propMetas = metaData.getPropertyMetadata();
      int size = propMetas != null ? propMetas.size() : 0;
      for (int i = 0; i < size; i++) {

        ConfigPropertyMetadata propMeta = propMetas.get(i);
        ConfigProperty property = new ConfigProperty(propMeta);
        getProperties().add(property);

        if (withDefaults) {
          property.setValue(propMeta.getDefaultValue());
        }
      }

      if (metaData.getDefaultSeverityLevel() != null) {
        mSeverityLevel = metaData.getDefaultSeverityLevel();
      }
    }
  }

  /**
   * Create a module without meta data.
   * 
   * @param name
   *          the name of the module
   */
  public Module(String name) {
    mName = name;
  }

  /**
   * Returns the name of the module.
   * 
   * @return the name of the module
   */
  public String getName() {
    return mMetaData != null ? mMetaData.getRuleName() : mName;
  }

  /**
   * Returns the unique id of the module.
   * 
   * @return the id
   */
  public String getId() {
    return mId;
  }

  /**
   * Sets the unique id of the module.
   * 
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    mId = id;
  }

  /**
   * Returns the custom messagess to display instead of Checkstyle's default message.
   * 
   * @return the customMessage
   */
  public Map<String, String> getCustomMessages() {
    return mCustomMessages;
  }

  /**
   * Returns the meta data associated with the module.
   * 
   * @return the meta data of the module
   */
  public RuleMetadata getMetaData() {
    return mMetaData;
  }

  /**
   * Sets the meta data of the module.
   * 
   * @param metaData
   *          the meta data
   */
  public void setMetaData(RuleMetadata metaData) {
    mMetaData = metaData;
  }

  /**
   * Returns all properties of this module.
   * 
   * @return the properties
   */
  public List<ConfigProperty> getProperties() {
    return mProperties;
  }

  /**
   * Returns the property data for a given property name.
   * 
   * @param property
   *          the property name
   * @return the coresponding property or <code>null</code>
   */
  public ConfigProperty getProperty(String property) {

    ConfigProperty propertyObj = null;

    int size = mProperties != null ? mProperties.size() : 0;
    for (int i = 0; i < size; i++) {
      ConfigProperty tmp = mProperties.get(i);

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
    return mComment;
  }

  /**
   * Sets the user comment for this module.
   * 
   * @param comment
   *          the comment
   */
  public void setComment(String comment) {
    mComment = comment;
  }

  /**
   * Returns the severity level of this module.
   * 
   * @return the severity level
   */
  public Severity getSeverity() {
    if (mMetaData != null && mMetaData.hasSeverity()) {

      return mSeverityLevel != null ? mSeverityLevel : getMetaData().getDefaultSeverityLevel();
    } else {
      return null;
    }
  }

  /**
   * Returns the last severity level before the module was set to ignore.
   * 
   * @return the last severity level
   */
  public Severity getLastEnabledSeverity() {
    return mLastEnabledSeverity;
  }

  /**
   * Sets the last enabled severity. This is used to restore the original severity setting after the
   * module has been set to ignore.
   * 
   * @param severity
   *          the severity
   */
  public void setLastEnabledSeverity(Severity severity) {
    mLastEnabledSeverity = severity;
  }

  /**
   * Sets the severity level.
   * 
   * @param severityLevel
   *          the severity level to set
   */
  public void setSeverity(Severity severityLevel) {

    Severity defaultLevel = null;

    if (mMetaData != null && mMetaData.hasSeverity()) {
      defaultLevel = getMetaData().getDefaultSeverityLevel();
    } else if (mMetaData == null) {
      defaultLevel = MetadataFactory.getDefaultSeverity();
    }

    if (defaultLevel != null) {
      if (severityLevel.equals(defaultLevel)) {
        mSeverityLevel = null;
        setLastEnabledSeverity(null);
      } else if (Severity.ignore.equals(severityLevel)) {
        if (mSeverityLevel != null && !Severity.ignore.equals(mSeverityLevel)) {
          setLastEnabledSeverity(mSeverityLevel);
        }

        mSeverityLevel = severityLevel;
      } else {
        mSeverityLevel = severityLevel;
      }
    }
  }

  /**
   * Return the map containing custom metadata for the module.
   * 
   * @return the custom metadata
   */
  public Map<String, String> getCustomMetaData() {
    return mCustomMetaData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Module clone() {
    try {
      Module clone = (Module) super.clone();
      clone.mProperties = new ArrayList<>();

      for (ConfigProperty prop : mProperties) {
        clone.getProperties().add(prop.clone());
      }

      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(); // should not happen
    }
  }
}