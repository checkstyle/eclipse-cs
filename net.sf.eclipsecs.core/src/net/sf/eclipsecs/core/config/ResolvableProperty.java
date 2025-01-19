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

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents a configuration property who's value must be resolved.
 *
 */
public class ResolvableProperty implements Cloneable {

  /** The name of the property. */
  private String propertyName;

  /** The property value. */
  private String value;

  /**
   * Creates a resolvable property.
   *
   * @param propertyName
   *          the name of the property
   * @param value
   *          the value of the property
   */
  public ResolvableProperty(String propertyName, String value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  /**
   * @return The value of the property.
   */
  public String getValue() {
    return value;
  }

  /**
   * @return The property's name.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * @param value
   *          Value for the property.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @param propertyName
   *          The property's name.
   */
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ResolvableProperty)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    ResolvableProperty rhs = (ResolvableProperty) obj;
    return Objects.equals(propertyName, rhs.propertyName) && Objects.equals(value, rhs.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(propertyName, value);
  }

  @Override
  public ResolvableProperty clone() {
    try {
      return (ResolvableProperty) super.clone();
    } catch (CloneNotSupportedException ex) {
      // should never happen
      throw new InternalError(ex);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("propertyName", propertyName).add("value", value)
            .toString();
  }
}
