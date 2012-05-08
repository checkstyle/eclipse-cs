package net.sf.eclipsecs.ui.quickfixes.coding;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;

public class ExplicitInitializationTest extends AbstractQuickfixTestCase {

    public void testExplicitInitialization() throws Exception {
        testQuickfix("ExplicitInitialization.xml", new ExplicitInitializationQuickfix());
    }

}
