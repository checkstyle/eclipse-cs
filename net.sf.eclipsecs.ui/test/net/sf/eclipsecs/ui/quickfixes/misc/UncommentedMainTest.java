
package net.sf.eclipsecs.ui.quickfixes.misc;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class UncommentedMainTest extends AbstractQuickfixTestCase {

  public void testUncommentedMain() throws Exception {
    testQuickfix("UncommentedMainInput.xml", new UncommentedMainQuickfix());
  }
}
