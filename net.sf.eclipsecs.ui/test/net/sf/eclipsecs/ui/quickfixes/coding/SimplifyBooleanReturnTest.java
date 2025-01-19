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

public class SimplifyBooleanReturnTest extends AbstractQuickfixTestCase {

  @Test
  public void simplifyBooleanReturnWithoutCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithoutCurlyBraces.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithCurlyBraces() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithCurlyBraces.xml", new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithBooleanLiteralCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithBooleanLiteralCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithQualifiedNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithQualifiedNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithSimpleNameCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSimpleNameCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithParanthesizedExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithParanthesizedExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithSuperFieldAccessCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperFieldAccessCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithSuperMethodInvocationCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithSuperMethodInvocationCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithThisExpressionCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithThisExpressionCondition.xml",
            new SimplifyBooleanReturnQuickfix());
  }

  @Test
  public void simplifyBooleanReturnWithNotCondition() throws Exception {
    testQuickfix("SimplifyBooleanReturnWithNotCondition.xml", new SimplifyBooleanReturnQuickfix());
  }

}
