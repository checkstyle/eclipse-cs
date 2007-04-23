
package com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class AvoidNestedBlocksTest extends AbstractQuickfixTestCase
{

    public void testNeedBracesIf() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("AvoidNestedBlocksInput.xml");
        testQuickfix(testData, new AvoidNextedBlocksQuickfix());
    }
}
