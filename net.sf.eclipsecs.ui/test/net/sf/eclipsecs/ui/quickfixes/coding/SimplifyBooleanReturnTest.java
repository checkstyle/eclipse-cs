//============================================================================
//
// Copyright (C) 2003-2023 the original author or authors.
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

package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

class SimplifyBooleanReturnTest extends AbstractQuickfixTestCase {

  @Test
  void simplifyBooleanReturnWithoutCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithoutCurlyBraces.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithCurlyBraces.xml", new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithBooleanLiteralCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithBooleanLiteralCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithQualifiedNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithQualifiedNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithSimpleNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSimpleNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithParanthesizedExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithParanthesizedExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithSuperFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithSuperMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithThisExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithThisExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  void simplifyBooleanReturnWithNotCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithNotCondition.xml", new SimplifyBooleanReturnQuickfix());
  }

}
