
package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class EmptyStatementTest extends AbstractQuickfixTestCase {

  public void testEmptyStatement() throws Exception {
    testQuickfix("EmptyStatementInput.xml", new EmptyStatementQuickfix());
  }

  public void testEmptyStatementNeg() throws Exception {
    testQuickfix("EmptyStatementInputNeg.xml", new EmptyStatementQuickfix());
  }
}
