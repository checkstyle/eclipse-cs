
package net.sf.eclipsecs.ui.quickfixes.misc;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalParametersTest extends AbstractQuickfixTestCase {

  @Test
  public void testFinalParameters() throws Exception {
    testQuickfix("FinalParametersInput.xml", new FinalParametersQuickfix());
  }
}
