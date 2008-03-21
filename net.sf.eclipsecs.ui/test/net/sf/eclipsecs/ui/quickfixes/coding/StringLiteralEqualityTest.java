
package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.coding.StringLiteralEqualityQuickfix;

public class StringLiteralEqualityTest extends AbstractQuickfixTestCase {

    public void testStringLiteralEquality() throws Exception {
        InputStream testData = this.getClass()
                .getResourceAsStream("StringLiteralEqualityInput.xml");
        testQuickfix(testData, new StringLiteralEqualityQuickfix());
    }
}
