//============================================================================
//
// Copyright (C) 2009 Lukas Frena
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

package net.sf.eclipsecs.core.transformer;

import java.util.HashMap;

/**
 * Abstract class which all transformationclasses have to implement. These classes handle how to
 * react on a formatter-setting.
 * 
 * @author Lukas Frena
 */
public abstract class FTransformationClass {
  /** The checkstyle-configuration for this rule. */
  private final CheckstyleSetting mCheckstyleSetting = new CheckstyleSetting();

  /** The value of this rule. */
  private String mValue;

  /**
   * Method for transforming the checkstyle-rule this class is associated to. Every
   * transformationclass has to implement this method. Should return the field formatterSetting
   * after adding rules to it.
   * 
   * @return The eclipse-formatter-configuration this rule needs.
   */
  public abstract CheckstyleSetting transformRule();

  /**
   * Method for setting the field mValue.
   * 
   * @param setting
   *          The formatter-setting associated to this class.
   * @param value
   *          The value of the setting.
   */
  protected final void setValue(final String value) {
    mValue = value;
  }

  /**
   * Method for reading the value the setting.
   * 
   * @return The value of the setting.
   */
  public final String getValue() {
    return mValue;
  }

  /**
   * Method for defining which checkstyle-rules should be used for transforming.
   * 
   * @param name
   *          The name of the checkstyle treewalker-module.
   * @param properties
   *          Properties of the module.
   */
  public final void useTreeWalkerModule(final String name,
          final HashMap<String, String> properties) {

    mCheckstyleSetting.addTreeWalkerModule(name, properties);
  }

  /**
   * Method for defining which checkstyle-rules should be used for transforming.
   * 
   * @param name
   *          The name of the checkstyle treewalker-module.
   * @param properties
   *          Properties of the module.
   */
  public final void useCheckerModule(final String name, final HashMap<String, String> properties) {

    mCheckstyleSetting.addCheckerModule(name, properties);
  }

  /**
   * Getter to return the current checkstyle-settings.
   * 
   * @return The formatter-settings.
   */
  public CheckstyleSetting getCheckstyleSetting() {
    return mCheckstyleSetting;
  }
}
