
package net.sf.eclipsecs.ui.quickfixes.blocks;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class NeedBracesTest extends AbstractQuickfixTestCase {

  @Test
  public void testNeedBracesIf() throws Exception {
    testQuickfix("NeedBracesInputIf.xml", new NeedBracesQuickfix());
  }

  @Test
  public void testNeedBracesElse() throws Exception {
    testQuickfix("NeedBracesInputElse.xml", new NeedBracesQuickfix());
  }

  @Test
  public void testNeedBracesElseIf() throws Exception {
    testQuickfix("NeedBracesInputElseIf.xml", new NeedBracesQuickfix());
  }

  @Test
  public void testNeedBracesFor() throws Exception {
    testQuickfix("NeedBracesInputFor.xml", new NeedBracesQuickfix());
  }

  @Test
  public void testNeedBracesWhile() throws Exception {
    testQuickfix("NeedBracesInputWhile.xml", new NeedBracesQuickfix());
  }

  @Test
  public void testNeedBracesDoWhile() throws Exception {
    testQuickfix("NeedBracesInputDoWhile.xml", new NeedBracesQuickfix());
  }
}
