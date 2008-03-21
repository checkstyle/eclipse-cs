
package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.coding.MissingSwitchDefaultQuickfix;

public class MissingSwitchDefaultTest extends AbstractQuickfixTestCase {

    public void testMissingSwitchDefault() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("MissingSwitchDefaultInput.xml");
        testQuickfix(testData, new MissingSwitchDefaultQuickfix());
    }

    public void testMissingSwitchDefaultInner() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream(
                "MissingSwitchDefaultInputInner.xml");
        testQuickfix(testData, new MissingSwitchDefaultQuickfix());
    }
}
