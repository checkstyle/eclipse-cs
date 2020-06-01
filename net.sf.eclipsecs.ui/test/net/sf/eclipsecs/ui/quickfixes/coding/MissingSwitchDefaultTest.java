
package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class MissingSwitchDefaultTest extends AbstractQuickfixTestCase {

  @Test
  public void testMissingSwitchDefault() throws Exception {
    testQuickfix("MissingSwitchDefaultInput.xml", new MissingSwitchDefaultQuickfix());
  }

  @Test
  public void testMissingSwitchDefaultInner() throws Exception {
    testQuickfix("MissingSwitchDefaultInputInner.xml", new MissingSwitchDefaultQuickfix());
  }
}
