
package net.sf.eclipsecs.ui.quickfixes.modifier;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ModifierOrderTest extends AbstractQuickfixTestCase {
  public void testModifierOrder() throws Exception {
    testQuickfix("ModifierOrderInput.xml", new ModifierOrderQuickfix());
  }
}
