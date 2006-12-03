
package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class MissingSwitchDefaultTest extends AbstractQuickfixTestCase
{

    public void testMissingSwitchDefault() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("MissingSwitchDefaultInput.xml");
        testQuickfix(testData, new MissingSwitchDefaultQuickfix());
    }

    public void testMissingSwitchDefaultInner() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream(
                "MissingSwitchDefaultInputInner.xml");
        testQuickfix(testData, new MissingSwitchDefaultQuickfix());
    }
}
