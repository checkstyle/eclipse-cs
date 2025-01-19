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
    String tok;

    while (token.hasMoreTokens()) {
      tok = token.nextToken();
      if ("ASSIGN".equals(tok) || "BAND_ASSIGN".equals(tok) || "BOR_ASSIGN".equals(tok)
              || "BSR_ASSIGN".equals(tok) || "BXOR_ASSIGN".equals(tok) || "DIV_ASSIGN".equals(tok)
              || "MINUS_ASSIGN".equals(tok) || "MOD_ASSIGN".equals(tok) || "PLUS_ASSIGN".equals(tok)
              || "SL_ASSIGN".equals(tok) || "SR_ASSIGN".equals(tok) || "STAR_ASSIGN".equals(tok)) {
        userFormatterSetting("insert_space_after_assignment_operator", "insert");
        userFormatterSetting("insert_space_before_assignment_operator", "insert");
      } else if (("BAND".equals(tok) || "BOR".equals(tok) || "BSR".equals(tok) || "BXOR".equals(tok)
              || "DIV".equals(tok) || "EQUAL".equals(tok) || "GE".equals(tok) || "GT".equals(tok)
              || "LAND".equals(tok) || "LE".equals(tok) || "LOR".equals(tok) || "LT".equals(tok)
              || "MINUS".equals(tok) || "MOD".equals(tok) || "NOT_EQUAL".equals(tok)
              || "PLUS".equals(tok) || "SL".equals(tok) || "SR".equals(tok)
              || "STAR".equals(tok))) {
        userFormatterSetting("insert_space_after_binary_operator", "insert");
        userFormatterSetting("insert_space_before_binary_operator", "insert");
      } else if ("COLON".equals(tok)) {
        userFormatterSetting("insert_space_before_colon_in_for", "insert");
        userFormatterSetting("insert_space_after_colon_in_for", "insert");
        userFormatterSetting("insert_space_before_colon_in_conditional", "insert");
        userFormatterSetting("insert_space_after_colon_in_conditional", "insert");
      } else if ("QUESTION".equals(tok)) {
        userFormatterSetting("insert_space_after_question_in_conditional", "insert");
        userFormatterSetting("insert_space_before_question_in_conditional", "insert");
      } else if ("LCURLY".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_brace_in_type_declaration", "insert");
        userFormatterSetting("insert_space_after_opening_brace_in_array_initializer", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_annotation_type_declaration",
                "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_block", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_method_declaration", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_enum_declaration", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_constructor_declaration",
                "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_enum_constant", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_switch", "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_anonymous_type_declaration",
                "insert");
        userFormatterSetting("insert_space_before_opening_brace_in_array_initializer", "insert");
      } else if ("RCURLY".equals(tok)) {
        userFormatterSetting("insert_space_after_closing_brace_in_block", "insert");
        userFormatterSetting("insert_space_before_closing_brace_in_array_initializer", "insert");
      } else if ("LITERAL_CATCH".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_paren_in_catch", "insert");
      } else if ("LITERAL_FOR".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_paren_in_for", "insert");
      } else if ("LITERAL_IF".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_paren_in_if", "insert");
      } else if ("LITERAL_RETURN".equals(tok)) {
        userFormatterSetting("insert_space_before_parenthesized_expression_in_return", "insert");
      } else if ("LITERAL_SYNCHRONIZED".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_paren_in_synchronized", "insert");
      } else if ("LITERAL_WHILE".equals(tok)) {
        userFormatterSetting("insert_space_before_opening_paren_in_while", "insert");
      }
    }
    return getFormatterSetting();
  }
}
