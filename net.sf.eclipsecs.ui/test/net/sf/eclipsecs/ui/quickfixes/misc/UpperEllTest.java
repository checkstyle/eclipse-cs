
package net.sf.eclipsecs.ui.quickfixes.misc;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class UpperEllTest extends AbstractQuickfixTestCase {

  public void testUpperEll() throws Exception {
    testQuickfix("UpperEllInput.xml", new UpperEllQuickfix());
  }
}
