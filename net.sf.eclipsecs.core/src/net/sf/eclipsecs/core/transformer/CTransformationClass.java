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

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class which all transformationclasses have to implement. These classes handle how to
 * react on a checkstyle-rule.
 * 
 * @author Lukas Frena
 */
public abstract class CTransformationClass {
  /** The eclipse-configuration for this rule. */
  private final FormatterConfiguration mFormatterSetting = new FormatterConfiguration();

  /** The map of attributes of this rule. */
  private final Map<String, String> mAttributes = new HashMap<>();

  /**
   * Method for transforming the checkstyle-rule this class is associated to. Every
   * transformationclass has to implement this method. Should return the field formatterSetting
   * after adding rules to it.
   * 
   * @return The eclipse-formatter-configuration this rule needs.
   */
  public abstract FormatterConfiguration transformRule();

  /**
   * Method for setting the field attributes.
   * 
   * @param rule
   *          The checkstyle-rule associated to this class.
   */
  protected final void setRule(final Configuration rule) {
    final String[] attrs = rule.getAttributeNames();
    for (final String att : attrs) {
      try {
        mAttributes.put(att, rule.getAttribute(att));
      } catch (CheckstyleException e) {
        // shouldn't happen since we only use existing attribute names
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Method for reading the value of an attribute.
   * 
   * @param attribute
   *          The name of the attribute.
   * @return The value of the attribute.
   */
  public final String getAttribute(final String attribute) {
    return mAttributes.get(attribute);
  }

  /**
   * Method for defining which eclipse-formatter-rules should be used for transforming.
   * 
   * @param rule
   *          A eclipse-formatter-rule.
   * @param val
   *          The value for the rule.
   */
  public final void userFormatterSetting(final String rule, final String val) {
    mFormatterSetting.addFormatterSetting("org.eclipse.jdt.core.formatter." + rule, val);
  }

  /**
   * Method for defining which eclipse-editor-rules should be used for transforming.
   * 
   * @param rule
   *          A eclipse-editor-rule.
   * @param val
   *          The value for the rule.
   */
  public final void useCleanupSetting(final String rule, final String val) {
    mFormatterSetting.addCleanupSetting("cleanup." + rule, val);
  }

  /**
   * Getter to return the current formatter-settings.
   * 
   * @return The formatter-settings.
   */
  public FormatterConfiguration getFormatterSetting() {
    return mFormatterSetting;
  }
}
