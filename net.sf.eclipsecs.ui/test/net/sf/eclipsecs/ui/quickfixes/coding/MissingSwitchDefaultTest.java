
package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class MissingSwitchDefaultTest extends AbstractQuickfixTestCase {

  public void testMissingSwitchDefault() throws Exception {
    testQuickfix("MissingSwitchDefaultInput.xml", new MissingSwitchDefaultQuickfix());
  }

  public void testMissingSwitchDefaultInner() throws Exception {
    testQuickfix("MissingSwitchDefaultInputInner.xml", new MissingSwitchDefaultQuickfix());
  }
}
