
package net.sf.eclipsecs.ui.quickfixes.design;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class FinalClassTest extends AbstractQuickfixTestCase {

  @Test
  public void testFinalClass() throws Exception {
    testQuickfix("FinalClassInput.xml", new FinalClassQuickfix());
  }
}
