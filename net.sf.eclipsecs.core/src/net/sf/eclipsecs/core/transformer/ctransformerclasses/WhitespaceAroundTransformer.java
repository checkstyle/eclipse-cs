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

package net.sf.eclipsecs.core.transformer.ctransformerclasses;

import java.util.StringTokenizer;

import net.sf.eclipsecs.core.transformer.CTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule WhitespaceAround to appropriate
 * eclipse-formatter-rules.
 *
 * @author Lukas Frena
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
      if (tok.equals("ASSIGN") || tok.equals("BAND_ASSIGN") || tok.equals("BOR_ASSIGN")
              || tok.equals("BSR_ASSIGN") || tok.equals("BXOR_ASSIGN") || tok.equals("DIV_ASSIGN")
              || tok.equals("MINUS_ASSIGN") || tok.equals("MOD_ASSIGN") || tok.equals("PLUS_ASSIGN")
              || tok.equals("SL_ASSIGN") || tok.equals("SR_ASSIGN") || tok.equals("STAR_ASSIGN")) {
        userFormatterSetting("insert_space_after_assignment_operator", "insert");
        userFormatterSetting("insert_space_before_assignment_operator", "insert");
      } else if ((tok.equals("BAND") || tok.equals("BOR") || tok.equals("BSR") || tok.equals("BXOR")
              || tok.equals("DIV") || tok.equals("EQUAL") || tok.equals("GE") || tok.equals("GT")
              || tok.equals("LAND") || tok.equals("LE") || tok.equals("LOR") || tok.equals("LT")
              || tok.equals("MINUS") || tok.equals("MOD") || tok.equals("NOT_EQUAL")
              || tok.equals("PLUS") || tok.equals("SL") || tok.equals("SR")
              || tok.equals("STAR"))) {
        userFormatterSetting("insert_space_after_binary_operator", "insert");
        userFormatterSetting("insert_space_before_binary_operator", "insert");
      } else if (tok.equals("COLON")) {
        userFormatterSetting("insert_space_before_colon_in_for", "insert");
        userFormatterSetting("insert_space_after_colon_in_for", "insert");
        userFormatterSetting("insert_space_before_colon_in_conditional", "insert");
        userFormatterSetting("insert_space_after_colon_in_conditional", "insert");
      } else if (tok.equals("QUESTION")) {
        userFormatterSetting("insert_space_after_question_in_conditional", "insert");
        userFormatterSetting("insert_space_before_question_in_conditional", "insert");
      } else if (tok.equals("LCURLY")) {
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
      } else if (tok.equals("RCURLY")) {
        userFormatterSetting("insert_space_after_closing_brace_in_block", "insert");
        userFormatterSetting("insert_space_before_closing_brace_in_array_initializer", "insert");
      } else if (tok.equals("LITERAL_CATCH")) {
        userFormatterSetting("insert_space_before_opening_paren_in_catch", "insert");
      } else if (tok.equals("LITERAL_FOR")) {
        userFormatterSetting("insert_space_before_opening_paren_in_for", "insert");
      } else if (tok.equals("LITERAL_IF")) {
        userFormatterSetting("insert_space_before_opening_paren_in_if", "insert");
      } else if (tok.equals("LITERAL_RETURN")) {
        userFormatterSetting("insert_space_before_parenthesized_expression_in_return", "insert");
      } else if (tok.equals("LITERAL_SYNCHRONIZED")) {
        userFormatterSetting("insert_space_before_opening_paren_in_synchronized", "insert");
      } else if (tok.equals("LITERAL_WHILE")) {
        userFormatterSetting("insert_space_before_opening_paren_in_while", "insert");
      }
    }
    return getFormatterSetting();
  }
}
