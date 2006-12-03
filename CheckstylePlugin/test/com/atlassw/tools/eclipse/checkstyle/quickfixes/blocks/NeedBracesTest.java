
package com.atlassw.tools.eclipse.checkstyle.quickfixes.blocks;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class NeedBracesTest extends AbstractQuickfixTestCase
{

    public void testNeedBracesIf() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputIf.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }

    public void testNeedBracesElse() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputElse.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }

    public void testNeedBracesElseIf() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputElseIf.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }

    public void testNeedBracesFor() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputFor.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }

    public void testNeedBracesWhile() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputWhile.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }

    public void testNeedBracesDoWhile() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("NeedBracesInputDoWhile.xml");
        testQuickfix(testData, new NeedBracesQuickfix());
    }
}
