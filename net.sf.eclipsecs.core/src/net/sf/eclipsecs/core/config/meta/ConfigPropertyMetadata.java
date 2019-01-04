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

package net.sf.eclipsecs.core.config.meta;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsecs.core.Messages;

/**
 * This class represents metadata about one of a rule's properties.
 */
public class ConfigPropertyMetadata {

  /** The type of the property data. */
  private ConfigPropertyType mDatatype;

  /** The name of the property. */
  private String mName;

  /** The default value of the property. */
  private String mDefaultValue;

  /** A differing default value from the Checkstyle core. */
  private String mOverrideDefaultValue;

  /** The description of the property. */
  private String mDescription;

  /** The list of possible property values. */
  private List<String> mEnumeration = new ArrayList<>();

  /**
   * Creates the property metadata.
   * 
   * @param type
   *          the property type
   * @param name
   *          the name of the property
   * @param defaultValue
   *          the default value
   * @param overrideDefaultValue
   *          a default value which overrides the Checkstyle default
   */
  public ConfigPropertyMetadata(ConfigPropertyType type, String name, String defaultValue,
          String overrideDefaultValue) {
    mDatatype = type;
    mName = name;
    mDefaultValue = defaultValue;
    mOverrideDefaultValue = overrideDefaultValue;
    mDescription = Messages.ConfigPropertyMetadata_txtNoDescription;
  }

  /**
   * Get the property's datatype.
   * 
   * @return The datatype
   */
  public ConfigPropertyType getDatatype() {
    return mDatatype;
  }

  /**
   * Get the property's name.
   * 
   * @return The name
   */
  public String getName() {
    return mName;
  }

  /**
   * Get the property's description.
   * 
   * @return The description
   */
  public String getDescription() {
    return mDescription;
  }

  /**
   * Sets the description of this property.
   * 
   * @param description
   *          the description
   */
  public void setDescription(String description) {
    mDescription = description;
  }

  /**
   * Get the default value.
   * 
   * @return The default value
   */
  public String getDefaultValue() {
    return mDefaultValue;
  }

  /**
   * Returns a default value differing from the Checkstye default for this property.
   * 
   * @return The differing checkstyle default value.
   */
  public String getOverrideDefault() {
    return mOverrideDefaultValue;
  }

  /**
   * Get the enumeration of allowable values.
   * 
   * @return Enumeration of values
   */
  public List<String> getPropertyEnumeration() {
    return mEnumeration;
  }

  /**
   * Returns the hidden.
   * 
   * @return boolean
   */
  public boolean isHidden() {
    return ConfigPropertyType.Hidden.equals(mDatatype);
  }

}