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

import java.util.List;

import net.sf.eclipsecs.core.transformer.AbstractCheckstyleTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule ParenPad to appropriate eclipse-formatter-rules.
 *
 */
public class ParenPadTransformer extends AbstractCheckstyleTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "CTOR_CALL, LPAREN, METHOD_CALL, RPAREN, SUPER_CTOR_CALL";
    }
    String option = getAttribute("option");
    if (option == null) {
      option = "nospace";
    }
    String value = switch (option) {
      case "nospace" -> "do not insert";
      default -> "insert";
    };

    for (String token : tokens.split("\\s*,\\s*")) {
      List<String> settings = switch (token) {
        case "LPAREN" -> List.of("insert_space_after_opening_paren_in_parenthesized_expression",
                "insert_space_after_opening_paren_in_while",
                "insert_space_after_opening_paren_in_for",
                "insert_space_after_opening_paren_in_if",
                "insert_space_after_opening_paren_in_switch",
                "insert_space_after_opening_paren_in_synchronized",
                "insert_space_after_opening_paren_in_catch",
                "insert_space_after_opening_paren_in_method_invocation",
                "insert_space_after_opening_paren_in_annotation",
                "insert_space_after_opening_paren_in_constructor_declaration",
                "insert_space_after_opening_paren_in_enum_constant",
                "insert_space_after_opening_paren_in_method_declaration");
        case "RPAREN" -> List.of("insert_space_before_closing_paren_in_parenthesized_expression",
                "insert_space_before_closing_paren_in_while",
                "insert_space_before_closing_paren_in_for",
                "insert_space_before_closing_paren_in_if",
                "insert_space_before_closing_paren_in_switch",
                "insert_space_before_closing_paren_in_synchronized",
                "insert_space_before_closing_paren_in_catch",
                "insert_space_before_closing_paren_in_method_invocation",
                "insert_space_before_closing_paren_in_method_declaration",
                "insert_space_before_closing_paren_in_constructor_declaration",
                "insert_space_before_closing_paren_in_enum_constant",
                "insert_space_before_closing_paren_in_annotation");
        case "CTOR_CALL", "METHOD_CALL", "SUPER_CTOR_CALL" -> List.of(
                "insert_space_before_closing_paren_in_method_invocation",
                "insert_space_after_opening_paren_in_method_invocation");
        default -> List.of();
      };
      settings.forEach(setting -> userFormatterSetting(setting, value));
    }
    return getFormatterSetting();
  }

}
