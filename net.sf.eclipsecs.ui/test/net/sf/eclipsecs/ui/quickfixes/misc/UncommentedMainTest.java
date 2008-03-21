
package net.sf.eclipsecs.ui.quickfixes.misc;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.misc.UncommentedMainQuickfix;

public class UncommentedMainTest extends AbstractQuickfixTestCase {

    public void testUncommentedMain() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("UncommentedMainInput.xml");
        testQuickfix(testData, new UncommentedMainQuickfix());
    }
}
