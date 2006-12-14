
package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class FinalLocalVariableTest extends AbstractQuickfixTestCase
{

    public void testFinalLocalVariable() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("FinalLocalVariableInput.xml");
        testQuickfix(testData, new FinalLocalVariableQuickfix());
    }
}
