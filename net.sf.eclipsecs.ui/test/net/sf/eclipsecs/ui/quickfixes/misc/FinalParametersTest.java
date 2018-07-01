
package net.sf.eclipsecs.ui.quickfixes.misc;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalParametersTest extends AbstractQuickfixTestCase {

  public void testFinalParameters() throws Exception {
    testQuickfix("FinalParametersInput.xml", new FinalParametersQuickfix());
  }
}
