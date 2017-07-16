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
 * Wrapperclass for converting the checkstyle-rule RightCurly to appropriate
 * eclipse-formatter-rules.
 * 
 * @author Lukas Frena
 */
public class RightCurlyTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    // TODO token LITERAL_TRY/IF
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "LITERAL_TRY, LITERAL_CATCH, LITERAL_FINALLY, LITERAL_IF, LITERAL_ELSE";
    }
    final StringTokenizer token = new StringTokenizer(tokens, ", ");
    String tok;

    String option = getAttribute("option");
    if (option == null) {
      option = "same";
    }
    if (option.equals("same")) {
      option = "do not insert";
    } else {
      option = "insert";
    }

    while (token.hasMoreTokens()) {
      tok = token.nextToken();
      if (tok.equals("LITERAL_CATCH")) {
        userFormatterSetting("insert_new_line_before_catch_in_try_statement", option);
      } else if (tok.equals("LITERAL_FINALLY")) {
        userFormatterSetting("insert_new_line_before_finally_in_try_statement", option);
      } else if (tok.equals("LITERAL_ELSE")) {
        userFormatterSetting("insert_new_line_before_else_in_if_statement", option);
      }
    }
    return getFormatterSetting();
  }

}
