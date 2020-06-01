
package net.sf.eclipsecs.ui.quickfixes.modifier;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class RedundantModifierTest extends AbstractQuickfixTestCase {

  @Test
  public void testRedundantModifier() throws Exception {
    testQuickfix("RedundantModifierInput.xml", new RedundantModifierQuickfix());
  }
}
