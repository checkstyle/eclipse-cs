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

public class RequireThisTest extends AbstractQuickfixTestCase {

  @Test
  public void testRequireThisFieldAccessAssignmentLHS() throws Exception {
    testQuickfix("RequireThisFieldAccessAssignmentLHS.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisFieldAccessAssignmentRHS() throws Exception {
    testQuickfix("RequireThisFieldAccessAssignmentRHS.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisFieldAccessArrayInitializer() throws Exception {
    testQuickfix("RequireThisFieldAccessArrayInitializer.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisFieldAccessInnerClass() throws Exception {
    testQuickfix("RequireThisFieldAccessInnerClass.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisMethodInvocation() throws Exception {
    testQuickfix("RequireThisMethodInvocation.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisMethodInvocationWithParam() throws Exception {
    testQuickfix("RequireThisMethodInvocationWithParam.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisMethodInvocationAssignmentRHS() throws Exception {
    testQuickfix("RequireThisMethodInvocationAssignmentRHS.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisMethodInvocationArrayInitializer() throws Exception {
    testQuickfix("RequireThisMethodInvocationArrayInitializer.xml", new RequireThisQuickfix());
  }

  @Test
  public void testRequireThisMethodInvocationInnerClass() throws Exception {
    testQuickfix("RequireThisMethodInvocationInnerClass.xml", new RequireThisQuickfix());
  }

}
