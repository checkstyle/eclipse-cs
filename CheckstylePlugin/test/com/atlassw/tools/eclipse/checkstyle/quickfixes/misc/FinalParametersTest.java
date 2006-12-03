
package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class FinalParametersTest extends AbstractQuickfixTestCase
{

    public void testFinalParameters() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("FinalParametersInput.xml");
        testQuickfix(testData, new FinalParametersQuickfix());
    }
}
