
package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalLocalVariableTest extends AbstractQuickfixTestCase {

  public void testFinalLocalVariable() throws Exception {
    testQuickfix("FinalLocalVariableInput.xml", new FinalLocalVariableQuickfix());
  }
}
