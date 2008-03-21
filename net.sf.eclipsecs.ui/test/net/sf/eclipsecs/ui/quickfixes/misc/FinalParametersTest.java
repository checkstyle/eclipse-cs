
package net.sf.eclipsecs.ui.quickfixes.misc;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.misc.FinalParametersQuickfix;

public class FinalParametersTest extends AbstractQuickfixTestCase {

    public void testFinalParameters() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("FinalParametersInput.xml");
        testQuickfix(testData, new FinalParametersQuickfix());
    }
}
