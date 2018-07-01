
package net.sf.eclipsecs.ui.quickfixes.design;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class DesignForExtensionTest extends AbstractQuickfixTestCase {

  public void testDesignForExtension() throws Exception {
    testQuickfix("DesignForExtensionInput.xml", new DesignForExtensionQuickfix());
  }
}
