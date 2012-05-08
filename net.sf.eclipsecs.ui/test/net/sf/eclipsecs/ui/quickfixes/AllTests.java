
package net.sf.eclipsecs.ui.quickfixes;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.eclipsecs.ui.quickfixes.blocks.AvoidNestedBlocksTest;
import net.sf.eclipsecs.ui.quickfixes.blocks.NeedBracesTest;
import net.sf.eclipsecs.ui.quickfixes.coding.DefaultComesLastTest;
import net.sf.eclipsecs.ui.quickfixes.coding.EmptyStatementTest;
import net.sf.eclipsecs.ui.quickfixes.coding.ExplicitInitializationTest;
import net.sf.eclipsecs.ui.quickfixes.coding.FinalLocalVariableTest;
import net.sf.eclipsecs.ui.quickfixes.coding.MissingSwitchDefaultTest;
import net.sf.eclipsecs.ui.quickfixes.coding.RequireThisTest;
import net.sf.eclipsecs.ui.quickfixes.coding.SimplifyBooleanReturnTest;
import net.sf.eclipsecs.ui.quickfixes.coding.StringLiteralEqualityTest;
import net.sf.eclipsecs.ui.quickfixes.design.DesignForExtensionTest;
import net.sf.eclipsecs.ui.quickfixes.design.FinalClassTest;
import net.sf.eclipsecs.ui.quickfixes.misc.ArrayTypeStyleTest;
import net.sf.eclipsecs.ui.quickfixes.misc.FinalParametersTest;
import net.sf.eclipsecs.ui.quickfixes.misc.UncommentedMainTest;
import net.sf.eclipsecs.ui.quickfixes.misc.UpperEllTest;
import net.sf.eclipsecs.ui.quickfixes.modifier.ModifierOrderTest;
import net.sf.eclipsecs.ui.quickfixes.modifier.RedundantModifierTest;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for net.sf.eclipsecs.ui.quickfixes");
        // $JUnit-BEGIN$

        // blocks
        suite.addTestSuite(AvoidNestedBlocksTest.class);
        suite.addTestSuite(NeedBracesTest.class);

        // coding
        suite.addTestSuite(DefaultComesLastTest.class);
        suite.addTestSuite(EmptyStatementTest.class);
        suite.addTestSuite(FinalLocalVariableTest.class);
        suite.addTestSuite(MissingSwitchDefaultTest.class);
        suite.addTestSuite(StringLiteralEqualityTest.class);
        suite.addTestSuite(RequireThisTest.class);
        suite.addTestSuite(ExplicitInitializationTest.class);
        suite.addTestSuite(SimplifyBooleanReturnTest.class);

        // design
        suite.addTestSuite(DesignForExtensionTest.class);
        suite.addTestSuite(FinalClassTest.class);

        // misc
        suite.addTestSuite(ArrayTypeStyleTest.class);
        suite.addTestSuite(FinalParametersTest.class);
        suite.addTestSuite(UncommentedMainTest.class);
        suite.addTestSuite(UpperEllTest.class);

        // modifier
        suite.addTestSuite(ModifierOrderTest.class);
        suite.addTestSuite(RedundantModifierTest.class);
        // $JUnit-END$
        return suite;
    }
}
