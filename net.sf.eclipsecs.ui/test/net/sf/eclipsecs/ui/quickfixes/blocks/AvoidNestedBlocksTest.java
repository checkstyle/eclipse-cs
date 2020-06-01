
package net.sf.eclipsecs.ui.quickfixes.blocks;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class AvoidNestedBlocksTest extends AbstractQuickfixTestCase {

  @Test
  public void testAvoidNestedBlocks() throws Exception {
    testQuickfix("AvoidNestedBlocksInput.xml", new AvoidNestedBlocksQuickfix());
  }
}
