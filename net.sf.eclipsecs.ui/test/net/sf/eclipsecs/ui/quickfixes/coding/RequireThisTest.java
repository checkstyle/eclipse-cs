package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class RequireThisTest extends AbstractQuickfixTestCase {

    public void testRequireThisFieldAccessAssignmentLHS() throws Exception {
        testQuickfix("RequireThisFieldAccessAssignmentLHS.xml", new RequireThisQuickfix());
    }

    public void testRequireThisFieldAccessAssignmentRHS() throws Exception {
        testQuickfix("RequireThisFieldAccessAssignmentRHS.xml", new RequireThisQuickfix());
    }

    public void testRequireThisFieldAccessArrayInitializer() throws Exception {
        testQuickfix("RequireThisFieldAccessArrayInitializer.xml", new RequireThisQuickfix());
    }

    public void testRequireThisFieldAccessInnerClass() throws Exception {
        testQuickfix("RequireThisFieldAccessInnerClass.xml", new RequireThisQuickfix());
    }

    public void testRequireThisMethodInvocation() throws Exception {
        testQuickfix("RequireThisMethodInvocation.xml", new RequireThisQuickfix());
    }

    public void testRequireThisMethodInvocationWithParam() throws Exception {
        testQuickfix("RequireThisMethodInvocationWithParam.xml", new RequireThisQuickfix());
    }

    public void testRequireThisMethodInvocationAssignmentRHS() throws Exception {
        testQuickfix("RequireThisMethodInvocationAssignmentRHS.xml", new RequireThisQuickfix());
    }

    public void testRequireThisMethodInvocationArrayInitializer() throws Exception {
        testQuickfix("RequireThisMethodInvocationArrayInitializer.xml", new RequireThisQuickfix());
    }

    public void testRequireThisMethodInvocationInnerClass() throws Exception {
        testQuickfix("RequireThisMethodInvocationInnerClass.xml", new RequireThisQuickfix());
    }

}
