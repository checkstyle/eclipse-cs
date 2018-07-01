
package net.sf.eclipsecs.ui.quickfixes.blocks;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class NeedBracesTest extends AbstractQuickfixTestCase {

  public void testNeedBracesIf() throws Exception {
    testQuickfix("NeedBracesInputIf.xml", new NeedBracesQuickfix());
  }

  public void testNeedBracesElse() throws Exception {
    testQuickfix("NeedBracesInputElse.xml", new NeedBracesQuickfix());
  }

  public void testNeedBracesElseIf() throws Exception {
    testQuickfix("NeedBracesInputElseIf.xml", new NeedBracesQuickfix());
  }

  public void testNeedBracesFor() throws Exception {
    testQuickfix("NeedBracesInputFor.xml", new NeedBracesQuickfix());
  }

  public void testNeedBracesWhile() throws Exception {
    testQuickfix("NeedBracesInputWhile.xml", new NeedBracesQuickfix());
  }

  public void testNeedBracesDoWhile() throws Exception {
    testQuickfix("NeedBracesInputDoWhile.xml", new NeedBracesQuickfix());
  }
}
