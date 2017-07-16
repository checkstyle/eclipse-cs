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
 * Wrapperclass for converting the checkstyle-rule MethodParamPad to appropriate
 * eclipse-formatter-rules.
 * 
 * @author Lukas Frena
 */
public class MethodParamPadTransformer extends CTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("tokens");
    if (val == null) {
      val = "CTOR_DEF, LITERAL_NEW, METHOD_CALL, METHOD_DEF, SUPER_CTOR_CALL";
    }

    // TODO tokens LITERAL_NEW
    final StringTokenizer args = new StringTokenizer(val, ", ");
    String token;
    while (args.hasMoreTokens()) {
      token = args.nextToken();
      if (token.equals("CTOR_DEF")) {
        userFormatterSetting("insert_space_before_opening_paren_in_constructor_declaration",
                "do not insert");
      } else if (token.equals("METHOD_CALL") || token.equals("SUPER_CTOR_CALL")) {
        userFormatterSetting("insert_space_before_opening_paren_in_method_invocation",
                "do not insert");
      } else if (token.equals("METHOD_DEF")) {
        userFormatterSetting("insert_space_before_opening_paren_in_method_declaration",
                "do not insert");
      }
    }
    return getFormatterSetting();
  }

}
