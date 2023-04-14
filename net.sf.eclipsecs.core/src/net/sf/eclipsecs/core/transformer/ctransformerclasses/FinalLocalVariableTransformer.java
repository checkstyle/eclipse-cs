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
 * Wrapperclass for converting the checkstyle-rule FinalLocalVariableWrap to appropriate
 * eclipse-formatter-rules.
 *
 * @author Lukas Frena
 */
public class FinalLocalVariableTransformer extends CTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("tokens");
    if (val == null) {
      val = "VARIABLE_DEF";
    }

    final StringTokenizer args = new StringTokenizer(val, ", ");
    String token;
    while (args.hasMoreTokens()) {
      token = args.nextToken();
      if (token.equals("VARIABLE_DEF")) {
        useCleanupSetting("make_local_variable_final", "true");
        useCleanupSetting("make_private_fields_final", "true");
      } else if (token.equals("PARAMETER_DEF")) {
        useCleanupSetting("make_parameters_final", "true");
      }
    }
    useCleanupSetting("make_variable_declarations_final", "true");
    return getFormatterSetting();
  }
}
