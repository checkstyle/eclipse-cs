
package com.atlassw.tools.eclipse.checkstyle.quickfixes;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks.NeedBracesTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.DefaultComesLastTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.EmptyStatementTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.coding.MissingSwitchDefaultTest;
import com.atlassw.tools.eclipse.checkstyle.quickfixes.misc.FinalParametersTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for com.atlassw.tools.eclipse.checkstyle.quickfixes");
        // $JUnit-BEGIN$
        suite.addTestSuite(NeedBracesTest.class);
        suite.addTestSuite(DefaultComesLastTest.class);
        suite.addTestSuite(EmptyStatementTest.class);
        suite.addTestSuite(MissingSwitchDefaultTest.class);
        suite.addTestSuite(FinalParametersTest.class);
        // $JUnit-END$
        return suite;
    }
}
