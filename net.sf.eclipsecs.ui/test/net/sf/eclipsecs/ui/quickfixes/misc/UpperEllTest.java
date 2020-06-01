
package net.sf.eclipsecs.ui.quickfixes.misc;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class UpperEllTest extends AbstractQuickfixTestCase {

  @Test
  public void testUpperEll() throws Exception {
    testQuickfix("UpperEllInput.xml", new UpperEllQuickfix());
  }
}
