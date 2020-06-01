
package net.sf.eclipsecs.ui.quickfixes.design;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class DesignForExtensionTest extends AbstractQuickfixTestCase {

  @Test
  public void testDesignForExtension() throws Exception {
    testQuickfix("DesignForExtensionInput.xml", new DesignForExtensionQuickfix());
  }
}
