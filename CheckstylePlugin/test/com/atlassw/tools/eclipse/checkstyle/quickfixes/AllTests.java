
package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks.NeedBracesTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.DefaultComesLastTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.EmptyStatementTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.MissingSwitchDefaultTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.design.DesignForExtensionTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.ArrayTypeStyleTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.FinalParametersTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.UncommentedMainTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.UpperEllTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier.ModifierOrderTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.modifier.RedundantModifierTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for com.atlassw.tools.eclipse.checkstyle.quickfixes");
        // $JUnit-BEGIN$

        // blocks
        suite.addTestSuite(NeedBracesTest.class);

        // coding
        suite.addTestSuite(DefaultComesLastTest.class);
        suite.addTestSuite(EmptyStatementTest.class);
        suite.addTestSuite(MissingSwitchDefaultTest.class);

        // design
        suite.addTestSuite(DesignForExtensionTest.class);

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
