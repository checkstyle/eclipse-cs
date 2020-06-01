
package net.sf.eclipsecs.ui.quickfixes.modifier;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ModifierOrderTest extends AbstractQuickfixTestCase {

  @Test
  public void testModifierOrder() throws Exception {
    testQuickfix("ModifierOrderInput.xml", new ModifierOrderQuickfix());
  }
}
