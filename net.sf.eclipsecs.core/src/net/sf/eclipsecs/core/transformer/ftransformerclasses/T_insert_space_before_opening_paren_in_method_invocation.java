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

package net.sf.eclipsecs.core.transformer.ftransformerclasses;

import java.util.HashMap;

import net.sf.eclipsecs.core.transformer.CheckstyleSetting;
import net.sf.eclipsecs.core.transformer.FTransformationClass;

/**
 * Transformerclass for converting the formatter-setting
 * "insert.space.before.opening.paren.in.method.invocation" to appropriate checkstyle-modules.
 *
 */
public class T_insert_space_before_opening_paren_in_method_invocation extends FTransformationClass {
  @Override
  public CheckstyleSetting transformRule() {
    final HashMap<String, String> properties = new HashMap<>();
    properties.put("tokens", "METHOD_CALL");
    useTreeWalkerModule("MethodParamPad", properties);
    return getCheckstyleSetting();
  }
}
