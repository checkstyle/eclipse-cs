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
 * Wrapperclass for converting the checkstyle-rule LeftCurly to appropriate eclipse-formatter-rules.
 *
 * @author Lukas Frena
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
    if (option.equals("eol")) {
      option = "end_of_line";
    } else if (option.equals("nl") || option.equals("nlow")) {
      option = "next_line";
    }

    while (token.hasMoreTokens()) {
      tok = token.nextToken();
      if (tok.equals("CLASS_DEF")) {
        userFormatterSetting("brace_position_for_anonymous_type_declaration", option);
        userFormatterSetting("brace_position_for_enum_constant", option);
        userFormatterSetting("brace_position_for_enum_declaration", option);
        userFormatterSetting("brace_position_for_type_declaration", option);
        userFormatterSetting("brace_position_for_annotation_type_declaration", option);
      } else if (tok.equals("INTERFACE_DEF")) {
        userFormatterSetting("brace_position_for_annotation_type_declaration", option);
        userFormatterSetting("brace_position_for_type_declaration", option);
      } else if (tok.equals("CTOR_DEF")) {
        userFormatterSetting("brace_position_for_constructor_declaration", option);
      } else if (tok.equals("METHOD_DEF")) {
        userFormatterSetting("brace_position_for_method_declaration", option);
      } else if (tok.equals("LITERAL_DO") || tok.equals("LITERAL_ELSE") || tok.equals("LITERAL_FOR")
              || tok.equals("LITERAL_IF") || tok.equals("LITERAL_WHILE")
              || tok.equals("LITERAL_CATCH") || tok.equals("LITERAL_FINALLY")
              || tok.equals("LITERAL_TRY")) {
        userFormatterSetting("brace_position_for_block", option);
      } else if (tok.equals("LITERAL_SWITCH")) {
        userFormatterSetting("brace_position_for_switch", option);
      }
    }
    return getFormatterSetting();
  }

}
