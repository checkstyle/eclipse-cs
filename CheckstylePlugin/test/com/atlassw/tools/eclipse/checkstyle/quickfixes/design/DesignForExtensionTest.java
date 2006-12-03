
package com.atlassw.tools.eclipse.checkstyle.quickfixes.design;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier.ModifierOrderQuickfix;

public class DesignForExtensionTest extends AbstractQuickfixTestCase
{

    public void testDesignForExtension() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("DesignForExtensionInput.xml");
        testQuickfix(testData, new DesignForExtensionQuickfix());
    }
}
