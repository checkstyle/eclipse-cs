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
 * Wrapperclass for converting the checkstyle-rule IdentationWrap to appropriate
 * eclipse-formatter-rules.
 *
 */
public class IndentationTransformer extends CTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    String val = getAttribute("basicOffset");
    if (val == null) {
      val = "4";
    }
    // TODO attributes braceAdjustment caseIndent
    userFormatterSetting("use_tabs_only_for_leading_indentations", "false");
    userFormatterSetting("tabulation.char", "space");
    userFormatterSetting("indentation.size", val);
    userFormatterSetting("tabulation.size", val);

    val = getAttribute("caseIndent");
    if (val == null) {
      val = "4";
    }

    if ("4".equals(val)) {
      userFormatterSetting("indent_switchstatements_compare_to_switch", "true");
    } else if ("0".equals(val)) {
      userFormatterSetting("indent_switchstatements_compare_to_switch", "false");
    }
    return getFormatterSetting();
  }
}
