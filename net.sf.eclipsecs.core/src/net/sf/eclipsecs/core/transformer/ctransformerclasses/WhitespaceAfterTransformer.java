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
 * Wrapperclass for converting the checkstyle-rule WhitespaceAfter to appropriate
 * eclipse-formatter-rules.
 * 
 * @author Lukas Frena
 */
public class WhitespaceAfterTransformer extends CTransformationClass {

  @Override
  public FormatterConfiguration transformRule() {
    // TODO token SEMI
    String tokens = getAttribute("tokens");
    if (tokens == null) {
      tokens = "COMMA, SEMI, TYPECAST";
    }
    final StringTokenizer token = new StringTokenizer(tokens, ", ");
    String tok;

    while (token.hasMoreTokens()) {
      tok = token.nextToken();
      if (tok.equals("COMMA")) {
        userFormatterSetting("insert_space_after_comma_in_annotation", "insert");
        userFormatterSetting("insert_space_after_comma_in_type_arguments", "insert");
        userFormatterSetting("insert_space_after_comma_in_type_parameters", "insert");
        userFormatterSetting("insert_space_after_comma_in_enum_constant_arguments", "insert");
        userFormatterSetting("insert_space_after_comma_in_enum_declarations", "insert");
        userFormatterSetting("insert_space_after_comma_in_constructor_declaration_parameters",
                "insert");
        userFormatterSetting("insert_space_after_comma_in_method_declaration_throws", "insert");
        userFormatterSetting("insert_space_after_comma_in_for_increments", "insert");
        userFormatterSetting("insert_space_after_comma_in_explicitconstructorcall_arguments",
                "insert");
        userFormatterSetting("insert_space_after_comma_in_superinterfaces", "insert");
        userFormatterSetting("insert_space_after_comma_in_method_declaration_parameters", "insert");
        userFormatterSetting("insert_space_after_comma_in_for_inits", "insert");
        userFormatterSetting("insert_space_after_comma_in_array_initializer", "insert");
        userFormatterSetting("insert_space_after_comma_in_allocation_expression", "insert");
        userFormatterSetting("insert_space_after_comma_in_constructor_declaration_throws",
                "insert");
        userFormatterSetting("insert_space_after_comma_in_multiple_field_declarations", "insert");
        userFormatterSetting("insert_space_after_comma_in_parameterized_type_reference", "insert");
        userFormatterSetting("insert_space_after_comma_in_method_invocation_arguments", "insert");
        userFormatterSetting("insert_space_after_comma_in_multiple_local_declarations", "insert");
      } else if (tok.equals("TYPECAST")) {
        userFormatterSetting("insert_space_after_closing_paren_in_cast", "insert");
      }
    }
    return getFormatterSetting();
  }

}
