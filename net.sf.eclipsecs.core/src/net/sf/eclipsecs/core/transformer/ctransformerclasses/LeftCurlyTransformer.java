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
 * Wrapperclass for converting the checkstyle-rule LeftCurly to appropriate eclipse-formatter-rules.
 *
 */
public class LeftCurlyTransformer extends CTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    // TODO token LITERAL_SYNCHRONIZED
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "CLASS_DEF, CTOR_DEF, INTERFACE_DEF, METHOD_DEF, LITERAL_CATCH, LITERAL_DO, LITERAL_ELSE, "
              + "LITERAL_FINALLY, LITERAL_FOR, LITERAL_IF, LITERAL_SYNCHRONIZED, LITERAL_TRY, LITERAL_WHILE";
    }
    final StringTokenizer token = new StringTokenizer(tokens, ", ");
    String tok;

    String option = getAttribute("option");
    if (option == null) {
      option = "eol";
    }
    if ("eol".equals(option)) {
      option = "end_of_line";
    } else if ("nl".equals(option) || "nlow".equals(option)) {
      option = "next_line";
    }

    while (token.hasMoreTokens()) {
      tok = token.nextToken();
      if ("CLASS_DEF".equals(tok)) {
        userFormatterSetting("brace_position_for_anonymous_type_declaration", option);
        userFormatterSetting("brace_position_for_enum_constant", option);
        userFormatterSetting("brace_position_for_enum_declaration", option);
        userFormatterSetting("brace_position_for_type_declaration", option);
        userFormatterSetting("brace_position_for_annotation_type_declaration", option);
      } else if ("INTERFACE_DEF".equals(tok)) {
        userFormatterSetting("brace_position_for_annotation_type_declaration", option);
        userFormatterSetting("brace_position_for_type_declaration", option);
      } else if ("CTOR_DEF".equals(tok)) {
        userFormatterSetting("brace_position_for_constructor_declaration", option);
      } else if ("METHOD_DEF".equals(tok)) {
        userFormatterSetting("brace_position_for_method_declaration", option);
      } else if ("LITERAL_DO".equals(tok) || "LITERAL_ELSE".equals(tok) || "LITERAL_FOR".equals(tok)
              || "LITERAL_IF".equals(tok) || "LITERAL_WHILE".equals(tok)
              || "LITERAL_CATCH".equals(tok) || "LITERAL_FINALLY".equals(tok)
              || "LITERAL_TRY".equals(tok)) {
        userFormatterSetting("brace_position_for_block", option);
      } else if ("LITERAL_SWITCH".equals(tok)) {
        userFormatterSetting("brace_position_for_switch", option);
      }
    }
    return getFormatterSetting();
  }

}
