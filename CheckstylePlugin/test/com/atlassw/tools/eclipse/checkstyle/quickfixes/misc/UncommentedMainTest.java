
package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class UncommentedMainTest extends AbstractQuickfixTestCase
{

    public void testUncommentedMain() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("UncommentedMainInput.xml");
        testQuickfix(testData, new UncommentedMainQuickfix());
    }
}
