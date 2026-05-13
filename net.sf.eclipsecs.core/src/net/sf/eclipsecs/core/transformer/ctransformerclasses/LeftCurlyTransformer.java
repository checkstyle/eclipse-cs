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

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule LeftCurly to appropriate eclipse-formatter-rules.
 *
 */
public class LeftCurlyTransformer extends AbstractCTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    // TODO token LITERAL_SYNCHRONIZED
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "CLASS_DEF, CTOR_DEF, INTERFACE_DEF, METHOD_DEF, LITERAL_CATCH, LITERAL_DO, LITERAL_ELSE, "
              + "LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_WHILE";
    }
    final StringTokenizer token = new StringTokenizer(tokens, ", ");

    String option = switch (getAttribute("option")) {
      case null -> "end_of_line";
      case "eol" -> "end_of_line";
      case "nl", "nlow" -> "next_line";
      case String s -> s;
    };

    while (token.hasMoreTokens()) {
      List<String> settings = switch (token.nextToken()) {
        case "CLASS_DEF" -> List.of("brace_position_for_anonymous_type_declaration",
                "brace_position_for_enum_constant",
                "brace_position_for_enum_declaration",
                "brace_position_for_type_declaration",
                "brace_position_for_annotation_type_declaration");
        case "INTERFACE_DEF" -> List.of(
                "brace_position_for_annotation_type_declaration",
                "brace_position_for_type_declaration");
        case "CTOR_DEF" -> List.of("brace_position_for_constructor_declaration");
        case "METHOD_DEF" -> List.of("brace_position_for_method_declaration");
        case "LITERAL_DO", "LITERAL_ELSE", "LITERAL_FOR", "LITERAL_IF", "LITERAL_WHILE", "LITERAL_CATCH",
             "LITERAL_FINALLY", "LITERAL_TRY" -> List.of("brace_position_for_block");
        case "LITERAL_SWITCH" -> List.of("brace_position_for_switch");
        default -> Collections.emptyList();
      };
      settings.forEach(setting -> userFormatterSetting(setting, option));
    }
    return getFormatterSetting();
  }

}
