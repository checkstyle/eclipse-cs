
package net.sf.eclipsecs.ui.quickfixes.design;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalClassTest extends AbstractQuickfixTestCase {

  public void testFinalClass() throws Exception {
    testQuickfix("FinalClassInput.xml", new FinalClassQuickfix());
  }
}
