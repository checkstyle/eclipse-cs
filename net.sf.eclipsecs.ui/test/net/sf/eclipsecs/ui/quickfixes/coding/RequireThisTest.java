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
