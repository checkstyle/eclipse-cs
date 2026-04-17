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

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.eclipsecs.core.transformer.CTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule WhitespaceAround to appropriate
 * eclipse-formatter-rules.
 *
 */
public class WhitespaceAroundTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    // TODO token SLIST TYPE_EXTENSION_AND
    // LITERAL_ASSERT/DO/ELSE/FINALLY/TRY
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "ASSIGN, BAND, BAND_ASSIGN, BOR, BOR_ASSIGN, BSR, BSR_ASSIGN, BXOR, BXOR_ASSIGN, COLON, DIV, "
              + "DIV_ASSIGN, EQUAL, GE, GT, LAND, LCURLY, LE, LITERAL_ASSERT, LITERAL_CATCH, LITERAL_DO, "
              + "LITERAL_ELSE, LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_RETURN, LITERAL_SYNCHRONIZED, "
              + "LITERAL_TRY, LITERAL_WHILE, LOR, LT, MINUS, MINUS_ASSIGN, MOD, MOD_ASSIGN, NOT_EQUAL, PLUS, "
              + "PLUS_ASSIGN, QUESTION, RCURLY, SL, SLIST, SL_ASSIGN, SR, SR_ASSIGN, STAR, STAR_ASSIGN";
    }
    final StringTokenizer token = new StringTokenizer(tokens, ", ");

    while (token.hasMoreTokens()) {
      List<String> settings = switch (token.nextToken()) {
        case "ASSIGN", "BAND_ASSIGN", "BOR_ASSIGN", "BSR_ASSIGN",
             "BXOR_ASSIGN", "DIV_ASSIGN", "MINUS_ASSIGN", "MOD_ASSIGN",
             "PLUS_ASSIGN", "SL_ASSIGN", "SR_ASSIGN", "STAR_ASSIGN" -> List.of(
                "insert_space_after_assignment_operator",
                "insert_space_before_assignment_operator");
        case "BAND", "BOR", "BSR", "BXOR", "DIV", "EQUAL",
             "GE", "GT", "LAND", "LE", "LOR", "LT", "MINUS",
             "MOD", "NOT_EQUAL", "PLUS", "SL", "SR", "STAR" -> List.of(
                "insert_space_after_binary_operator",
                "insert_space_before_binary_operator");
        case "COLON" -> List.of(
                "insert_space_before_colon_in_for",
                "insert_space_after_colon_in_for",
                "insert_space_before_colon_in_conditional",
                "insert_space_after_colon_in_conditional");
        case "QUESTION" -> List.of(
                "insert_space_before_question_in_conditional",
                "insert_space_after_question_in_conditional");
        case "LCURLY" -> List.of(
                "insert_space_before_opening_brace_in_type_declaration",
                "insert_space_after_opening_brace_in_array_initializer",
                "insert_space_before_opening_brace_in_annotation_type_declaration",
                "insert_space_before_opening_brace_in_block",
                "insert_space_before_opening_brace_in_method_declaration",
                "insert_space_before_opening_brace_in_enum_declaration",
                "insert_space_before_opening_brace_in_constructor_declaration",
                "insert_space_before_opening_brace_in_enum_constant",
                "insert_space_before_opening_brace_in_switch",
                "insert_space_before_opening_brace_in_anonymous_type_declaration",
                "insert_space_before_opening_brace_in_array_initializer");
        case "RCURLY" -> List.of(
                "insert_space_after_closing_brace_in_block",
                "insert_space_before_closing_brace_in_array_initializer");
        case "LITERAL_CATCH" -> List.of("insert_space_before_opening_paren_in_catch");
        case "LITERAL_FOR" -> List.of("insert_space_before_opening_paren_in_for");
        case "LITERAL_IF" -> List.of("insert_space_before_opening_paren_in_if");
        case "LITERAL_RETURN" -> List.of("insert_space_before_parenthesized_expression_in_return");
        case "LITERAL_SYNCHRONIZED" -> List.of(
                "insert_space_before_opening_paren_in_synchronized");
        case "LITERAL_WHILE" -> List.of("insert_space_before_opening_paren_in_while");
        default -> Collections.emptyList();
      };
      settings.forEach(setting -> userFormatterSetting(setting, "insert"));
    }
    return getFormatterSetting();
  }
}
