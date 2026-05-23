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

import net.sf.eclipsecs.core.transformer.AbstractCTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule FinalParameters to appropriate
 * eclipse-formatter-rules.
 *
 */
public class FinalParametersTransformer extends AbstractCTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "METHOD_DEF, CTOR_DEF";
    }

    for (String token : tokens.split("\\s*,\\s*")) {
      switch (token) {
        case "METHOD_DEF", "CTOR_DEF" -> useCleanupSetting("make_parameters_final", "true");
        case "LITERAL_CATCH", "FOR_EACH_CLAUSE", "PATTERN_VARIABLE_DEF" ->
          useCleanupSetting("make_local_variable_final", "true");
        default -> {
          // nothing to transform
        }
      }
    }
    useCleanupSetting("make_variable_declarations_final", "true");
    return getFormatterSetting();
  }
}
