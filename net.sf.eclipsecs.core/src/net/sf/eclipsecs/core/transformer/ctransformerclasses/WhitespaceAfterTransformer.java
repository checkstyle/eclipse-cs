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
 * Wrapper class for converting the checkstyle-rule WhitespaceAfter to appropriate
 * eclipse-formatter-rules.
 *
 */
public class WhitespaceAfterTransformer extends AbstractCheckstyleTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "COMMA, DO_WHILE, ELLIPSIS, LAMBDA, LITERAL_CATCH, LITERAL_DO, LITERAL_ELSE, "
              + "LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_RETURN, LITERAL_SWITCH, "
              + "LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_WHEN, LITERAL_WHILE, "
              + "LITERAL_YIELD, SEMI, TYPECAST";
    }

    for (String token : tokens.split("\\s*,\\s*")) {
      List<String> settings = switch (token) {
        case "COMMA" -> List.of("insert_space_after_comma_in_allocation_expression",
                "insert_space_after_comma_in_annotation",
                "insert_space_after_comma_in_array_initializer",
                "insert_space_after_comma_in_constructor_declaration_parameters",
                "insert_space_after_comma_in_constructor_declaration_throws",
                "insert_space_after_comma_in_enum_constant_arguments",
                "insert_space_after_comma_in_enum_declarations",
                "insert_space_after_comma_in_explicitconstructorcall_arguments",
                "insert_space_after_comma_in_for_increments",
                "insert_space_after_comma_in_for_inits",
                "insert_space_after_comma_in_method_declaration_parameters",
                "insert_space_after_comma_in_method_declaration_throws",
                "insert_space_after_comma_in_method_invocation_arguments",
                "insert_space_after_comma_in_multiple_field_declarations",
                "insert_space_after_comma_in_multiple_local_declarations",
                "insert_space_after_comma_in_parameterized_type_reference",
                "insert_space_after_comma_in_permitted_types",
                "insert_space_after_comma_in_record_components",
                "insert_space_after_comma_in_superinterfaces",
                "insert_space_after_comma_in_switch_case_expressions",
                "insert_space_after_comma_in_type_arguments",
                "insert_space_after_comma_in_type_parameters");
        case "SEMI" -> List.of("insert_space_after_semicolon_in_for",
                "insert_space_after_semicolon_in_try_resources");
        case "TYPECAST" -> List.of("insert_space_after_closing_paren_in_cast");
        case "LITERAL_IF" -> List.of("insert_space_before_opening_paren_in_if");
        case "LITERAL_FOR" -> List.of("insert_space_before_opening_paren_in_for");
        case "LITERAL_WHILE", "DO_WHILE" ->
          List.of("insert_space_before_opening_paren_in_while");
        case "LITERAL_CATCH" -> List.of("insert_space_before_opening_paren_in_catch");
        case "LITERAL_SWITCH" -> List.of("insert_space_before_opening_paren_in_switch");
        case "LITERAL_SYNCHRONIZED" ->
          List.of("insert_space_before_opening_paren_in_synchronized");
        case "LITERAL_TRY" -> List.of("insert_space_before_opening_paren_in_try");
        case "LITERAL_RETURN" ->
          List.of("insert_space_before_parenthesized_expression_in_return");
        case "LAMBDA" -> List.of("insert_space_after_lambda_arrow");
        case "ELLIPSIS" -> List.of("insert_space_after_ellipsis");
        default -> List.of();
      };
      settings.forEach(setting -> userFormatterSetting(setting, "insert"));
    }
    return getFormatterSetting();
  }

}
