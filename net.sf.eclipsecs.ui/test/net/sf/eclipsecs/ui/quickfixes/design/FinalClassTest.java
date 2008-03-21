
package net.sf.eclipsecs.ui.quickfixes.design;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.design.FinalClassQuickfix;

public class FinalClassTest extends AbstractQuickfixTestCase {

    public void testFinalClass() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("FinalClassInput.xml");
        testQuickfix(testData, new FinalClassQuickfix());
    }
}
