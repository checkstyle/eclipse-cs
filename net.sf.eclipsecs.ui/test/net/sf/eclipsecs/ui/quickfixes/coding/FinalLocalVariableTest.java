
package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalLocalVariableTest extends AbstractQuickfixTestCase {

  @Test
  public void testFinalLocalVariable() throws Exception {
    testQuickfix("FinalLocalVariableInput.xml", new FinalLocalVariableQuickfix());
  }
}
