
package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class UpperEllTest extends AbstractQuickfixTestCase
{

    public void testUpperEll() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("UpperEllInput.xml");
        testQuickfix(testData, new UpperEllQuickfix());
    }
}
