
package com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.FinalParametersQuickfix;

public class ModifierOrderTest extends AbstractQuickfixTestCase
{
    public void testModifierOrder() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("ModifierOrderInput.xml");
        testQuickfix(testData, new ModifierOrderQuickfix());
    }
}
