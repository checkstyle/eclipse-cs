
package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.coding.EmptyStatementQuickfix;

public class EmptyStatementTest extends AbstractQuickfixTestCase {

    public void testEmptyStatement() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("EmptyStatementInput.xml");
        testQuickfix(testData, new EmptyStatementQuickfix());
    }

    public void testEmptyStatementNeg() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("EmptyStatementInputNeg.xml");
        testQuickfix(testData, new EmptyStatementQuickfix());
    }
}
