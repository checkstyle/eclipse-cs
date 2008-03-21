
package net.sf.eclipsecs.ui.quickfixes.blocks;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.blocks.AvoidNextedBlocksQuickfix;

public class AvoidNestedBlocksTest extends AbstractQuickfixTestCase {

    public void testNeedBracesIf() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("AvoidNestedBlocksInput.xml");
        testQuickfix(testData, new AvoidNextedBlocksQuickfix());
    }
}
