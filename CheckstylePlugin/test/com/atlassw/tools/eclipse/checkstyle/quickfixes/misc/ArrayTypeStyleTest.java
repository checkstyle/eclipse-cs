
package com.atlassw.tools.eclipse.checkstyle.quickfixes.misc;

import java.io.InputStream;

import com.atlassw.tools.eclipse.checkstyle.quickfixes.AbstractQuickfixTestCase;

public class ArrayTypeStyleTest extends AbstractQuickfixTestCase
{

    public void testArrayTypeStyleField() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream("ArrayTypeStyleInputField.xml");
        testQuickfix(testData, new ArrayTypeStyleQuickfix());
    }

    public void testArrayTypeStyleMethodParam() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream(
                "ArrayTypeStyleInputMethodParam.xml");
        testQuickfix(testData, new ArrayTypeStyleQuickfix());
    }

    public void testArrayTypeStyleVariable() throws Exception
    {
        InputStream testData = this.getClass().getResourceAsStream(
                "ArrayTypeStyleInputVariable.xml");
        testQuickfix(testData, new ArrayTypeStyleQuickfix());
    }
}
