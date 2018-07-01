
package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class StringLiteralEqualityTest extends AbstractQuickfixTestCase {

  public void testStringLiteralEquality() throws Exception {
    testQuickfix("StringLiteralEqualityInput.xml", new StringLiteralEqualityQuickfix());
  }
}
