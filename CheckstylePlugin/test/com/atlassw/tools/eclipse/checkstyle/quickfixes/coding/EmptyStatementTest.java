
package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class EmptyStatementTest extends AbstractQuickfixTestCase
{

    public void testEmptyStatement() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("EmptyStatementInput.xml");
        testQuickfix(testData, new EmptyStatementQuickfix());
    }

    public void testEmptyStatementNeg() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("EmptyStatementInputNeg.xml");
        testQuickfix(testData, new EmptyStatementQuickfix());
    }
}
