
package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class DefaultComesLastTest extends AbstractQuickfixTestCase {

  @Test
  public void testDefaultComesLast() throws Exception {
    testQuickfix("DefaultComesLastInput.xml", new DefaultComesLastQuickfix());
  }

  @Test
  public void testDefaultComesLastInner() throws Exception {
    testQuickfix("DefaultComesLastInputInner.xml", new DefaultComesLastQuickfix());
  }
}
