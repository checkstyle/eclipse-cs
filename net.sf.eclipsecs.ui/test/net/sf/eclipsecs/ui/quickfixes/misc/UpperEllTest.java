
package net.sf.eclipsecs.ui.quickfixes.misc;

import java.io.InputStream;

import net.sf.eclipsecs.ui.quickfixes.AbstractQuickfixTestCase;
import net.sf.eclipsecs.ui.quickfixes.misc.UpperEllQuickfix;

public class UpperEllTest extends AbstractQuickfixTestCase {

    public void testUpperEll() throws Exception {
        InputStream testData = this.getClass().getResourceAsStream("UpperEllInput.xml");
        testQuickfix(testData, new UpperEllQuickfix());
    }
}
