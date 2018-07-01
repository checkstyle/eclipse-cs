
package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class DefaultComesLastTest extends AbstractQuickfixTestCase {

  public void testDefaultComesLast() throws Exception {
    testQuickfix("DefaultComesLastInput.xml", new DefaultComesLastQuickfix());
  }

  public void testDefaultComesLastInner() throws Exception {
    testQuickfix("DefaultComesLastInputInner.xml", new DefaultComesLastQuickfix());
  }
}
