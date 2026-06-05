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

import net.sf.eclipsecs.core.transformer.AbstractCheckstyleTransformationClass;
import net.sf.eclipsecs.core.transformer.FormatterConfiguration;

/**
 * Wrapper class for converting the checkstyle-rule Indentation to appropriate
 * eclipse-formatter-rules.
 *
 */
public class IndentationTransformer extends AbstractCheckstyleTransformationClass {
  @Override
  public FormatterConfiguration transformRule() {
    // basicOffset -> indentation.size, tabulation.size
    String basicOffset = getAttribute("basicOffset");
    if (basicOffset == null) {
      basicOffset = "4";
    }
    userFormatterSetting("use_tabs_only_for_leading_indentations", "false");
    userFormatterSetting("tabulation.char", "space");
    userFormatterSetting("indentation.size", basicOffset);
    userFormatterSetting("tabulation.size", basicOffset);

    // caseIndent -> indent_switchstatements_compare_to_switch
    // (non-zero means cases are indented relative to the switch)
    String caseIndent = getAttribute("caseIndent");
    if (caseIndent == null) {
      caseIndent = "4";
    }
    userFormatterSetting("indent_switchstatements_compare_to_switch",
            "0".equals(caseIndent) ? "false" : "true");

    // lineWrappingIndentation -> continuation_indentation
    String lineWrappingIndentation = getAttribute("lineWrappingIndentation");
    if (lineWrappingIndentation == null) {
      lineWrappingIndentation = "4";
    }
    userFormatterSetting("continuation_indentation", lineWrappingIndentation);

    // arrayInitIndent -> continuation_indentation_for_array_initializer
    String arrayInitIndent = getAttribute("arrayInitIndent");
    if (arrayInitIndent == null) {
      arrayInitIndent = "4";
    }
    userFormatterSetting("continuation_indentation_for_array_initializer", arrayInitIndent);

    return getFormatterSetting();
  }
}
