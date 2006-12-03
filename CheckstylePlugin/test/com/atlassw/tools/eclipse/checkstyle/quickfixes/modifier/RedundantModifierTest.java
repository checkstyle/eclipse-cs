
package com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.FinalParametersQuickfix;

public class RedundantModifierTest extends AbstractQuickfixTestCase
{
    public void testRedundantModifier() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("RedundantModifierInput.xml");
        testQuickfix(testData, new RedundantModifierQuickfix());
    }
}
