
package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class EmptyStatementTest extends AbstractQuickfixTestCase {

  @Test
  public void testEmptyStatement() throws Exception {
    testQuickfix("EmptyStatementInput.xml", new EmptyStatementQuickfix());
  }

  @Test
  public void testEmptyStatementNeg() throws Exception {
    testQuickfix("EmptyStatementInputNeg.xml", new EmptyStatementQuickfix());
  }
}
