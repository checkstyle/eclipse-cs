
package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class StringLiteralEqualityTest extends AbstractQuickfixTestCase {

  @Test
  public void testStringLiteralEquality() throws Exception {
    testQuickfix("StringLiteralEqualityInput.xml", new StringLiteralEqualityQuickfix());
  }
}
