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

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule NoWhitespaceBefore to appropriate
 * eclipse-formatter-rules.
 */
public class NoWhitespaceBeforeTransformer extends AbstractCTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("tokens");
    if (val == null) {
      val = "COMMA, SEMI, POST_INC, POST_DEC, ELLIPSIS, LABELED_STAT";
    }

    for (String token : val.split("\\s*,\\s*")) {
      List<String> settings = switch (token) {
        case "POST_INC", "POST_DEC" ->
          List.of("insert_space_before_postfix_operator");
        case "COMMA" ->
          List.of("insert_space_before_comma_in_allocation_expression",
                  "insert_space_before_comma_in_annotation",
                  "insert_space_before_comma_in_array_initializer",
                  "insert_space_before_comma_in_constructor_declaration_parameters",
                  "insert_space_before_comma_in_constructor_declaration_throws",
                  "insert_space_before_comma_in_enum_constant_arguments",
                  "insert_space_before_comma_in_enum_declarations",
                  "insert_space_before_comma_in_explicitconstructorcall_arguments",
                  "insert_space_before_comma_in_for_increments",
                  "insert_space_before_comma_in_for_inits",
                  "insert_space_before_comma_in_method_declaration_parameters",
                  "insert_space_before_comma_in_method_declaration_throws",
                  "insert_space_before_comma_in_method_invocation_arguments",
                  "insert_space_before_comma_in_multiple_field_declarations",
                  "insert_space_before_comma_in_multiple_local_declarations",
                  "insert_space_before_comma_in_parameterized_type_reference",
                  "insert_space_before_comma_in_permitted_types",
                  "insert_space_before_comma_in_record_components",
                  "insert_space_before_comma_in_superinterfaces",
                  "insert_space_before_comma_in_switch_case_expressions",
                  "insert_space_before_comma_in_type_arguments",
                  "insert_space_before_comma_in_type_parameters");
        case "SEMI" ->
          List.of("insert_space_before_semicolon",
                  "insert_space_before_semicolon_in_for",
                  "insert_space_before_semicolon_in_try_resources");
        case "ELLIPSIS" ->
          List.of("insert_space_before_ellipsis");
        case "LABELED_STAT" ->
          List.of("insert_space_before_colon_in_labeled_statement");
        case "GENERIC_START" ->
          List.of("insert_space_before_opening_angle_bracket_in_parameterized_type_reference",
                  "insert_space_before_opening_angle_bracket_in_type_arguments",
                  "insert_space_before_opening_angle_bracket_in_type_parameters");
        case "GENERIC_END" ->
          List.of("insert_space_before_closing_angle_bracket_in_parameterized_type_reference",
                  "insert_space_before_closing_angle_bracket_in_type_arguments",
                  "insert_space_before_closing_angle_bracket_in_type_parameters");
        default -> List.of();
      };
      settings.forEach(setting -> userFormatterSetting(setting, "do not insert"));
    }
    return getFormatterSetting();
  }

}
