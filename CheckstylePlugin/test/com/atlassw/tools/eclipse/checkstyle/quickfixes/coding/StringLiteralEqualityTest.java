
package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class StringLiteralEqualityTest extends AbstractQuickfixTestCase
{

    public void testStringLiteralEquality() throws Exception
    {
        InputStream testData = this.getClass()
                .getResourceAsStream("StringLiteralEqualityInput.xml");
        testQuickfix(testData, new StringLiteralEqualityQuickfix());
    }
}
