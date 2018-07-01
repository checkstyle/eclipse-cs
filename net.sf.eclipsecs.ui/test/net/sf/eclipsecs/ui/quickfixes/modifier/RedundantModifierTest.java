
package net.sf.eclipsecs.ui.quickfixes.modifier;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class RedundantModifierTest extends AbstractQuickfixTestCase {
  public void testRedundantModifier() throws Exception {
    testQuickfix("RedundantModifierInput.xml", new RedundantModifierQuickfix());
  }
}
