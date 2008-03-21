
package net.sf.eclipsecs.ui.quickfixes.modifier;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.modifier.ModifierOrderQuickfix;

public class ModifierOrderTest extends AbstractQuickfixTestCase {
    public void testModifierOrder() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("ModifierOrderInput.xml");
        testQuickfix(testData, new ModifierOrderQuickfix());
    }
}
