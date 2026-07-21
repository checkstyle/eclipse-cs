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
 * Wrapper class for converting the checkstyle-rule NoWhitespaceAfter to appropriate
 * eclipse-formatter-rules.
 *
 */
public class NoWhitespaceAfterTransformer extends AbstractCTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("tokens");
    if (val == null) {
      val = "ARRAY_INIT, AT, INC, DEC, UNARY_MINUS, UNARY_PLUS, BNOT, LNOT, DOT, "
              + "ARRAY_DECLARATOR, INDEX_OP";
    }

    for (String token : val.split("\\s*,\\s*")) {
      List<String> settings = switch (token) {
        case "INC", "DEC" -> List.of("insert_space_after_prefix_operator");
        case "UNARY_MINUS", "UNARY_PLUS", "BNOT", "LNOT" -> List
                .of("insert_space_after_unary_operator");
        case "TYPECAST" -> List.of("insert_space_after_closing_paren_in_cast");
        case "ARRAY_INIT" -> List.of("insert_space_after_opening_brace_in_array_initializer");
        case "AT" -> List.of("insert_space_after_at_in_annotation",
                "insert_space_after_at_in_annotation_type_declaration");
        case "ARRAY_DECLARATOR" -> List
                .of("insert_space_before_opening_bracket_in_array_type_reference");
        case "INDEX_OP" -> List.of("insert_space_before_opening_bracket_in_array_reference");
        case "LITERAL_SYNCHRONIZED" -> List.of("insert_space_before_opening_paren_in_synchronized");
        default -> List.of();
      };
      settings.forEach(setting -> userFormatterSetting(setting, "do not insert"));
    }
    return getFormatterSetting();
  }

}
