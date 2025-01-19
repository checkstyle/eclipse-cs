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
 * Wrapperclass for converting the checkstyle-rule NoWhitespaceAfter to appropriate
 * eclipse-formatter-rules.
 *
 */
public class NoWhitespaceAfterTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("tokens");
    if (val == null) {
      val = "ARRAY_INIT, BNOT, DEC, DOT, INC, LNOT, UNARY_MINUS, UNARY_PLUS";
    }

    final StringTokenizer args = new StringTokenizer(val, ", ");
    String token;
    // TODO tokens DOT ARRAY_INIT
    while (args.hasMoreTokens()) {
      token = args.nextToken();
      if ("DEC".equals(token) || "INC".equals(token)) {
        userFormatterSetting("insert_space_after_prefix_operator", "do not insert");
      } else if ("UNARY_MINUS".equals(token) || "LNOT".equals(token) || "UNARY_PLUS".equals(token)
              || "BNOT".equals(token)) {
        userFormatterSetting("insert_space_after_unary_operator", "do not insert");
      } else if ("TYPECAST".equals(token)) {
        userFormatterSetting("insert_space_after_closing_paren_in_cast", "do not insert");
      }
    }
    return getFormatterSetting();
  }

}
