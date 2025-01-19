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
 * Wrapperclass for converting the checkstyle-rule ParenPad to appropriate eclipse-formatter-rules.
 *
 */
public class ParenPadTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String tokenString = getAttribute("tokens");
    if (tokenString == null) {
      tokenString = "CTOR_CALL, LPAREN, METHOD_CALL, RPAREN, SUPER_CTOR_CALL";
    }
    String option = getAttribute("option");
    if (option == null) {
      option = "nospace";
    }
    if (option.equals("nospace")) {
      option = "do not insert";
    } else {
      option = "insert";
    }

    final StringTokenizer tokens = new StringTokenizer(tokenString, ", ");
    String token;
    while (tokens.hasMoreTokens()) {
      token = tokens.nextToken();
      if (token.equals("LPAREN")) {
        userFormatterSetting("insert_space_after_opening_paren_in_parenthesized_expression",
                option);
        userFormatterSetting("insert_space_after_opening_paren_in_while", option);
        userFormatterSetting("insert_space_after_opening_paren_in_for", option);
        userFormatterSetting("insert_space_after_opening_paren_in_if", option);
        userFormatterSetting("insert_space_after_opening_paren_in_switch", option);
        userFormatterSetting("insert_space_after_opening_paren_in_synchronized", option);
        userFormatterSetting("insert_space_after_opening_paren_in_catch", option);
        userFormatterSetting("insert_space_after_opening_paren_in_method_invocation", option);
        userFormatterSetting("insert_space_after_opening_paren_in_annotation", option);
        userFormatterSetting("insert_space_after_opening_paren_in_constructor_declaration", option);
        userFormatterSetting("insert_space_after_opening_paren_in_enum_constant", option);
        userFormatterSetting("insert_space_after_opening_paren_in_method_declaration", option);
      } else if (token.equals("RPAREN")) {
        userFormatterSetting("insert_space_before_closing_paren_in_parenthesized_expression",
                option);
        userFormatterSetting("insert_space_before_closing_paren_in_while", option);
        userFormatterSetting("insert_space_before_closing_paren_in_for", option);
        userFormatterSetting("insert_space_before_closing_paren_in_if", option);
        userFormatterSetting("insert_space_before_closing_paren_in_switch", option);
        userFormatterSetting("insert_space_before_closing_paren_in_synchronized", option);
        userFormatterSetting("insert_space_before_closing_paren_in_catch", option);
        userFormatterSetting("insert_space_before_closing_paren_in_method_invocation", option);
        userFormatterSetting("insert_space_before_closing_paren_in_method_declaration", option);
        userFormatterSetting("insert_space_before_closing_paren_in_constructor_declaration",
                option);
        userFormatterSetting("insert_space_before_closing_paren_in_enum_constant", option);
        userFormatterSetting("insert_space_before_closing_paren_in_annotation", option);
      } else if (token.equals("CTOR_CALL") || token.equals("METHOD_CALL")
              || token.equals("SUPER_CTOR_CALL")) {
        userFormatterSetting("insert_space_before_closing_paren_in_method_invocation", option);
        userFormatterSetting("insert_space_after_opening_paren_in_method_invocation", option);
      }
    }
    return getFormatterSetting();
  }

}
