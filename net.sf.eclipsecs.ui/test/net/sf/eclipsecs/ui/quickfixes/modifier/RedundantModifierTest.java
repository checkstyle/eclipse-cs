
package net.sf.eclipsecs.ui.quickfixes.modifier;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.modifier.RedundantModifierQuickfix;

public class RedundantModifierTest extends AbstractQuickfixTestCase {
    public void testRedundantModifier() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("RedundantModifierInput.xml");
        testQuickfix(testData, new RedundantModifierQuickfix());
    }
}
