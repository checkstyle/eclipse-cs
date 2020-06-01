
package net.sf.eclipsecs.ui.quickfixes.misc;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class UncommentedMainTest extends AbstractQuickfixTestCase {

  @Test
  public void testUncommentedMain() throws Exception {
    testQuickfix("UncommentedMainInput.xml", new UncommentedMainQuickfix());
  }
}
