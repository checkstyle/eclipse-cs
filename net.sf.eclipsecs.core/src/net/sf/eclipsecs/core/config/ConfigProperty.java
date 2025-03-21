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

import net.sf.eclipsecs.core.config.meta.ConfigPropertyMetadata;

/**
 * A simple configuration consisting of a name/value pair.
 */
public class ConfigProperty implements Comparable<ConfigProperty>, Cloneable {

  /** The name of the property. */
  private String name;

  /** The value of the property. */
  private String value;

  /** The meta data of the property. */
  private ConfigPropertyMetadata metaData;

  /** Signals that the property value is actually a ${}-like reference. */
  private boolean propertyReference;

  /**
   * Constructor.
   *
   * @param metaData
   *          the property meta data
   */
  public ConfigProperty(ConfigPropertyMetadata metaData) {
    this(metaData.getName(), metaData.getOverrideDefault() != null ? metaData.getOverrideDefault()
            : metaData.getDefaultValue());
    this.metaData = metaData;
  }

  /**
   * Constructor.
   *
   * @param name
   *          Property name.
   * @param value
   *          Property value.
   */
  public ConfigProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Get the property's name.
   *
   * @return The name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the property's name.
   *
   * @param name
   *          The new name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the value.
   *
   * @return String
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value
   *          The value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the meta data for this property.
   *
   * @return the meta data
   */
  public ConfigPropertyMetadata getMetaData() {
    return metaData;
  }

  /**
   * Sets the meta data for this property.
   *
   * @param metaData
   *          the meta data
   */
  public void setMetaData(ConfigPropertyMetadata metaData) {
    this.metaData = metaData;
  }

  /**
   * Returns if the property value is/contains a ${}-style property reference.
   *
   * @return <code>true</code> if the value is a property reference, <code>false</code> otherwise
   */
  public boolean isPropertyReference() {
    return propertyReference;
  }

  /**
   * Sets if the property value is/contains a ${}-style property reference.
   *
   * @param propertyReference
   *          <code>true</code> if the value is a property reference, <code>false</code> otherwise
   */
  public void setPropertyReference(boolean propertyReference) {
    this.propertyReference = propertyReference;
  }

  @Override
  public int compareTo(ConfigProperty obj) {
    return this.name.compareTo(obj.name);
  }

  @Override
  public ConfigProperty clone() {
    try {
      return (ConfigProperty) super.clone();
    } catch (CloneNotSupportedException ex) {
      // Should not happen
      throw new InternalError(ex);
    }
  }
}
