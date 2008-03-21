
package net.sf.eclipsecs.ui.quickfixes.coding;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.coding.DefaultComesLastQuickfix;

public class DefaultComesLastTest extends AbstractQuickfixTestCase {

    public void testDefaultComesLast() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("DefaultComesLastInput.xml");
        testQuickfix(testData, new DefaultComesLastQuickfix());
    }

    public void testDefaultComesLastInner() throws Exception {
        InputStream testData = this.getClass()
                .getResourceAsStream("DefaultComesLastInputInner.xml");
        testQuickfix(testData, new DefaultComesLastQuickfix());
    }
}
