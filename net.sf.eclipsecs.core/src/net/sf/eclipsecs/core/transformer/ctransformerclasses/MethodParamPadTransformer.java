//============================================================================
//
// Copyright (C) 2003-2023  Lukas Frena
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

package net.sf.eclipsecs.core.transformer.ctransformerclasses;

import net.sf.eclipsecs.core.transformer.AbstractCheckstyleTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule MethodParamPad to appropriate
 * eclipse-formatter-rules.
 *
 */
public class MethodParamPadTransformer extends AbstractCheckstyleTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String option = getAttribute("option");
    if (option == null) {
      option = "nospace";
    }
    String space = switch (option) {
      case "space" -> "insert";
      default -> "do not insert";
    };

    String val = getAttribute("tokens");
    if (val == null) {
      val = "CTOR_DEF, CTOR_CALL, LITERAL_NEW, METHOD_CALL, METHOD_DEF, SUPER_CTOR_CALL, "
              + "ENUM_CONSTANT_DEF, RECORD_DEF, RECORD_PATTERN_DEF";
    }

    for (String token : val.split("\\s*,\\s*")) {
      switch (token) {
        case "CTOR_DEF" -> userFormatterSetting(
                "insert_space_before_opening_paren_in_constructor_declaration", space);
        case "METHOD_CALL", "SUPER_CTOR_CALL", "CTOR_CALL", "LITERAL_NEW" -> userFormatterSetting(
                "insert_space_before_opening_paren_in_method_invocation", space);
        case "METHOD_DEF" -> userFormatterSetting(
                "insert_space_before_opening_paren_in_method_declaration", space);
        case "ENUM_CONSTANT_DEF" -> userFormatterSetting(
                "insert_space_before_opening_paren_in_enum_constant", space);
        case "RECORD_DEF" -> userFormatterSetting(
                "insert_space_before_opening_paren_in_record_declaration", space);
        default -> {
          // nothing to transform
        }
      }
    }
    return getFormatterSetting();
  }

}
