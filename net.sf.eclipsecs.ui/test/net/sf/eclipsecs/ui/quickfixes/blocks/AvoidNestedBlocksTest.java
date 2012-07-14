
package net.sf.eclipsecs.ui.quickfixes.blocks;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.blocks.AvoidNestedBlocksQuickfix;

public class AvoidNestedBlocksTest extends AbstractQuickfixTestCase {

    public void testAvoidNestedBlocks() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("AvoidNestedBlocksInput.xml");
        testQuickfix(testData, new AvoidNestedBlocksQuickfix());
    }
}
