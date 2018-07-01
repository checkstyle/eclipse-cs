
package net.sf.eclipsecs.ui.quickfixes.blocks;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class AvoidNestedBlocksTest extends AbstractQuickfixTestCase {

  public void testAvoidNestedBlocks() throws Exception {
    testQuickfix("AvoidNestedBlocksInput.xml", new AvoidNestedBlocksQuickfix());
  }
}
