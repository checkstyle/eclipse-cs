package net.sf.eclipsecs.ui.quickfixes.coding;

import org.junit.jupiter.api.Test;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ExplicitInitializationTest extends AbstractQuickfixTestCase {

  @Test
  public void testExplicitInitialization() throws Exception {
    testQuickfix("ExplicitInitialization.xml", new ExplicitInitializationQuickfix());
  }

}
