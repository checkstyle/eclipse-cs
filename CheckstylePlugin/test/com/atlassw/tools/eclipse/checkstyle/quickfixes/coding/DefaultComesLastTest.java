
package com.atlassw.tools.eclipse.checkstyle.quickfixes.coding;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class DefaultComesLastTest extends AbstractQuickfixTestCase
{

    public void testDefaultComesLast() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("DefaultComesLastInput.xml");
        testQuickfix(testData, new DefaultComesLastQuickfix());
    }

    public void testDefaultComesLastInner() throws Exception
    {
        InputStream testData = this.getClass()
                .getResourceAsStream("DefaultComesLastInputInner.xml");
        testQuickfix(testData, new DefaultComesLastQuickfix());
    }
}
