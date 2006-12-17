
package com.atlassw.tools.eclipse.checkstyle.quickfixes.design;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier.ModifierOrderQuickfix;

public class FinalClassTest extends AbstractQuickfixTestCase
{

    public void testFinalClass() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("FinalClassInput.xml");
        testQuickfix(testData, new FinalClassQuickfix());
    }
}
