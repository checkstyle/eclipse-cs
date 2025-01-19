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

import net.sf.eclipsecs.core.transformer.CTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapperclass for converting the checkstyle-rule TypecastParenPad to appropriate
 * eclipse-formatter-rules.
 *
 */
public class TypecastParenPadTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    String option = getAttribute("option");
    if (option == null) {
      option = "nospace";
    }
    if ("space".equals(option)) {
      option = "insert";
    } else {
      option = "do not insert";
    }
    userFormatterSetting("insert_space_before_closing_paren_in_cast", option);
    userFormatterSetting("insert_space_after_opening_paren_in_cast", option);
    return getFormatterSetting();
  }

}
