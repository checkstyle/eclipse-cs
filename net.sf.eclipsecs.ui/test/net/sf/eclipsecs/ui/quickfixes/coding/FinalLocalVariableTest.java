
package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.coding.FinalLocalVariableQuickfix;

public class FinalLocalVariableTest extends AbstractQuickfixTestCase {

    public void testFinalLocalVariable() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("FinalLocalVariableInput.xml");
        testQuickfix(testData, new FinalLocalVariableQuickfix());
    }
}
